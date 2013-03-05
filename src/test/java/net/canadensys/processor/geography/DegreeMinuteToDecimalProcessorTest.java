package net.canadensys.processor.geography;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
	
	private int LAT_IDX = 0;
	private int LNG_IDX = 1;
	
	@Test
	public void testDegreeMinuteToDecimalProcessor(){
		DegreeMinuteToDecimalProcessor dmtdProcessor = new DegreeMinuteToDecimalProcessor();
		Double[] output = new Double[2];
		//test supported syntax
		dmtdProcessor.process("40°26′47″N","74° 0' 21.5022\"W",output, null);
		assertEquals(40.44639f,output[LAT_IDX].floatValue(),0);
		dmtdProcessor.process("40:26:47N ","30°17′12″E",output, null);
		assertEquals(40.44639f,output[LAT_IDX].floatValue(),0);
		dmtdProcessor.process("40°26'47\"N","30°17′12″E",output, null);
		assertEquals(40.44639f,output[LAT_IDX].floatValue(),0);
		dmtdProcessor.process("40d 26' 47\" N","30°17′12″E",output, null);
		assertEquals(40.44639f,output[LAT_IDX].floatValue(),0);
		dmtdProcessor.process("40d 26m 47N ","30°17′12″E",output, null);
		assertEquals(40.44639f,output[LAT_IDX].floatValue(),0);
		dmtdProcessor.process("40d 26m 47sN ","30°17′12″E",output, null);
		assertEquals(40.44639f,output[LAT_IDX].floatValue(),0);
		dmtdProcessor.process("40d 26m 47sN","74° 0' 21.5022\"W",output,null);
		assertEquals(-74.0059731f,output[LNG_IDX].floatValue(),0);
		
		//test with double precision
		dmtdProcessor.process("40°26′47″N","76°27'11\" W",output,null);
		assertEquals(-76.45305555555557d,output[LNG_IDX],0);
		
		//no seconds
		dmtdProcessor.process("40d 26'N","30°17′12″E",output, null);
		assertEquals(40.433334f,output[LAT_IDX].floatValue(),0);
		
		//decimal minutes
		dmtdProcessor.process("40d 26.17'N","30°17′12″E",output, null);
		assertEquals(40.436165f,output[LAT_IDX].floatValue(),0);
		
		//decimal on the second
		dmtdProcessor.process("40:26:47.5N","30°17′12″E", output, null);
		assertEquals(40.44653f, output[LAT_IDX].floatValue(),0);
		
		//Test Java bean
		MockRawOccurrenceModel rawModel = new MockRawOccurrenceModel();
		MockOccurrenceModel occModel = new MockOccurrenceModel();
		rawModel.setVerbatimLatitude("40°26′47″N");
		rawModel.setVerbatimLongitude("30°17′12″E");
		dmtdProcessor = new DegreeMinuteToDecimalProcessor("verbatimLatitude","verbatimLongitude","decimalLatitude", "decimalLongitude");
		dmtdProcessor.processBean(rawModel, occModel, null, null);
		assertEquals(40.44639f, occModel.getDecimalLatitude().floatValue(),0);
	}
	
	@Test
	public void testDegreeMinuteToDecimalValidation(){
		DataProcessor dataProcessor = new DegreeMinuteToDecimalProcessor("verbatimLatitude","verbatimLongitude","decimalLatitude", "decimalLongitude");
		MockRawOccurrenceModel rawModel = new MockRawOccurrenceModel();
		rawModel.setVerbatimLatitude("40°26′47″N");
		rawModel.setVerbatimLongitude("30°17′12″E");
		assertTrue(dataProcessor.validateBean(rawModel, false, null, null));
		
		//test mandatory flag
		rawModel.setVerbatimLatitude("");
		rawModel.setVerbatimLongitude("");
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
		Double[] output = new Double[2];
		Double[] nullOutput = {null,null};
		
		//test no cardinal direction
		dmtdProcessor.process("40°26'47\"","30°17′12″E",output, null);
		assertArrayEquals(nullOutput, output);
		
		//test wrong cardinal direction
		dmtdProcessor.process("40°26'47T","30°17′12″E",output, null);
		assertArrayEquals(nullOutput, output);
		
		//test no cardinal directions
		dmtdProcessor.process("40°N26'47\"","30°17′12″",output, null);
		assertArrayEquals(nullOutput, output);
		
		//decimal on the degree is not supported
		dmtdProcessor.process("40.1:26:47N","30°17′12″E",output, null);
		assertArrayEquals(nullOutput, output);
		
		//test decimal coordinate
		dmtdProcessor.process("40.44653N","30°17′12″E",output, null);
		assertArrayEquals(nullOutput, output);
		
		//second but no minute
		dmtdProcessor.process("40d8.29sN","30°17′12″E",output, null);
		assertArrayEquals(nullOutput, output);
		
		//decimal on the minute AND second is not supported
		dmtdProcessor.process("40:26.1:47N","30°17′12″E",output, null);
		assertArrayEquals(nullOutput, output);
		dmtdProcessor.process("40:26.1:47.2N","30°17′12″E",output, null);
		assertArrayEquals(nullOutput, output);
	}
}
