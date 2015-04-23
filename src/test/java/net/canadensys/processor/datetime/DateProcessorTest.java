package net.canadensys.processor.datetime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;

import net.canadensys.FileBasedTest;
import net.canadensys.processor.ProcessingResult;
import net.canadensys.processor.dwc.mock.MockOccurrenceModel;
import net.canadensys.processor.dwc.mock.MockRawOccurrenceModel;
import net.canadensys.utils.NumberUtils;

import org.junit.Test;

/**
 * Unit tests for the DateProcessor
 * 
 * @author canadensys
 * 
 */
public class DateProcessorTest {

	private static final String TEST_FILE_NAME = "/dateFormat.txt";

	@Test
	public void testFromFile() {

		final DateProcessor dateProcessor = new DateProcessor("eventDate", "eventStartYear", "eventStartMonth", "eventStartDay");

		try {
			final File dateFile = new File(getClass().getResource(TEST_FILE_NAME).toURI());
			FileBasedTest fileBasedTest = new FileBasedTest(dateFile) {
				Integer year, month, day;
				MockRawOccurrenceModel mockRawModel = new MockRawOccurrenceModel();
				MockOccurrenceModel mockModel = new MockOccurrenceModel();

				@Override
				public void processLine(String[] elements, int lineNumber) {
					if (elements.length == 4) {
						mockRawModel.setEventDate(elements[0]);
						dateProcessor.processBean(mockRawModel, mockModel, null, null);

						year = NumberUtils.parseNumber(elements[1], Integer.class);
						month = NumberUtils.parseNumber(elements[2], Integer.class);
						day = NumberUtils.parseNumber(elements[3], Integer.class);
						String assertText = "[Line #" + lineNumber + " in " + dateFile.getName() + "]";
						assertEquals(assertText, year, mockModel.getEventStartYear());
						assertEquals(assertText, month, mockModel.getEventStartMonth());
						assertEquals(assertText, day, mockModel.getEventStartDay());
						System.out.println("Testing date : " + elements[0]);
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
	}

	@Test
	public void testDateValidation() {
		DateProcessor dateProcessor = new DateProcessor("eventDate", "eventStartYear", "eventStartMonth", "eventStartDay");
		MockRawOccurrenceModel mockRawModel = new MockRawOccurrenceModel();
		mockRawModel.setEventDate("2010-01-02");

		assertTrue(dateProcessor.validateBean(mockRawModel, false, null, null));

		// test mandatory flag
		mockRawModel.setEventDate(null);
		assertFalse(dateProcessor.validateBean(mockRawModel, true, null, null));
		assertTrue(dateProcessor.validateBean(mockRawModel, false, null, null));
	}

	@Test
	public void testFuzyDates() {
		DateProcessor dateProcessor = new DateProcessor("eventDate", "eventStartYear", "eventStartMonth", "eventStartDay");

		MockRawOccurrenceModel mockRawModel = new MockRawOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();

		mockRawModel.setEventDate("10-11-2010");
		ProcessingResult pr = new ProcessingResult();
		dateProcessor.processBean(mockRawModel, mockModel, null, pr);

		assertNull(mockModel.getEventStartYear());
		assertNull(mockModel.getEventStartMonth());
		assertNull(mockModel.getEventStartDay());
		assertTrue(pr.getErrorList().size() >= 1);
	}

	@Test
	public void testBadRomanNumeralsDates() {
		DateProcessor dateProcessor = new DateProcessor("eventDate", "eventStartYear", "eventStartMonth", "eventStartDay");

		MockRawOccurrenceModel mockRawModel = new MockRawOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();

		// bad roman numeral
		mockRawModel.setEventDate("10-VX-2010");
		ProcessingResult pr = new ProcessingResult();
		dateProcessor.processBean(mockRawModel, mockModel, null, pr);

		assertNull(mockModel.getEventStartYear());
		assertNull(mockModel.getEventStartMonth());
		assertNull(mockModel.getEventStartDay());
		assertTrue(pr.getErrorList().size() >= 1);

		// bad month
		mockRawModel.setEventDate("10-XV-2010");
		pr.getErrorList().clear();
		dateProcessor.processBean(mockRawModel, mockModel, null, pr);

		assertNull(mockModel.getEventStartYear());
		assertNull(mockModel.getEventStartMonth());
		assertNull(mockModel.getEventStartDay());
		assertTrue(pr.getErrorList().size() >= 1);

		// roman numeral for day
		mockRawModel.setEventDate("X-05-2010");
		pr.getErrorList().clear();
		dateProcessor.processBean(mockRawModel, mockModel, null, pr);

		assertNull(mockModel.getEventStartYear());
		assertNull(mockModel.getEventStartMonth());
		assertNull(mockModel.getEventStartDay());
		assertTrue(pr.getErrorList().size() >= 1);
	}

	@Test
	public void testStandardizeDatePunctuation() {
		DateProcessor dateProcessor = new DateProcessor("eventDate", "eventStartYear", "eventStartMonth", "eventStartDay");

		assertEquals("8-11-2003", dateProcessor.standardizeDatePunctuation("8/11/2003"));
		assertEquals("08-11-2003", dateProcessor.standardizeDatePunctuation("08.11.2003"));
		assertEquals("08-11-2003", dateProcessor.standardizeDatePunctuation("08-11-2003"));
		assertEquals("08-11-2003", dateProcessor.standardizeDatePunctuation("08.11 2003"));
		assertEquals("10-10-2000", dateProcessor.standardizeDatePunctuation("10 10 2000"));
		assertEquals("10-Oct-2000", dateProcessor.standardizeDatePunctuation("10 Oct 2000"));
		assertEquals("10-Oct-2000", dateProcessor.standardizeDatePunctuation("10/Oct/2000"));
		assertEquals("10-Oct-2000", dateProcessor.standardizeDatePunctuation("10.Oct. 2000"));

		// Should remain the same
		assertEquals("10-Oct-2000", dateProcessor.standardizeDatePunctuation("10-Oct-2000"));
	}
}
