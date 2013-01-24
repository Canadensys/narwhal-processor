package net.canadensys.processor.dwc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import net.canadensys.processor.dwc.mock.MockOccurrenceModel;
import net.canadensys.processor.dwc.mock.MockRawOccurrenceModel;

import org.junit.Test;

/**
 * Test for the basic NumericPairDataProcessor
 * @author canadensys
 *
 */
public class NumericPairDataProcessorTest {
	
	@Test
	public void testProcessing(){
		MockRawOccurrenceModel mockRawModel = new MockRawOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();
		
		mockRawModel.setMinAltitude("125.8m");
		mockRawModel.setMaxAltitude("1147 meters");
		
		NumericPairDataProcessor processor = new NumericPairDataProcessor("minAltitude", "maxAltitude");
		processor.processBean(mockRawModel, mockModel, null, null);
		
		assertEquals(125.8, mockModel.getMinAltitude(),0);
		assertEquals(1147, mockModel.getMaxAltitude(),0);
		
		//Test negative and single dot
		mockRawModel.setMinAltitude("-125.8m");
		mockRawModel.setMaxAltitude("-1147. meters");
		processor.processBean(mockRawModel, mockModel, null, null);
		
		assertEquals(-125.8, mockModel.getMinAltitude(),0);
		assertEquals(-1147, mockModel.getMaxAltitude(),0);
	}
	
	@Test
	public void testNumericPairDataValidation(){
		NumericPairDataProcessor processor = new NumericPairDataProcessor("minAltitude", "maxAltitude");
		MockRawOccurrenceModel mockRawModel = new MockRawOccurrenceModel();
		
		mockRawModel.setMinAltitude("125.8m");
		mockRawModel.setMaxAltitude("1147 meters");
		Map<String,Object> param = new HashMap<String,Object>();
		param.put(NumericPairDataProcessor.VALIDATE_CLASS_TAG, Double.class);
		assertTrue(processor.validateBean(mockRawModel, false, param, null));
		
		//test mandatory flag
		mockRawModel.setMinAltitude(null);
		mockRawModel.setMaxAltitude(null);
		assertFalse(processor.validateBean(mockRawModel, true, param, null));
		assertTrue(processor.validateBean(mockRawModel, false, param, null));		
	}
	
	@Test
	public void testProcessingWrongData(){
		MockRawOccurrenceModel mockRawModel = new MockRawOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();
		
		mockRawModel.setMinAltitude("125.8m");
		mockRawModel.setMaxAltitude("meters");
		
		NumericPairDataProcessor processor = new NumericPairDataProcessor("minAltitude", "maxAltitude");
		processor.processBean(mockRawModel, mockModel, null, null);
		
		assertEquals(125.8, mockModel.getMinAltitude(),0);
		assertNull( mockModel.getMaxAltitude());
		
		//test double dot
		mockRawModel.setMinAltitude("125.8.1m");
		mockRawModel.setMaxAltitude("-1147..meters");
		processor.processBean(mockRawModel, mockModel, null, null);
		
		assertNull(mockModel.getMinAltitude());
		assertNull(mockModel.getMaxAltitude());
	}
}