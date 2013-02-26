package net.canadensys.processor.geography;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.canadensys.processor.DataProcessor;
import net.canadensys.processor.ProcessingResult;
import net.canadensys.utils.NumberUtils;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to process a degree/minute/second coordinate into a decimal coordinate.
 * Supported format :
 * 40°26′47″N
 * 40:26:47N
 * 40°26'47"N
 * 40d 26' 47" N
 * 40° 26.7717N
 * 40d 26' 47.986" N
 * 
 * You should reuse the same instance to save resources.
 * @author canadensys
 *
 */
public class DegreeMinuteToDecimalProcessor implements DataProcessor{
	
	final Logger logger = LoggerFactory.getLogger(DegreeMinuteToDecimalProcessor.class);
	
	protected static int DEGREE_IDX = 0;
	protected static int MINUTE_IDX = 1;
	protected static int SECOND_IDX = 2;
	
	protected static Pattern CHECK_CARDINAL_DIRECTION_PATTERN = Pattern.compile("[NESW]\\s*$", Pattern.CASE_INSENSITIVE);
	protected static Pattern CHECK_SOUTH_WEST_PATTERN = Pattern.compile("[SW]\\s*$", Pattern.CASE_INSENSITIVE);
	
	//negative sign(and whitespace) and NSEW letters and whitespace //\s*$
	protected static Pattern REMOVE_CARDINAL_DIRECTION_PATTERN = Pattern.compile("(^\\s?-\\s?)|(\\s?[NSEW]\\s*$)", Pattern.CASE_INSENSITIVE);
	
	protected static final Pattern KEEP_NUMBERS_PATTERN = Pattern.compile("[^\\d]");

	//Regex info : (?:X) 	X, as a non-capturing group	
	//(\\d{1,3}) :get 1 to 3 digits
	//(?:[°d: ]+ :followed by at least °d: or a space
	//(\d*\.?\d+) :any int or decimal
	//(?:[\'m′: ])* :followed by an optional 'm:′
	//(\d*\.?\d+)* :any int or decimal
	//[\"s″ ]? :followed by an optional "s″
	protected static Pattern  SPLIT_DMS_PARTS = Pattern.compile("(\\d{1,3})(?:[°d: ]+)(\\d*\\.?\\d+)(?:[\'m′: ])*(\\d*\\.?\\d+)*[\"s″ ]?");
	
	protected static final String DEFAULT_COORDINATE_NAME = "coordinate";
	
	//Those field names will only be used with JavaBean
	protected String coordinateInName = null;
	protected String coordinateOutName = null;
	protected ResourceBundle resourceBundle = null;
	
	//Only USE_NULL makes sense here
	protected ErrorHandlingModeEnum errorHandlingMode = ErrorHandlingModeEnum.USE_NULL;
	
	/**
	 * Default constructor, default field names will be used.
	 */
	public DegreeMinuteToDecimalProcessor(){
		this(DEFAULT_COORDINATE_NAME,DEFAULT_COORDINATE_NAME);
	}
	
	/**
	 * 
	 * @param coordinateInName name of the String field in the input JavaBean
	 * @param coordinateOutName name of the Double field in the output JavaBean
	 */
	public DegreeMinuteToDecimalProcessor(String coordinateInName, String coordinateOutName){
		this.coordinateInName = coordinateInName;
		this.coordinateOutName = coordinateOutName;
		//always a default Locale
		setLocale(Locale.ENGLISH);
	}
	
	/**
	 * Degree/minute/second to decimal Bean processing function.
	 * @param in Java bean containing the coordinate degree/minute/seconds as String
	 * @param out Java bean containing the decimal coordinate as Double 
	 * @param params Will be ignored so use null
	 * @param result Optional ProcessingResult
	 */
	@Override
	public void processBean(Object in, Object out, Map<String, Object> params, ProcessingResult result) {
		try {
			String val1 = (String)PropertyUtils.getSimpleProperty(in, coordinateInName);
			Double coord = process(val1,result);
			PropertyUtils.setSimpleProperty(out, coordinateOutName, coord);
		} catch (IllegalAccessException e) {
			logger.error("Bean access error", e);
		} catch (InvocationTargetException e) {
			logger.error("Bean access error", e);
		} catch (NoSuchMethodException e) {
			logger.error("Bean access error", e);
		}
	}
	
	@Override
	public boolean validateBean(Object in, boolean isMandatory, Map<String, Object> params, ProcessingResult result) {
		String textCoordinate = null;
		try {
			textCoordinate = (String)PropertyUtils.getSimpleProperty(in, coordinateInName);
			if(process(textCoordinate,result) != null){
				return true;
			}
		//change to multiple Exception catch when moving to Java 7
		} catch (IllegalAccessException e) {
			logger.error("Bean access error", e);
			return false;
		} catch (InvocationTargetException e) {
			logger.error("Bean access error", e);
			return false;
		} catch (NoSuchMethodException e) {
			logger.error("Bean access error", e);
			return false;
		}
		
		//not valid, check if the value was mandatory
		if(!isMandatory && StringUtils.isBlank(textCoordinate)){
			return true;
		}
		return false;
	}
		
	/**
	 * Degree/minute/second to decimal processing function.
	 * @param dms degree/minute/second string
	 * @param result optional
	 * @return decimal value of the dms coordinate or null
	 */
	public Double process(String dms, ProcessingResult result){
		
		if(StringUtils.isBlank(dms)){
			return null;
		}
		
		//make sure we have a cardinal direction at the end
		if(!CHECK_CARDINAL_DIRECTION_PATTERN.matcher(dms).find()){
			if(result != null){
				result.addError(
						MessageFormat.format(resourceBundle.getString("dms.error.noCardinalDirection"),dms));
			}
			return null;
		}
		
		//check if the dms contains S (south) or W (west) string	    
		Matcher m = CHECK_SOUTH_WEST_PATTERN.matcher(dms);
		int negation = m.find() ? -1 : 1;
	    
		//remove minus sign and cardinal direction from the string
		String p_dms = REMOVE_CARDINAL_DIRECTION_PATTERN.matcher(dms).replaceAll("");
		
		//this will be used for validation purpose
		String allNumbers = KEEP_NUMBERS_PATTERN.matcher(dms).replaceAll("");
		
		m = SPLIT_DMS_PARTS.matcher(p_dms);
		//find() need to be called to use group(idx)
		if(!m.find()){
			if(result != null){
				result.addError(
						MessageFormat.format(resourceBundle.getString("dms.error.patternDoesntMatch"),dms));
			}
			return null;
		}
				
		//start at 1 (0 is always the complete match)
		//TODO remove this part
		String[] parts = new String[3];
		int partsIdx = 0;
		String currPart;
		for(int i=1;i<=m.groupCount();i++){
			currPart = m.group(i);
			if(!StringUtils.isBlank(currPart)){
				parts[partsIdx] = currPart;
				partsIdx++;
			}
		}
		Double degree = NumberUtils.parseNumber(parts[DEGREE_IDX], Double.class,0d);
		Double minute = NumberUtils.parseNumber(parts[MINUTE_IDX], Double.class,0d);
		Double second = NumberUtils.parseNumber(parts[SECOND_IDX], Double.class,0d);
		
		//make sure that we extracted all the numbers
		if(!KEEP_NUMBERS_PATTERN.matcher(parts[DEGREE_IDX]+parts[MINUTE_IDX]+StringUtils.defaultString(parts[SECOND_IDX], ""))
				.replaceAll("").equalsIgnoreCase(allNumbers)){
			if(result != null){
				result.addError(
						MessageFormat.format(resourceBundle.getString("dms.error.patternDoesntMatch"),dms));
			}
			return null;
		}
		
		//make sure that if we have decimals on minute, we don't second
		if(parts[MINUTE_IDX] != null){
			if(parts[MINUTE_IDX].contains(".")){
				if(parts[SECOND_IDX] != null){
					if(result != null){
						result.addError(
								MessageFormat.format(resourceBundle.getString("dms.error.decimalMinuteError"),dms));
					}
					return null;
				}
			}
		}
				
		if(parts[SECOND_IDX] == null){
			if(p_dms.matches(".*[\"s″].*")){
				if(result != null){
					result.addError(
							MessageFormat.format(resourceBundle.getString("dms.error.unprocessable"),dms));
				}
				return null;
			}
		}
				
		//compute decimal value
		double decimal = (degree + (minute/60) + (second/3600))*negation;
		return decimal;
	}
	
	@Override
	public ErrorHandlingModeEnum getErrorHandlingMode() {
		return errorHandlingMode;
	}
	
	@Override
	public void setLocale(Locale locale) {
		this.resourceBundle = ResourceBundle.getBundle(ERROR_BUNDLE_NAME, locale);
	}
}
