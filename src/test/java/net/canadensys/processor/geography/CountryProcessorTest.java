package net.canadensys.processor.geography;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.canadensys.processor.AbstractDataProcessor;
import net.canadensys.processor.AbstractDataProcessor.ErrorHandlingModeEnum;
import net.canadensys.processor.ProcessingResult;
import net.canadensys.processor.dwc.mock.MockOccurrenceModel;

import org.junit.Test;

/**
 * Tests for the CountryProcessor
 * 
 * @author canadensys
 * 
 */
public class CountryProcessorTest {

	@Test
	public void testCountryProcessor() {
		AbstractDataProcessor dataProcessor = new CountryProcessor();
		MockOccurrenceModel mockRawModel = new MockOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();

		mockRawModel.setCountry("u.s.a");
		ProcessingResult pr = new ProcessingResult();
		dataProcessor.processBean(mockRawModel, mockModel, null, pr);
		assertEquals("United States", mockModel.getCountry());
	}

	@Test
	public void testCountryValidation() {
		AbstractDataProcessor dataProcessor = new CountryProcessor();
		MockOccurrenceModel mockRawModel = new MockOccurrenceModel();
		mockRawModel.setCountry("u.s.a");
		assertTrue(dataProcessor.validateBean(mockRawModel, false, null, null));

		// test mandatory flag
		mockRawModel.setCountry("");
		assertFalse(dataProcessor.validateBean(mockRawModel, true, null, null));
		assertTrue(dataProcessor.validateBean(mockRawModel, false, null, null));
	}

	@Test
	public void testWrongCountry() {
		AbstractDataProcessor dataProcessor = new CountryProcessor();
		MockOccurrenceModel mockRawModel = new MockOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();

		mockRawModel.setCountry("xyz");
		ProcessingResult pr = new ProcessingResult();
		dataProcessor.processBean(mockRawModel, mockModel, null, pr);
		assertEquals("xyz", mockModel.getCountry());
	}

	@Test
	public void testWrongCountryOtherErrorHandling() {
		AbstractDataProcessor dataProcessor = new CountryProcessor("country", ErrorHandlingModeEnum.USE_NULL);
		MockOccurrenceModel mockRawModel = new MockOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();

		// test null
		mockRawModel.setCountry("xyz");
		ProcessingResult pr = new ProcessingResult();
		dataProcessor.processBean(mockRawModel, mockModel, null, pr);
		assertNull(mockModel.getCountry());

		// test empty value
		dataProcessor = new CountryProcessor("country", ErrorHandlingModeEnum.USE_EMPTY);
		dataProcessor.processBean(mockRawModel, mockModel, null, pr);
		assertEquals("", mockModel.getCountry());
	}
}
