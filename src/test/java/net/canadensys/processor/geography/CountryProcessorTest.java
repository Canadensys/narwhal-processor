package net.canadensys.processor.geography;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import net.canadensys.processor.DataProcessor;
import net.canadensys.processor.DataProcessor.ErrorHandlingModeEnum;
import net.canadensys.processor.ProcessingResult;
import net.canadensys.processor.dwc.mock.MockOccurrenceModel;

import org.junit.Test;

/**
 * Tests for the CountryProcessor
 * @author canadensys
 *
 */
public class CountryProcessorTest {
	
	@Test
	public void testCountryProcessor(){
		DataProcessor dateProcessor = new CountryProcessor();
		MockOccurrenceModel mockRawModel = new MockOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();
		
		mockRawModel.setCountry("u.s.a");
		ProcessingResult pr = new ProcessingResult();
		dateProcessor.processBean(mockRawModel, mockModel, null, pr);
		assertEquals("United States",mockModel.getCountry());
	}
	
	@Test
	public void testWrongCountry(){
		DataProcessor dateProcessor = new CountryProcessor();
		MockOccurrenceModel mockRawModel = new MockOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();
		
		mockRawModel.setCountry("xyz");
		ProcessingResult pr = new ProcessingResult();
		dateProcessor.processBean(mockRawModel, mockModel, null, pr);
		assertEquals("xyz",mockModel.getCountry());
	}
	
	public void testWrongCountryOtherErrorHandling(){
		DataProcessor dateProcessor = new CountryProcessor("country",ErrorHandlingModeEnum.USE_NULL);
		MockOccurrenceModel mockRawModel = new MockOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();
		
		//test null
		mockRawModel.setCountry("xyz");
		ProcessingResult pr = new ProcessingResult();
		dateProcessor.processBean(mockRawModel, mockModel, null, pr);
		assertNull(mockModel.getCountry());
		
		//test empty value
		dateProcessor = new CountryProcessor("country",ErrorHandlingModeEnum.USE_EMPTY);
		dateProcessor.processBean(mockRawModel, mockModel, null, pr);
		assertEquals("",mockModel.getCountry());
	}
}
