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

package org.openvpms.component.business.service.archetype;

// openvpms-framework
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// commmons-jxpath
import org.apache.commons.jxpath.JXPathContext;

// commons-lang
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.property.AssertionProperty;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyList;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeShortNameConstraint;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;

/**
 * This is a helper class for retrieving lookups reference data.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LookupHelper  {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(LookupHelper.class);

    /**
     * Return a list of lookups for the specified {@link NodeDescriptor} or
     * an empty list if not applicable
     *
     * @param service
     *            a reference to the archetype service
     * @param descriptor
     *            the node descriptor
     * @return List<Lookup>
     * @throws LookupHelperException                      
     */
    @SuppressWarnings("unchecked")
    public static List<Lookup> get(IArchetypeService service, NodeDescriptor descriptor) {
        List<Lookup> lookups = new ArrayList<Lookup>();

        if (descriptor.isLookup()) {
            if (descriptor.containsAssertionType("lookup")) {
                AssertionDescriptor assertion = descriptor.getAssertionDescriptor("lookup");
                
                // This is a remote lookup
                Map<String, NamedProperty> props = assertion.getPropertyMap().getProperties();
                String type = (props.get("type") == null) ? null : 
                    (String)props.get("type").getValue();
                String source = (props.get("source") == null) ? null : 
                    (String)props.get("source").getValue();

                if ((StringUtils.isEmpty(type)) || 
                    (StringUtils.isEmpty(source))) {
                    throw new LookupHelperException(
                            LookupHelperException.ErrorCode.InvalidAssertion,
                            new Object[] { assertion.getName() });
                }
                
                if (type.equals("lookup")) {
                    lookups = LookupHelper.getLookups(service, source, 0, -1);
                } else {
                    // invalid lookup type throw an exception
                    throw new LookupHelperException(
                            LookupHelperException.ErrorCode.InvalidLookupType,
                            new Object[] { type });
                }
            } else if (descriptor.containsAssertionType("lookup.local")){
                // it is a local lookup
                // TODO This is very inefficient..we should cache them in
                // this service
                AssertionDescriptor assertion = descriptor.getAssertionDescriptor("lookup.local");
                PropertyList list = (PropertyList)assertion.getPropertyMap()
                    .getProperties().get("entries");
                for (NamedProperty prop : list.getProperties()) {
                    AssertionProperty aprop = (AssertionProperty)prop;
                    lookups.add(new Lookup(ArchetypeId.LocalLookupId, 
                            aprop.getName(), aprop.getValue()));
                }
            } else {
                throw new LookupHelperException(
                        LookupHelperException.ErrorCode.InvalidLookupAssertion,
                        new Object[] { descriptor.getName() });
            }
        }

        return lookups;
    }

    /**
     * Return a list of {@link Lookup} instances given the specified 
     * {@link NodeDescriptor} and {@link IMObject}. 
     * <p>
     * This method should be used if you want to constrain a lookup
     * search based on a source or target relationship.
     * 
     * @param service
     *            a reference to the archetype service
     * @param descriptor
     *            the node descriptor
     * @param object
     *            the object to use.                         
     * @return List<Lookup>
     * @throws LookupHelperException                      
     */
    @SuppressWarnings("unchecked")
    public static List<Lookup> get(IArchetypeService service, NodeDescriptor descriptor, IMObject object) {
        List<Lookup> lookups = new ArrayList<Lookup>();

        // TODO This needs to be fixed up so that it is more pluggabl
        // Need to define a better interface for the different assertion
        // types.
        if (descriptor.isLookup()) {
            if (descriptor.containsAssertionType("lookup")) {
                // This is a remote lookup
                AssertionDescriptor assertion = descriptor.getAssertionDescriptor("lookup");
                String type = (String) assertion.getPropertyMap().getProperties()
                    .get("type").getValue();

                // if the type and concept properties are not specified
                // then throw an exception
                if (StringUtils.isEmpty(type)) {
                    throw new LookupHelperException(
                            LookupHelperException.ErrorCode.TypeNotSpecified,
                            new Object[] { assertion.getName() });
                }

                if (type.equals("lookup")) {
                    // one way lookup
                    String source = (String) assertion.getPropertyMap().getProperties()
                        .get("source").getValue();
                    if (StringUtils.isEmpty(source)) {
                        throw new LookupHelperException(
                                LookupHelperException.ErrorCode.SourceNotSpecified,
                                new Object[] { assertion.getName(), type });
                    }
                    lookups = LookupHelper.getLookups(service, source, 0, -1);
                } else if (type.equals("targetLookup")) {
                    // constrained lookup specifying the source
                    Map<String, NamedProperty> props = assertion.getPropertyMap().getProperties();
                    String relationship = (props.get("relationship") == null) ? null : 
                        (String)props.get("relationship").getValue();
                    String value = (props.get("value") == null) ? null : 
                        (String)props.get("value").getValue();
                    if ((StringUtils.isEmpty(relationship)) ||
                        (StringUtils.isEmpty(value))) {
                        throw new LookupHelperException(
                                LookupHelperException.ErrorCode.InvalidTargetLookupSpec);
                    }

                    // we need to get the value
                    String srcVal = (String)JXPathContext.newContext(object).getValue(value);
                    String[] source = getArchetypeShortNames(service, relationship, "source");    
                    String[] target = getArchetypeShortNames(service, relationship, "target"); 
                    
                    Lookup lookup = LookupHelper.getLookup(service, source, srcVal);
                    if (lookup != null) {
                        lookups = LookupHelper.getTagetLookups(service, lookup, 
                                target);
                    }
                } else if (type.equals("sourceLookup")) {
                    // constrained lookup specifying the source
                    Map<String, NamedProperty> props = assertion.getPropertyMap().getProperties();
                    String relationship = (props.get("relationship") == null) ? null : 
                        (String)props.get("relationship").getValue();
                    String value = (props.get("value") == null) ? null : 
                        (String)props.get("value").getValue();
                    if ((StringUtils.isEmpty(relationship)) ||
                        (StringUtils.isEmpty(value))) {
                        throw new LookupHelperException(
                                LookupHelperException.ErrorCode.InvalidSourceLookupSpec);
                    }
                    
                    // we need to get the value
                    String tarVal = (String)JXPathContext.newContext(object).getValue(value);
                    String[] source = getArchetypeShortNames(service, relationship, "source");    
                    String[] target = getArchetypeShortNames(service, relationship, "target"); 
                    
                    Lookup lookup = LookupHelper.getLookup(service, target, tarVal);
                    if (lookup != null) {
                        lookups = LookupHelper.getSourceLookups(service, lookup, 
                                source);
                    }
                } else {
                    // invalid lookup type throw an exception
                    throw new LookupHelperException(
                            LookupHelperException.ErrorCode.InvalidLookupType,
                            new Object[] { type });
                }
            } else if (descriptor.containsAssertionType("lookup.local")){
                // it is a local lookup
                // TODO This is very inefficient..we should cache them in
                // this service
                AssertionDescriptor assertion = descriptor.getAssertionDescriptor("lookup.local");
                PropertyList list = (PropertyList)assertion.getPropertyMap()
                    .getProperties().get("entries");
                for (NamedProperty prop : list.getProperties()) {
                    AssertionProperty aprop = (AssertionProperty)prop;
                    lookups.add(new Lookup(ArchetypeId.LocalLookupId, 
                            aprop.getName(), aprop.getValue()));
                }
            } else if (descriptor.containsAssertionType("lookup.assertionType")){
                // retrieve all the assertionTypes from the archetype service
                // we need to 
                List<AssertionTypeDescriptor> adescs = 
                    service.getAssertionTypeDescriptors();
                for (AssertionTypeDescriptor adesc : adescs) {
                    lookups.add(new Lookup(ArchetypeId.LocalLookupId, 
                            adesc.getName(), adesc.getName()));
                }
            } else {
                throw new LookupHelperException(
                        LookupHelperException.ErrorCode.InvalidLookupAssertion,
                        new Object[] { descriptor.getName() });
            }
        }

        return lookups;
    }

    /**
     * Helper method that returns a single lookup with the specified
     * archetype short name and value
     * 
     * @param service
     *            the archetype sevice
     * @param shortName
     *            the archetype short name
     * @param value
     *            the value of the node                        
     * @return IPage<Lookup>       
     * @throws ArchetypeServiceException
     *            if the request cannot complete     
     */
    @SuppressWarnings("unchecked")
    public static Lookup getLookup(IArchetypeService service, String shortName,
            String value) {
        Lookup lookup = null;
        
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, true)
            .add(new NodeConstraint("name", value))
            .setFirstRow(0)
            .setNumOfRows(1);
        IPage<IMObject> page = service.get(query);
        if (page.getTotalNumOfRows() > 0) {
            lookup = (Lookup)page.getRows().iterator().next();
        }

        // warn if there is more than one lookup with the same value
        if (page.getTotalNumOfRows() > 1) {
            logger.warn("There are " + page.getTotalNumOfRows() + 
                    "lookups with shortName: " + shortName +
                    " and value: " + value);
        }
        
        return lookup;
    }
    
    /**
     * Helper method that returns a single lookup with the specified
     * archetype short names and value
     * 
     * @param service
     *            the archetype sevice
     * @param shortNames
     *            the archetype short names to search on
     * @param value
     *            the value of the node                        
     * @return IPage<Lookup>       
     * @throws ArchetypeServiceException
     *            if the request cannot complete     
     */
    @SuppressWarnings("unchecked")
    public static Lookup getLookup(IArchetypeService service, String[] shortNames,
            String value) {
        Lookup lookup = null;
        
        ArchetypeQuery query = new ArchetypeQuery(shortNames, false, true)
            .add(new NodeConstraint("name", value))
            .setFirstRow(0)
            .setNumOfRows(1);
        IPage<IMObject> page = service.get(query);
        if (page.getTotalNumOfRows() > 0) {
            lookup = (Lookup)page.getRows().iterator().next();
        }

        // warn if there is more than one lookup with the same value
        if (page.getTotalNumOfRows() > 1) {
            logger.warn("There are " + page.getTotalNumOfRows() + 
                    "lookups with shortNames: " + shortNames +
                    " and value: " + value);
        }
        
        return lookup;
    }
    
    /**
     * Helper method that returns a specific lookup given a short name and a
     * value
     * 
     * @param service
     *            the archetype sevice
     * @param shortName
     *            the archetype short name
     * @param value
     *            the value of the          
     * @param firstRow
     *            the first row to retriev
     * @param numOfRows
     *            the num of rows to retieve
     * @return IPage<Lookup>       
     * @throws ArchetypeServiceException
     *            if the request cannot complete     
     */
    @SuppressWarnings("unchecked")
    public static List<Lookup> getLookups(IArchetypeService service, String shortName,
            int firstRow, int numOfRows) {
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, true)
            .setFirstRow(firstRow)
            .setNumOfRows(numOfRows);
        
        return new ArrayList<Lookup>((List)service.get(query).getRows());
    }
    
    /**
     * Helper method to return a {@link Page} of target {@link Lookup} instances
     * give a reference source {@link Lookup} 
     * <p>
     * Note this will work if the archetype names in your system conform to the 
     * strcuture indicated below. 
     * <p>
     * All lookups have an entity name of 'lookup' and all lookup relationships 
     * have a short name of 'lookuprel.common'
     * 
     * @param service
     *            a reference to the archetype service
     * @param source
     *            the source lookup
     * @param target
     *            the archetype shortNames of the target
     * @return List<Lookup>
     *            a list of lookup objects
     * @throws LookupHelperException
     *            if the request cannot complete                                               
     */
    @SuppressWarnings("unchecked")
    public static List<Lookup> getTagetLookups(IArchetypeService service, 
            Lookup source, String[] target) {
        // now we need to look for the relationship archetype
        ArchetypeQuery query = new ArchetypeQuery(new ArchetypeShortNameConstraint(
                target, false, false))
            .add(new CollectionNodeConstraint("target", false)
                    .add(new ObjectRefNodeConstraint("source", source.getObjectReference())))
            .add(new NodeSortConstraint("name", true))
            .setActiveOnly(true);

        return new ArrayList<Lookup>((List)service.get(query).getRows());
    }

    /**
     * Helper class to return a {@link Page} of source {@link Lookup} instances
     * give a reference target{@link Lookup} 
     * <p>
     * Note this will work if the archetype names in your system conform to the 
     * strcuture indicated below. 
     * <p>
     * All lookups have an entity name of 'lookup' and all lookup relationships 
     * have a short name of 'lookuprel.common'
     * 
     * @param service
     *            a reference to the archetype service
     * @param target
     *            the target lookup
     * @param source
     *            the list of shortnames for the source
     * @return List<Lookup>
     *            a list of lookup objects
     * @throws ArchetypeServiceException
     *            if the request cannot complete                                               
     */
    @SuppressWarnings("unchecked")
    public static List<Lookup> getSourceLookups(IArchetypeService service, 
            Lookup target, String[] source) {
        ArchetypeQuery query = new ArchetypeQuery(new ArchetypeShortNameConstraint(
                source, false, false))
            .add(new CollectionNodeConstraint("source", false)
                    .add(new ObjectRefNodeConstraint("target", target.getObjectReference())))
            .add(new NodeSortConstraint("name", true))
            .setActiveOnly(true);

        return new ArrayList<Lookup>((List)service.get(query).getRows());
    }
    
    /**
     * Return a list of short names given the relationship archetype and a node 
     * name If the node does not exist for the specified archetype then raise an
     * exception. In addition, if there are no archetype range assertions defined
     * for the node then also throw an exception. 
     * 
     * @param service
     *            the archetype service
     * @param relationship
     *            the relationship archetype           
     * @param node
     *            the node name
     * @param String[]
     *            an archetype of short names                             
     * @throws LookupHelperException            
     */
    private static String[] getArchetypeShortNames(IArchetypeService service, 
            String relationship, String node) {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(relationship);
        if (adesc == null) {
            throw new LookupHelperException(
                    LookupHelperException.ErrorCode.LookupRelationshipArchetypeNotDefined,
                    new Object[] {relationship});
        }
        NodeDescriptor ndesc = adesc.getNodeDescriptor(node);
        if (ndesc == null) {
            throw new LookupHelperException(
                    LookupHelperException.ErrorCode.InvalidLookupRelationshipArchetypeDefinition,
                    new Object[] {relationship, node});
        }
        
        String[] types = ndesc.getArchetypeRange();
        if (types.length == 0) {
            throw new LookupHelperException(
                    LookupHelperException.ErrorCode.NoArchetypeRangeInLookupRelationship,
                    new Object[] {relationship, node});
        }

        return types;
    }
}
