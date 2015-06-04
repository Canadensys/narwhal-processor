package net.canadensys.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.InputStream;

import net.canadensys.parser.DictionaryBasedValueParser;
import net.canadensys.processor.AbstractDataProcessor.ErrorHandlingModeEnum;
import net.canadensys.processor.dwc.mock.MockOccurrenceModel;

import org.junit.Test;

/**
 * Test DictionaryBasedValueParser and DictionaryBackedProcessor behavior.
 *
 * @author Pedro
 * @author cgendreau
 *
 */
public class DictionaryBackedProcessorTest {

	@Test
	public void testProvinceStateDictionary() {

		// Load test dictionary dictionary (Brazilian departments)
		DictionaryBasedValueParser brazilProvincesParser = new DictionaryBasedValueParser(new InputStream[] { this.getClass().getResourceAsStream(
				"/dictionary.txt") });

		// Create processor objects for each dictionary:
		DictionaryBackedProcessor processor = new DictionaryBackedProcessor("stateprovince", brazilProvincesParser);

		// Test bean processing
		MockOccurrenceModel mockRawModel = new MockOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();
		mockRawModel.setStateprovince("Estado de Sao Paulo");
		processor.processBean(mockRawModel, mockModel, null, null);
		assertEquals("BR-SP", mockModel.getStateprovince());

		// Process values in 2 steps: 1) map value to ISO Code 2) Map ISO Code to full department name:
		assertEquals("Paraíba", processor.process(processor.process("ParaÌba	", null), null));
		assertEquals("Roraima", processor.process(processor.process("Roraima;Amazonas", null), null));
	}

	@Test
	public void testWrongValueErrorHandling() {
		// Load test dictionary dictionary (Brazilian departments)
		DictionaryBasedValueParser brazilProvincesParser = new DictionaryBasedValueParser(new InputStream[] { this.getClass().getResourceAsStream(
				"/dictionary.txt") });

		// Create processor objects for each dictionary:
		DictionaryBackedProcessor processor = new DictionaryBackedProcessor("stateprovince", brazilProvincesParser, ErrorHandlingModeEnum.USE_NULL);

		MockOccurrenceModel mockRawModel = new MockOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();

		// test null
		mockRawModel.setStateprovince("xyz");
		ProcessingResult pr = new ProcessingResult();
		processor.processBean(mockRawModel, mockModel, null, pr);
		assertNull(mockModel.getStateprovince());

		// test empty value
		processor = new DictionaryBackedProcessor("stateprovince", brazilProvincesParser, ErrorHandlingModeEnum.USE_EMPTY);
		processor.processBean(mockRawModel, mockModel, null, pr);
		assertEquals("", mockModel.getStateprovince());
	}

	@Test
	public void testCommentLine() {
		// Load test dictionary dictionary (Brazilian departments)
		DictionaryBasedValueParser brazilProvincesParser = new DictionaryBasedValueParser(new InputStream[] { this.getClass().getResourceAsStream(
				"/dictionary.txt") });

		// Create processor objects for each dictionary:
		DictionaryBackedProcessor processor = new DictionaryBackedProcessor("stateprovince", brazilProvincesParser, ErrorHandlingModeEnum.USE_NULL);
		assertNull(processor.process("BR-TST", null));
	}
}
