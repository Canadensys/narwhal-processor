package net.canadensys.processor.geography;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.canadensys.processor.DataProcessor;
import net.canadensys.processor.ProcessingResult;
import net.canadensys.utils.NumberUtils;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

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
	//(\d*\.?\d+) :any int or float
	//(?:[\'m′: ])* :followed by an optional 'm:′
	//(\d*\.?\d+)* :any int or float
	//[\"s″ ]? :followed by an optional "s″
	protected static Pattern  SPLIT_DMS_PARTS = Pattern.compile("(\\d{1,3})(?:[°d: ]+)(\\d*\\.?\\d+)(?:[\'m′: ])*(\\d*\\.?\\d+)*[\"s″ ]?");
	
	protected static final String DEFAULT_COORDINATE_NAME = "coordinate";
	
	//Those field names will only be used with JavaBean
	protected String coordinateInName = null;
	protected String coordinateOutName = null;
	
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
	 * @param coordinateOutName name of the Float field in the output JavaBean
	 */
	public DegreeMinuteToDecimalProcessor(String coordinateInName, String coordinateOutName){
		this.coordinateInName = coordinateInName;
		this.coordinateOutName = coordinateOutName;
	}
	
	/**
	 * Degree/minute/second to decimal Bean processing function.
	 * @param in Java bean containing the coordinate degree/minute/seconds as String
	 * @param out Java bean containing the decimal coordinate as Float 
	 * @param params Will be ignored so use null
	 * @param result Optional ProcessingResult
	 */
	@Override
	public void processBean(Object in, Object out, Map<String, Object> params, ProcessingResult result) {
		try {
			String val1 = (String)PropertyUtils.getSimpleProperty(in, coordinateInName);
			Float coord = process(val1,result);
			PropertyUtils.setSimpleProperty(out, coordinateOutName, coord);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
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
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		//not valid, check if the value was mandatory
		if(!isMandatory && StringUtils.isBlank(textCoordinate)){
			return true;
		}
		return false;
	}
		
	/**
	 * Degree/minute/second to decimal processing function.
	 * Note : Could move to Double if Float precision is not enough
	 * @param dms degree/minute/second string
	 * @param result optional
	 * @return decimal value of the dms coordinate or null
	 */
	public Float process(String dms, ProcessingResult result){
		
		if(StringUtils.isBlank(dms)){
			return null;
		}
		
		//make sure we have a cardinal direction at the end
		if(!CHECK_CARDINAL_DIRECTION_PATTERN.matcher(dms).find()){
			if(result != null){
				result.addError("No cardinal direction provided in [" + dms +"]");
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
				result.addError("Could not match the pattern on [" + dms +"]");
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
		Float degree = NumberUtils.parseNumber(parts[DEGREE_IDX], Float.class,0f);
		Float minute = NumberUtils.parseNumber(parts[MINUTE_IDX], Float.class,0f);
		Float second = NumberUtils.parseNumber(parts[SECOND_IDX], Float.class,0f);
		
		//make sure that we extracted all the numbers
		if(!KEEP_NUMBERS_PATTERN.matcher(parts[DEGREE_IDX]+parts[MINUTE_IDX]+StringUtils.defaultString(parts[SECOND_IDX], ""))
				.replaceAll("").equalsIgnoreCase(allNumbers)){
			if(result != null){
				result.addError("Could not match the pattern on [" + dms +"]");
			}
			return null;
		}
		
		//make sure that if we have decimals on minute, we don't second
		if(parts[MINUTE_IDX] != null){
			if(parts[MINUTE_IDX].contains(".")){
				if(parts[SECOND_IDX] != null){
					if(result != null){
						result.addError("Could not handle decimal on minute if second is provided [" + dms +"]");
					}
					return null;
				}
			}
		}
				
		if(parts[SECOND_IDX] == null){
			if(p_dms.matches(".*[\"s″].*")){
				if(result != null){
					result.addError("Could not handle this [" + dms +"]");
				}
				return null;
			}
		}
				
		//compute decimal value
		float decimal = (degree + (minute/60) + (second/3600))*negation;
		return decimal;
	}
	
	@Override
	public ErrorHandlingModeEnum getErrorHandlingMode() {
		return errorHandlingMode;
	}
}
