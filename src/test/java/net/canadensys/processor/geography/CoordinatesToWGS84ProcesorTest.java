package net.canadensys.processor.geography;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;

import net.canadensys.FileBasedTest;
import net.canadensys.processor.ProcessingResult;

import org.apache.commons.lang3.StringUtils;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Test for the CoordinatesToWGS84Processor
 * @author canadensys
 *
 */
public class CoordinatesToWGS84ProcesorTest {
	
	private static final int TEST_FILE_COLUMNS = 7;
	private static final String TEST_FILE_NAME = "/CoordinatesToWGS84.txt";

	@Test
	public void testWithNoCRS(){
		CoordinatesToWGS84Processor ctwProcessor = new CoordinatesToWGS84Processor();
		ProcessingResult pr = new ProcessingResult();
		Double[] output = ctwProcessor.process(1d, 2d, null, pr);
		assertNull(output[LatLongProcessorHelper.LATITUDE_IDX]);
		assertNull(output[LatLongProcessorHelper.LONGITUDE_IDX]);
		assertTrue(StringUtils.isNotBlank(pr.getErrorString()));
	}
	
	@Test
	public void testProcessor(){
		final CoordinatesToWGS84Processor ctwProcessor = new CoordinatesToWGS84Processor();
		//test supported syntax (from the test file)
		try {
			final File dateFile = new File(getClass().getResource(TEST_FILE_NAME).toURI());
			FileBasedTest fileBasedTest = new FileBasedTest(dateFile) {
				Double[] output = null;
				Double lat, lng, delta;
				CoordinateReferenceSystem crs;
				@Override
				public void processLine(String[] elements, int lineNumber) {
					if(elements.length == TEST_FILE_COLUMNS){
						try {
							crs = CRS.decode(elements[0]);
						} catch (Exception e) {
							fail("[Line #" + lineNumber + " in " + dateFile.getName()+"] is not valid. Invalid CRS: " + elements[0]);
						}
						output = ctwProcessor.process(Double.parseDouble(elements[1]), Double.parseDouble(elements[2]), crs, null);
						lat = Double.parseDouble(elements[3]);
						lng = Double.parseDouble(elements[4]);
						delta = Double.parseDouble(elements[5]);
						String assertText = "[Line #" + lineNumber + " in " + dateFile.getName()+"]";
						assertNotNull(assertText, output);
						assertEquals(assertText, lat, output[LatLongProcessorHelper.LATITUDE_IDX], delta);
						assertEquals(assertText, lng, output[LatLongProcessorHelper.LONGITUDE_IDX], delta);
					}
					else{
						fail("[Line #" + lineNumber + " in " + dateFile.getName()+"] is not valid.");
					}
				}
			};
			
			fileBasedTest.processFile();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
