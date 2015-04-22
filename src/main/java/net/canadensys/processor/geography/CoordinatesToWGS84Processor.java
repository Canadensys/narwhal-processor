package net.canadensys.processor.geography;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;

import net.canadensys.processor.AbstractDataProcessor;
import net.canadensys.processor.ProcessingResult;

import org.apache.commons.beanutils.PropertyUtils;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * Coordinates conversion from most of the ESPG code to WSG84 using GeoTools 10.
 * The provided coordinates must be expressed as double x, double y.
 * 
 * @author canadensys
 * 
 */
public class CoordinatesToWGS84Processor extends AbstractDataProcessor {

	final Logger logger = LoggerFactory.getLogger(CoordinatesToWGS84Processor.class);

	// default Java bean field names.
	protected static final String DEFAULT_X_NAME = "x";
	protected static final String DEFAULT_Y_NAME = "y";
	protected static final String DEFAULT_LATITUDE_NAME = "lat";
	protected static final String DEFAULT_LONGITUDE_NAME = "lng";

	// Java bean field name
	protected String xCoordinateInName, yCoordinateInName = null;
	protected String latitudeOutName, longitudeOutName = null;

	protected static final CoordinateReferenceSystem TARGET_CRS;
	protected CoordinateReferenceSystem sourceCRS = null;

	// Only USE_NULL makes sense here
	protected ErrorHandlingModeEnum errorHandlingMode = ErrorHandlingModeEnum.USE_NULL;

	// Setup EPSG:4326 (wsg84)
	static {
		System.setProperty("org.geotools.referencing.forceXY", "true");
		CoordinateReferenceSystem tmpCrs = null;
		try {
			tmpCrs = CRS.decode("EPSG:4326");
		}
		catch (NoSuchAuthorityCodeException e) {
			e.printStackTrace();
		}
		catch (FactoryException e) {
			e.printStackTrace();
		}
		TARGET_CRS = tmpCrs;
	}

	/**
	 * This constructor will only allow you to use
	 * process(Double x, Double y, CoordinateReferenceSystem sourceCRS, ProcessingResult result) method.
	 */
	public CoordinatesToWGS84Processor() {
		this(null, DEFAULT_X_NAME, DEFAULT_Y_NAME, DEFAULT_LATITUDE_NAME, DEFAULT_LONGITUDE_NAME);
	}

	/**
	 * Create a processor for a specific source coordinate system.
	 * Default field names will be used for Java bean access.
	 * 
	 * @param sourceCoordinateSystem
	 *            e.g. EPSG:26918
	 * @throws UnsupportedOperationException
	 *             if the provided sourceCoordinateSystem is not supported
	 */
	public CoordinatesToWGS84Processor(String sourceCoordinateSystem) throws UnsupportedOperationException {
		this(sourceCoordinateSystem, DEFAULT_X_NAME, DEFAULT_Y_NAME, DEFAULT_LATITUDE_NAME, DEFAULT_LONGITUDE_NAME);
	}

	public CoordinatesToWGS84Processor(String sourceCoordinateSystem, String xCoordinateInName, String yCoordinateInName, String latitudeOutName,
			String longitudeOutName) throws UnsupportedOperationException {
		try {
			if (sourceCoordinateSystem != null) {
				sourceCRS = CRS.decode(sourceCoordinateSystem);
			}

			this.xCoordinateInName = xCoordinateInName;
			this.yCoordinateInName = yCoordinateInName;
			this.latitudeOutName = latitudeOutName;
			this.longitudeOutName = longitudeOutName;

			// always a default Locale
			setLocale(Locale.ENGLISH);
		}
		catch (NoSuchAuthorityCodeException e) {
			logger.error("CoordinatesToWGS84Procesor constuctor error", e);
			throw new UnsupportedOperationException(e);
		}
		catch (FactoryException e) {
			logger.error("CoordinatesToWGS84Procesor constuctor error", e);
			throw new UnsupportedOperationException(e);
		}
	}

	@Override
	public ErrorHandlingModeEnum getErrorHandlingMode() {
		return errorHandlingMode;
	}

	@Override
	public void processBean(Object in, Object out, Map<String, Object> params, ProcessingResult result) {
		try {
			Double x = (Double) PropertyUtils.getSimpleProperty(in, xCoordinateInName);
			Double y = (Double) PropertyUtils.getSimpleProperty(in, yCoordinateInName);

			Double[] output = process(x, y, result);
			PropertyUtils.setSimpleProperty(out, latitudeOutName, output[LatLongProcessorHelper.LATITUDE_IDX]);
			PropertyUtils.setSimpleProperty(out, longitudeOutName, output[LatLongProcessorHelper.LONGITUDE_IDX]);
		}
		catch (IllegalAccessException e) {
			logger.error("Bean access error", e);
		}
		catch (InvocationTargetException e) {
			logger.error("Bean access error", e);
		}
		catch (NoSuchMethodException e) {
			logger.error("Bean access error", e);
		}
	}

	@Override
	public boolean validateBean(Object in, boolean isMandatory, Map<String, Object> params, ProcessingResult result) {
		Double x = null, y = null;
		try {
			x = (Double) PropertyUtils.getSimpleProperty(in, xCoordinateInName);
			y = (Double) PropertyUtils.getSimpleProperty(in, yCoordinateInName);

			Double[] output = process(x, y, result);
			if (output[LatLongProcessorHelper.LATITUDE_IDX] != null && output[LatLongProcessorHelper.LONGITUDE_IDX] != null) {
				return true;
			}
			// change to multiple Exception catch when moving to Java 7
		}
		catch (IllegalAccessException e) {
			logger.error("Bean access error", e);
			return false;
		}
		catch (InvocationTargetException e) {
			logger.error("Bean access error", e);
			return false;
		}
		catch (NoSuchMethodException e) {
			logger.error("Bean access error", e);
			return false;
		}

		// not valid, check if the value was mandatory
		if (!isMandatory && x != null && y != null) {
			return true;
		}
		return false;
	}

	public Double[] process(Double x, Double y, ProcessingResult result) {
		return process(x, y, sourceCRS, result);
	}

	/**
	 * Process a coordinate as x,y into a WSG84 decimal latitude, longitude value.
	 * 
	 * @param x
	 * @param y
	 * @param sourceCRS
	 * @param result
	 * @return
	 */
	public Double[] process(Double x, Double y, CoordinateReferenceSystem sourceCRS, ProcessingResult result) {
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		Double[] output = new Double[2];

		if (sourceCRS == null) {
			if (result != null) {
				result.addError(resourceBundle.getString("coordinateConversion.error.invalidSourceCRS"));
			}
			return output;
		}

		Coordinate coord = new Coordinate(x, y);
		Point sourcePoint = geometryFactory.createPoint(coord);
		try {
			// to ensure the best precision, do not use lenient transform
			MathTransform transform = CRS.findMathTransform(sourceCRS, TARGET_CRS, false);
			Geometry targetGeometry = JTS.transform(sourcePoint, transform);
			output[LatLongProcessorHelper.LATITUDE_IDX] = targetGeometry.getCoordinate().y;
			output[LatLongProcessorHelper.LONGITUDE_IDX] = targetGeometry.getCoordinate().x;
		}
		catch (FactoryException e) {
			if (result != null) {
				result.addError(MessageFormat.format(resourceBundle.getString("coordinateConversion.error.noTransformation"), sourceCRS.getName()));
			}
		}
		catch (MismatchedDimensionException e) {
			if (result != null) {
				result.addError(MessageFormat.format(resourceBundle.getString("coordinateConversion.error.transformError"), coord.toString()));
			}
		}
		catch (TransformException e) {
			if (result != null) {
				result.addError(MessageFormat.format(resourceBundle.getString("coordinateConversion.error.transformError"), coord.toString()));
			}
		}
		return output;
	}

	/**
	 * Process an array of coordinates as x,y into a WSG84 decimal latitude, longitude value.
	 * 
	 * @param x
	 *            array of x coordinates
	 * @param y
	 *            array of y coordinates
	 * @param sourceCRS
	 *            source CRS of coordinates
	 * @param result
	 *            optional array of ProcessingResult
	 * @return
	 */
	public Double[][] processBatch(Double[] x, Double[] y, CoordinateReferenceSystem sourceCRS, ProcessingResult[] result) {
		// sanity check
		if (x == null || y == null || (x.length != y.length)) {
			return null;
		}

		int batchSize = x.length;
		Double[][] output = new Double[batchSize][];
		ProcessingResult currResult = null;

		for (int i = 0; i < batchSize; i++) {
			if (result != null) {
				currResult = result[i];
			}
			output[i] = process(x[i], y[i], sourceCRS, currResult);
		}
		return output;
	}

}
