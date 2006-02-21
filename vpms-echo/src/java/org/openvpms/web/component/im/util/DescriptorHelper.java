package org.openvpms.web.component.im.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyList;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyMap;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Helper class for working with {@link ArchetypeDescriptor}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public final class DescriptorHelper {

    /**
     * The logger.
     */
    private static final Log _log = LogFactory.getLog(DescriptorHelper.class);


    /**
     * Returns an archetype descriptor, given its shortname.
     *
     * @param shortName the shortname
     * @return the descriptor corresponding to <code>shortName</code>, or
     *         <code>null</code> if none exists
     */
    public static ArchetypeDescriptor getArchetypeDescriptor(String shortName) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        return service.getArchetypeDescriptor(shortName);
    }

    /**
     * Returns the display name for an archetype.
     *
     * @param shortName the archetype short name
     * @return the archetype display name, or <code>null</code> if none exists
     */
    public static String getDisplayName(String shortName) {
        ArchetypeDescriptor descriptor = getArchetypeDescriptor(shortName);
        return (descriptor != null) ? descriptor.getDisplayName() : null;
    }

    /**
     * Returns the display name for an object.
     *
     * @param object the object
     * @return a display name for the object, or <code>null</code> if none
     *         exists
     */
    public static String getDisplayName(IMObject object) {
        ArchetypeDescriptor descriptor = getArchetypeDescriptor(object);
        return (descriptor != null) ? descriptor.getDisplayName() : null;
    }

    /**
     * Returns the archetype descriptor for the specified object.
     *
     * @param object the object
     * @return the archetype descriptor corresponding to <code>object</code>
     */
    public static ArchetypeDescriptor getArchetypeDescriptor(IMObject object) {
        ArchetypeDescriptor descriptor;
        ArchetypeId archId = object.getArchetypeId();
        IArchetypeService service = ServiceHelper.getArchetypeService();

        //TODO This is a work around until we resolve the current
        // problem with archetyping and archetype. We need to
        // extend this page and create a new archetype specific
        // edit page.
        if (object instanceof AssertionDescriptor) {
            AssertionTypeDescriptor atDesc = service.getAssertionTypeDescriptor(
                    object.getName());
            archId = new ArchetypeId(atDesc.getPropertyArchetype());
        }

        descriptor = service.getArchetypeDescriptor(archId);
        if (descriptor == null) {
            descriptor = getArchetypeDescriptor(
                    object.getArchetypeId().getShortName());
        }

        if (_log.isDebugEnabled()) {
            _log.debug("Returning archetypeDescriptor="
                    + (descriptor == null ? null : descriptor.getName())
                    + " for archId=" + archId
                    + " and object=" + object.getClass().getName());
        }

        return descriptor;
    }

    /**
     * Determines the minimum cardinality from an archetype range assertion.
     *
     * @param descriptor the node descriptor
     * @param shortName  the archetype short name
     * @return the minimum cardinality, or <code>0</code> if none is specified
     */
    public static int getMinCardinality(NodeDescriptor descriptor,
                                        String shortName) {
        int result = 0;
        NamedProperty property
                = getArchetypeProperty(descriptor, shortName, "minCardinality");
        if (property != null) {
            result = Integer.parseInt(property.getValue().toString());
        }
        return result;
    }

    /**
     * Determines the maximum cardinality from an archetype range assertion.
     *
     * @param descriptor the node descriptor
     * @param shortName  the archetype short name
     * @return the maximum cardinality, or <code>0</code> if none is specified
     */
    public static int getMaxCardinality(NodeDescriptor descriptor,
                                        String shortName) {
        int result = 0;
        NamedProperty property
                = getArchetypeProperty(descriptor, shortName, "maxCardinality");
        if (property != null) {
            if (NodeDescriptor.UNBOUNDED_AS_STRING.equals(property.getValue()))
            {
                result = NodeDescriptor.UNBOUNDED;
            } else {
                result = Integer.parseInt(property.getValue().toString());
            }
        }
        return result;
    }

    /**
     * Helper to return a named propery.
     *
     * @param descriptor the node descriptor
     * @param shortName  the archetype  short name
     * @param name       the property name
     * @return the named property, or <code>null</code> if none exists
     */
    private static NamedProperty getArchetypeProperty(NodeDescriptor descriptor,
                                                      String shortName,
                                                      String name) {
        NamedProperty result = null;
        PropertyMap properties = getArchetypeProperties(descriptor, shortName);
        if (properties != null) {
            result = properties.getProperties().get(name);
        }
        return result;
    }

    /**
     * Returns properties from a descriptors archetype range for a paricular
     * archetype
     *
     * @param descriptor the node descriptor
     * @param shortName  the archetype short name to match on
     * @return the properties for the specfied archetype, or <code>null</code>
     *         if none exists
     */
    private static PropertyMap getArchetypeProperties(NodeDescriptor descriptor,
                                                      String shortName) {
        PropertyMap result = null;
        AssertionDescriptor assertionDesc
                = descriptor.getAssertionDescriptor("archetypeRange");
        if (assertionDesc != null) {
            PropertyList archetypes
                    = (PropertyList) assertionDesc.getProperty("archetypes");
            NamedProperty[] list = archetypes.getPropertiesAsArray();
            for (int i = 0; i < list.length; ++i) {
                PropertyMap archetype = (PropertyMap) list[i];
                NamedProperty name = archetype.getProperties().get("shortName");
                if (name.getValue().equals(shortName)) {
                    result = archetype;
                    break;
                }
            }
        }
        return result;
    }

}
