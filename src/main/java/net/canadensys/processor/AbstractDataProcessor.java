package net.canadensys.processor;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * All implementations shall be Thread-Safe once created.
 * Considering this, setters should be avoided.
 *
 * @author canadensys
 *
 */
public abstract class AbstractDataProcessor {

	public enum ErrorHandlingModeEnum {
		USE_NULL, USE_EMPTY, USE_ORIGINAL
	};

	public static String ERROR_BUNDLE_NAME = "languages/errors";

	protected ResourceBundle resourceBundle;

	/**
	 * Set the Locale to used to create the error messages
	 * Configuration method, should be called at creation time.
	 *
	 * @param locale
	 *            used to record errors in ProcessingResult
	 */
	public void setLocale(Locale locale) {
		this.resourceBundle = ResourceBundle.getBundle(ERROR_BUNDLE_NAME, locale);
	}

	protected String getValueOnError(ErrorHandlingModeEnum errorHandlingMode, String originalValue) {
		switch (errorHandlingMode) {
			case USE_ORIGINAL:
				return originalValue;
			case USE_NULL:
				return null;
			case USE_EMPTY:
				return "";
			default:
				return null;
		}
	}

	/**
	 * Get the current error handling mode.
	 * What the processor should do when the data can not be processed.
	 *
	 * @return current error handling mode
	 */
	public abstract ErrorHandlingModeEnum getErrorHandlingMode();

	/**
	 * Process a Java bean. Optimistic casting will be tried, it means that the processor will
	 * cast a property to a specific type.
	 *
	 * @param in
	 *            in Java bean
	 * @param out
	 *            out Java bean (specified property(ies) will be overwritten).
	 * @param params
	 *            additional parameters (optional for some processor)
	 * @param optional
	 *            result variable, use only if you plan to log what happened
	 */
	public abstract void processBean(Object in, Object out, Map<String, Object> params, ProcessingResult result);

	/**
	 * Validates a Java Bean. The validation is not context related. It means that it will only ensure
	 * that the data can be process without errors.
	 *
	 * @param in
	 * @param isMandatory
	 *            is a missing value valid or not?
	 * @param params
	 *            additional parameters (optional for some processor)
	 * @param result
	 */
	public abstract boolean validateBean(Object in, boolean isMandatory, Map<String, Object> params, ProcessingResult result);

}
