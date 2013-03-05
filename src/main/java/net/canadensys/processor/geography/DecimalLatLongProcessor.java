package net.canadensys.processor.geography;

import net.canadensys.processor.ProcessingResult;
import net.canadensys.processor.numeric.NumericPairDataProcessor;

/**
 * Data processor to handle latitude and longitude data.
 * Boundaries validation will be applied.
 * @author canadensys
 *
 */
public class DecimalLatLongProcessor extends NumericPairDataProcessor{
	
	//DarwinCore terms
	protected static final String DWC_DECIMAL_LATITUDE = "decimalLatitude";
	protected static final String DWC_DECIMAL_LONGITUDE = "decimalLongitude";
	
	protected LatLongProcessorHelper latLongHelper = null;

	/**
	 * Default constructor, property name will be assigned to the matching DarwinCore property name.
	 */
	public DecimalLatLongProcessor() {
		this(DWC_DECIMAL_LATITUDE, DWC_DECIMAL_LONGITUDE);
	}
	
	public DecimalLatLongProcessor(String latitudePropertyName, String longitudePropertyName) {
		super(latitudePropertyName, longitudePropertyName);
		latLongHelper = new LatLongProcessorHelper(resourceBundle);
	}
	
	public DecimalLatLongProcessor(String latitudeInPropertyName, String longitudeInPropertyName,
			String latitudeOutPropertyName, String longitudeOutPropertyName) {
		super(latitudeInPropertyName, longitudeInPropertyName, latitudeOutPropertyName, longitudeOutPropertyName);
	}
	
	@Override
	public void process(String value1, String value2, Number[] output,
			Class<? extends Number> clazz, ProcessingResult result) {
		super.process(value1, value2, output, clazz, result);
		
		//validate output boundaries
		latLongHelper.ensureLatLongBoundaries(output, result);
	}
}
