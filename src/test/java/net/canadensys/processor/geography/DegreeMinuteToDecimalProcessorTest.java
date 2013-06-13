package net.canadensys.processor.geography;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URISyntaxException;

import net.canadensys.FileBasedTest;
import net.canadensys.processor.AbstractDataProcessor;
import net.canadensys.processor.ProcessingResult;
import net.canadensys.processor.dwc.mock.MockOccurrenceModel;
import net.canadensys.processor.dwc.mock.MockRawOccurrenceModel;
import net.canadensys.utils.NumberUtils;

import org.junit.Test;

/**
 * Tests for the DegreeMinuteToDecimalProcessor
 * @author canadensys
 *
 */
public class DegreeMinuteToDecimalProcessorTest {
	
	private static final String TEST_FILE_NAME = "/DDMMSSCoordinatesFormat.txt";
	
	private int LAT_IDX = 0;
	private int LNG_IDX = 1;
	
	@Test
	public void testDegreeMinuteToDecimalProcessor(){
		final DegreeMinuteToDecimalProcessor dmtdProcessor = new DegreeMinuteToDecimalProcessor();
		Double[] output = new Double[2];
		
		//test supported syntax (from the test file)
		try {
			final File dateFile = new File(getClass().getResource(TEST_FILE_NAME).toURI());
			FileBasedTest fileBasedTest = new FileBasedTest(dateFile) {
				Double[] output = null;
				Double lat, lng, delta;
				@Override
				public void processLine(String[] elements, int lineNumber) {
					if(elements.length == 5){
						output = dmtdProcessor.process(elements[0], elements[1], null);
						lat = NumberUtils.parseNumber(elements[2], Double.class);
						lng = NumberUtils.parseNumber(elements[3], Double.class);
						delta = NumberUtils.parseNumber(elements[4], Double.class);
						String assertText = "[Line #" + lineNumber + " in " + dateFile.getName()+"]";
						assertNotNull(assertText, output);
						assertEquals(assertText, lat, output[LAT_IDX], delta);
						assertEquals(assertText, lng, output[LNG_IDX], delta);
					}
				}
			};
			
			fileBasedTest.processFile();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		output = dmtdProcessor.process("40º26′47″N","74º 0' 21.5022\"W", null);
		assertEquals(40.44639,output[LAT_IDX],0.00001);

		output = dmtdProcessor.process("40°26′47″N","74° 0' 21.5022\"W", null);
		assertEquals(40.44639,output[LAT_IDX],0.00001);
		
		//test with double precision
		output = dmtdProcessor.process("40°26′47″N","76°27'11\" W",null);
		assertEquals(-76.45305555555557d,output[LNG_IDX],0);
		
		//no seconds
		output = dmtdProcessor.process("40d 26'N","30°17′12″E", null);
		assertEquals(40.433334f,output[LAT_IDX].floatValue(),0);
		
		//decimal minutes
		output = dmtdProcessor.process("40d 26.17'N","30°17′12″E", null);
		assertEquals(40.436165f,output[LAT_IDX].floatValue(),0);
		
		//decimal on the second
		output = dmtdProcessor.process("40:26:47.5N","30°17′12″E", null);
		assertEquals(40.44653f, output[LAT_IDX].floatValue(),0);
		
		//already decimal degree
		output = dmtdProcessor.process("45.5° N","-129.6° W", null);
		assertEquals(45.5f, output[LAT_IDX].floatValue(),0);
		assertEquals(-129.6f, output[LNG_IDX].floatValue(),0);
		
		//one digit degree
		output = dmtdProcessor.process("1:2:3 N","4:5:6 W", null);
		assertEquals(1.0341667f, output[LAT_IDX].floatValue(),0);
		assertEquals(-4.085f, output[LNG_IDX].floatValue(),0);
		
		//Test Java bean
		MockRawOccurrenceModel rawModel = new MockRawOccurrenceModel();
		MockOccurrenceModel occModel = new MockOccurrenceModel();
		rawModel.setVerbatimLatitude("40°26′47″N");
		rawModel.setVerbatimLongitude("30°17′12″E");

		DegreeMinuteToDecimalProcessor dmtdProcessor2 = new DegreeMinuteToDecimalProcessor("verbatimLatitude","verbatimLongitude","decimalLatitude", "decimalLongitude");
		dmtdProcessor2.processBean(rawModel, occModel, null, null);
		assertEquals(40.44639f, occModel.getDecimalLatitude().floatValue(),0);
	}
	
	@Test
	public void testDegreeMinuteToDecimalValidation(){
		AbstractDataProcessor dataProcessor = new DegreeMinuteToDecimalProcessor("verbatimLatitude","verbatimLongitude","decimalLatitude", "decimalLongitude");
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
		Double[] nullOutput = {null,null};
		
		//test no cardinal direction
		assertArrayEquals(nullOutput, dmtdProcessor.process("40°26'47\"","30°17′12″E", null));
		
		//test wrong cardinal direction
		assertArrayEquals(nullOutput, dmtdProcessor.process("40°26'47T","30°17′12″E", null));
		
		//test no cardinal directions
		assertArrayEquals(nullOutput, dmtdProcessor.process("40°N26'47\"","30°17′12″", null));
	
		//decimal on the degree is not supported if minute and/or second is used
		assertArrayEquals(nullOutput, dmtdProcessor.process("40.1:26:47N","30°17′12″E", null));
		
		//test decimal coordinate
		assertArrayEquals(nullOutput, dmtdProcessor.process("40.44653N","30°17′12″E", null));
		
		//second but no minute
		assertArrayEquals(nullOutput, dmtdProcessor.process("40d8.29sN","30°17′12″E", null));
		
		//decimal on the minute AND second is not supported
		assertArrayEquals(nullOutput, dmtdProcessor.process("40:26.1:47N","30°17′12″E", null));
		assertArrayEquals(nullOutput, dmtdProcessor.process("40:26.1:47.2N","30°17′12″E", null));
	}
}
