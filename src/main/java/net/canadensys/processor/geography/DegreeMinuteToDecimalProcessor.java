package net.canadensys.processor.geography;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.canadensys.processor.AbstractDataProcessor;
import net.canadensys.processor.ProcessingResult;
import net.canadensys.utils.NumberUtils;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to process a degree/minute/second coordinate string into a decimal coordinate.
 * Supported format :
 * 40°26′47″N
 * 40º26'47"N
 * 40:26:47N
 * 40°26'47"N
 * 40d 26' 47" N
 * 40° 26.7717N
 * 40d 26' 47.986" N
 * 
 * Caller should reuse the same instance to save resources.
 * 
 * @author canadensys
 * 
 */
public class DegreeMinuteToDecimalProcessor extends AbstractDataProcessor {

	final Logger logger = LoggerFactory.getLogger(DegreeMinuteToDecimalProcessor.class);

	protected static int DEGREE_IDX = 0;
	protected static int MINUTE_IDX = 1;
	protected static int SECOND_IDX = 2;

	protected static int MAX_MINUTE_SECOND = 60;

	protected static Pattern CHECK_CARDINAL_DIRECTION_PATTERN = Pattern.compile("[NESW]\\s*$", Pattern.CASE_INSENSITIVE);
	protected static Pattern CHECK_SOUTH_WEST_PATTERN = Pattern.compile("[SW]\\s*$", Pattern.CASE_INSENSITIVE);

	protected static Pattern CHECK_LATITUDE = Pattern.compile("[NS]\\s*$", Pattern.CASE_INSENSITIVE);
	protected static Pattern CHECK_LONGITUDE = Pattern.compile("[EW]\\s*$", Pattern.CASE_INSENSITIVE);

	// negative sign(and whitespace) and NSEW letters and whitespace //\s*$
	protected static Pattern REMOVE_CARDINAL_DIRECTION_PATTERN = Pattern.compile("(^\\s?-\\s?)|(\\s?[NSEW]\\s*$)", Pattern.CASE_INSENSITIVE);

	protected static final Pattern KEEP_NUMBERS_PATTERN = Pattern.compile("[^\\d]");

	// Regex info : (?:X) X, as a non-capturing group
	// (\d*\.?\d+) :any int or decimal
	// (?:[°d: ]+ :followed by at least °d: or a space
	// (\d*\.?\d+) :any int or decimal (optional)
	// (?:['m′: ])* :followed by an optional 'm:′
	// (\d*\.?\d+)* :any int or decimal (optional)
	// [\"s″ ]? :followed by an optional "s″
	// This regex requires validation on extracted groups
	protected static Pattern SPLIT_DMS_PARTS = Pattern.compile("(\\d*\\.?\\d+)(?:[º°d: ]+)(\\d*\\.?\\d+)*(?:['m′‘'’‛: ])*(\\d*\\.?\\d+)*[\"s″“‟” ]?");

	// default Java bean field names.
	protected static final String DEFAULT_LATITUDE_NAME = "lat";
	protected static final String DEFAULT_LONGITUDE_NAME = "lng";

	// Java beans field names
	protected String latitudeInName, longitudeInName = null;
	protected String latitudeOutName, longitudeOutName = null;
	protected LatLongProcessorHelper latLongHelper = null;

	// Only USE_NULL makes sense here
	protected ErrorHandlingModeEnum errorHandlingMode = ErrorHandlingModeEnum.USE_NULL;

	/**
	 * Default constructor, default field names will be used.
	 */
	public DegreeMinuteToDecimalProcessor() {
		this(DEFAULT_LATITUDE_NAME, DEFAULT_LONGITUDE_NAME);
	}

	/**
	 * 
	 * @param latitudeInName
	 *            name of the String latitude field in the input and the output Java bean
	 * @param longitudeInName
	 *            name of the String longitude field in the input and the output Java bean
	 */
	public DegreeMinuteToDecimalProcessor(String latitudeInName, String longitudeInName) {
		this(latitudeInName, longitudeInName, latitudeInName, longitudeInName);
	}

	/**
	 * 
	 * @param latitudeInName
	 *            name of the String latitude field in the input Java bean
	 * @param longitudeInName
	 *            name of the String longitude field in the input Java bean
	 * @param latitudeOutName
	 *            name of the Double longitude field in the output Java bean
	 * @param longitudeOutName
	 *            name of the Double longitude field in the output Java bean
	 */
	public DegreeMinuteToDecimalProcessor(String latitudeInName, String longitudeInName, String latitudeOutName, String longitudeOutName) {
		this.latitudeInName = latitudeInName;
		this.longitudeInName = longitudeInName;
		this.latitudeOutName = latitudeOutName;
		this.longitudeOutName = longitudeOutName;

		setLocale(Locale.ENGLISH);
		latLongHelper = new LatLongProcessorHelper(resourceBundle);
	}

	/**
	 * Degree/minute/second to decimal Bean processing function.
	 * 
	 * @param in
	 *            Java bean containing the coordinate degree/minute/seconds as String
	 * @param out
	 *            Java bean containing the decimal coordinate as Double
	 * @param params
	 *            Will be ignored so use null
	 * @param result
	 *            Optional ProcessingResult
	 */
	@Override
	public void processBean(Object in, Object out, Map<String, Object> params, ProcessingResult result) {
		try {
			String lat = (String) PropertyUtils.getSimpleProperty(in, latitudeInName);
			String lng = (String) PropertyUtils.getSimpleProperty(in, longitudeInName);

			Double[] output = process(lat, lng, result);
			PropertyUtils.setSimpleProperty(out, latitudeOutName, output[LatLongProcessorHelper.LATITUDE_IDX]);
			PropertyUtils.setSimpleProperty(out, longitudeOutName, output[LatLongProcessorHelper.LONGITUDE_IDX]);
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

	@Override
	public boolean validateBean(Object in, boolean isMandatory, Map<String, Object> params, ProcessingResult result) {
		String lat = null, lng = null;
		try {
			lat = (String) PropertyUtils.getSimpleProperty(in, latitudeInName);
			lng = (String) PropertyUtils.getSimpleProperty(in, longitudeInName);

			Double[] output = process(lat, lng, result);
			if (output[LatLongProcessorHelper.LATITUDE_IDX] != null && output[LatLongProcessorHelper.LONGITUDE_IDX] != null) {
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

		// not valid, check if the value was mandatory
		if (!isMandatory && StringUtils.isBlank(lat) && StringUtils.isBlank(lng)) {
			return true;
		}
		return false;
	}

	/**
	 * Degree/minute/second to decimal processing function.
	 * 
	 * @param dmsLat
	 *            degree/minute/second latitude string
	 * @param dmsLlong
	 *            degree/minute/second longitude string
	 * @param result
	 *            optional
	 * @return decimal values of the dms coordinate or null
	 */
	public Double[] process(String dmsLat, String dmsLong, ProcessingResult result) {
		Double[] output = new Double[2];
		output[LatLongProcessorHelper.LATITUDE_IDX] = dmsToDecimalDegree(dmsLat, result);
		output[LatLongProcessorHelper.LONGITUDE_IDX] = dmsToDecimalDegree(dmsLong, result);

		// make sure that cardinal directions are valid
		if (output[LatLongProcessorHelper.LATITUDE_IDX] != null && !CHECK_LATITUDE.matcher(dmsLat).find()) {
			if (result != null) {
				result.addError(MessageFormat.format(resourceBundle.getString("dms.error.noCardinalDirection"), dmsLat));
			}
			output[LatLongProcessorHelper.LATITUDE_IDX] = null;
		}
		if (output[LatLongProcessorHelper.LONGITUDE_IDX] != null && !CHECK_LONGITUDE.matcher(dmsLong).find()) {
			if (result != null) {
				result.addError(MessageFormat.format(resourceBundle.getString("dms.error.noCardinalDirection"), dmsLong));
			}
			output[LatLongProcessorHelper.LONGITUDE_IDX] = null;
		}

		// use delegate to validate boundaries
		latLongHelper.ensureLatLongBoundaries(output, result);
		return output;
	}

	/**
	 * Degree/minute/second to decimal processing function.
	 * 
	 * @param dms
	 *            degree/minute/second string
	 * @param result
	 *            optional
	 * @return decimal value of the dms coordinate or null
	 */
	protected Double dmsToDecimalDegree(String dms, ProcessingResult result) {

		if (StringUtils.isBlank(dms)) {
			return null;
		}

		// make sure we have a cardinal direction at the end
		if (!CHECK_CARDINAL_DIRECTION_PATTERN.matcher(dms).find()) {
			if (result != null) {
				result.addError(MessageFormat.format(resourceBundle.getString("dms.error.noCardinalDirection"), dms));
			}
			return null;
		}

		// check if the dms contains S (south) or W (west) string
		Matcher m = CHECK_SOUTH_WEST_PATTERN.matcher(dms);
		int negation = m.find() ? -1 : 1;

		// remove minus sign and cardinal direction from the string
		String p_dms = REMOVE_CARDINAL_DIRECTION_PATTERN.matcher(dms).replaceAll("");

		// this will be used for validation purpose
		String allNumbers = KEEP_NUMBERS_PATTERN.matcher(dms).replaceAll("");

		m = SPLIT_DMS_PARTS.matcher(p_dms);
		// find() need to be called to use group(idx)
		if (!m.find()) {
			if (result != null) {
				result.addError(MessageFormat.format(resourceBundle.getString("dms.error.patternDoesntMatch"), dms));
			}
			return null;
		}

		// start at 1 (0 is always the complete match)
		// TODO Could we remove this part?
		String[] parts = new String[3];
		int partsIdx = 0;
		String currPart;
		for (int i = 1; i <= m.groupCount(); i++) {
			currPart = m.group(i);
			if (!StringUtils.isBlank(currPart)) {
				parts[partsIdx] = currPart;
				partsIdx++;
			}
		}
		Double degree = NumberUtils.parseNumber(parts[DEGREE_IDX], Double.class, 0d);
		Double minute = NumberUtils.parseNumber(parts[MINUTE_IDX], Double.class, 0d);
		Double second = NumberUtils.parseNumber(parts[SECOND_IDX], Double.class, 0d);

		// check numeric bounds of minute and seconds
		if (!checkMinuteSecondBounds(minute, second, dms, result)) {
			return null;
		}

		// make sure that we extracted all numbers
		if (!KEEP_NUMBERS_PATTERN.matcher(parts[DEGREE_IDX] + parts[MINUTE_IDX] + StringUtils.defaultString(parts[SECOND_IDX], "")).replaceAll("")
				.equalsIgnoreCase(allNumbers)) {
			if (result != null) {
				result.addError(MessageFormat.format(resourceBundle.getString("dms.error.patternDoesntMatch"), dms));
			}
			return null;
		}

		// If we have decimals on degree, we don't have minute and second
		if (parts[DEGREE_IDX] != null && parts[DEGREE_IDX].contains(".")) {
			if (parts[MINUTE_IDX] != null || parts[SECOND_IDX] != null) {
				if (result != null) {
					result.addError(MessageFormat.format(resourceBundle.getString("dms.error.decimalDegreeError"), dms));
				}
				return null;
			}
		}

		// If we have decimals on minute, we don't have second
		if (parts[MINUTE_IDX] != null && parts[MINUTE_IDX].contains(".")) {
			if (parts[SECOND_IDX] != null) {
				if (result != null) {
					result.addError(MessageFormat.format(resourceBundle.getString("dms.error.decimalMinuteError"), dms));
				}
				return null;
			}
		}

		// If we have extracted nothing in second, but we have a second identifier ("s″), minute was not provided.
		if (parts[SECOND_IDX] == null) {
			if (p_dms.matches(".*[\"s″].*")) {
				if (result != null) {
					result.addError(MessageFormat.format(resourceBundle.getString("dms.error.unprocessable"), dms));
				}
				return null;
			}
		}

		// compute decimal value
		double decimal = (degree + (minute / 60) + (second / 3600)) * negation;
		return decimal;
	}

	/**
	 * Check if the extract minutes and seconds values are within bounds.
	 * 
	 * @param minute
	 * @param second
	 * @param dms
	 *            original degree, minute, second string
	 * @param result
	 *            can be null
	 * @return bounds are respected or not
	 */
	private boolean checkMinuteSecondBounds(Double minute, Double second, String dms, ProcessingResult result) {
		if (minute >= MAX_MINUTE_SECOND) {
			if (result != null) {
				result.addError(MessageFormat.format(resourceBundle.getString("dms.error.minuteOutOfBounds"), dms));
			}
			return false;
		}

		if (second >= MAX_MINUTE_SECOND) {
			if (result != null) {
				result.addError(MessageFormat.format(resourceBundle.getString("dms.error.secondOutOfBounds"), dms));
			}
			return false;
		}
		return true;
	}

	@Override
	public ErrorHandlingModeEnum getErrorHandlingMode() {
		return errorHandlingMode;
	}
}
