package net.canadensys.processor.geography;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;

import net.canadensys.FileBasedTest;

import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Test for the CoordinatesToWGS84Processor
 * @author canadensys
 *
 */
public class CoordinatesToWGS84ProcesorTest {
	
	private static final String TEST_FILE_NAME = "/CoordinatesToWGS84.txt";
	
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
					if(elements.length == 6){
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
