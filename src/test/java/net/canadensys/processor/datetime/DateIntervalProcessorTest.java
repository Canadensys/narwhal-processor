package net.canadensys.processor.datetime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;

import net.canadensys.FileBasedTest;
import net.canadensys.processor.ProcessingResult;
import net.canadensys.processor.dwc.mock.MockRawOccurrenceModel;

import org.junit.Test;

/**
 * Unit tests for the DateIntervalProcessorTest
 * 
 * @author canadensys
 * 
 */
public class DateIntervalProcessorTest {
	private static final String TEST_FILE_NAME = "/dateIntervalFormat.txt";

	@Test
	public void testFromFile() {

		final DateIntervalProcessor dateIntervalProcessor = new DateIntervalProcessor();

		try {
			final File dateFile = new File(getClass().getResource(TEST_FILE_NAME).toURI());
			FileBasedTest fileBasedTest = new FileBasedTest(dateFile) {
				String startDate, endDate;
				String[] result;

				@Override
				public void processLine(String[] elements, int lineNumber) {
					if (elements.length == 3) {

						result = dateIntervalProcessor.process(elements[0], null);
						startDate = elements[1];
						endDate = elements[2];

						String assertText = "[Line #" + lineNumber + " in " + dateFile.getName() + "]";
						assertEquals(assertText, startDate, result[DateIntervalProcessor.START_DATE_IDX]);
						assertEquals(assertText, endDate, result[DateIntervalProcessor.END_DATE_IDX]);
						System.out.println("Testing date interval : " + elements[0]);
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
	public void testDateIntervalValidation() {
		DateIntervalProcessor dateIntervalProcessor = new DateIntervalProcessor("eventDate", "eventStartDate", "eventEndDate");
		MockRawOccurrenceModel mockRawModel = new MockRawOccurrenceModel();
		mockRawModel.setEventDate("1977-08-16/1977-08-20");

		assertTrue(dateIntervalProcessor.validateBean(mockRawModel, false, null, null));

		// test mandatory flag
		mockRawModel.setEventDate(null);
		assertFalse(dateIntervalProcessor.validateBean(mockRawModel, true, null, null));
		assertTrue(dateIntervalProcessor.validateBean(mockRawModel, false, null, null));
	}

	@Test
	public void testNonSymetricDateInterval() {
		DateIntervalProcessor dateIntervalProcessor = new DateIntervalProcessor("eventDate", "eventStartDate", "eventEndDate");
		MockRawOccurrenceModel mockRawModel = new MockRawOccurrenceModel();
		mockRawModel.setEventDate("1977-08-16,20");

		ProcessingResult pr = new ProcessingResult();
		assertFalse(dateIntervalProcessor.validateBean(mockRawModel, false, null, pr));
		assertTrue(pr.getErrorList().size() > 0);
	}
}
