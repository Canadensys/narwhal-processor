package net.canadensys.parser;

import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.gbif.common.parsers.core.FileBasedDictionaryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

/**
 * Based on GBIF parsers, this implementation of FileBasedDictionaryParser is used when we have a dictionary
 * but we do not have matching enumeration(s) e.g. stateprovince.
 * Could also be used to parse fields like institutionCode or collectionCode if we have a dictionary.
 *
 * @author Pedro
 * @author cgendreau
 *
 */
public class DictionaryBasedValueParser extends FileBasedDictionaryParser<String> {

	final Logger logger = LoggerFactory.getLogger(DictionaryBasedValueParser.class);

	private static final CharMatcher LETTER_MATCHER = CharMatcher.JAVA_LETTER.or(CharMatcher.WHITESPACE).precomputed();
	private static final CharMatcher WHITESPACE_MATCHER = CharMatcher.WHITESPACE.precomputed();

	public DictionaryBasedValueParser(InputStream[] dictionaries) {
		this(false, dictionaries);
	}

	public DictionaryBasedValueParser(boolean caseSensitive, InputStream[] dictionaries) {
		super(caseSensitive);

		if (dictionaries != null) {
			for (InputStream input : dictionaries) {
				init(input);
			}
		}
	}

	@Override
	protected String normalize(String value) {
		if (value != null) {
			String processedValue = LETTER_MATCHER.retainFrom(value);
			processedValue = WHITESPACE_MATCHER.trimAndCollapseFrom(processedValue, ' ');
			processedValue = StringUtils.stripAccents(processedValue);
			processedValue = Strings.emptyToNull(processedValue);
			/**
			 * Normalisation of a value used both by adding to the internal dictionary and parsing values.
			 * The default does trim and uppercase the value for Strings, but leaves other types unaltered.
			 * Override this method to provide specific normalisations for parsers.
			 */
			return super.normalize(processedValue);
		}
		return null;
	}

	@Override
	protected String fromDictFile(String value) {
		return value;
	}
}
