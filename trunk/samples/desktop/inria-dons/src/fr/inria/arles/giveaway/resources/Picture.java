package fr.inria.arles.giveaway.resources;

import java.util.Set;
import fr.inria.arles.yarta.resources.Content;
import fr.inria.arles.yarta.resources.Resource;
import fr.inria.arles.giveaway.msemanagement.MSEManagerEx;

/**
 * 
 * Picture interface definition.
 *
 */
public interface Picture extends Resource, Content {
	public static final String typeURI = MSEManagerEx.baseMSEURI + "#Picture";

	/** the URI for property title */
	public static final String PROPERTY_TITLE_URI = baseMSEURI + "#title";

	/** the URI for property source */
	public static final String PROPERTY_SOURCE_URI = baseMSEURI + "#source";

	/** the URI for property format */
	public static final String PROPERTY_FORMAT_URI = baseMSEURI + "#format";

	/** the URI for property identifier */
	public static final String PROPERTY_IDENTIFIER_URI = baseMSEURI + "#identifier";

	/** the URI for property isTagged */
	public static final String PROPERTY_ISTAGGED_URI = baseMSEURI + "#isTagged";

	/**
	 * inverse of {@link #getPicture()}
	 */
	public Set<Announcement> getPicture_inverse();
}