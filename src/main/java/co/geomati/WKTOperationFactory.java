package co.geomati;

import java.net.URL;

import org.geotools.referencing.factory.epsg.CoordinateOperationFactoryUsingWKT;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;

/**
 * Authority allowing users to define their own CoordinateOperations in a
 * separate file. Will override EPSG definitions.
 * 
 * @author Oscar Fonts
 */
public class WKTOperationFactory extends CoordinateOperationFactoryUsingWKT implements CoordinateOperationAuthorityFactory {

	private String definitions = null;

	public WKTOperationFactory(String definitions) {
		super(null, MAXIMUM_PRIORITY);
		this.definitions = definitions;
	}

	/**
	 * Returns the URL to the property file that contains Operation definitions.
	 * 
	 * @return The URL, or {@code null} if none.
	 */
	protected URL getDefinitionsURL() {
		URL location = getClass().getResource(definitions);
		return location;
	}
}
