package net.canadensys.processor.dwc;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.regex.Pattern;

import net.canadensys.processor.DataProcessor;
import net.canadensys.processor.ProcessingResult;
import net.canadensys.utils.NumberUtils;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Data processor to handle a pair of numeric fields that are inter-connected.
 * Will attempt to normalize value contained in 2 Strings fields to Number fields.
 * The string value can contain a suffix (units, comments, ...).
 * NO conversion will be attempted.
 * @author canadensys
 *
 */
public class NumericPairDataProcessor implements DataProcessor{
	
	protected static final Pattern KEEP_NUMERIC_PATTERN = Pattern.compile("[^\\d\\.-]");
	
	private static final String DEFAULT_VALUE1_NAME = "min";
	private static final String DEFAULT_VALUE2_NAME = "max";
	
	//Tag to get the Class to use for validation purpose
	public static final String VALIDATE_CLASS_TAG = "Class";
	
	private String value1Name,value2Name;
	
	//Only USE_NULL make sense here
	private ErrorHandlingModeEnum errorHandlingMode = ErrorHandlingModeEnum.USE_NULL;
	
	/**
	 * Default constructor, default field names will be used
	 */
	public NumericPairDataProcessor(){
		this(DEFAULT_VALUE1_NAME,DEFAULT_VALUE2_NAME);
	}
	
	public NumericPairDataProcessor(String value1Name, String value2Name){
		this.value1Name = value1Name;
		this.value2Name = value2Name;
	}
	
	/**
	 * Numeric pair Bean processing function.
	 * @param in Java bean containing the 2 properties as String
	 * @param out Java bean containing the 2 properties as class extending Number (primitive type NOT supported)
	 * @param params Will be ignored so use null
	 * @param result Optional ProcessingResult
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void processBean(Object in, Object out, Map<String, Object> params, ProcessingResult result) {
		
		try {
			Number[] output = new Number[2];
			String val1 = (String)PropertyUtils.getSimpleProperty(in, value1Name);
			String val2 = (String)PropertyUtils.getSimpleProperty(in, value2Name);
			
			process(val1,val2,output,PropertyUtils.getPropertyType(out, value1Name),result);
						
			PropertyUtils.setSimpleProperty(out, value1Name, output[0]);
			PropertyUtils.setSimpleProperty(out, value2Name, output[1]);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * A numeric pair is valid if the 2 elements are valid.
	 * @param in
	 * @param isMandatory
	 * @param params you must provide the class to use to validate the date using the VALIDATE_CLASS_TAG tag.
	 * @param result
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean validateBean(Object in, boolean isMandatory, Map<String, Object> params, ProcessingResult result) {
		Number[] output = new Number[2];
		String val1 = null, val2 = null;
		//Get the class in which the result should be casted
		Class<? extends Number> clazz = null;
		if(params != null){
			clazz = (Class<? extends Number>)params.get(VALIDATE_CLASS_TAG);
		}
		
		try {
			val1 = (String)PropertyUtils.getSimpleProperty(in, value1Name);
			val2 = (String)PropertyUtils.getSimpleProperty(in, value2Name);
			process(val1,val2,output,clazz,result);
			if(output[0] != null && output[1] != null){
				return true;
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		//no valid numeric pair was found, check if this value was mandatory
		if(!isMandatory && StringUtils.isBlank(val1) && StringUtils.isBlank(val2)){
			return true;
		}
		return false;
	}
	
	/**
	 * Numeric pair processing function
	 * @param value1 
	 * @param value2
	 * @param minMaxOutput initialized array(size==2) that will contain the parsed data or null if not parsable
	 * @param clazz Class wanted for element of the minMaxOutput
	 * @param result optional processing result
	 */
	public void process(String value1, String value2, Number[] output, Class<? extends Number> clazz, ProcessingResult result){
		String originalValue1 = value1;
		String originalValue2 = value2;
		if(!StringUtils.isBlank(originalValue1)){
			value1 = KEEP_NUMERIC_PATTERN.matcher(originalValue1).replaceAll("");
		}
		if(!StringUtils.isBlank(originalValue2)){
			value2 = KEEP_NUMERIC_PATTERN.matcher(originalValue2).replaceAll("");
		}
		output[0] = NumberUtils.parseNumber(value1, clazz);
		output[1] = NumberUtils.parseNumber(value2, clazz);
		
		//Do we need to log the result?
		if(result != null){
			//It's an error only if the original value was not null
			if(output[0] == null && !StringUtils.isBlank(originalValue1)){
				result.addError("Value ["+originalValue1+"] could not be processed.");
			}
			if(output[1] == null && !StringUtils.isBlank(originalValue2)){
				result.addError("Value ["+originalValue2+"] could not be processed.");
			}
		}
	}

	@Override
	public ErrorHandlingModeEnum getErrorHandlingMode() {
		return errorHandlingMode;
	}
}
