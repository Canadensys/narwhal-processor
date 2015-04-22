package net.canadensys.processor.dwc.mock;

/**
 * Mock model representing an OccurrenceModel with raw data.
 * 
 * @author canadensys
 * 
 */
public class MockRawOccurrenceModel {

	private String decimalLatitude;
	private String decimalLongitude;

	private String verbatimCoordinates;

	private String verbatimLatitude;
	private String verbatimLongitude;

	private String minAltitude;
	private String maxAltitude;

	private String eventDate;

	private String country;
	private String stateprovince;

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

	public String getEventDate() {
		return eventDate;
	}

	public void setEventDate(String eventDate) {
		this.eventDate = eventDate;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getStateprovince() {
		return stateprovince;
	}

	public void setStateprovince(String stateprovince) {
		this.stateprovince = stateprovince;
	}

	public String getVerbatimLatitude() {
		return verbatimLatitude;
	}

	public void setVerbatimLatitude(String verbatimLatitude) {
		this.verbatimLatitude = verbatimLatitude;
	}

	public String getVerbatimLongitude() {
		return verbatimLongitude;
	}

	public void setVerbatimLongitude(String verbatimLongitude) {
		this.verbatimLongitude = verbatimLongitude;
	}

	public String getVerbatimCoordinates() {
		return verbatimCoordinates;
	}

	public void setVerbatimCoordinates(String verbatimCoordinates) {
		this.verbatimCoordinates = verbatimCoordinates;
	}
}
