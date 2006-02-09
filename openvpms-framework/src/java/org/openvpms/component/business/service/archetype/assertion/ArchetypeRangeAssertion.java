/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.assertion;

// java core
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

// log4j
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.property.AssertionProperty;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyCollection;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyList;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyMap;
import org.openvpms.component.business.service.archetype.IArchetypeService;

/**
 * These assertions are applied to archetype and parts of archetypes. These are
 * all static functions that take an object and property map as arguments and
 * return a boolean as a result.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeRangeAssertion {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(ArchetypeRangeAssertion.class);

    /**
     * Hold a reference to the archetype service, which is required by some
     * methods
     */
    private static IArchetypeService archetypeService;
    
    /**
     * Construct an instance of the class with the specified dependencies.
     * 
     * @param service
     *            the archetype service
     */
    public ArchetypeRangeAssertion(IArchetypeService service) {
        archetypeService = service;
    }

    /**
     * This method is called during the create phase of an archetype. It will
     * check the assertion and determine whether it needs to do any work.
     * <p>
     * If the node descriptor is a collection and it is not a parent child
     * relationship then there may be a need to create and associated default
     * entries to the collection
     * 
     * @param target
     *            the target object
     * @param node
     *            the node descriptor
     * @param assertion
     *            the particular assertion
     */
    public static void create(Object target, NodeDescriptor node, 
            AssertionDescriptor assertion) {
        if ((node.isCollection()) &&
            (!node.isParentChild())) {
            List<ArchetypeRangeInfo> atypes = getArchetypeRangeInfo(assertion);
            if (atypes.size() == 0) {
                return;
            }
            
            // iterate through all the archetype range and if a default value
            // has been specfied attempt to locate the entry and add it to
            // the collection
            for (ArchetypeRangeInfo type : atypes) {
                if (StringUtils.isEmpty(type.defaultValue)) {
                    continue;
                }
                
                // okay a default value needs to be created
                ArchetypeDescriptor adesc = archetypeService.getArchetypeDescriptor(type.shortName);
                if (adesc == null) {
                    throw new AssertionRuntimeException(
                            AssertionRuntimeException.ErrorCode.ArchetypeDoesNotExist,
                            new Object[] {"archetypeRangeAssertion", "create",
                                    type.shortName });
                }
                
                try {
                    ArchetypeId aid = adesc.getType();
                    List<IMObject> objects = archetypeService.get(aid.getRmName(), 
                            aid.getEntityName(), aid.getConcept(), 
                            type.defaultValue, true);
                    
                    // if can not find a matching entity
                    if (objects.size() == 0) {
                        throw new AssertionRuntimeException(
                                AssertionRuntimeException.ErrorCode.FailedToFindArchetype,
                                new Object[] {"archetypeRangeAssertion", "create",
                                        type.shortName, type.defaultValue });
                    }
                    
                    // only expect to retrieve a single object
                    if (objects.size() > 1) {
                        throw new AssertionRuntimeException(
                                AssertionRuntimeException.ErrorCode.TooManyObjectOnCreate,
                                new Object[] {"archetypeRangeAssertion", "create",
                                        type.shortName, type.defaultValue });
                    }
                    
                    // now add it to the collection
                    node.addChildToCollection((IMObject)target, objects.get(0));
                } catch (Exception exception) {
                    throw new AssertionRuntimeException(
                            AssertionRuntimeException.ErrorCode.FailedInCreate,
                            new Object[] {"archetypeRangeAssertion", "create",
                                    type.shortName }, exception);
                }
            }
            
        }
    }
    
    /**
     * This will iterate through the collection for the specified node
     * descriptor and check that each element is in the archetype range.
     * 
     * @param target
     *            the target object
     * @param node
     *            the node descriptor for this assertion
     * @param assertion
     *            the particular assertion
     */
    public static boolean validate(Object target,
            NodeDescriptor node, AssertionDescriptor assertion) {
        List<ArchetypeRangeInfo> atypes = getArchetypeRangeInfo(assertion);

        // no children to match
        if (target == null) {
            return true;
        }

        // no children to match
        if (atypes.size() == 0) {
            return true;
        }

        // first check to see if the object is of a collection type
        Collection entries = null;
        if (target instanceof Collection) {
            entries = (Collection) target;
        } else if (target instanceof Map) {
            entries = (Collection) ((Map) target).values();
        } else if (target instanceof PropertyCollection) {
            entries = ((PropertyCollection) target).values();
        }
        
        if (entries != null) {
            // if it is a collection type then process all the objects in
            // the collection
            if (entries.size() > 0) {
                for (Object entry : entries) {
                    if (entry instanceof IMObject) {
                        IMObject imobj = (IMObject) entry;
                        boolean match = false;

                        // first check that it matches the collection filter
                        // only porcess entries tht match the filter
                        if (!node.matchesFilter(imobj)) {
                            continue;
                        }
                        
                        for (ArchetypeRangeInfo type : atypes) {
                            if (imobj.getArchetypeId().getShortName().matches(type.shortName)) {
                                type.count++;
                                match = true;
                                break;
                            }
                        }
    
                        // if there is no match then break
                        if (!match) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
            
            // now check that min and max cardinality have been satisfied
            for (ArchetypeRangeInfo type : atypes) {
                if ((type.count < type.minCardinality) ||
                    (type.maxCardinality != -1 && type.count > type.maxCardinality)) {
                    return false;
                }
            }
            return true;
        } else if (target instanceof IMObjectReference) {
            // the target is an IMObjectReference so check the reference.
            IMObjectReference imref = (IMObjectReference) target;
            for (ArchetypeRangeInfo type : atypes) {
                if (imref.getArchetypeId().getShortName().matches(type.shortName)) {
                    return true; 
                }
            }
        }
        
        return false;
    }

    /**
     * Retrieve the archetype range given the specified
     * {@link AssertionDescriptor}
     * 
     * @param desc
     *            the assertion descriptor
     * @return ArchetypeRangeInfo[] an array of the archetype property info
     */
    private static List<ArchetypeRangeInfo> getArchetypeRangeInfo(AssertionDescriptor desc) {
        List<ArchetypeRangeInfo> infos = new ArrayList<ArchetypeRangeInfo>();
        PropertyList archetypes = (PropertyList) desc.getPropertyMap()
                .getProperties().get("archetypes");
        for (NamedProperty archetype : archetypes.getProperties()) {
            ArchetypeRangeInfo info = new ArchetypeRangeInfo();

            // check for short name
            AssertionProperty prop = (AssertionProperty)((PropertyMap) archetype)
                .getProperties().get("shortName");
            if (prop == null) {
                logger.warn("archetypeRangeAssertion does not specify a short name");
                continue;
            }
            info.shortName = prop.getValue();
            
            // check for min cardinality name
            prop = (AssertionProperty)((PropertyMap) archetype)
                .getProperties().get("minCardinality");
            if (prop == null) {
                info.minCardinality = 0;
            } else {
                info.minCardinality = Integer.parseInt(prop.getValue());
            }
            
            // check for max cardinality name
            prop = (AssertionProperty)((PropertyMap) archetype)
                .getProperties().get("maxCardinality");
            if (prop == null) {
                info.maxCardinality = -1;
            } else {
                info.maxCardinality = Integer.parseInt(prop.getValue());
            }
            
            // check for default value
            prop = (AssertionProperty)((PropertyMap) archetype)
                .getProperties().get("defaultValue");
            if (prop != null) {
                info.defaultValue = prop.getValue();
            }
            infos.add(info);
        }

        return infos;
    }
    
    
    /**
     * Static inner class to hold a data structure
     * 
     * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
     * @version $LastChangedDate$
     */
    private static class ArchetypeRangeInfo {
        String shortName;
        String defaultValue;
        int minCardinality;
        int maxCardinality;
        int count;
        
    }
}


