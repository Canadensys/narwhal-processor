package net.canadensys.processor.geography;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;

import net.canadensys.processor.AbstractDataProcessor;
import net.canadensys.processor.ProcessingResult;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.gbif.api.vocabulary.Country;
import org.gbif.common.parsers.CountryParser;
import org.gbif.common.parsers.core.ParseResult;
import org.gbif.common.parsers.core.ParseResult.CONFIDENCE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Country processor to handle country names.
 * Parsing a country string into a controlled vocabulary (org.gbif.api.model.vocabulary.Country).
 * You should reuse the same instance to save resources.
 *
 * @author canadensys
 *
 */
public class CountryProcessor extends AbstractDataProcessor {

	final Logger logger = LoggerFactory.getLogger(CountryProcessor.class);

	protected static CountryParser COUNTRY_PARSER = CountryParser.getInstance();
	protected static final String DEFAULT_COUNTRY_NAME = "country";

	protected String countryName = null;
	protected ErrorHandlingModeEnum errorHandlingMode;

	/**
	 * Default constructor, default field names and ErrorHandlingModeEnum.USE_ORIGINAL will be used.
	 */
	public CountryProcessor() {
		this(DEFAULT_COUNTRY_NAME, ErrorHandlingModeEnum.USE_ORIGINAL);
	}

	public CountryProcessor(String countryName, ErrorHandlingModeEnum errorHandlingMode) {
		this.errorHandlingMode = errorHandlingMode;
		this.countryName = countryName;
		// always a default Locale
		setLocale(Locale.ENGLISH);
	}

	/**
	 * Country Bean processing function.
	 *
	 * @param in
	 *            Java bean containing the country as String
	 * @param out
	 *            Java bean containing the country (from controlled vocabulary) as String
	 * @param params
	 *            Will be ignored so use null
	 * @param result
	 *            Optional ProcessingResult
	 */
	@Override
	public void processBean(Object in, Object out, Map<String, Object> params, ProcessingResult result) {
		try {
			String textCountry = (String) PropertyUtils.getSimpleProperty(in, countryName);
			Country resultCountry = process(textCountry, result);

			String country = null;
			if (resultCountry != null) {
				country = resultCountry.getTitle();
			}
			else {
				// should it be done in process(...) function?
				switch (errorHandlingMode) {
					case USE_ORIGINAL:
						country = textCountry;
						break;
					case USE_NULL:
						country = null;
						break;
					case USE_EMPTY:
						country = "";
						break;
					default:
						break;
				}
			}
			PropertyUtils.setSimpleProperty(out, countryName, country);
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
		String textCountry = null;
		try {
			textCountry = (String) PropertyUtils.getSimpleProperty(in, countryName);
			if (process(textCountry, result) != null) {
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
		if (!isMandatory && StringUtils.isBlank(textCountry)) {
			return true;
		}
		return false;
	}

	/**
	 * Country processing function.
	 * Note that the errorHandlingMode will be ignored by this function.
	 *
	 * @param countryStr
	 *            country string to be processed
	 * @param result
	 *            optional ProcessingResult
	 * @return matching Country.
	 */
	public Country process(String countryStr, ProcessingResult result) {
		if (StringUtils.isBlank(countryStr)) {
			return null;
		}

		ParseResult<Country> parsingResult = COUNTRY_PARSER.parse(countryStr);
		if (parsingResult.isSuccessful() && parsingResult.getConfidence().equals(CONFIDENCE.DEFINITE)) {
			return parsingResult.getPayload();
		}
		else {
			if (result != null) {
				result.addError(MessageFormat.format(resourceBundle.getString("country.error.notFound"), countryStr));
			}
		}
		return null;
	}

	@Override
	public ErrorHandlingModeEnum getErrorHandlingMode() {
		return errorHandlingMode;
	}
}
