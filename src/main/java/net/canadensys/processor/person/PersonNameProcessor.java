package net.canadensys.processor.person;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import net.canadensys.processor.AbstractDataProcessor;
import net.canadensys.processor.ProcessingResult;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 *
 * Regex based person name processor used to extract strings representing the person(s) name from a raw string.
 * e.g. "John W. Eastham 1963; stet! E.A. Snyder" will be processed as a List<String> with elements "John W. Eastham" and "E.A. Snyder"
 *
 * @author David P. Shorthouse
 * @author cgendreau
 *
 */
public class PersonNameProcessor extends AbstractDataProcessor {

	final Logger logger = LoggerFactory.getLogger(PersonNameProcessor.class);
	private final String beanPropertyName;

	private static final Pattern NORMALIZE = Pattern.compile(
			"\\bet\\s+al(\\.)?|" +
					"\\bu\\.\\s*a\\.|" +
					"(\\band|\\&)\\s+others|" +
					"\\betc(\\.)?|" +
					"\\b,\\s+\\d+|" +
					"\\b(?i:unknown)\\b|" +
					"\\b(?i:ann?onymous)\\b|" +
					"\\b(?i:undetermined)\\b|" +
					"[\":\\d+]");

	private static String BRACKETS_CHAR = "[\\[\\]()]";

	private static String ILLEGAL_CHAR = "[?!=]";

	private static String SPLIT_NAMES = "[–;|&\\/]|" +
			"\\b(?:\\s+-\\s+)\\b|" +
			"\\b(?i:with|and|et)\\b|" +
			"\\b(?i:annotated(\\s+by)?\\s+)\b|" +
			"\\b(?i:conf\\.?(\\s+by)?\\s+|confirmed(\\s+by)?\\s+)\\b|" +
			"\\b(?i:checked(\\s+by)?\\s+)\\b|" +
			"\\b(?i:det\\.?(\\s+by)?\\s+)\\b|" +
			"\\b(?i:dupl?\\.?(\\s+by)?\\s+|duplicate(\\s+by)?\\s+)\\b|" +
			"\\b(?i:ex\\.?(\\s+by)?\\s+|examined(\\s+by)?\\s+)\\b|" +
			"\\b(?i:in?dentified(\\s+by)?\\s+)\\b|" +
			"\\b(?i:in\\s+part(\\s+by)?\\s+)\\b|" +
			"\\b(?i:redet\\.?(\\s+by?)?\\s+)\\b|" +
			"\\b(?i:reidentified(\\s+by)?\\s+)\\b|" +
			"\\b(?i:stet!?)\\b|" +
			"\\b(?i:then(\\s+by)?\\s+)\\b|" +
			"\\b(?i:ver\\.?(\\s+by)?\\s+|verf\\.?(\\s+by)?\\s+|verified?(\\s+by)?\\s+)\\b";

	protected ErrorHandlingModeEnum errorHandlingMode = null;

	public PersonNameProcessor() {
		this(null, ErrorHandlingModeEnum.USE_ORIGINAL);
	}

	public PersonNameProcessor(String beanPropertyName, ErrorHandlingModeEnum errorHandlingMode) {
		this.beanPropertyName = beanPropertyName;
		this.errorHandlingMode = errorHandlingMode;
		// always a default Locale
		setLocale(Locale.ENGLISH);
	}

	/**
	 * Normalize a raw value by removing unwanted characters
	 *
	 * @param rawValue
	 * @return
	 */
	private String normalize(String rawValue) {
		rawValue = rawValue.replaceAll(BRACKETS_CHAR, StringUtils.SPACE);
		rawValue = rawValue.replaceAll(ILLEGAL_CHAR, StringUtils.EMPTY);
		return rawValue;
	}

	@Override
	public ErrorHandlingModeEnum getErrorHandlingMode() {
		return errorHandlingMode;
	}

	@Override
	public void processBean(Object in, Object out, Map<String, Object> params, ProcessingResult result) {
		try {
			String rawPersonName = (String) PropertyUtils.getSimpleProperty(in, beanPropertyName);
			List<String> personNameList = process(rawPersonName, result);

			if (!personNameList.isEmpty()) {
				PropertyUtils.setSimpleProperty(out, beanPropertyName, StringUtils.join(personNameList, "|"));
			}
			else {
				PropertyUtils.setSimpleProperty(out, beanPropertyName, getValueOnError(errorHandlingMode, rawPersonName));
			}
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
			if (!process(propertyText, result).isEmpty()) {
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

	/**
	 * Split a string representing one or multiple persons name into a List of normalized name(s).
	 *
	 * @param rawValue
	 * @param result
	 * @return
	 */
	public List<String> process(String rawValue, ProcessingResult result) {
		List<String> nameList = Lists.newArrayList();
		String[] names = NORMALIZE.matcher(normalize(rawValue)).replaceAll("").split(SPLIT_NAMES);

		// make sure to trim all single names
		for (int i = 0; i < names.length; i++) {
			names[i] = names[i].trim();
			if (!StringUtils.isBlank(names[i])) {
				nameList.add(names[i]);
			}
		}
		return nameList;
	}

}
