package net.canadensys.processor.datetime;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.canadensys.lang.RomanNumeral;
import net.canadensys.processor.AbstractDataProcessor;
import net.canadensys.processor.ProcessingResult;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.DateTimeException;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.Year;
import org.threeten.bp.YearMonth;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.threeten.bp.temporal.TemporalAccessor;

/**
 * Data processor to handle dates including partial dates.
 * Some DateTimeFormatter/pattern documentation :
 * - http://threeten.sourceforge.net/apidocs-2012-10-25/javax/time/format/DateTimeFormatter.html#parseBest(java.lang.CharSequence,%20java.lang.Class
 * ...)
 * - http://threeten.sourceforge.net/apidocs-2012-10-25/javax/time/format/DateTimeFormatters.html#pattern(java.lang.String)
 * Good to know :
 * - http://threeten.sourceforge.net/apidocs/javax/time/calendar/LocalDate.html
 *
 * @author canadensys
 *
 */
public class DateProcessor extends AbstractDataProcessor {

	final Logger logger = LoggerFactory.getLogger(DateProcessor.class);

	public static final int YEAR_IDX = 0;
	public static final int MONTH_IDX = 1;
	public static final int DAY_IDX = 2;

	// default field names
	private static final String DEFAULT_DATE_NAME = "date";
	private static final String DEFAULT_YEAR_NAME = "year";
	private static final String DEFAULT_MONTH_NAME = "month";
	private static final String DEFAULT_DAY_NAME = "day";

	protected String dateName, yearName, monthName, dayName;

	// Only USE_NULL make sense here
	private final ErrorHandlingModeEnum errorHandlingMode = ErrorHandlingModeEnum.USE_NULL;

	// All patterns are using dash(-) as separator, all other separators will be replaced
	// Gregorian little-endian, starting with day
	private static final DateTimeFormatter LE_D_MMM_YYYY_PATTERN = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("d-MMM-yyyy")
			.toFormatter(Locale.US);
	private static final DateTimeFormatter LE_D_MMMM_YYYY_PATTERN = new DateTimeFormatterBuilder().parseCaseInsensitive()
			.appendPattern("d-MMMM-yyyy").toFormatter(Locale.US);
	// Could bring conflicts with middle-endian like in 13-10-2012
	private static final DateTimeFormatter LE_D_M_YYYY_PATTERN = new DateTimeFormatterBuilder().appendPattern("d-M-yyyy").toFormatter(Locale.US);

	// Gregorian big-endian, starting with year
	// ISO 8601
	private static final DateTimeFormatter BE_ISO8601_BASIC_PATTERN = new DateTimeFormatterBuilder().appendPattern("yyyyMMdd").toFormatter(Locale.US);
	private static final DateTimeFormatter BE_ISO8601_PARTIAL_DATE_PATTERN = new DateTimeFormatterBuilder().appendPattern("yyyy[-M[-d]]")
			.toFormatter(Locale.US);
	private static final DateTimeFormatter BE_YYYY_MMM_D_PATTERN = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("yyyy-MMM-d")
			.toFormatter(Locale.US);
	private static final DateTimeFormatter BE_YYYY_MMMM_D_PATTERN = new DateTimeFormatterBuilder().parseCaseInsensitive()
			.appendPattern("yyyy-MMMM-d").toFormatter(Locale.US);

	// Middle-endian, starting with month
	// 11-9-2003, 11.9.2003, 11.09.03, or 11/09/03
	private static final DateTimeFormatter ME_MMM_D_YYYY_PATTERN = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("MMM-d-yyyy")
			.toFormatter(Locale.US);
	private static final DateTimeFormatter ME_MMMM_D_YYYY_PATTERN = new DateTimeFormatterBuilder().parseCaseInsensitive()
			.appendPattern("MMMM-d-yyyy").toFormatter(Locale.US);

	// Could bring conflicts with little-endian like in 13-10-2012
	private static final DateTimeFormatter ME_M_D_YYYY_PATTERN = new DateTimeFormatterBuilder().appendPattern("M-d-yyyy").toFormatter(Locale.US);
	// Not sure this one is safe to implement
	// private static final DateTimeFormatter ME_MM_DD_YY_PATTERN = DateTimeFormatters.pattern("M-d-yy", Locale.US);

	// Partial dates
	private static final DateTimeFormatter PARTIAL_MONTH_YEAR_PATTERN = new DateTimeFormatterBuilder().parseCaseInsensitive()
			.appendPattern("MMM-yyyy").toFormatter(Locale.US);
	private static final DateTimeFormatter PARTIAL_FULL_MONTH_YEAR_PATTERN = new DateTimeFormatterBuilder().parseCaseInsensitive()
			.appendPattern("MMMM-yyyy").toFormatter(Locale.US);

	private static final DateTimeFormatter PARTIAL_MONTH_PATTERN = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("MMM")
			.toFormatter(Locale.US);
	private static final DateTimeFormatter PARTIAL_FULL_MONTH_PATTERN = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("MMMM")
			.toFormatter(Locale.US);

	protected static final String STANDARDIZE_PUNCT_PATTERN = "[.|/ ,]+";
	protected static final Pattern ROMAN_NUMERAL_PATTERN = Pattern.compile("\\d[.\\-/ ]([XVI]+)[.\\-/ ]\\d", Pattern.CASE_INSENSITIVE);

	// keep a reference on the complete (non partial) date patterns list
	private static DateTimeFormatter[] COMPLETE_DATE_PATTERNS = new DateTimeFormatter[] { BE_ISO8601_BASIC_PATTERN, ME_MMM_D_YYYY_PATTERN,
		ME_MMMM_D_YYYY_PATTERN, BE_YYYY_MMM_D_PATTERN, BE_YYYY_MMMM_D_PATTERN, LE_D_MMM_YYYY_PATTERN, LE_D_MMMM_YYYY_PATTERN };
	// keep a reference on the non-numerical month (using a word to express the month) complete (non partial) date patterns list
	private static DateTimeFormatter[] NON_NUMERICAL_MONTH_COMPLETE_DATE_PATTERNS = new DateTimeFormatter[] { LE_D_MMM_YYYY_PATTERN,
		LE_D_MMMM_YYYY_PATTERN, ME_MMM_D_YYYY_PATTERN, ME_MMMM_D_YYYY_PATTERN, BE_YYYY_MMM_D_PATTERN, BE_YYYY_MMMM_D_PATTERN };

	protected List<Locale> supportedLocale;

	/**
	 * Default constructor, default field names will be used
	 */
	public DateProcessor() {
		this(DEFAULT_DATE_NAME, DEFAULT_YEAR_NAME, DEFAULT_MONTH_NAME, DEFAULT_DAY_NAME);
	}

	/**
	 * @param dateName
	 *            name of the field containing the date string
	 * @param yearName
	 *            name of the field where the year will be stored
	 * @param monthName
	 *            name of the field where the month will be stored
	 * @param dayName
	 *            name of the field where the day will be stored
	 */
	public DateProcessor(String dateName, String yearName, String monthName, String dayName) {
		this.dateName = dateName;
		this.yearName = yearName;
		this.monthName = monthName;
		this.dayName = dayName;
		// always a default Locale
		setLocale(Locale.ENGLISH);
		supportedLocale = new ArrayList<Locale>();
		supportedLocale.add(Locale.FRENCH);
		supportedLocale.add(new Locale("ES"));
	}

	/**
	 * Date and partial date Bean processing function.
	 *
	 * @param in
	 *            Java bean containing the date property as String
	 * @param out
	 *            Java bean containing the 3 properties that will keep the 3 parts of the date
	 * @param params
	 *            Will be ignored so use null, could eventually be used to include decades
	 */
	@Override
	public void processBean(Object in, Object out, Map<String, Object> params, ProcessingResult result) {
		try {
			Integer[] output = null;
			String textDate = (String) PropertyUtils.getSimpleProperty(in, dateName);

			output = process(textDate, result);

			PropertyUtils.setSimpleProperty(out, yearName, output[YEAR_IDX]);
			PropertyUtils.setSimpleProperty(out, monthName, output[MONTH_IDX]);
			PropertyUtils.setSimpleProperty(out, dayName, output[DAY_IDX]);
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

	/**
	 * Validates a date with partial date support. It means that if only one field can be parsed, this is
	 * considered valid.
	 */
	@Override
	public boolean validateBean(Object in, boolean isMandatory, Map<String, Object> params, ProcessingResult result) {
		Integer[] output = null;
		String textDate = null;

		try {
			textDate = (String) PropertyUtils.getSimpleProperty(in, dateName);
			output = process(textDate, result);
			// we support partial date so if we have only one part, it's valid
			if (output[0] != null || output[1] != null || output[2] != null) {
				return true;
			}
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

		// no valid date was found, check if this value was mandatory
		if (!isMandatory && StringUtils.isBlank(textDate)) {
			return true;
		}
		return false;
	}

	/**
	 * Date processing function
	 *
	 * @param dateText
	 *            a test representing the date or partial-date
	 * @param result
	 *            optional processing result
	 * @return initialized array(size==3) that will contain the parsed data(year,month,day) or null.
	 */
	public Integer[] process(String dateText, ProcessingResult result) {
		Integer[] output = new Integer[3];
		if (StringUtils.isBlank(dateText)) {
			return output;
		}

		dateText = standardizeDatePunctuation(dateText);

		try {
			// try ISO 8601 (with partial date like 2008 or 2008-12)
			setPartialDate(output, BE_ISO8601_PARTIAL_DATE_PATTERN.parseBest(dateText, LocalDate.FROM, YearMonth.FROM, Year.FROM));
			return output;
		}
		catch (DateTimeException cpe) {
		}

		// Try to find a complete date
		LocalDate localDate = tryParseCompleteDate(COMPLETE_DATE_PATTERNS, dateText);
		if (localDate != null) {
			setPartialDate(output, localDate);
			return output;
		}

		// PARTIAL DATE
		try {
			// try format like Jun 1895
			setPartialDate(output, PARTIAL_MONTH_YEAR_PATTERN.parse(dateText, YearMonth.FROM));
			return output;
		}
		catch (DateTimeException cpe) {
		}

		try {
			// try format like Jun
			setPartialDate(output, PARTIAL_MONTH_PATTERN.parse(dateText, Month.FROM));
			return output;
		}
		catch (DateTimeException cpe) {
		}

		// Warning - Fuzzy dates handling
		LocalDate le_d_m_yyyy_date = null;
		LocalDate me_m_d_yyyy_date = null;
		try {
			le_d_m_yyyy_date = LE_D_M_YYYY_PATTERN.parse(dateText, LocalDate.FROM);
		}
		catch (DateTimeException cpe) {
		}

		try {
			me_m_d_yyyy_date = ME_M_D_YYYY_PATTERN.parse(dateText, LocalDate.FROM);
		}
		catch (DateTimeException cpe) {
		}

		// make sure the date can't be parsed into the 2 different patterns
		// but allow if it gives the same date (e.g. 8-8-2010)
		if (le_d_m_yyyy_date != null && me_m_d_yyyy_date != null && !le_d_m_yyyy_date.equals(me_m_d_yyyy_date)) {
			if (result != null) {
				result.addError(MessageFormat.format(resourceBundle.getString("date.error.vagueDate"), dateText));
			}
			return output;
		}
		if (le_d_m_yyyy_date != null) {
			setPartialDate(output, le_d_m_yyyy_date);
			return output;
		}
		if (me_m_d_yyyy_date != null) {
			setPartialDate(output, me_m_d_yyyy_date);
			return output;
		}

		// try Roman Numerals
		if (processRomanNumeralDate(dateText, output, result)) {
			return output;
		}

		// try with different Locale
		localDate = tryParseWithSupportedLocale(NON_NUMERICAL_MONTH_COMPLETE_DATE_PATTERNS, dateText);
		if (localDate != null) {
			setPartialDate(output, localDate);
			return output;
		}

		if (result != null) {
			result.addError(MessageFormat.format(resourceBundle.getString("date.error.unprocessable"), dateText));
		}
		return output;
	}

	/**
	 * Fill the partialDate array according to the content of the Calendrical object.
	 *
	 * @param partialDate
	 *            initialized array of size 3
	 * @param cal
	 */
	protected void setPartialDate(Integer[] partialDate, TemporalAccessor cal) {
		if (cal instanceof LocalDate) {
			LocalDate lc = (LocalDate) cal;
			partialDate[DAY_IDX] = lc.getDayOfMonth();
			partialDate[MONTH_IDX] = lc.getMonth().getValue();
			partialDate[YEAR_IDX] = lc.getYear();
		}
		else if (cal instanceof YearMonth) {
			YearMonth ym = (YearMonth) cal;
			partialDate[MONTH_IDX] = ym.getMonth().getValue();
			partialDate[YEAR_IDX] = ym.getYear();
		}
		else if (cal instanceof Year) {
			partialDate[YEAR_IDX] = ((Year) cal).getValue();
		}
		else if (cal instanceof Month) {
			partialDate[MONTH_IDX] = ((Month) cal).getValue();
		}
		else {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * This function will replace all dots (.), space ( ), slashes(/) and coma(,) characters by a dash(-).
	 *
	 * @param date
	 * @return
	 */
	public String standardizeDatePunctuation(String date) {
		return date.replaceAll(STANDARDIZE_PUNCT_PATTERN, "-");
	}

	/**
	 * This function will parse a date with a Roman numeral as the month.
	 * e.g. 8/xi/2003, 8.xi.2003, 8-xi.2003, or 8.XI.2003
	 *
	 * @param dateText
	 * @param output
	 * @param result
	 * @return could be parsed as date with a Roman numeral as the month.
	 */
	public boolean processRomanNumeralDate(String dateText, Integer[] output, ProcessingResult result) {
		Matcher romanNumeralMatcher = ROMAN_NUMERAL_PATTERN.matcher(dateText);
		if (romanNumeralMatcher.find()) {
			String romNum = romanNumeralMatcher.group(1);
			try {
				int intValue = new RomanNumeral(romNum).toInt();
				String newTextDate = dateText.replace(romNum, Integer.toString(intValue));
				// Standardize the date punctuation based on the new date text
				newTextDate = standardizeDatePunctuation(newTextDate);
				// We only support Roman numeral for the month
				try {
					LocalDate le_d_m_yyyy_date = LE_D_M_YYYY_PATTERN.parse(newTextDate, LocalDate.FROM);
					setPartialDate(output, le_d_m_yyyy_date);
					return true;
				}
				catch (DateTimeException e) {
				}
				try {
					LocalDate le_d_m_yyyy_date = BE_ISO8601_PARTIAL_DATE_PATTERN.parse(newTextDate, LocalDate.FROM);
					setPartialDate(output, le_d_m_yyyy_date);
					return true;
				}
				catch (DateTimeException e) {
				}
			}
			catch (NumberFormatException ex) {
				if (result != null) {
					result.addError(MessageFormat.format(resourceBundle.getString("date.error.romanNumeralUnprocessable"), dateText));
				}
			}
		}
		return false;
	}

	@Override
	public ErrorHandlingModeEnum getErrorHandlingMode() {
		return errorHandlingMode;
	}

	/**
	 * Try to parse a complete date from dateText.
	 * Partial dates are not supported since LocalDate.rule() is used.
	 *
	 * @param dateTimeFormatterList
	 * @param dateText
	 * @return the LocalDate or null
	 */
	private LocalDate tryParseCompleteDate(DateTimeFormatter[] dateTimeFormatterList, String dateText) {
		LocalDate localDate = null;
		for (DateTimeFormatter currDateTimeFormatter : dateTimeFormatterList) {
			try {
				localDate = currDateTimeFormatter.parse(dateText, LocalDate.FROM);
				return localDate;
			}
			catch (DateTimeException e) {
			}
		}
		return null;
	}

	/**
	 * Try to parse a dateText with all supported Locale.
	 * Partial dates are not supported since LocalDate.rule() is used.
	 *
	 * @param dateTimeFormatterList
	 * @param dateText
	 * @return the LocalDate or null
	 */
	private LocalDate tryParseWithSupportedLocale(DateTimeFormatter[] dateTimeFormatterList, String dateText) {
		LocalDate localDate = null;
		for (DateTimeFormatter currDateTimeFormatter : dateTimeFormatterList) {
			for (Locale currLocale : supportedLocale) {
				try {
					localDate = currDateTimeFormatter.withLocale(currLocale).parse(dateText, LocalDate.FROM);
					return localDate;
				}
				catch (DateTimeException e) {
				}
			}
		}
		return null;
	}
}
