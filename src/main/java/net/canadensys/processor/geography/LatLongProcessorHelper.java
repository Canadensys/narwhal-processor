package net.canadensys.processor.geography;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import net.canadensys.processor.ProcessingResult;

/**
 * Helper class to be used by processors for common lat/long related tasks.
 * @author canadensys
 *
 */
public class LatLongProcessorHelper {
	
	public static final int LATITUDE_IDX = 0;
	public static final int LONGITUDE_IDX = 1;
	
	//decimalLatitude : Legal values lie between -90 and 90, inclusive.
	public static final int MIN_LATITUDE = -90;
	public static final int MAX_LATITUDE = 90;
	
	//decimalLongitude : Legal values lie between -180 and 180, inclusive.
	public static final int MIN_LONGITUDE = -180;
	public static final int MAX_LONGITUDE = 180;
	
	protected ResourceBundle resourceBundle = null;
	
	public LatLongProcessorHelper(ResourceBundle resourceBundle){
		this.resourceBundle = resourceBundle;
	}

	/**
	 * This function is making sure that the latitude and longitude are inside the defined boundaries.
	 * If not, the latitude AND the longitude will be set to null since only one of them is not useful.
	 * @param output
	 * @param result
	 */
	public void ensureLatLongBoundaries(Number[] output, ProcessingResult result){
		//validate output boundaries
		if(output[LATITUDE_IDX]!=null){
			if(output[LATITUDE_IDX].intValue() > MAX_LATITUDE || output[LATITUDE_IDX].intValue() < MIN_LATITUDE){
				if(result != null){
					result.addError(
							MessageFormat.format(resourceBundle.getString("decimalLatLong.error.outOfBounds"),output[LATITUDE_IDX].intValue(),MIN_LATITUDE,MAX_LATITUDE));
				}
				output[LATITUDE_IDX] = null;
			}
		}
		
		if(output[LONGITUDE_IDX]!=null){
			if(output[LONGITUDE_IDX].intValue() > MAX_LONGITUDE || output[LONGITUDE_IDX].intValue() < MIN_LONGITUDE){
				if(result != null){
					result.addError(
							MessageFormat.format(resourceBundle.getString("decimalLatLong.error.outOfBounds"),output[LONGITUDE_IDX].intValue(),MIN_LONGITUDE,MAX_LONGITUDE));
				}
				output[LONGITUDE_IDX] = null;
			}
		}
		
		//to be a valid coordinate we need both latitude and longitude to be valid
		if(output[LATITUDE_IDX] == null || output[LONGITUDE_IDX] == null){
			output[LATITUDE_IDX] = null;
			output[LONGITUDE_IDX] = null;
		}
	}
}
