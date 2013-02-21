package net.canadensys.processor.geography;

import net.canadensys.processor.ProcessingResult;
import net.canadensys.processor.dwc.NumericPairDataProcessor;

/**
 * Data processor to handle latitude and longitude data.
 * Boundaries validation will be applied.
 * @author canadensys
 *
 */
public class DecimalLatLongProcessor extends NumericPairDataProcessor{
	
	public static final int LATITUDE_IDX = 0;
	public static final int LONGITUDE_IDX = 1;
	
	//decimalLatitude : Legal values lie between -90 and 90, inclusive.
	public static final int MIN_LATITUDE = -90;
	public static final int MAX_LATITUDE = 90;
	
	//decimalLongitude : Legal values lie between -180 and 180, inclusive.
	public static final int MIN_LONGITUDE = -180;
	public static final int MAX_LONGITUDE = 180;
	
	//DarwinCore terms
	protected static final String DWC_DECIMAL_LATITUDE = "decimalLatitude";
	protected static final String DWC_DECIMAL_LONGITUDE = "decimalLongitude";
	
	/**
	 * Default constructor, property name will be assigned to the matching DarwinCore property name.
	 */
	public DecimalLatLongProcessor() {
		super(DWC_DECIMAL_LATITUDE, DWC_DECIMAL_LONGITUDE);
	}
	
	public DecimalLatLongProcessor(String latitudePropertyName, String longitudePropertyName) {
		super(latitudePropertyName, longitudePropertyName);
	}
	
	@Override
	public void process(String value1, String value2, Number[] output,
			Class<? extends Number> clazz, ProcessingResult result) {
		super.process(value1, value2, output, clazz, result);
		
		//validate output boundaries
		if(output[LATITUDE_IDX]!=null){
			if(output[LATITUDE_IDX].intValue() > MAX_LATITUDE || output[LATITUDE_IDX].intValue() < MIN_LATITUDE){
				if(result != null){
					result.addError("Value ["+output[LATITUDE_IDX].intValue()+"] is out of bound. Should be between "
							+ MIN_LATITUDE +  " and " + MAX_LATITUDE);
				}
				output[LATITUDE_IDX] = null;
			}
		}
		
		if(output[LONGITUDE_IDX]!=null){
			if(output[LONGITUDE_IDX].intValue() > MAX_LONGITUDE || output[LONGITUDE_IDX].intValue() < MIN_LONGITUDE){
				if(result != null){
					result.addError("Value ["+output[LONGITUDE_IDX].intValue()+"] is out of bound.Should be between "
							+ MIN_LONGITUDE +  " and " + MAX_LONGITUDE);
				}
				output[LONGITUDE_IDX] = null;
			}
		}
		
		//to be a valid coordinate we need both latitude and longitude to be valid
		if(output[LATITUDE_IDX] == null || output[LONGITUDE_IDX] == null){
			output[LATITUDE_IDX] = null;
			output[LONGITUDE_IDX] = null;
		}
	}
}
