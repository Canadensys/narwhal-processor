package net.canadensys.processor.dwc.mock;

/**
 * Mock model representing an OccurrenceModel with processed data.
 * @author canandesys
 *
 */
public class MockOccurrenceModel {
	
	private String country;
	
	private Double decimalLatitude;
	private Double decimalLongitude;
	
	private Double minAltitude;
	private Double maxAltitude;
	
	private Integer eventStartYear;
	private Integer eventStartMonth;
	private Integer eventStartDay;
	
	private String continent;
	
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
	public Integer getEventStartYear() {
		return eventStartYear;
	}
	public void setEventStartYear(Integer eventStartYear) {
		this.eventStartYear = eventStartYear;
	}
	public Integer getEventStartMonth() {
		return eventStartMonth;
	}
	public void setEventStartMonth(Integer eventStartMonth) {
		this.eventStartMonth = eventStartMonth;
	}
	public Integer getEventStartDay() {
		return eventStartDay;
	}
	public void setEventStartDay(Integer eventStartDay) {
		this.eventStartDay = eventStartDay;
	}
	public String getContinent() {
		return continent;
	}
	public void setContinent(String continent) {
		this.continent = continent;
	}
	
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
}
