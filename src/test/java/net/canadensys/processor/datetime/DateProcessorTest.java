package net.canadensys.processor.datetime;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

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
	
	@Test
	public void testFromFile(){
		File csvDateFile;
		DateProcessor dateProcessor = new DateProcessor("eventDate", "eventStartYear", "eventStartMonth", "eventStartDay");
		
		MockRawOccurrenceModel mockRawModel = new MockRawOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();
		
		try {
			csvDateFile = new File(getClass().getResource("/dateFormat.csv").toURI());
			List<String> fileLines = FileUtils.readLines(csvDateFile);
			String[] splittedLine;
			Integer year,month,day;
			for(String currLine : fileLines){
				if(!currLine.startsWith("#")){
					splittedLine = currLine.split(";");
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
