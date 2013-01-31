package net.canadensys.processor.geography;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import net.canadensys.processor.DataProcessor;
import net.canadensys.processor.ProcessingResult;

/**
 * Allows to split a coordinate pair string into latitude and longitude parts.
 * The pair must be separated by one of the following characters ,;/\t
 * The resulting latitude and longitude are strings and could be invalid or unusable in their current representation.
 * 
 * @author canadensys
 *
 */
public class CoordinatePairProcessor implements DataProcessor{
	
	public static final int LATITUDE = 0;
	public static final int LONGITUDE = 1;
	
	protected static Pattern  DECIMAL_COORD_PAIR = Pattern.compile("(^\\s?\\-?\\d{1,3}\\.?\\d+)[,;\\/ \\t]+(\\-?\\d{1,3}\\.?\\d+)");
	protected static Pattern  DMS_COORD_PAIR = Pattern.compile("(.+)[,;\\/\\t]+(.+)");
	
	protected static Pattern CHECK_LONGITUDE = Pattern.compile("[EW]\\s*$", Pattern.CASE_INSENSITIVE);
	
	protected static final String DEFAULT_COORDINATE_PAIR_NAME = "coordinates";
	protected static final String DEFAULT_LATITUDE_NAME = "latitude";
	protected static final String DEFAULT_LONGITUDE_NAME = "longitude";
	
	//Those field names will only be used with JavaBean
	protected String coordinatePairName = null;
	protected String latitudeName = null;
	protected String longitudeName = null;
	
	//Only USE_NULL makes sense here
	protected ErrorHandlingModeEnum errorHandlingMode = ErrorHandlingModeEnum.USE_NULL;
	
	/**
	 * Default constructor, default field names will be used.
	 */
	public CoordinatePairProcessor(){
		this(DEFAULT_COORDINATE_PAIR_NAME,DEFAULT_LATITUDE_NAME,DEFAULT_LONGITUDE_NAME);
	}
	
	/**
	 * @param coordinatePairName name of the String field in the input JavaBean
	 * @param latitudeName name of the latitude String field in the output JavaBean
	 * @param longitudeName name of the longitude String field in the output JavaBean
	 */
	public CoordinatePairProcessor(String coordinatePairName, String latitudeName,  String longitudeName){
		this.coordinatePairName = coordinatePairName;
		this.latitudeName = latitudeName;
		this.longitudeName = longitudeName;
	}
	
	/**
	 * Coordinate pair Bean processing function.
	 * @param in JavaBean containing the coordinate pair as String
	 * @param out Java Bean containing latitude and longitude as String
	 * @param params Will be ignored so use null
	 * @param result Optional ProcessingResult
	 */
	@Override
	public void processBean(Object in, Object out, Map<String, Object> params, ProcessingResult result) {
		try {
			String coordinate = (String)PropertyUtils.getSimpleProperty(in, coordinatePairName);
			String[] coordinatePair = process(coordinate,result);
			if(coordinatePair != null){
				PropertyUtils.setSimpleProperty(out, latitudeName, coordinatePair[LATITUDE]);
				PropertyUtils.setSimpleProperty(out, longitudeName, coordinatePair[LONGITUDE]);
			}
			else{
				PropertyUtils.setSimpleProperty(out, latitudeName, null);
				PropertyUtils.setSimpleProperty(out, longitudeName, null);
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Coordinate pair processing function.
	 * @param coordinatePair coordinate pair as decimal or degree/minute/seconds. Decimal coordinates must be
	 * in lat,long order. dd/mm/ss can be in both orders, the E or W letter will be used to identify longitude.
	 * @return 2 dimensional String array containing latitude and longitude or null
	 */
	public String[] process(String coordinatePair, ProcessingResult result){
		
		if(StringUtils.isBlank(coordinatePair)){
			return null;
		}
		
		//Instantiate later to avoid unnecessary <new>
		String[] coordinates = null;
		
		//try decimal coordinates
		Matcher m = DECIMAL_COORD_PAIR.matcher(coordinatePair);
		//find() need to be called to use group(idx)
		if(m.find()){
			if(m.groupCount() == 2){
				coordinates = new String[2];
				coordinates[LATITUDE] = m.group(1);
				coordinates[LONGITUDE] = m.group(2);
				return coordinates;
			}
		}
		
		m = DMS_COORD_PAIR.matcher(coordinatePair);
		//find() need to be called to use group(idx)
		if(m.find()){
			if(m.groupCount() == 2){
				coordinates = new String[2];
				if(CHECK_LONGITUDE.matcher(m.group(2)).find()){
					coordinates[LATITUDE] = m.group(1);
					coordinates[LONGITUDE] = m.group(2);
				}
				else{
					if(CHECK_LONGITUDE.matcher(m.group(1)).find()){
						coordinates[LATITUDE] = m.group(2);
						coordinates[LONGITUDE] = m.group(1);
					}
					else{
						if(result != null){
							result.addError("Could not find cardinal direction in [" + coordinatePair + "]");
						}
					}
				}
			}
		}
		if(coordinates == null || coordinates[LATITUDE] == null || coordinates[LONGITUDE] == null){
			if(result != null){
				result.addError("Could not find valid coordinate pair in [" + coordinatePair + "]");
			}
			return null;
		}
		return coordinates;
	}

	@Override
	public ErrorHandlingModeEnum getErrorHandlingMode() {
		return errorHandlingMode;
	}

	@Override
	public boolean validateBean(Object in, boolean isMandatory, Map<String, Object> params, ProcessingResult result) {
		String textCoordinate = null;
		try {
			textCoordinate = (String)PropertyUtils.getSimpleProperty(in, coordinatePairName);
			String[] coordinatePair = process(textCoordinate,result);
			//should we test empty string?
			if(coordinatePair != null){
				return true;
			}
		//change to multiple Exception catch when moving to Java 7
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return false;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return false;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return false;
		}
		
		//not valid, check if the value was mandatory
		if(!isMandatory && StringUtils.isBlank(textCoordinate)){
			return true;
		}
		return false;
	}

}
