package net.canadensys.processor;

import java.util.Map;

import net.canadensys.parser.TermValueParser;

import org.gbif.common.parsers.ParseResult;
import org.gbif.common.parsers.ParseResult.CONFIDENCE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DictionaryBackedProcessor extends AbstractDataProcessor {

	final Logger logger = LoggerFactory.getLogger(DictionaryBackedProcessor.class);

	protected ErrorHandlingModeEnum errorHandlingMode;
	private TermValueParser termValueParser;

	public DictionaryBackedProcessor(TermValueParser dictionary) {
		this.termValueParser = dictionary;
	}

	public String processValue(String value) {
		ParseResult<String> parsingResult = termValueParser.parse(value);
		if (parsingResult.isSuccessful() && parsingResult.getConfidence().equals(CONFIDENCE.DEFINITE)) {
			return parsingResult.getPayload();
		}
		else {
			return null;
		}
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