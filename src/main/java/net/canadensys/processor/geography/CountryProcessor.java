package net.canadensys.processor.geography;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import net.canadensys.processor.DataProcessor;
import net.canadensys.processor.ProcessingResult;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.gbif.api.model.vocabulary.Country;
import org.gbif.common.parsers.ParseResult;
import org.gbif.common.parsers.ParseResult.CONFIDENCE;
import org.gbif.common.parsers.countryname.CountryNameParser;

/**
 * Country processor to handle country data.
 * Parsing a country string into a controlled vocabulary (org.gbif.api.model.vocabulary.Country).
 * You should reuse the same instance to save resources.
 * @author canadensys
 *
 */
public class CountryProcessor implements DataProcessor {
	
	protected static CountryNameParser COUNTRY_NAME_PARSER = CountryNameParser.getInstance();
	protected static final String DEFAULT_COUNTRY_NAME = "country";
	
	protected String countryName = null;
	protected ErrorHandlingModeEnum errorHandlingMode;
	
	/**
	 * Default constructor, default field names will be used.
	 */
	public CountryProcessor(){
		this(DEFAULT_COUNTRY_NAME,ErrorHandlingModeEnum.USE_ORIGINAL);
	}
	
	public CountryProcessor(String countryName, ErrorHandlingModeEnum errorHandlingMode){
		this.errorHandlingMode = errorHandlingMode;
		this.countryName = countryName;
	}
	
	@Override
	public ErrorHandlingModeEnum getErrorHandlingMode() {
		return errorHandlingMode;
	}
	
	/**
	 * Country Bean processing function.
	 * @param in Java bean containing the country as String
	 * @param out Java bean containing the country (from controlled vocabulary) as String
	 * @param params Will be ignored so use null
	 * @param result Optional ProcessingResult
	 */
	@Override
	public void processBean(Object in, Object out, Map<String, Object> params, ProcessingResult result) {
		try {
			MutableObject<Country> output = new MutableObject<Country>();
			String textCountry = (String)PropertyUtils.getSimpleProperty(in, countryName);
			
			process(textCountry,output,result);
			
			String country=null;
			if(output.getValue() != null){
				country =  output.getValue().getTitle();
			}
			else{
				//should it be done in process(...) function?
				switch (errorHandlingMode) {
					case USE_ORIGINAL:country=textCountry;
						break;
					case USE_NULL:country=null;
						break;
					case USE_EMPTY:country="";
						break;
					default:
						break;
				}
			}
			PropertyUtils.setSimpleProperty(out, countryName, country);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Country processing function
	 * @param countryStr country string to be processed
	 * @param country MutableObject to allow to set country
	 * @param result optional ProcessingResult
	 */
	public void process(String countryStr, MutableObject<Country> country, ProcessingResult result){
		if(StringUtils.isBlank(countryStr)){
			return;
		}
		
		ParseResult<String> parsingResult = COUNTRY_NAME_PARSER.parse(countryStr);
		if(parsingResult.isSuccessful() && parsingResult.getConfidence().equals(CONFIDENCE.DEFINITE)){
			country.setValue(Country.fromIsoCode(parsingResult.getPayload()));
		}
		else{
			if(result != null){
				result.addError("Couldn't find a matching country for [" + countryStr +"]");
			}
		}
	}
}
