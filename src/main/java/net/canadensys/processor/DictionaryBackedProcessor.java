package net.canadensys.processor;

import java.util.Map;

import net.canadensys.parser.TermValueParser;

import org.gbif.common.parsers.FileBasedDictionaryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DictionaryBackedProcessor extends AbstractDataProcessor {

	final Logger logger = LoggerFactory.getLogger(DictionaryBackedProcessor.class);

	protected ErrorHandlingModeEnum errorHandlingMode;
	private TermValueParser stateProvinceNameParser;

	public DictionaryBackedProcessor(ErrorHandlingModeEnum errorHandlingMode) {
		this.errorHandlingMode = errorHandlingMode;

	}

	public DictionaryBackedProcessor(String fieldToBeChecked, FileBasedDictionaryParser dictionary) {

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