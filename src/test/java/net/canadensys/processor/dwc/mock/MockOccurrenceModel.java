package net.canadensys.processor.dwc.mock;

/**
 * Mock model representing an OccurrenceModel with processed data.
 * @author canandesys
 *
 */
public class MockOccurrenceModel {
	
	private Double decimalLatitude;
	private Double decimalLongitude;
	
	private Double minAltitude;
	private Double maxAltitude;
	
	public Double getMinAltitude() {
		return minAltitude;
	}
	public void setMinAltitude(Double minAltitude) {
		this.minAltitude = minAltitude;
	}
	public Double getMaxAltitude() {
		return maxAltitude;
	}
	public void setMaxAltitude(Double maxAltitude) {
		this.maxAltitude = maxAltitude;
	}
	public Double getDecimalLatitude() {
		return decimalLatitude;
	}
	public void setDecimalLatitude(Double decimalLatitude) {
		this.decimalLatitude = decimalLatitude;
	}
	public Double getDecimalLongitude() {
		return decimalLongitude;
	}
	public void setDecimalLongitude(Double decimalLongitude) {
		this.decimalLongitude = decimalLongitude;
	}
}
