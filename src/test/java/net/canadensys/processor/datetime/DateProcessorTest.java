package net.canadensys.processor.datetime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import net.canadensys.processor.dwc.mock.MockOccurrenceModel;
import net.canadensys.processor.dwc.mock.MockRawOccurrenceModel;

import org.junit.Test;

/**
 * Test for the DateProcessor
 * @author canadensys
 *
 */
public class DateProcessorTest {
	
	@Test
	public void testISO8601Date(){
		DateProcessor dateProcessor = new DateProcessor("eventDate", "eventStartYear", "eventStartMonth", "eventStartDay");
		
		MockRawOccurrenceModel mockRawModel = new MockRawOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();
		
		//Test 1987-06-03
		mockRawModel.setEventDate("1987-06-03");
		dateProcessor.processBean(mockRawModel, mockModel, null, null);
		
		assertEquals(1987, mockModel.getEventStartYear().intValue());
		assertEquals(06, mockModel.getEventStartMonth().intValue());
		assertEquals(03, mockModel.getEventStartDay().intValue());
		
		//Test 1987-07
		mockRawModel.setEventDate("1987-07");
		dateProcessor.processBean(mockRawModel, mockModel, null, null);
		
		assertEquals(1987, mockModel.getEventStartYear().intValue());
		assertEquals(07, mockModel.getEventStartMonth().intValue());
		assertNull( mockModel.getEventStartDay());
		
		//Test 1987
		mockRawModel.setEventDate("1987");
		dateProcessor.processBean(mockRawModel, mockModel, null, null);
		
		assertEquals(1987, mockModel.getEventStartYear().intValue());
		assertNull(mockModel.getEventStartMonth());
		assertNull(mockModel.getEventStartDay());
	}
	
	@Test
	public void testEnglishLanguageDate(){
		DateProcessor dateProcessor = new DateProcessor("eventDate", "eventStartYear", "eventStartMonth", "eventStartDay");
		
		MockRawOccurrenceModel mockRawModel = new MockRawOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();

		mockRawModel.setEventDate("Jun 3 1987");
		dateProcessor.processBean(mockRawModel, mockModel, null, null);
		assertEquals(1987, mockModel.getEventStartYear().intValue());
		assertEquals(06, mockModel.getEventStartMonth().intValue());
		assertEquals(03, mockModel.getEventStartDay().intValue());
		
		mockRawModel.setEventDate("Jun 03 1987");
		dateProcessor.processBean(mockRawModel, mockModel, null, null);
		assertEquals(1987, mockModel.getEventStartYear().intValue());
		assertEquals(06, mockModel.getEventStartMonth().intValue());
		assertEquals(03, mockModel.getEventStartDay().intValue());
		
		mockRawModel.setEventDate("Jun 1987");
		dateProcessor.processBean(mockRawModel, mockModel, null, null);
		assertEquals(1987, mockModel.getEventStartYear().intValue());
		assertEquals(06, mockModel.getEventStartMonth().intValue());
		assertNull(mockModel.getEventStartDay());
		
		mockRawModel.setEventDate("Jun");
		dateProcessor.processBean(mockRawModel, mockModel, null, null);
		assertNull(mockModel.getEventStartYear());
		assertEquals(06, mockModel.getEventStartMonth().intValue());
		assertNull(mockModel.getEventStartDay());
		
		mockRawModel.setEventDate("3 Jun 1987");
		dateProcessor.processBean(mockRawModel, mockModel, null, null);
		assertEquals(1987, mockModel.getEventStartYear().intValue());
		assertEquals(06, mockModel.getEventStartMonth().intValue());
		assertEquals(03, mockModel.getEventStartDay().intValue());
		
		mockRawModel.setEventDate("03 Jun 1987");
		dateProcessor.processBean(mockRawModel, mockModel, null, null);
		assertEquals(1987, mockModel.getEventStartYear().intValue());
		assertEquals(06, mockModel.getEventStartMonth().intValue());
		assertEquals(03, mockModel.getEventStartDay().intValue());		
	}
}
