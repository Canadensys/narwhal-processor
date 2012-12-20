package net.canadensys.processor.datetime;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Map;

import javax.time.LocalDate;
import javax.time.MonthOfYear;
import javax.time.calendrical.Calendrical;
import javax.time.extended.Year;
import javax.time.extended.YearMonth;
import javax.time.format.CalendricalParseException;
import javax.time.format.DateTimeFormatter;
import javax.time.format.DateTimeFormatters;

import net.canadensys.processor.DataProcessor;
import net.canadensys.processor.ProcessingResult;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Data processor to handle dates including partial dates.
 * Some DateTimeFormatter/pattern documentation : 
 * - http://threeten.sourceforge.net/apidocs-2012-10-25/javax/time/format/DateTimeFormatter.html#parseBest(java.lang.CharSequence,%20java.lang.Class...)
 * - http://threeten.sourceforge.net/apidocs-2012-10-25/javax/time/format/DateTimeFormatters.html#pattern(java.lang.String)
 * TODO : Test non-US locale.
 * TODO : Build formatter with DateTimeFormatterBuilder.parseCaseInsensitive
 * @author canadensys
 *
 */
public class DateProcessor implements DataProcessor{
		
	public static final int YEAR_IDX = 0;
	public static final int MONTH_IDX = 1;
	public static final int DAY_IDX = 2;
	
	//default field names
	private static final String DEFAULT_DATE_NAME = "date";
	private static final String DEFAULT_YEAR_NAME = "year";
	private static final String DEFAULT_MONTH_NAME = "month";
	private static final String DEFAULT_DAY_NAME = "day";
	
	private String dateName, yearName, monthName, dayName;
	
	//Only USE_NULL make sense here
	private ErrorHandlingModeEnum errorHandlingMode = ErrorHandlingModeEnum.USE_NULL;
	
	//Gregorian little-endian, starting with day 
	private static final DateTimeFormatter LE_D_MMM_YYYY_PATTERN = DateTimeFormatters.pattern("d MMM yyyy", Locale.US);
	//Could bring conflicts with middle-endian like in 13-10-2012
	private static final DateTimeFormatter LE_D_M_YYYY_PATTERN = DateTimeFormatters.pattern("d-M-yyyy", Locale.US);
	
	//Gregorian big-endian, starting with year
	//ISO 8601
	private static final DateTimeFormatter BE_ISO8601_BASIC_PATTERN = DateTimeFormatters.pattern("yyyyMMdd", Locale.US);
	private static final DateTimeFormatter BE_ISO8601_PARTIAL_DATE_PATTERN = DateTimeFormatters.pattern("yyyy[-M[-d]]", Locale.US);
	private static final DateTimeFormatter BE_YYYY_MMM_D_PATTERN = DateTimeFormatters.pattern("yyyy MMM d", Locale.US);
	
	//Middle-endian, starting with month
	//11-9-2003, 11.9.2003, 11.09.03, or 11/09/03
	private static final DateTimeFormatter ME_MMM_D_YYYY_PATTERN = DateTimeFormatters.pattern("MMM d yyyy", Locale.US);
	//Could bring conflicts with little-endian like in 13-10-2012
	private static final DateTimeFormatter ME_M_D_YYYY_PATTERN = DateTimeFormatters.pattern("M-d-yyyy", Locale.US);
	//Not sure this one is safe to implement
	//private static final DateTimeFormatter ME_MM_DD_YY_PATTERN = DateTimeFormatters.pattern("M-d-yy", Locale.US);
	
	//Partial date
	private static final DateTimeFormatter PARTIAL_MONTH_YEAR_PATTERN = DateTimeFormatters.pattern("MMM yyyy", Locale.US);
	private static final DateTimeFormatter PARTIAL_MONTH_PATTERN = DateTimeFormatters.pattern("MMM", Locale.US);
	
	protected static final String STANDARDIZE_PUNCT_PATTERN = "(?<=\\d+)[.|/](?=\\d+)";
	
	/**
	 * Default constructor, default field names will be used
	 */
	public DateProcessor(){
		this(DEFAULT_DATE_NAME,DEFAULT_YEAR_NAME,DEFAULT_MONTH_NAME,DEFAULT_DAY_NAME);
	}
	
	/**
	 * @param dateName name of the field containing the date string
	 * @param yearName name of the field where the year will be stored
	 * @param monthName name of the field where the month will be stored
	 * @param dayName name of the field where the day will be stored
	 */
	public DateProcessor(String dateName, String yearName, String monthName, String dayName){
		this.dateName = dateName;
		this.yearName = yearName;
		this.monthName = monthName;
		this.dayName = dayName;
	}
	
	/**
	 * Date and partial date Bean processing function.
	 * @param in Java bean containing the date property as String
	 * @param out Java bean containing the 3 properties that will keep the 3 parts of the date
	 * @param params Will be ignored so use null, could eventually be used to include decades
	 */
	@Override
	public void processBean(Object in, Object out, Map<String, Object> params, ProcessingResult result) {
		try {
			Integer[] output = new Integer[3];
			String textDate = (String)PropertyUtils.getSimpleProperty(in, dateName);
			
			process(textDate,output,result);
						
			PropertyUtils.setSimpleProperty(out, yearName, output[YEAR_IDX]);
			PropertyUtils.setSimpleProperty(out, monthName, output[MONTH_IDX]);
			PropertyUtils.setSimpleProperty(out, dayName, output[DAY_IDX]);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Date processing function
	 * @param dateText a test representing the date or partial-date
	 * @param output initialized array(size==3) that will contain the parsed data(year,month,day) or null.
	 * @param result optional processing result
	 */
	public void process(String dateText, Integer[] output, ProcessingResult result){
		if(StringUtils.isBlank(dateText)){
			return;
		}
		
		dateText = standardizeDatePunctuation(dateText);
		
		try{
			//try ISO 8601 (with partial date like 2008 or 2008-12)
			setPartialDate(output,BE_ISO8601_PARTIAL_DATE_PATTERN.parseBest(dateText, LocalDate.rule(),YearMonth.rule(),Year.rule()));
			return;
		}
		catch(CalendricalParseException cpe){}
		
		try{
			//try ISO 8601 (with partial date like 20081227)
			setPartialDate(output,BE_ISO8601_BASIC_PATTERN.parse(dateText, LocalDate.rule()));
			return;
		}
		catch(CalendricalParseException cpe){}
		
		try{
			//try format like Jul 6 1987
			setPartialDate(output,ME_MMM_D_YYYY_PATTERN.parse(dateText, LocalDate.rule()));
			return;
		}
		catch(CalendricalParseException cpe){}
		
		try{
			//try format like 1987 Jul 6
			setPartialDate(output,BE_YYYY_MMM_D_PATTERN.parse(dateText, LocalDate.rule()));
			return;
		}
		catch(CalendricalParseException cpe){}
		
		try{
			//try format like 6 Jul 1986
			setPartialDate(output,LE_D_MMM_YYYY_PATTERN.parse(dateText, LocalDate.rule()));
			return;
		}
		catch(CalendricalParseException cpe){}
		
		//PARTIAL DATE
		try{
			//try format like Jun 1895
			setPartialDate(output,PARTIAL_MONTH_YEAR_PATTERN.parse(dateText, YearMonth.rule()));
			return;
		}
		catch(CalendricalParseException cpe){}
		
		try{
			//try format like Jun
			setPartialDate(output,PARTIAL_MONTH_PATTERN.parse(dateText, MonthOfYear.rule()));
			return;
		}
		catch(CalendricalParseException cpe){}
		
		//Warning - Fuzzy dates handling
		LocalDate le_d_m_yyyy_date = null;
		LocalDate me_m_d_yyyy_date = null;
		try{
			le_d_m_yyyy_date = LE_D_M_YYYY_PATTERN.parse(dateText, LocalDate.rule());
		}
		catch(CalendricalParseException cpe){}
		
		try{
			me_m_d_yyyy_date = ME_M_D_YYYY_PATTERN.parse(dateText, LocalDate.rule());
		}
		catch(CalendricalParseException cpe){}
		
		//make sure the date can't be parsed into the 2 different patterns
		if(le_d_m_yyyy_date != null && me_m_d_yyyy_date != null){
			if(result != null){
				result.addError("The date ["+dateText+"] could not be precisely determined.");
			}
			return;
		}
		if(le_d_m_yyyy_date != null){
			setPartialDate(output,le_d_m_yyyy_date);
			return;
		}
		if(me_m_d_yyyy_date != null){
			setPartialDate(output,me_m_d_yyyy_date);
			return;
		}
		
		if(result != null){
			result.addError("The date ["+dateText+"] could not be processed.");
		}
	}
	

	/**
	 * Fill the partialDate array according to the content of the Calendrical object.
	 * @param partialDate initialized array of size 3
	 * @param cal
	 */
	protected void setPartialDate(Integer[] partialDate, Calendrical cal){
		if(cal instanceof LocalDate){
			LocalDate lc = (LocalDate)cal;
			partialDate[DAY_IDX] = lc.getDayOfMonth();
			partialDate[MONTH_IDX] = lc.getMonthOfYear().getValue();
			partialDate[YEAR_IDX] = lc.getYear();			
		}
		else if(cal instanceof YearMonth){
			YearMonth ym = (YearMonth)cal; 
			partialDate[MONTH_IDX] = ym.getMonthOfYear().getValue();
			partialDate[YEAR_IDX] = ym.getYear();
		}
		else if(cal instanceof Year){
			partialDate[YEAR_IDX] = ((Year)cal).getValue();
		}
		else if(cal instanceof MonthOfYear){
			partialDate[MONTH_IDX] = ((MonthOfYear)cal).getValue();
		}
		else{
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * This function will replace all dots (.) and slashes(/) characters by a dash(-).
	 * This will be applied only if a number is found ahead of the punctuation.
	 * 2007.07.31 => 2007-07-31, 3/27/2002 => 3-27-2002, 7 Nov 2012 => 7 Nov 2012 (not changed)
	 * @param date
	 * @return
	 */
	public String standardizeDatePunctuation(String date){
		return date.replaceAll(STANDARDIZE_PUNCT_PATTERN, "-");
	}
	
	@Override
	public ErrorHandlingModeEnum getErrorHandlingMode() {
		return errorHandlingMode;
	}
}
