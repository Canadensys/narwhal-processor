package net.canadensys.processor.geography;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.NoSuchElementException;

import net.canadensys.processor.DataProcessor;
import net.canadensys.processor.DataProcessor.ErrorHandlingModeEnum;
import net.canadensys.processor.ProcessingResult;
import net.canadensys.processor.dwc.mock.MockOccurrenceModel;
import net.canadensys.vocabulary.stateprovince.BEProvince;
import net.canadensys.vocabulary.stateprovince.CODepartment;
import net.canadensys.vocabulary.stateprovince.StateProvinceEnum;
import net.canadensys.vocabulary.stateprovince.CAProvince;

import org.gbif.api.model.vocabulary.Country;
import org.junit.Test;

/**
 * Tests for the CountryProcessor
 * @author canadensys
 *
 */
public class StateProvinceProcessorTest {
	
	@Test
	public void testProvinceState(){
		StateProvinceProcessor<CAProvince> spProcessor = new StateProvinceProcessor<CAProvince>(Country.CANADA, CAProvince.class);
		StateProvinceEnum spe = spProcessor.process("LABRADOR", null);
		assertEquals(CAProvince.NEWFOUNDLAND_AND_LABRADOR, spe);

		MockOccurrenceModel mockRawModel = new MockOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();
		mockRawModel.setStateprovince("The Northwest Territories");
		spProcessor.processBean(mockRawModel, mockModel, null, null);
		assertEquals(CAProvince.NORTHWEST_TERRITORIES.getName(), mockModel.getStateprovince());
	}
	
	/**
	 * Test Belgium dictionary
	 */
	@Test
	public void testBEProvince(){
		StateProvinceProcessor<BEProvince> spProcessor = new StateProvinceProcessor<BEProvince>(Country.BELGIUM, BEProvince.class);
		StateProvinceEnum spe = spProcessor.process("Provinz Antwerpen", null);
		assertEquals(BEProvince.ANTWERP, spe);
	}
	
	/**
	 * Test Colombia dictionary
	 */
	@Test
	public void testCODepartment(){
		StateProvinceProcessor<CODepartment> spProcessor = new StateProvinceProcessor<CODepartment>(Country.COLOMBIA, CODepartment.class);
		StateProvinceEnum spe = spProcessor.process("San Andres y Providencia", null);
		assertEquals(CODepartment.SAN_ANDRES_PROVIDENCIA_SANTA_CATALINA, spe);
	}
	
	@Test
	public void testProvinceStateValidation(){
		DataProcessor dataProcessor = new StateProvinceProcessor<CAProvince>(Country.CANADA, CAProvince.class);
		MockOccurrenceModel mockRawModel = new MockOccurrenceModel();
		mockRawModel.setStateprovince("The Northwest Territories");
		assertTrue(dataProcessor.validateBean(mockRawModel, false, null, null));
		
		//test mandatory flag
		mockRawModel.setStateprovince(null);
		assertFalse(dataProcessor.validateBean(mockRawModel, true, null, null));
		assertTrue(dataProcessor.validateBean(mockRawModel, false, null, null));
	}
	
	@Test
	public void testWrongProvinceState(){
		DataProcessor spProcessor = new StateProvinceProcessor<CAProvince>(Country.CANADA, CAProvince.class);
		MockOccurrenceModel mockRawModel = new MockOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();
		
		mockRawModel.setStateprovince("xyz");
		ProcessingResult pr = new ProcessingResult();
		spProcessor.processBean(mockRawModel, mockModel, null, pr);
		assertEquals("xyz",mockModel.getStateprovince());
	}
	
	@Test(expected=NoSuchElementException.class)
	public void testWrongInitialization(){
		new StateProvinceProcessor<TestStateProvinceEnum>(Country.CANADA, TestStateProvinceEnum.class);
	}
	
	@Test
	public void testWrongProvinceStateOtherErrorHandling(){
		DataProcessor spProcessor = new StateProvinceProcessor<CAProvince>(Country.CANADA, CAProvince.class,"stateprovince",ErrorHandlingModeEnum.USE_NULL);
		MockOccurrenceModel mockRawModel = new MockOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();
		
		//test null
		mockRawModel.setStateprovince("xyz");
		ProcessingResult pr = new ProcessingResult();
		spProcessor.processBean(mockRawModel, mockModel, null, pr);
		assertNull(mockModel.getStateprovince());
		
		//test empty value
		spProcessor = new StateProvinceProcessor<CAProvince>(Country.CANADA, CAProvince.class,"stateprovince",ErrorHandlingModeEnum.USE_EMPTY);
		spProcessor.processBean(mockRawModel, mockModel, null, pr);
		assertEquals("",mockModel.getStateprovince());
	}
	
	public enum TestStateProvinceEnum implements StateProvinceEnum{
		T1;
		@Override
		public String getCode() {
			return "test";
		}

		@Override
		public String getName() {
			return "test";
		}
	}
}
