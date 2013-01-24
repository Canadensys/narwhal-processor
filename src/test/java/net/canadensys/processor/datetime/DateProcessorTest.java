package net.canadensys.processor.datetime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import net.canadensys.processor.ProcessingResult;
import net.canadensys.processor.dwc.mock.MockOccurrenceModel;
import net.canadensys.processor.dwc.mock.MockRawOccurrenceModel;
import net.canadensys.utils.NumberUtils;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 * Test for the DateProcessor
 * @author canadensys
 *
 */
public class DateProcessorTest {
	
	private static final String COMMENT_LINE_CHAR = "#";
	private static final String ELEMENT_SEPARATOR = ";";
	
	@Test
	public void testFromFile(){
		File csvDateFile;
		DateProcessor dateProcessor = new DateProcessor("eventDate", "eventStartYear", "eventStartMonth", "eventStartDay");
		
		MockRawOccurrenceModel mockRawModel = new MockRawOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();
		
		try {
			csvDateFile = new File(getClass().getResource("/dateFormat.txt").toURI());
			List<String> fileLines = FileUtils.readLines(csvDateFile);
			String[] splittedLine;
			Integer year,month,day;
			for(String currLine : fileLines){
				if(!currLine.startsWith(COMMENT_LINE_CHAR)){
					splittedLine = currLine.split(ELEMENT_SEPARATOR);
					if(splittedLine.length == 4){
						mockRawModel.setEventDate(splittedLine[0]);
						dateProcessor.processBean(mockRawModel, mockModel, null, null);
						
						year = NumberUtils.parseNumber(splittedLine[1], Integer.class);
						month = NumberUtils.parseNumber(splittedLine[2], Integer.class);
						day = NumberUtils.parseNumber(splittedLine[3], Integer.class);
						
						assertEquals(year, mockModel.getEventStartYear());
						assertEquals(month, mockModel.getEventStartMonth());
						assertEquals(day, mockModel.getEventStartDay());
						System.out.println("Testing date : " + splittedLine[0]);
					}
				}
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testDateValidation(){
		DateProcessor dateProcessor = new DateProcessor("eventDate", "eventStartYear", "eventStartMonth", "eventStartDay");
		MockRawOccurrenceModel mockRawModel = new MockRawOccurrenceModel();
		mockRawModel.setEventDate("2010-01-02");
		
		assertTrue(dateProcessor.validateBean(mockRawModel, false, null, null));
		
		//test mandatory flag
		mockRawModel.setEventDate(null);
		assertFalse(dateProcessor.validateBean(mockRawModel, true, null, null));
		assertTrue(dateProcessor.validateBean(mockRawModel, false, null, null));
	}
	
	@Test
	public void testFuzyDates(){
		DateProcessor dateProcessor = new DateProcessor("eventDate", "eventStartYear", "eventStartMonth", "eventStartDay");
		
		MockRawOccurrenceModel mockRawModel = new MockRawOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();

		mockRawModel.setEventDate("10-11-2010");
		ProcessingResult pr = new ProcessingResult();
		dateProcessor.processBean(mockRawModel, mockModel, null, pr);
		
		assertNull(mockModel.getEventStartYear());
		assertNull(mockModel.getEventStartMonth());
		assertNull(mockModel.getEventStartDay());
		assertTrue(pr.getErrorList().size() >=1);
	}
	
	@Test
	public void testStandardizeDatePunctuation(){
		DateProcessor dateProcessor = new DateProcessor("eventDate", "eventStartYear", "eventStartMonth", "eventStartDay");
		
		assertEquals("8-11-2003",dateProcessor.standardizeDatePunctuation("8/11/2003"));
		assertEquals("08-11-2003",dateProcessor.standardizeDatePunctuation("08.11.2003"));
		assertEquals("08-11-2003",dateProcessor.standardizeDatePunctuation("08-11-2003"));
		assertEquals("08-11 2003",dateProcessor.standardizeDatePunctuation("08.11 2003"));
		
		//Should remain the same
		assertEquals("10/Oct/2000",dateProcessor.standardizeDatePunctuation("10/Oct/2000"));
		assertEquals("10.Oct.2000",dateProcessor.standardizeDatePunctuation("10.Oct.2000"));
		assertEquals("10-Oct-2000",dateProcessor.standardizeDatePunctuation("10-Oct-2000"));
		assertEquals("10 Oct 2000",dateProcessor.standardizeDatePunctuation("10 Oct 2000"));
		assertEquals("10 10 2000",dateProcessor.standardizeDatePunctuation("10 10 2000"));
	}
}
