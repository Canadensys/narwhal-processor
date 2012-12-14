package net.canadensys.processor;

import java.util.ArrayList;
import java.util.List;

/**
 * Holding processing results.
 * @author canadensys
 *
 */
public class ProcessingResult {
	
	private List<String> errorList;
	
	public ProcessingResult(){
		errorList = new ArrayList<String>();
	}
	
	public void addError(String errorDescription){
		errorList.add(errorDescription);
	}
	
	public List<String> getErrorList(){
		return errorList;
	}
}
