package net.canadensys.processor.geography;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.canadensys.processor.DataProcessor;
import net.canadensys.processor.ProcessingResult;
import net.canadensys.processor.dwc.mock.MockOccurrenceModel;
import net.canadensys.processor.dwc.mock.MockRawOccurrenceModel;

import org.junit.Test;

/**
 * Tests for the DegreeMinuteToDecimalProcessor
 * @author canadensys
 *
 */
public class DegreeMinuteToDecimalProcessorTest {
	
	@Test
	public void testDegreeMinuteToDecimalProcessor(){		
		DegreeMinuteToDecimalProcessor dmtdProcessor = new DegreeMinuteToDecimalProcessor();
		assertEquals(40.44639f,dmtdProcessor.process("40°26′47″N", null).floatValue(),0);
		assertEquals(40.44639f,dmtdProcessor.process("40:26:47N ", null).floatValue(),0);
		assertEquals(40.44639f,dmtdProcessor.process("40°26'47\"N", null).floatValue(),0);
		assertEquals(40.44639f,dmtdProcessor.process("40d 26' 47\" N", null).floatValue(),0);
		
		//no seconds
		assertEquals(40.433334f,dmtdProcessor.process("40d 26'N", null).floatValue(),0);
		
		//decimal minutes
		assertEquals(40.436165f,dmtdProcessor.process("40d 26.17'N", null).floatValue(),0);
		//assertEquals(40.446195f,dmtdProcessor.process("40° 26.7717", null).floatValue(),0);
		
		//decimal on the second
		assertEquals(40.44653f, dmtdProcessor.process("40:26:47.5N", null).floatValue(),0);
		
		//Test bean
		MockRawOccurrenceModel rawModel = new MockRawOccurrenceModel();
		MockOccurrenceModel occModel = new MockOccurrenceModel();
		rawModel.setVerbatimLatitude("40°26′47″N");
		
		dmtdProcessor = new DegreeMinuteToDecimalProcessor("verbatimLatitude","floatDecimalLatitude");
		dmtdProcessor.processBean(rawModel, occModel, null, null);
		assertEquals(40.44639f, occModel.getFloatDecimalLatitude().floatValue(),0);
	}
	
	@Test
	public void testDegreeMinuteToDecimalValidation(){
		DataProcessor dataProcessor = new DegreeMinuteToDecimalProcessor("verbatimLatitude","floatDecimalLatitude");
		MockRawOccurrenceModel rawModel = new MockRawOccurrenceModel();
		rawModel.setVerbatimLatitude("40°26′47″N");
		assertTrue(dataProcessor.validateBean(rawModel, false, null, null));
		
		//test mandatory flag
		rawModel.setVerbatimLatitude("");
		assertFalse(dataProcessor.validateBean(rawModel, true, null, null));
		assertTrue(dataProcessor.validateBean(rawModel, false, null, null));
		
		rawModel.setVerbatimLatitude("N");
		ProcessingResult pr = new ProcessingResult();
		assertFalse(dataProcessor.validateBean(rawModel, false, null, pr));
		assertEquals(1,pr.getErrorList().size());
	}
	
	@Test
	public void testWrongDMSToDecimal(){
		DegreeMinuteToDecimalProcessor dmtdProcessor = new DegreeMinuteToDecimalProcessor();
		//test no cardinal direction
		assertNull(dmtdProcessor.process("40°26'47\"", null));
		
		//test wrong cardinal direction
		assertNull(dmtdProcessor.process("40°26'47T", null));
		
		//test wrong cardinal direction
		assertNull(dmtdProcessor.process("40°N26'47\"", null));
		
		//decimal on the degree is not supported
		assertNull(dmtdProcessor.process("40.1:26:47N", null));
		
		//test decimal coordinate
		assertNull(dmtdProcessor.process("40.44653N", null));
		
		//second but no minute
		assertNull(dmtdProcessor.process("40d8.29sN", null));
		
		//decimal on the minute AND second is not supported
		assertNull(dmtdProcessor.process("40:26.1:47N", null));
		assertNull(dmtdProcessor.process("40:26.1:47.2N", null));
	}
}
