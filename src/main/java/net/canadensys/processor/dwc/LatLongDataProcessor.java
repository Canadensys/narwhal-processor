package net.canadensys.processor.dwc;

import net.canadensys.processor.ProcessingResult;

/**
 * Data processor to handle latitude and longitude data.
 * Boundaries validation will be applied.
 * @author canadensys
 *
 */
public class LatLongDataProcessor extends NumericPairDataProcessor{
	
	//decimalLatitude : Legal values lie between -90 and 90, inclusive.
	public static final int MIN_LATITUDE = -90;
	public static final int MAX_LATITUDE = 90;
	
	//decimalLongitude : Legal values lie between -180 and 180, inclusive.
	public static final int MIN_LONGITUDE = -180;
	public static final int MAX_LONGITUDE = 180;
	
	//DarwinCore terms
	protected static final String DWC_DECIMAL_LATITUDE = "decimalLatitude";
	protected static final String DWC_DECIMAL_LONGITUDE = "decimalLongitude";
	
	public LatLongDataProcessor(String latitudePropertyName, String longitudePropertyName) {
		super(latitudePropertyName, longitudePropertyName);
	}
	
	/**
	 * Default constructor, property name will be assigned to the matching DarwinCore property name.
	 */
	public LatLongDataProcessor() {
		super(DWC_DECIMAL_LATITUDE, DWC_DECIMAL_LONGITUDE);
	}
	
	@Override
	public void process(String value1, String value2, Number[] output,
			Class<? extends Number> clazz, ProcessingResult result) {
		super.process(value1, value2, output, clazz, result);
		
		//validate output boundaries
		if(output[0]!=null){
			if(output[0].intValue() > MAX_LATITUDE || output[0].intValue() < MIN_LATITUDE){
				output[0] = null;
				if(result != null){
					result.addError("Value ["+output[0].intValue()+"] is out of bound. Should be between "
							+ MIN_LATITUDE +  " and " + MAX_LATITUDE);
				}
			}
		}
		
		if(output[1]!=null){
			if(output[1].intValue() > MAX_LONGITUDE || output[1].intValue() < MIN_LONGITUDE){
				output[1] = null;
				if(result != null){
					result.addError("Value ["+output[0].intValue()+"] is out of bound.Should be between "
							+ MIN_LONGITUDE +  " and " + MAX_LONGITUDE);
				}
			}
		}
	}	
}
