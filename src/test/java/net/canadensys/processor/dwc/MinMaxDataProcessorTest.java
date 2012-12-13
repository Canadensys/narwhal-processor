package net.canadensys.processor.dwc;

import static org.junit.Assert.*;

import net.canadensys.processor.dwc.mock.MockOccurrenceModel;
import net.canadensys.processor.dwc.mock.MockRawOccurrenceModel;

import org.junit.Test;

/**
 * Test for basic MinMaxDataProcessor
 * @author canadensys
 *
 */
public class MinMaxDataProcessorTest {
	
	@Test
	public void testProcessing(){		
		MockRawOccurrenceModel mockRawModel = new MockRawOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();
		
		mockRawModel.setMinAltitude("125.8m");
		mockRawModel.setMaxAltitude("1147 meters");
		
		MinMaxDataProcessor processor = new MinMaxDataProcessor("minAltitude", "maxAltitude");
		processor.processBean(mockRawModel, mockModel, null);
		
		assertEquals(125.8, mockModel.getMinAltitude(),0);
		assertEquals(1147, mockModel.getMaxAltitude(),0);
		
		//Test negative and single dot
		mockRawModel.setMinAltitude("-125.8m");
		mockRawModel.setMaxAltitude("-1147. meters");
		processor.processBean(mockRawModel, mockModel, null);
		
		assertEquals(-125.8, mockModel.getMinAltitude(),0);
		assertEquals(-1147, mockModel.getMaxAltitude(),0);
	}
	
	@Test
	public void testProcessingWrongData(){
		MockRawOccurrenceModel mockRawModel = new MockRawOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();
		
		mockRawModel.setMinAltitude("125.8m");
		mockRawModel.setMaxAltitude("meters");
		
		MinMaxDataProcessor processor = new MinMaxDataProcessor("minAltitude", "maxAltitude");
		processor.processBean(mockRawModel, mockModel, null);
		
		assertEquals(125.8, mockModel.getMinAltitude(),0);
		assertNull( mockModel.getMaxAltitude());
		
		//test double dot
		mockRawModel.setMinAltitude("125.8.1m");
		mockRawModel.setMaxAltitude("-1147..meters");
		processor.processBean(mockRawModel, mockModel, null);
		
		assertNull(mockModel.getMinAltitude());
		assertNull(mockModel.getMaxAltitude());
	}
}