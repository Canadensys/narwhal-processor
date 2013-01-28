package net.canadensys.processor.geography;

import static org.junit.Assert.*;

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
		assertEquals(40.44639f,dmtdProcessor.process("40:26:47N", null).floatValue(),0);
		assertEquals(40.44639f,dmtdProcessor.process("40°26'47\"N", null).floatValue(),0);
		assertEquals(40.44639f,dmtdProcessor.process("40d 26' 47\" N", null).floatValue(),0);
		
		//no seconds
		assertEquals(40.433334f,dmtdProcessor.process("40d 26'N", null).floatValue(),0);
		
		//decimal minutes
		assertEquals(40.436165f,dmtdProcessor.process("40d 26.17'N", null).floatValue(),0);
		//assertEquals(40.446195f,dmtdProcessor.process("40° 26.7717", null).floatValue(),0);
		
		//decimal on the second
		assertEquals(40.44653f, dmtdProcessor.process("40:26:47.5N", null).floatValue(),0);
		
		//test bean
		
	}
	
	@Test
	public void testWrongDMSToDecimal(){
		DegreeMinuteToDecimalProcessor dmtdProcessor = new DegreeMinuteToDecimalProcessor();
		//test no cardinal direction
		assertNull(dmtdProcessor.process("40°26'47\"", null));
		
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
