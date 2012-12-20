package net.canadensys.processor.geography;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.canadensys.processor.DataProcessor;
import net.canadensys.processor.ProcessingResult;
import net.canadensys.vocabulary.Continent;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableObject;

/**
 * CountryContinent processor to find the continent of a country.
 * Simple matching between the ISO 3166-1 alpha-2 country code and the continent code.
 * You should reuse the same instance to save resources.
 * @author canadensys
 *
 */
public class CountryContinentProcessor implements DataProcessor{
	
	protected static final String SEPARATOR = "\t";
	protected static final String DEFAULT_CONTINENT_COUNTRY_FILE = "/dictionaries/geography/country_continent.txt";
	protected static final String DEFAULT_COUNTRY_ISO_LETTER_CODE_NAME = "countryISOLetterCode";
	protected static final String DEFAULT_CONTINENT_NAME = "continent"; 
	
	protected Map<String,String> countryContinentMap = null;
	protected ErrorHandlingModeEnum errorHandlingMode = null;
	
	protected String countryISOLetterCodeName = null;
	protected String continentName = null;
	
	/**
	 * Default constructor, default field names will be used
	 */
	public CountryContinentProcessor(){
		this(DEFAULT_COUNTRY_ISO_LETTER_CODE_NAME, DEFAULT_CONTINENT_NAME);
	}
	
	/**
	 * 
	 * @param countryISOLetterCodeName name of the field containing the ISO 3166-1 alpha-2 country code
	 * @param continentName name of the field where the continent standard name will be stored
	 */
	public CountryContinentProcessor(String countryISOLetterCodeName, String continentName){
		this(countryISOLetterCodeName,continentName,CountryContinentProcessor.class.getResourceAsStream(DEFAULT_CONTINENT_COUNTRY_FILE),ErrorHandlingModeEnum.USE_NULL);
	}
	
	/**
	 * 
	 * @param countryISOLetterCodeName name of the field containing the ISO 3166-1 alpha-2 country code
	 * @param continentName name of the field where the continent standard name will be stored
	 * @param input InputStream of the Country->Continent dictionary
	 * @param errorHandlingMode USE_NULL or USE_EMPTY
	 */
	public CountryContinentProcessor(String countryISOLetterCodeName, String continentName, InputStream input, ErrorHandlingModeEnum errorHandlingMode){
		this.countryISOLetterCodeName = countryISOLetterCodeName;
		this.continentName = continentName;
		setErrorHandlingMode(errorHandlingMode);
		
		countryContinentMap = new HashMap<String, String>();
		List<String> lines = null;
		try {
			lines = IOUtils.readLines(input);
			String[] lineArr;
			for(String currLine : lines){
				lineArr = currLine.split(SEPARATOR);
				countryContinentMap.put(lineArr[0],lineArr[1]);
			}
		} catch (IOException ioEx) {
			//do not keep incomplete data
			countryContinentMap.clear();
		}
		//make it read-only
		countryContinentMap = Collections.unmodifiableMap(countryContinentMap);
	}
	
	/**
	 * Numeric pair Bean processing function.
	 * @param in Java bean containing the ISO country letter code as String
	 * @param out Java bean containing the continent name as String
	 * @param params Will be ignored so use null
	 * @param result Optional ProcessingResult
	 */
	@Override
	public void processBean(Object in, Object out, Map<String, Object> params, ProcessingResult result) {
		try {
			MutableObject<Continent> output = new MutableObject<Continent>();
			String textDate = (String)PropertyUtils.getSimpleProperty(in, countryISOLetterCodeName);
			
			process(textDate,output,result);
			
			String continent;
			if(output.getValue() != null){
				continent =  output.getValue().getTitle();
			}
			else{
				//should it be done in process(...) function?
				continent = (errorHandlingMode == ErrorHandlingModeEnum.USE_NULL ? null : "");
			}
			PropertyUtils.setSimpleProperty(out, continentName, continent);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * Country->Continent processing function
	 * @param countryISOLetterCode (ISO 3166-1 alpha-2)
	 * @param continent MutableObject to allow to set content
	 * @param result optional ProcessingResult
	 */
	public void process(String countryISOLetterCode, MutableObject<Continent> continent, ProcessingResult result){
		if(StringUtils.isBlank(countryISOLetterCode)){
			return;
		}
		String continentCode = countryContinentMap.get(countryISOLetterCode);
		if(continentCode != null){
			continent.setValue(Continent.fromCode(continentCode));
		}
		else{
			if(result != null){
				result.addError("Couldn't find a matching continent for country ISO Letter Code [" + countryISOLetterCode +"]");
			}
		}
	}
	
	/**
	 * This setter should only be called by the constructor
	 */
	protected void setErrorHandlingMode(ErrorHandlingModeEnum errorHandlingMode){
		//only USE_NULL and USE_EMPTY are valid options
		if(errorHandlingMode == ErrorHandlingModeEnum.USE_NULL || errorHandlingMode == ErrorHandlingModeEnum.USE_EMPTY){
			this.errorHandlingMode = errorHandlingMode;
		}
		else{
			throw new IllegalArgumentException("Invalid ErrorHandlingModeEnum argument");
		}
	}
	
	@Override
	public ErrorHandlingModeEnum getErrorHandlingMode() {
		return errorHandlingMode;
	}
}
