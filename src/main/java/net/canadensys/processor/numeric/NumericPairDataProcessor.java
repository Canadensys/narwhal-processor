package net.canadensys.processor.numeric;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import net.canadensys.processor.AbstractDataProcessor;
import net.canadensys.processor.ProcessingResult;
import net.canadensys.utils.NumberUtils;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data processor to handle a pair of numeric fields that are inter-connected.
 * Will attempt to normalize value contained in 2 Strings fields to Number fields.
 * The string value can contain a suffix (units, comments, ...).
 * NO unit conversions will be attempted.
 * 
 * @author canadensys
 * 
 */
public class NumericPairDataProcessor extends AbstractDataProcessor {

	final Logger logger = LoggerFactory.getLogger(NumericPairDataProcessor.class);

	protected static final Pattern KEEP_NUMERIC_PATTERN = Pattern.compile("[^\\d\\.-]");

	private static final String DEFAULT_VALUE1_NAME = "min";
	private static final String DEFAULT_VALUE2_NAME = "max";

	// Tag to get the Class to use for validation purpose
	public static final String VALIDATE_CLASS_TAG = "Class";

	// Name of the fields inside the Java bean
	protected String value1InName, value2InName;
	protected String value1OutName, value2OutName;

	// Only USE_NULL make sense here
	private ErrorHandlingModeEnum errorHandlingMode = ErrorHandlingModeEnum.USE_NULL;

	/**
	 * Default constructor, default field names will be used
	 */
	public NumericPairDataProcessor() {
		this(DEFAULT_VALUE1_NAME, DEFAULT_VALUE2_NAME);
	}

	/**
	 * Constructor that allows to set the name of the fields to use in the 'in' Java bean.
	 * The same names will be used so the 'out' Java bean.
	 * 
	 * @param value1InName
	 *            name of the field 1 inside the 'in' Java bean
	 * @param value2InName
	 *            name of the field 2 inside the 'in' Java bean
	 */
	public NumericPairDataProcessor(String value1InName, String value2InName) {
		// if out values are not provided, use the same value as in.
		this(value1InName, value2InName, value1InName, value2InName);
	}

	/**
	 * Constructor that allows to set the name of the fields to use in the 'in' and 'out' Java beans.
	 * 
	 * @param value1InName
	 * @param value2InName
	 * @param value1OutName
	 * @param value2OutName
	 */
	public NumericPairDataProcessor(String value1InName, String value2InName, String value1OutName, String value2OutName) {
		this.value1InName = value1InName;
		this.value2InName = value2InName;

		this.value1OutName = value1OutName;
		this.value2OutName = value2OutName;
		// always a default Locale
		setLocale(Locale.ENGLISH);
	}

	/**
	 * Numeric pair Bean processing function.
	 * 
	 * @param in
	 *            Java bean containing the 2 properties as String
	 * @param out
	 *            Java bean containing the 2 properties as class extending Number (primitive type NOT supported)
	 * @param params
	 *            Will be ignored so use null
	 * @param result
	 *            Optional ProcessingResult
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void processBean(Object in, Object out, Map<String, Object> params, ProcessingResult result) {

		try {
			String val1 = (String) PropertyUtils.getSimpleProperty(in, value1InName);
			String val2 = (String) PropertyUtils.getSimpleProperty(in, value2InName);

			Number[] output = process(val1, val2, PropertyUtils.getPropertyType(out, value1OutName), result);

			PropertyUtils.setSimpleProperty(out, value1OutName, output[0]);
			PropertyUtils.setSimpleProperty(out, value2OutName, output[1]);
		}
		catch (IllegalAccessException e) {
			logger.error("Bean access error", e);
		}
		catch (InvocationTargetException e) {
			logger.error("Bean access error", e);
		}
		catch (NoSuchMethodException e) {
			logger.error("Bean access error", e);
		}
	}

	/**
	 * A numeric pair is valid if the 2 elements are valid.
	 * 
	 * @param in
	 * @param isMandatory
	 * @param params
	 *            you must provide the class to use to validate the date using the VALIDATE_CLASS_TAG tag.
	 * @param result
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean validateBean(Object in, boolean isMandatory, Map<String, Object> params, ProcessingResult result) {
		String val1 = null, val2 = null;
		// Get the class in which the result should be casted
		Class<? extends Number> clazz = null;
		if (params != null) {
			clazz = (Class<? extends Number>) params.get(VALIDATE_CLASS_TAG);
		}

		try {
			val1 = (String) PropertyUtils.getSimpleProperty(in, value1InName);
			val2 = (String) PropertyUtils.getSimpleProperty(in, value2InName);
			Number[] output = process(val1, val2, clazz, result);
			if (output[0] != null && output[1] != null) {
				return true;
			}
			// change to multiple Exception catch when moving to Java 7
		}
		catch (IllegalAccessException e) {
			logger.error("Bean access error", e);
			return false;
		}
		catch (InvocationTargetException e) {
			logger.error("Bean access error", e);
			return false;
		}
		catch (NoSuchMethodException e) {
			logger.error("Bean access error", e);
			return false;
		}

		// no valid numeric pair was found, check if this value was mandatory
		if (!isMandatory && StringUtils.isBlank(val1) && StringUtils.isBlank(val2)) {
			return true;
		}
		return false;
	}

	/**
	 * Numeric pair processing function
	 * 
	 * @param value1
	 * @param value2
	 * @param clazz
	 *            Class wanted for element of the minMaxOutput
	 * @param result
	 *            optional processing result
	 * @return 2 dimensions array with instance of clazz or null if the process failed
	 */
	public Number[] process(String value1, String value2, Class<? extends Number> clazz, ProcessingResult result) {
		String originalValue1 = value1;
		String originalValue2 = value2;
		if (!StringUtils.isBlank(originalValue1)) {
			value1 = KEEP_NUMERIC_PATTERN.matcher(originalValue1).replaceAll("");
		}
		if (!StringUtils.isBlank(originalValue2)) {
			value2 = KEEP_NUMERIC_PATTERN.matcher(originalValue2).replaceAll("");
		}
		Number[] output = new Number[2];
		output[0] = NumberUtils.parseNumber(value1, clazz);
		output[1] = NumberUtils.parseNumber(value2, clazz);

		// Do we need to log the result?
		if (result != null) {
			// It's an error only if the original value was not null
			if (output[0] == null && !StringUtils.isBlank(originalValue1)) {
				result.addError(MessageFormat.format(resourceBundle.getString("numericPair.error.unprocessable"), originalValue1));
			}
			if (output[1] == null && !StringUtils.isBlank(originalValue2)) {
				result.addError(MessageFormat.format(resourceBundle.getString("numericPair.error.unprocessable"), originalValue2));
			}
		}
		return output;
	}

	@Override
	public ErrorHandlingModeEnum getErrorHandlingMode() {
		return errorHandlingMode;
	}
}
