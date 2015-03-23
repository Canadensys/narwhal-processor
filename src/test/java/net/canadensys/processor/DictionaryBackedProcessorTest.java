package net.canadensys.processor;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import net.canadensys.parser.TermValueParser;

import org.gbif.common.parsers.FileBasedDictionaryParser;
import org.junit.Test;

public class DictionaryBackedProcessorTest {

	@Test
	public void testCanadaProvinceState(){

	FileBasedDictionaryParser canadaProvincesParser = new
	TermValueParser(new InputStream[] {
			this.getClass().getResourceAsStream("/dictionaries/geography/CA_StateProvinceName.txt")
	});
	
	DictionaryBackedProcessor canadaProvincesProcessor = new DictionaryBackedProcessor(canadaProvincesParser);
    assertEquals("CA-NL", canadaProvincesProcessor.processValue("Labrador"));
    assertEquals("CA-NL", canadaProvincesProcessor.processValue("NEWFOUNdLAND AND LABRADOR"));

    FileBasedDictionaryParser brazilProvincesParser = new
			TermValueParser(new InputStream[] {
					this.getClass().getResourceAsStream("/dictionaries/geography/BR_StateProvinceName.txt")
			});

    DictionaryBackedProcessor brasilProvincesProcessor = new DictionaryBackedProcessor(brazilProvincesParser);
    assertEquals("BR-PB", brasilProvincesProcessor.processValue("ParaÌba	"));
    assertEquals("BR-RR", brasilProvincesProcessor.processValue("Roraima;Amazonas"));

	}
}
