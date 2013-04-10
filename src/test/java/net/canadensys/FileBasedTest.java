package net.canadensys;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * Utility class to run a unit test using the values (input values and expected values) from a file.
 * @author canadensys
 *
 */
public abstract class FileBasedTest {
	
	//Default values
	private static final String COMMENT_LINE_CHAR = "#";
	private static final String ELEMENT_SEPARATOR = ";";
	
	private File testFile;
	private String commentLineChar;
	private String elementSeparatorChar;
	
	public FileBasedTest(File testFile){
		this(testFile,ELEMENT_SEPARATOR,COMMENT_LINE_CHAR);
	}
	
	/**
	 * 
	 * @param testFile
	 * @param elementSeparatorChar separator of the elements within the same line
	 * @param commentLineChar character that identifies a commented line
	 */
	public FileBasedTest(File testFile,String elementSeparatorChar, String commentLineChar){
		this.testFile = testFile;
		this.commentLineChar = commentLineChar;
		this.elementSeparatorChar = elementSeparatorChar;
	}
	
	/**
	 * Method that will be called on each lines(that is not a comment) of the file.
	 * @param elements
	 * @param lineNumber number or the line (starting at 1) including the commented lines
	 */
	public abstract void processLine(String[] elements,int lineNumber);
	
	/**
	 * Call this method to initiate the reading of the test file
	 */
	public void processFile(){
		try {
			List<String> fileLines = FileUtils.readLines(testFile);
			int lineNumber = 1;
			for(String currLine : fileLines){
				if(!currLine.startsWith(commentLineChar)){
					processLine(currLine.split(elementSeparatorChar),lineNumber);
				}
				lineNumber++;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
