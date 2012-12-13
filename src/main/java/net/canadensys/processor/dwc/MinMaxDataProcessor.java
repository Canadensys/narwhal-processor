package net.canadensys.processor.dwc;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.regex.Pattern;

import net.canadensys.processor.DataProcessor;
import net.canadensys.utils.NumberUtils;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Data processor to handle minimum and maximum fields.
 * Used to normalize Strings containing minimum and maximum value to Number.
 * Units (meter,feet) will be dropped and NO conversion will be attempted.
 * @author canadensys
 *
 */
public class MinMaxDataProcessor implements DataProcessor{
	
	private static final Pattern KEEP_NUMERIC_PATTERN = Pattern.compile("[^\\d\\.-]");
	
	private String minPropertyName = "min";
	private String maxPropertyName = "max";
	private ErrorHandlingModeEnum errorHandlingMode = ErrorHandlingModeEnum.USE_NULL;
	
	public MinMaxDataProcessor(String minPropertyName, String maxPropertyName){
		this.minPropertyName = minPropertyName;
		this.maxPropertyName = maxPropertyName;
	}
	
	/**
	 * MinMax Bean processing function.
	 * @param in Java bean containing a min and a max property as String
	 * @param max Java bean containing a min and a max property as Number (+ primitive class)
	 * @param params Will be ignored so use null
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void processBean(Object in, Object out, Map<String, Object> params) {
		
		try {
			Number[] output = new Number[2];
			String min = (String)PropertyUtils.getSimpleProperty(in, minPropertyName);
			String max = (String)PropertyUtils.getSimpleProperty(in, maxPropertyName);
			
			Class<? extends Number> clazz = PropertyUtils.getPropertyType(out, minPropertyName);
			//Make sure to use the Wrapper instead of the primitive type
			if(clazz.isPrimitive()){
				clazz = (Class<? extends Number>)ClassUtils.primitiveToWrapper(clazz);
			}
			process(min,max,output,clazz);
						
			PropertyUtils.setSimpleProperty(out, minPropertyName, output[0]);
			PropertyUtils.setSimpleProperty(out, maxPropertyName, output[1]);
			
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Min/Max processing function
	 * @param min 
	 * @param max
	 * @param minMaxOutput initialized array(size>2) that will contained the parsed data or null if not parsable
	 * @param clazz Class wanted for element of the minMaxOutput
	 */
	public void process(String min, String max, Number[] minMaxOutput, Class<? extends Number> clazz){
		if(!StringUtils.isBlank(min)){
			min = KEEP_NUMERIC_PATTERN.matcher(min).replaceAll("");
		}
		if(!StringUtils.isBlank(max)){
			max = KEEP_NUMERIC_PATTERN.matcher(max).replaceAll("");
		}		
		minMaxOutput[0] = NumberUtils.parseNumber(min, clazz);
		minMaxOutput[1] = NumberUtils.parseNumber(max, clazz);
	}

	@Override
	public ErrorHandlingModeEnum getErrorHandlingMode() {
		return errorHandlingMode;
	}
}
