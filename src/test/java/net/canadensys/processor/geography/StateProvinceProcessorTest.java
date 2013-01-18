package net.canadensys.processor.geography;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.NoSuchElementException;

import net.canadensys.processor.DataProcessor;
import net.canadensys.processor.DataProcessor.ErrorHandlingModeEnum;
import net.canadensys.processor.ProcessingResult;
import net.canadensys.processor.dwc.mock.MockOccurrenceModel;
import net.canadensys.vocabulary.stateprovince.CanadaProvince;
import net.canadensys.vocabulary.stateprovince.StateProvinceEnum;

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
		StateProvinceProcessor<CanadaProvince> spProcessor = new StateProvinceProcessor<CanadaProvince>(Country.CANADA, CanadaProvince.class);
		StateProvinceEnum spe = spProcessor.process("LABRADOR", null);
		assertEquals(CanadaProvince.NEWFOUNDLAND_AND_LABRADOR, spe);
		
		MockOccurrenceModel mockRawModel = new MockOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();
		mockRawModel.setStateprovince("The Northwest Territories");
		spProcessor.processBean(mockRawModel, mockModel, null, null);
		assertEquals(CanadaProvince.NORTHWEST_TERRITORIES.getName(), mockModel.getStateprovince());
	}
	
	@Test
	public void testWrongProvinceState(){
		DataProcessor spProcessor = new StateProvinceProcessor<CanadaProvince>(Country.CANADA, CanadaProvince.class);
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
		DataProcessor spProcessor = new StateProvinceProcessor<CanadaProvince>(Country.CANADA, CanadaProvince.class,"stateprovince",ErrorHandlingModeEnum.USE_NULL);
		MockOccurrenceModel mockRawModel = new MockOccurrenceModel();
		MockOccurrenceModel mockModel = new MockOccurrenceModel();
		
		//test null
		mockRawModel.setStateprovince("xyz");
		ProcessingResult pr = new ProcessingResult();
		spProcessor.processBean(mockRawModel, mockModel, null, pr);
		assertNull(mockModel.getStateprovince());
		
		//test empty value
		spProcessor = new StateProvinceProcessor<CanadaProvince>(Country.CANADA, CanadaProvince.class,"stateprovince",ErrorHandlingModeEnum.USE_EMPTY);
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
