package net.canadensys.processor;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.gbif.common.parsers.core.FileBasedDictionaryParser;
import org.gbif.common.parsers.core.ParseResult;
import org.gbif.common.parsers.core.ParseResult.CONFIDENCE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pedro
 * @author cgendreau
 *
 */
public class DictionaryBackedProcessor extends AbstractDataProcessor {

	final Logger logger = LoggerFactory.getLogger(DictionaryBackedProcessor.class);

	private final ErrorHandlingModeEnum errorHandlingMode = ErrorHandlingModeEnum.USE_ORIGINAL;
	private final FileBasedDictionaryParser<String> fileBasedDisctionaryParser;
	private final String beanPropertyName;

	public DictionaryBackedProcessor(FileBasedDictionaryParser<String> fileBasedDictionaryParser) {
		this(null, fileBasedDictionaryParser);
	}

	public DictionaryBackedProcessor(String beanPropertyName, FileBasedDictionaryParser<String> fileBasedDictionaryParser) {
		this.fileBasedDisctionaryParser = fileBasedDictionaryParser;
		this.beanPropertyName = beanPropertyName;
	}

	public String process(String value, ProcessingResult result) {
		ParseResult<String> parsingResult = fileBasedDisctionaryParser.parse(value);
		if (parsingResult.isSuccessful() && parsingResult.getConfidence().equals(CONFIDENCE.DEFINITE)) {
			return parsingResult.getPayload();
		}

		if (result != null) {
			// TODO beanPropertyName can be null
			result.addError(
					MessageFormat.format(resourceBundle.getString("dictionary.error.notFound"), value, beanPropertyName));
		}
		return null;
	}

	@Override
	public ErrorHandlingModeEnum getErrorHandlingMode() {
		return this.errorHandlingMode;
	}

	@Override
	public void processBean(Object in, Object out, Map<String, Object> params, ProcessingResult result) {
		try {
			String propertyText = (String) PropertyUtils.getSimpleProperty(in, beanPropertyName);
			String propertyProcessed = process(propertyText, result);

			if (propertyProcessed == null) {
				switch (errorHandlingMode) {
					case USE_ORIGINAL:
						propertyProcessed = propertyText;
						break;
					case USE_NULL:
						propertyProcessed = null;
						break;
					case USE_EMPTY:
						propertyProcessed = "";
						break;
					default:
						propertyProcessed = null;
						break;
				}
			}
			PropertyUtils.setSimpleProperty(out, beanPropertyName, propertyProcessed);
		}
		catch (IllegalAccessException e) {
			logger.error("Bean access error", e);
		}
		catch (InvocationTargetException e) {
			logger.error("Bean access error", e);
		}
		catch (NoSuchMethodException e) {
			logger.error("Bean access error", e);
		}
	}

	@Override
	public boolean validateBean(Object in, boolean isMandatory, Map<String, Object> params, ProcessingResult result) {
		String propertyText = null;
		try {
			propertyText = (String) PropertyUtils.getSimpleProperty(in, beanPropertyName);
			if (process(propertyText, result) != null) {
				return true;
			}
			// change to multiple Exception catch when moving to Java 7
		}
		catch (IllegalAccessException e) {
			logger.error("Bean access error", e);
			return false;
		}
		catch (InvocationTargetException e) {
			logger.error("Bean access error", e);
			return false;
		}
		catch (NoSuchMethodException e) {
			logger.error("Bean access error", e);
			return false;
		}

		// no valid country was found, check if this value was mandatory
		if (!isMandatory && StringUtils.isBlank(propertyText)) {
			return true;
		}
		return false;
	}
}