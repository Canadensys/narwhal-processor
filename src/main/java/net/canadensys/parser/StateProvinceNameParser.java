package net.canadensys.parser;

import java.io.InputStream;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;
import org.gbif.api.model.vocabulary.Country;
import org.gbif.common.parsers.FileBasedDictionaryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

import net.canadensys.processor.geography.StateProvinceProcessor;
import net.canadensys.vocabulary.stateprovince.StateProvinceEnum;

/**
 * FileBasedDictionaryParser implementation for state/province name parsing.
 * Based on org.gbif.common.parsers.countryname.CountryNameParser
 * 
 * To allow the parsing into a controlled vocabulary those 2 conditions must be met :
 * 1) A valid implementation of StateProvinceEnum for this country must exists in the canadensys-core library
 * 2) A dictionary file (/dictionaries/geography/CountryIso2LetterCode_StateProvinceName.txt) must exist and be valid.
 * 
 * @author canadensys
 *
 * @param <T> any enum that implements StateProvinceEnum
 */
public class StateProvinceNameParser<T extends Enum<T> & StateProvinceEnum>  extends FileBasedDictionaryParser{
	
	final Logger logger = LoggerFactory.getLogger(StateProvinceNameParser.class);
	
	private static final CharMatcher LETTER_MATCHER = CharMatcher.JAVA_LETTER.or(CharMatcher.WHITESPACE).precomputed();
	private static final CharMatcher WHITESPACE_MATCHER = CharMatcher.WHITESPACE.precomputed();
	
	public StateProvinceNameParser(Country targetCountry, Class<T> stateProvinceClass){
		super(false, new InputStream[] { 
				StateProvinceProcessor.class.getResourceAsStream("/dictionaries/geography/"+targetCountry.getIso2LetterCode()+ "_StateProvinceName.txt")
		});
		
		StateProvinceEnum[] statesProvinces = stateProvinceClass.getEnumConstants();
		if(statesProvinces == null || statesProvinces.length <= 0){
			String errorText = "No well-formed StateProvinceEnum found for country " + targetCountry.getTitle() + " and Class " + stateProvinceClass;
			logger.error(errorText);
			throw new NoSuchElementException(errorText);
		}
		
		for (StateProvinceEnum cp : statesProvinces){
			add(cp.getName(),cp.getCode());
			add(cp.getCode(), cp.getCode());
		}
	}
	
	@Override
	protected String normalize(String value) {
		if (value != null){
			String stateProvince = LETTER_MATCHER.retainFrom(value);
			stateProvince = WHITESPACE_MATCHER.trimAndCollapseFrom(stateProvince, ' ');
			stateProvince = StringUtils.stripAccents(stateProvince);
			stateProvince = Strings.emptyToNull(stateProvince);
			return super.normalize(stateProvince);
		}
		return null;
    }
}
