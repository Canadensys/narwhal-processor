package net.canadensys.processor.datetime;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.canadensys.processor.AbstractDataProcessor;
import net.canadensys.processor.ProcessingResult;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data processor to handle date interval.
 * This is a basic implementation.
 * Dates are extracted by finding symmetry in the date interval punctuation.
 * Extracted dates are not necessarily valid and should be processed with the appropriate DataProcessor.
 *  
 * @author canadensys
 *
 */
public class DateIntervalProcessor extends AbstractDataProcessor {

	final Logger logger = LoggerFactory.getLogger(DateProcessor.class);
	
	protected static Pattern DATE_INTERVAL_SEPARATOR_PATTERN = Pattern.compile("[.\\-/]",Pattern.CASE_INSENSITIVE);
	
	public static final int START_DATE_IDX = 0;
	public static final int END_DATE_IDX = 1;
	
	//default field names
	private static final String DEFAULT_DATE_INTERVAL_NAME = "dateInterval";
	private static final String DEFAULT_DATE_START_NAME = "dateStart";
	private static final String DEFAULT_DATE_END_NAME = "dateEnd";
	
	protected String dateIntervalName, dateStartName, dateEndName;
	
	/**
	 * Default constructor, default field names will be used
	 */
	public DateIntervalProcessor(){
		this(DEFAULT_DATE_INTERVAL_NAME,DEFAULT_DATE_START_NAME,DEFAULT_DATE_END_NAME);
	}
	
	
	/**
	 * @param dateIntervalName name of the field containing the date interval string
	 * @param dateStartName name of the field where the start date will be stored
	 * @param dateEndName name of the field where the end date will be stored
	 */
	public DateIntervalProcessor(String dateIntervalName, String dateStartName, String dateEndName){
		this.dateIntervalName = dateIntervalName;
		this.dateStartName = dateStartName;
		this.dateEndName = dateEndName;
	}
	
	@Override
	public ErrorHandlingModeEnum getErrorHandlingMode() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void processBean(Object in, Object out, Map<String, Object> params, ProcessingResult result) {
		try {
			String[] output = null;
			String textDateInterval = (String)PropertyUtils.getSimpleProperty(in, dateIntervalName);
			
			output = process(textDateInterval,result);
						
			PropertyUtils.setSimpleProperty(out, dateStartName, output[START_DATE_IDX]);
			PropertyUtils.setSimpleProperty(out, dateEndName, output[END_DATE_IDX]);
		} catch (IllegalAccessException e) {
			logger.error("Bean access error", e);
		} catch (InvocationTargetException e) {
			logger.error("Bean access error", e);
		} catch (NoSuchMethodException e) {
			logger.error("Bean access error", e);
		}
	}
	
	/**
	 * Validates that a date interval could be parsed as 2 dates according to the current rules.
	 * This will only tell you that a date interval can be processed without errors.
	 * Parsed dates should not be considered valid and should be processed with an appropriate 
	 * DataProcessor.
	 */
	@Override
	public boolean validateBean(Object in, boolean isMandatory, Map<String, Object> params, ProcessingResult result) {
		String[] output = null;
		String textDateInterval = null;

		try {
			textDateInterval = (String)PropertyUtils.getSimpleProperty(in, dateIntervalName);
			output = process(textDateInterval,result);
			//we need the 2 dates to be considered valid
			if(output[0] != null && output[1]!= null){
				return true;
			}
		} catch (IllegalAccessException e) {
			logger.error("Bean access error", e);
			return false;
		} catch (InvocationTargetException e) {
			logger.error("Bean access error", e);
			return false;
		} catch (NoSuchMethodException e) {
			logger.error("Bean access error", e);
			return false;
		}
		
		//no valid date was found, check if this value was mandatory
		if(!isMandatory && StringUtils.isBlank(textDateInterval)){
			return true;
		}
		return false;
	}
	
	/**
	 * Date interval processing function
	 * @param dateIntervalText text representing the date interval
	 * @param result optional processing result
	 * @return initialized array(size==2) that will contain the parsed data(start date, end date) or null.
	 */
	public String[] process(String dateIntervalText, ProcessingResult result){
		String[] output = new String[2];
		if(StringUtils.isBlank(dateIntervalText)){
			return output;
		}
		dateIntervalText = dateIntervalText.trim();
		Matcher m = DATE_INTERVAL_SEPARATOR_PATTERN.matcher(dateIntervalText);
		StringBuilder separatorStr = new StringBuilder();
		List<Integer> idx = new ArrayList<Integer>();
		while (m.find()) {
			separatorStr.append(m.group());
			idx.add(m.start());
		}
		
		//separatorStr has even length ? 
		if((separatorStr.length() & 1) != 0 ) {
			int separatorIdx = idx.get(separatorStr.length()/2);
			//make sure punctuation is symmetric
			if(separatorStr.substring(0, separatorStr.length()/2).equals(separatorStr.substring(separatorStr.length()/2+1))){
				output[START_DATE_IDX] = dateIntervalText.substring(0,separatorIdx).trim();
				output[END_DATE_IDX] = dateIntervalText.substring(separatorIdx+1,dateIntervalText.length()).trim();
			}
			else{
				if(result != null){
					result.addError(
						MessageFormat.format(resourceBundle.getString("dateInterval.error.nonSymmetric"),dateIntervalText));
				}
			}
		} else {//odd...
			if(result != null){
				result.addError(
					MessageFormat.format(resourceBundle.getString("date.error.nonSymmetric"),dateIntervalText));
			}	
		}
		return output;
	}
}
