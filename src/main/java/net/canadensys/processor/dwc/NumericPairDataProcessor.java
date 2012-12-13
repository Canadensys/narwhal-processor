package net.canadensys.processor.dwc;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.regex.Pattern;

import net.canadensys.processor.DataProcessor;
import net.canadensys.utils.NumberUtils;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Data processor to handle a pair of numeric fields that are inter-connected.
 * Will attempt to normalize value contained in 2 Strings fields to Number fields.
 * The string value can contain a suffix (units, comments, ...).
 * NO conversion will be attempted.
 * @author canadensys
 *
 */
public class NumericPairDataProcessor implements DataProcessor{
	
	protected static final Pattern KEEP_NUMERIC_PATTERN = Pattern.compile("[^\\d\\.-]");
	
	private String value1Name = "min";
	private String value2Name = "max";
	//Only USE_NULL make sense here
	private ErrorHandlingModeEnum errorHandlingMode = ErrorHandlingModeEnum.USE_NULL;
	
	public NumericPairDataProcessor(String value1Name, String value2Name){
		this.value1Name = value1Name;
		this.value2Name = value2Name;
	}
	
	/**
	 * Numeric pair Bean processing function.
	 * @param in Java bean containing the 2 properties as String
	 * @param max Java bean containing  the 2 properties as class extending Number (primitive type NOT supported)
	 * @param params Will be ignored so use null
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void processBean(Object in, Object out, Map<String, Object> params) {
		
		try {
			Number[] output = new Number[2];
			String val1 = (String)PropertyUtils.getSimpleProperty(in, value1Name);
			String val2 = (String)PropertyUtils.getSimpleProperty(in, value2Name);
			
			process(val1,val2,output,PropertyUtils.getPropertyType(out, value1Name));
						
			PropertyUtils.setSimpleProperty(out, value1Name, output[0]);
			PropertyUtils.setSimpleProperty(out, value2Name, output[1]);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Numeric pair processing function
	 * @param value1 
	 * @param value2
	 * @param minMaxOutput initialized array(size==2) that will contained the parsed data or null if not parsable
	 * @param clazz Class wanted for element of the minMaxOutput
	 */
	public void process(String value1, String value2, Number[] output, Class<? extends Number> clazz){
		if(!StringUtils.isBlank(value1)){
			value1 = KEEP_NUMERIC_PATTERN.matcher(value1).replaceAll("");
		}
		if(!StringUtils.isBlank(value2)){
			value2 = KEEP_NUMERIC_PATTERN.matcher(value2).replaceAll("");
		}		
		output[0] = NumberUtils.parseNumber(value1, clazz);
		output[1] = NumberUtils.parseNumber(value2, clazz);
	}

	@Override
	public ErrorHandlingModeEnum getErrorHandlingMode() {
		return errorHandlingMode;
	}
}
