package net.canadensys.processor.geography;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.canadensys.processor.DataProcessor;
import net.canadensys.processor.dwc.mock.MockRawOccurrenceModel;

import org.junit.Test;

/**
 * Tests for the CoordinatePairProcessor
 * @author canadensys
 *
 */
public class CoordinatePairProcessorTest {
	
	@Test
	public void testCoordinatePairProcessor(){
		CoordinatePairProcessor cpProcessor = new CoordinatePairProcessor();
		
		String[] coordinates = cpProcessor.process("-71.87d;35.98degree", null);
		assertArrayEquals(new String[]{"-71.87d","35.98degree"}, coordinates);
		
		coordinates = cpProcessor.process("40°26'47\"N/74° 0' 21.5022\"W", null);
		assertArrayEquals(new String[]{"40°26'47\"N","74° 0' 21.5022\"W"}, coordinates);
		
		//test inverted coordinates
		coordinates = cpProcessor.process("74° 0' 21.5022\"W/40°26'47\"N", null);
		assertArrayEquals(new String[]{"40°26'47\"N","74° 0' 21.5022\"W"}, coordinates);
		
		//test JavaBean
		cpProcessor = new CoordinatePairProcessor("verbatimCoordinates","verbatimLatitude","verbatimLongitude");
		MockRawOccurrenceModel rawModel = new MockRawOccurrenceModel();
		MockRawOccurrenceModel rawOutModel = new MockRawOccurrenceModel();
		rawModel.setVerbatimCoordinates("74° 0' 21.5022\"W/40°26'47\"N");
		cpProcessor.processBean(rawModel, rawOutModel, null, null);
		
		assertEquals("40°26'47\"N",rawOutModel.getVerbatimLatitude());
		assertEquals("74° 0' 21.5022\"W",rawOutModel.getVerbatimLongitude());
	}
	
	@Test
	public void testCoordinatePairValidation(){
		DataProcessor cpProcessor = new CoordinatePairProcessor("verbatimCoordinates","verbatimLatitude","verbatimLongitude");
		MockRawOccurrenceModel rawModel = new MockRawOccurrenceModel();
		rawModel.setVerbatimCoordinates("74° 0' 21.5022\"W/40°26'47\"N");
		assertTrue(cpProcessor.validateBean(rawModel, false, null, null));
		
		rawModel.setVerbatimCoordinates("test");
		assertFalse(cpProcessor.validateBean(rawModel, false, null, null));
		
		//test mandatory flag
		rawModel.setVerbatimCoordinates("");
		assertFalse(cpProcessor.validateBean(rawModel, true, null, null));
		assertTrue(cpProcessor.validateBean(rawModel, false, null, null));
	}
	
	@Test
	public void testWrongCoordinatePairProcessor(){
		CoordinatePairProcessor cpProcessor = new CoordinatePairProcessor();
		
		//only one coordinates
		assertNull(cpProcessor.process("-71.87d;", null));
		assertNull(cpProcessor.process("40°26'47\"N", null));
		
		//no cardinal direction
		assertNull(cpProcessor.process("40°26'47\"N,74° 0' 21.5022", null));
	}
}
