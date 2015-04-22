package net.canadensys.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Holding processing results. Thread-Safe by default.
 * 
 * @author canadensys
 * 
 */
public class ProcessingResult {

	private boolean synchronizedList;
	private List<String> errorList;

	public ProcessingResult() {
		this(true);
	}

	/**
	 * 
	 * @param synchronizedList
	 *            use a synchronized or not to keep errors. You could turn this off if you are not
	 *            using the object in a multi-threading context (non-synchronized list are a faster).
	 */
	public ProcessingResult(boolean synchronizedList) {
		this.synchronizedList = synchronizedList;
		if (synchronizedList) {
			errorList = Collections.synchronizedList(new ArrayList<String>());
		}
		else {
			errorList = new ArrayList<String>();
		}
	}

	public void addError(String errorDescription) {
		errorList.add(errorDescription);
	}

	/**
	 * Returns the list of error, in mutli-threading context, make sure to manually synchronize the list before calling
	 * multiple functions (e.g. iterate over it).
	 * 
	 * @return
	 */
	public List<String> getErrorList() {
		return errorList;
	}

	public String getErrorString() {
		String errorString;
		if (synchronizedList) {
			synchronized (errorList) {
				errorString = StringUtils.join(errorList, ",");
			}
		}
		else {
			errorString = StringUtils.join(errorList, ",");
		}
		return errorString;
	}

	/**
	 * Clear all errors
	 */
	public void clear() {
		errorList.clear();
	}
}
