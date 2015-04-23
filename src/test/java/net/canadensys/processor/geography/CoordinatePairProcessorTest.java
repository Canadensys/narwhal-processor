package net.canadensys.processor.geography;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;

import net.canadensys.FileBasedTest;
import net.canadensys.processor.AbstractDataProcessor;
import net.canadensys.processor.ProcessingResult;
import net.canadensys.processor.dwc.mock.MockRawOccurrenceModel;

import org.junit.Test;

/**
 * Tests for the CoordinatePairProcessor
 * @author canadensys
 *
 */
public class CoordinatePairProcessorTest {
	
	private static final String TEST_FILE_NAME = "/coordinatePairFormat.txt";
	private static final String TEST_FILE_ELEMENT_SEPARATOR = "%";
		
	@Test
	public void testCoordinatePairProcessor(){
		
		final CoordinatePairProcessor cpProcessor = new CoordinatePairProcessor();
		//String[] output;
		
		//test supported syntax (from the test file)
		try {
			final File dateFile = new File(getClass().getResource(TEST_FILE_NAME).toURI());
			FileBasedTest fileBasedTest = new FileBasedTest(dateFile,TEST_FILE_ELEMENT_SEPARATOR) {
				String[] output = null;
				String lat, lng;
				@Override
				public void processLine(String[] elements, int lineNumber) {
					if(elements.length == 3){
						output = cpProcessor.process(elements[0],null);
						lat = elements[1];
						lng = elements[2];
						String assertText = "[Line #" + lineNumber + " in " + dateFile.getName()+"]";
						assertNotNull(assertText, output);
						assertEquals(assertText, lat, output[CoordinatePairProcessor.LATITUDE_IDX]);
						assertEquals(assertText, lng, output[CoordinatePairProcessor.LONGITUDE_IDX]);
					}
					else{
						fail("[Line #" + lineNumber + " in " + dateFile.getName()+"] is not valid.");
					}
				}
			};
			
			fileBasedTest.processFile();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		//test JavaBean
		CoordinatePairProcessor cpProcessorBean = new CoordinatePairProcessor("verbatimCoordinates","verbatimLatitude","verbatimLongitude");
		MockRawOccurrenceModel rawModel = new MockRawOccurrenceModel();
		MockRawOccurrenceModel rawOutModel = new MockRawOccurrenceModel();
		rawModel.setVerbatimCoordinates("74° 0' 21.5022\"W/40°26'47\"N");
		cpProcessorBean.processBean(rawModel, rawOutModel, null, null);
		
		assertEquals("40°26'47\"N",rawOutModel.getVerbatimLatitude());
		assertEquals("74° 0' 21.5022\"W",rawOutModel.getVerbatimLongitude());
	}
	
	@Test
	public void testCoordinatePairValidation(){
		AbstractDataProcessor cpProcessor = new CoordinatePairProcessor("verbatimCoordinates","verbatimLatitude","verbatimLongitude");
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
		
		//no latitude
		ProcessingResult pr = new ProcessingResult();
		assertNull(cpProcessor.process("40°26'47\"W,74° 0' 21.5022E", pr));
	}
}
