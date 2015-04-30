package net.canadensys.processor.person;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;

import net.canadensys.FileBasedTest;
import net.canadensys.processor.AbstractDataProcessor.ErrorHandlingModeEnum;
import net.canadensys.processor.dwc.mock.MockOccurrenceModel;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * Test PersonNameProcessor from a reference file.
 *
 * @author cgendreau
 *
 */
public class PersonNameProcessorTest {

	private static final int TEST_FILE_COLUMNS = 2;
	private static final String TEST_FILE_NAME = "/personsName.txt";

	@Test
	public void testPersonNameProcessor() throws URISyntaxException {
		final PersonNameProcessor pnProcessor = new PersonNameProcessor();
		final File dateFile = new File(getClass().getResource(TEST_FILE_NAME).toURI());

		FileBasedTest fileBasedTest = new FileBasedTest(dateFile, "%") {
			@Override
			public void processLine(String[] elements, int lineNumber) {
				String rawNames;
				String names;
				if (elements.length == TEST_FILE_COLUMNS) {
					rawNames = elements[0];
					names = elements[1];
					assertEquals(names, StringUtils.join(pnProcessor.process(rawNames, null), "|"));
				}
				else {
					fail("[Line #" + lineNumber + " in " + dateFile.getName() + "] is not valid.");
				}
			}
		};
		fileBasedTest.processFile();
	}

	@Test
	public void testPersonNameProcessorWithBean() {
		MockOccurrenceModel rawOcc = new MockOccurrenceModel();
		MockOccurrenceModel occ = new MockOccurrenceModel();
		rawOcc.setRecordedBy("Charles Darwin; Carl Linnaeus");

		final PersonNameProcessor pnProcessor = new PersonNameProcessor("recordedBy", ErrorHandlingModeEnum.USE_ORIGINAL);
		pnProcessor.processBean(rawOcc, occ, null, null);
		assertEquals("Charles Darwin|Carl Linnaeus", occ.getRecordedBy());
	}

}
