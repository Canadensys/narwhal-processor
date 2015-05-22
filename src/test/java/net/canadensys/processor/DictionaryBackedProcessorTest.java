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
	public void testCanadaProvinceState() {

		// Dictionary CA_StateProvinceName.txt to be removed in favor of WingLongitude shared dictionary
		DictionaryBasedValueParser canadaProvincesParser = new DictionaryBasedValueParser(new InputStream[] { this.getClass().getResourceAsStream(
				"/dictionaries/geography/CA_StateProvinceName.txt") });

		DictionaryBackedProcessor processor = new DictionaryBackedProcessor("stateprovince", canadaProvincesParser);
		assertEquals("CA-NL", processor.process("Labrador", null));

		// Test bean processing
		MockOccurrenceModel mockRawModel = new MockOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();
		mockRawModel.setStateprovince("The Northwest Territories");
		processor.processBean(mockRawModel, mockModel, null, null);
		assertEquals("CA-NT", mockModel.getStateprovince());

		// Load test dictionary dictionary (Brazilian departments)
		DictionaryBasedValueParser brazilProvincesParser = new DictionaryBasedValueParser(new InputStream[] { this.getClass().getResourceAsStream(
				"/dictionary.txt") });

		// Create processor objects for each dictionary:
		DictionaryBackedProcessor brasilProvincesProcessor = new DictionaryBackedProcessor(brazilProvincesParser);

		// Process values in 2 steps: 1) map value to ISO Code 2) Map ISO Code to full deparment name:
		assertEquals("Paraíba", brasilProvincesProcessor.process(brasilProvincesProcessor.process("ParaÌba	", null), null));
		assertEquals("Roraima", brasilProvincesProcessor.process(brasilProvincesProcessor.process("Roraima;Amazonas", null), null));
	}

	@Test
	public void testWrongValueErrorHandling() {
		DictionaryBasedValueParser canadaProvincesParser = new DictionaryBasedValueParser(new InputStream[] { this.getClass().getResourceAsStream(
				"/dictionaries/geography/CA_StateProvinceName.txt") });

		AbstractDataProcessor processor = new DictionaryBackedProcessor("stateprovince", canadaProvincesParser, ErrorHandlingModeEnum.USE_NULL);

		MockOccurrenceModel mockRawModel = new MockOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();

		// test null
		mockRawModel.setStateprovince("xyz");
		ProcessingResult pr = new ProcessingResult();
		processor.processBean(mockRawModel, mockModel, null, pr);
		assertNull(mockModel.getStateprovince());

		// test empty value
		processor = new DictionaryBackedProcessor("stateprovince", canadaProvincesParser, ErrorHandlingModeEnum.USE_EMPTY);
		processor.processBean(mockRawModel, mockModel, null, pr);
		assertEquals("", mockModel.getStateprovince());
	}
}
