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

    // Load dictionary for brazillian departments normalization:
    FileBasedDictionaryParser brazilProvincesParser = new
			TermValueParser(new InputStream[] {
					this.getClass().getResourceAsStream("/dictionaries/geography/BR_StateProvinceName.txt")
			});
    // Load dictionary to convert from brazillian department ISO codes to department common name:
    FileBasedDictionaryParser brazilProvincesISOCommonParser = new
			TermValueParser(new InputStream[] {
					this.getClass().getResourceAsStream("/dictionaries/geography/BR_StateProvinceISOName.txt")
			});
    // Create processor objects for each dictionary:
    DictionaryBackedProcessor brasilProvincesProcessor = new DictionaryBackedProcessor(brazilProvincesParser);
    DictionaryBackedProcessor brazilProvincesISOCommonProcessor = new DictionaryBackedProcessor(brazilProvincesISOCommonParser);
    
    // Process values in 2 steps: 1) map value to ISO Code 2) Map ISO Code to full deparment name:
    assertEquals("Paraíba", brazilProvincesISOCommonProcessor.processValue(brasilProvincesProcessor.processValue("ParaÌba	")));
    assertEquals("Roraima", brazilProvincesISOCommonProcessor.processValue(brasilProvincesProcessor.processValue("Roraima;Amazonas")));
	}
}
