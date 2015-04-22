package net.canadensys.processor.geography;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

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
 * 
 * @author canadensys
 * 
 */
public class DegreeMinuteToDecimalProcessorTest {

	private static final String TEST_FILE_NAME = "/ValidDDMMSSCoordinates.txt";
	private static final String INVALID_DATA_FILE_NAME = "/InvalidDDMMSSCoordinates.txt";

	@Test
	public void testDegreeMinuteToDecimalProcessor() {
		final DegreeMinuteToDecimalProcessor dmtdProcessor = new DegreeMinuteToDecimalProcessor();

		// test supported syntax (from the test file)
		try {
			final File dateFile = new File(getClass().getResource(TEST_FILE_NAME).toURI());
			FileBasedTest fileBasedTest = new FileBasedTest(dateFile) {
				Double[] output = null;
				Double lat, lng, delta;

				@Override
				public void processLine(String[] elements, int lineNumber) {
					if (elements.length == 5) {
						output = dmtdProcessor.process(elements[0], elements[1], null);
						lat = NumberUtils.parseNumber(elements[2], Double.class);
						lng = NumberUtils.parseNumber(elements[3], Double.class);
						delta = NumberUtils.parseNumber(elements[4], Double.class);
						String assertText = "[Line #" + lineNumber + " in " + dateFile.getName() + "]";
						assertNotNull(assertText, output);
						assertEquals(assertText, lat, output[LatLongProcessorHelper.LATITUDE_IDX], delta);
						assertEquals(assertText, lng, output[LatLongProcessorHelper.LONGITUDE_IDX], delta);
					}
					else {
						fail("[Line #" + lineNumber + " in " + dateFile.getName() + "] is not valid.");
					}
				}
			};

			fileBasedTest.processFile();
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}

		Double[] output = new Double[2];

		// test with double precision
		output = dmtdProcessor.process("40°26′47″N", "76°27'11\" W", null);
		assertEquals(-76.45305555555557d, output[LatLongProcessorHelper.LONGITUDE_IDX], 0);

		// no seconds
		output = dmtdProcessor.process("40d 26'N", "30°17′12″E", null);
		assertEquals(40.433334f, output[LatLongProcessorHelper.LATITUDE_IDX].floatValue(), 0);

		// decimal minutes
		output = dmtdProcessor.process("40d 26.17'N", "30°17′12″E", null);
		assertEquals(40.436165f, output[LatLongProcessorHelper.LATITUDE_IDX].floatValue(), 0);

		// decimal on the second
		output = dmtdProcessor.process("40:26:47.5N", "30°17′12″E", null);
		assertEquals(40.44653f, output[LatLongProcessorHelper.LATITUDE_IDX].floatValue(), 0);

		// already decimal degree
		output = dmtdProcessor.process("45.5° N", "-129.6° W", null);
		assertEquals(45.5f, output[LatLongProcessorHelper.LATITUDE_IDX].floatValue(), 0);
		assertEquals(-129.6f, output[LatLongProcessorHelper.LONGITUDE_IDX].floatValue(), 0);

		// one digit degree
		output = dmtdProcessor.process("1:2:3 N", "4:5:6 W", null);
		assertEquals(1.0341667f, output[LatLongProcessorHelper.LATITUDE_IDX].floatValue(), 0);
		assertEquals(-4.085f, output[LatLongProcessorHelper.LONGITUDE_IDX].floatValue(), 0);

		// Test Java bean
		MockRawOccurrenceModel rawModel = new MockRawOccurrenceModel();
		MockOccurrenceModel occModel = new MockOccurrenceModel();
		rawModel.setVerbatimLatitude("40°26′47″N");
		rawModel.setVerbatimLongitude("30°17′12″E");

		DegreeMinuteToDecimalProcessor dmtdProcessor2 = new DegreeMinuteToDecimalProcessor("verbatimLatitude", "verbatimLongitude",
				"decimalLatitude", "decimalLongitude");
		dmtdProcessor2.processBean(rawModel, occModel, null, null);
		assertEquals(40.44639f, occModel.getDecimalLatitude().floatValue(), 0);
	}

	@Test
	public void testDegreeMinuteToDecimalValidation() {
		AbstractDataProcessor dataProcessor = new DegreeMinuteToDecimalProcessor("verbatimLatitude", "verbatimLongitude", "decimalLatitude",
				"decimalLongitude");
		MockRawOccurrenceModel rawModel = new MockRawOccurrenceModel();
		rawModel.setVerbatimLatitude("40°26′47″N");
		rawModel.setVerbatimLongitude("30°17′12″E");
		assertTrue(dataProcessor.validateBean(rawModel, false, null, null));

		// test mandatory flag
		rawModel.setVerbatimLatitude("");
		rawModel.setVerbatimLongitude("");
		assertFalse(dataProcessor.validateBean(rawModel, true, null, null));
		assertTrue(dataProcessor.validateBean(rawModel, false, null, null));

		rawModel.setVerbatimLatitude("N");
		ProcessingResult pr = new ProcessingResult();
		assertFalse(dataProcessor.validateBean(rawModel, false, null, pr));
		assertEquals(1, pr.getErrorList().size());
	}

	/**
	 * Test all coordinates from the file INVALID_DATA_FILE_NAME that should not process.
	 */
	@Test
	public void testWrongDMSToDecimal() {
		final DegreeMinuteToDecimalProcessor dmtdProcessor = new DegreeMinuteToDecimalProcessor();
		final Double[] nullOutput = { null, null };

		try {
			final File dateFile = new File(getClass().getResource(INVALID_DATA_FILE_NAME).toURI());
			FileBasedTest fileBasedTest = new FileBasedTest(dateFile) {
				@Override
				public void processLine(String[] elements, int lineNumber) {
					if (elements.length == 2) {
						String assertText = "[Line #" + lineNumber + " in " + dateFile.getName() + "]";
						assertArrayEquals(assertText, nullOutput, dmtdProcessor.process(elements[0], elements[1], null));
					}
					else {
						fail("[Line #" + lineNumber + " in " + dateFile.getName() + "] is not valid.");
					}
				}
			};

			fileBasedTest.processFile();
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
			fail();
		}
	}
}
