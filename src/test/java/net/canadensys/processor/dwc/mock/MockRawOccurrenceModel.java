package net.canadensys.processor.dwc.mock;

/**
 * Mock model representing an OccurrenceModel with raw data.
 * @author canadensys
 *
 */
public class MockRawOccurrenceModel {
	
	private String decimalLatitude;
	private String decimalLongitude;
	
	private String minAltitude;
	private String maxAltitude;
	
	public String getMinAltitude() {
		return minAltitude;
	}
	public void setMinAltitude(String minAltitude) {
		this.minAltitude = minAltitude;
	}
	public String getMaxAltitude() {
		return maxAltitude;
	}
	public void setMaxAltitude(String maxAltitude) {
		this.maxAltitude = maxAltitude;
	}
	public String getDecimalLatitude() {
		return decimalLatitude;
	}
	public void setDecimalLatitude(String decimalLatitude) {
		this.decimalLatitude = decimalLatitude;
	}
	public String getDecimalLongitude() {
		return decimalLongitude;
	}
	public void setDecimalLongitude(String decimalLongitude) {
		this.decimalLongitude = decimalLongitude;
	}
}
