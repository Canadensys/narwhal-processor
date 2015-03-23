package net.canadensys.parser;

import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.gbif.common.parsers.FileBasedDictionaryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

public class TermValueParser extends FileBasedDictionaryParser {

	final Logger logger = LoggerFactory.getLogger(TermValueParser.class);

	private static final CharMatcher LETTER_MATCHER = CharMatcher.JAVA_LETTER.or(CharMatcher.WHITESPACE).precomputed();
	private static final CharMatcher WHITESPACE_MATCHER = CharMatcher.WHITESPACE.precomputed();

	public TermValueParser(InputStream[] dictionaries) {
		// Default caseSensitive to false
		super(false, dictionaries);
	}

	@Override
	protected String normalize(String value) {
		if (value != null) {
			String stateProvince = LETTER_MATCHER.retainFrom(value);
			stateProvince = WHITESPACE_MATCHER.trimAndCollapseFrom(stateProvince, ' ');
			stateProvince = StringUtils.stripAccents(stateProvince);
			stateProvince = Strings.emptyToNull(stateProvince);
			/**
			 * Normalisation of a value used both by adding to the internal dictionary and parsing values.
			 * The default does trim and uppercase the value for Strings, but leaves other types unaltered.
			 * Override this method to provide specific normalisations for parsers.
			 */
			return super.normalize(stateProvince);
		}
		return null;
	}
}
