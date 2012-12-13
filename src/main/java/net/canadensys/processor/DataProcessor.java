package net.canadensys.processor;

import java.util.Map;

/**
 * All implementations shall be Thread-Safe once created.
 * Considering this, setters should be avoided.
 * @author canadensys
 *
 */
public interface DataProcessor {

	public enum ErrorHandlingModeEnum {USE_NULL,USE_EMPTY,USE_ORIGINAL};
	
	/**
	 * Get the current error handling mode.
	 * What the processor should do when the data can not be processed.
	 * @return current error handling mode
	 */
	public ErrorHandlingModeEnum getErrorHandlingMode();
	
	/**
	 * Process a Java bean. Optimistic casting will be tried, it means that the processor will
	 * cast a property to a specific type. 
	 * @param in in Java bean
	 * @param out out Java bean (specified property(ies) will be overwritten).
	 * @param params additional parameters (optional for some processor)
	 */
	public void processBean(Object in, Object out, Map<String, Object> params);
	
}
