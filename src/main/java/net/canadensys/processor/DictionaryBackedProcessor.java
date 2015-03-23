package net.canadensys.processor;

import java.io.InputStream;
import java.util.Map;

import net.canadensys.parser.TermValueParser;

import org.gbif.common.parsers.FileBasedDictionaryParser;
import org.gbif.common.parsers.ParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DictionaryBackedProcessor extends AbstractDataProcessor {

	final Logger logger = LoggerFactory.getLogger(DictionaryBackedProcessor.class);

	protected ErrorHandlingModeEnum errorHandlingMode;
	private TermValueParser stateProvinceNameParser;

	public DictionaryBackedProcessor(String fieldToBeChecked, String dictionaryFilePath, FileBasedDictionaryParser dictionary) {
		stateProvinceNameParser = new TermValueParser(false, new InputStream[] { DictionaryBackedProcessor.class.getResourceAsStream(dictionaryFilePath) });
		processValue(fieldToBeChecked);
		
	}

	public ParseResult<String>processValue(String value) {
        return stateProvinceNameParser.parse(value);
	}
	
	@Override
	public ErrorHandlingModeEnum getErrorHandlingMode() {
		return this.errorHandlingMode;
	}

	@Override
	public void processBean(Object in, Object out, Map<String, Object> params, ProcessingResult result) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean validateBean(Object in, boolean isMandatory, Map<String, Object> params, ProcessingResult result) {
		// TODO Auto-generated method stub
		return false;
	}
}