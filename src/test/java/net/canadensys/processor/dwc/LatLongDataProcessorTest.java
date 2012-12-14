package net.canadensys.processor.dwc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import net.canadensys.processor.ProcessingResult;
import net.canadensys.processor.dwc.mock.MockOccurrenceModel;
import net.canadensys.processor.dwc.mock.MockRawOccurrenceModel;

import org.junit.Test;

/**
 * Test for the LatLongDataProcessor
 * @author canadensys
 *
 */
public class LatLongDataProcessorTest {

	@Test
	public void testProcessing(){		
		MockRawOccurrenceModel mockRawModel = new MockRawOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();
		
		mockRawModel.setDecimalLatitude("45.8º");
		mockRawModel.setDecimalLongitude("100.4765 degree");
		
		NumericPairDataProcessor processor = new LatLongDataProcessor();
		processor.processBean(mockRawModel, mockModel, null, null);
		
		assertEquals(45.8, mockModel.getDecimalLatitude(),0);
		assertEquals(100.4765, mockModel.getDecimalLongitude(),0);
	}
	
	@Test
	public void testProcessingWrongData(){
		MockRawOccurrenceModel mockRawModel = new MockRawOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();
		
		//we do not set the latitude
		mockRawModel.setDecimalLongitude("degree");
		
		ProcessingResult result = new ProcessingResult();
		NumericPairDataProcessor processor = new LatLongDataProcessor();
		processor.processBean(mockRawModel, mockModel, null, result);
		
		assertNull(mockModel.getDecimalLatitude());
		assertNull(mockModel.getDecimalLongitude());
		assertEquals(1, result.getErrorList().size());
	}
}
