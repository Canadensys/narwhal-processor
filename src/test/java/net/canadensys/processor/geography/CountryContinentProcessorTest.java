package net.canadensys.processor.geography;

import static org.junit.Assert.*;

import net.canadensys.processor.DataProcessor;
import net.canadensys.processor.ProcessingResult;
import net.canadensys.processor.dwc.mock.MockOccurrenceModel;
import net.canadensys.vocabulary.Continent;

import org.junit.Test;

/**
 * Test for the CountryContinentProcessor
 * @author canadensys
 *
 */
public class CountryContinentProcessorTest {

	@Test
	public void testCountryContinent(){
		DataProcessor dataProcessor = new CountryContinentProcessor("countryISOLetterCode", "continent");
		
		MockCountryCodeHolder mockRawModel = new MockCountryCodeHolder();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();

		mockRawModel.setCountryISOLetterCode("CA");
		ProcessingResult pr = new ProcessingResult();
		dataProcessor.processBean(mockRawModel, mockModel, null, pr);
		
		assertEquals(Continent.NORTH_AMERICA.getTitle(), mockModel.getContinent());
	}
	
	@Test
	public void testCountryContinentValidation(){
		DataProcessor dataProcessor = new CountryContinentProcessor("countryISOLetterCode", "continent");
		MockCountryCodeHolder mockRawModel = new MockCountryCodeHolder();
		mockRawModel.setCountryISOLetterCode("CA");
		assertTrue(dataProcessor.validateBean(mockRawModel, false, null, null));
		
		//test mandatory flag
		mockRawModel.setCountryISOLetterCode(null);
		assertFalse(dataProcessor.validateBean(mockRawModel, true, null, null));
		assertTrue(dataProcessor.validateBean(mockRawModel, false, null, null));
	}
	
	@Test
	public void testWrongCountryContinent(){
		DataProcessor dataProcessor = new CountryContinentProcessor("countryISOLetterCode", "continent");
		
		MockCountryCodeHolder mockRawModel = new MockCountryCodeHolder();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();

		mockRawModel.setCountryISOLetterCode("test");
		ProcessingResult pr = new ProcessingResult();
		dataProcessor.processBean(mockRawModel, mockModel, null, pr);
		assertNull(mockModel.getContinent());
	}
	
	/**
	 * Create a inner public class to test the bean feature.
	 * We use an inner class since it will only be used here.
	 */
	public class MockCountryCodeHolder{
		private String countryISOLetterCode;

		public String getCountryISOLetterCode() {
			return countryISOLetterCode;
		}
		public void setCountryISOLetterCode(String countryISOLetterCode) {
			this.countryISOLetterCode = countryISOLetterCode;
		}
	}
}
