package net.canadensys.processor;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import net.canadensys.parser.TermValueParser;
import net.canadensys.processor.dwc.mock.MockOccurrenceModel;

import org.gbif.common.parsers.FileBasedDictionaryParser;
import org.junit.Test;

public class DictionaryBackedProcessorTest {

	@Test
	public void testCanadaProvinceState() {

		FileBasedDictionaryParser canadaProvincesParser = new TermValueParser(new InputStream[] { this.getClass().getResourceAsStream(
				"/dictionaries/geography/CA_StateProvinceName.txt") });

		DictionaryBackedProcessor canadaProvincesProcessor = new DictionaryBackedProcessor("stateprovince", canadaProvincesParser);
		assertEquals("CA-NL", canadaProvincesProcessor.process("Labrador", null));

		// Test bean processing
		MockOccurrenceModel mockRawModel = new MockOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();
		mockRawModel.setStateprovince("The Northwest Territories");
		canadaProvincesProcessor.processBean(mockRawModel, mockModel, null, null);
		assertEquals("CA-NT", mockModel.getStateprovince());

		// Load dictionary for brazillian departments normalization:
		FileBasedDictionaryParser brazilProvincesParser = new TermValueParser(new InputStream[] { this.getClass().getResourceAsStream(
				"/dictionary.txt") });

		// Create processor objects for each dictionary:
		DictionaryBackedProcessor brasilProvincesProcessor = new DictionaryBackedProcessor(brazilProvincesParser);

		// Process values in 2 steps: 1) map value to ISO Code 2) Map ISO Code to full deparment name:
		assertEquals("Paraíba", brasilProvincesProcessor.process(brasilProvincesProcessor.process("ParaÌba	", null), null));
		assertEquals("Roraima", brasilProvincesProcessor.process(brasilProvincesProcessor.process("Roraima;Amazonas", null), null));
	}
}
