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

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.property.AssertionProperty;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyList;
import org.openvpms.component.business.domain.im.lookup.Lookup;

/**
 * This is a helper class for retrieving lookups reference data.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LookupHelper  {
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
                    lookups = ArchetypeQueryHelper.getLookups(service, source, 0, -1);
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
                    lookups = ArchetypeQueryHelper.getLookups(service, source, 0, -1);
                } else if (type.equals("targetLookup")) {
                    // constrained lookup specifying the source
                    Map<String, NamedProperty> props = assertion.getPropertyMap().getProperties();
                    String source = (props.get("source") == null) ? null : 
                        (String)props.get("source").getValue();
                    String target = (props.get("target") == null) ? null : 
                        (String)props.get("target").getValue();
                    String value = (props.get("value") == null) ? null : 
                        (String)props.get("value").getValue();
                    if ((StringUtils.isEmpty(source)) ||
                        (StringUtils.isEmpty(target)) ||
                        (StringUtils.isEmpty(value))) {
                        throw new LookupHelperException(
                                LookupHelperException.ErrorCode.InvalidTargetLookupSpec);
                    }

                    // we need to get the value
                    String srcVal = (String)JXPathContext.newContext(object).getValue(value);
                    Lookup lookup = ArchetypeQueryHelper.getLookup(service, source, srcVal);
                    if (lookup != null) {
                        lookups = new ArrayList<Lookup>((List)ArchetypeQueryHelper
                            .getTagetLookups(service, lookup, target, 0, -1).getRows());
                    }
                } else if (type.equals("sourceLookup")) {
                    // constrained lookup specifying the source
                    Map<String, NamedProperty> props = assertion.getPropertyMap().getProperties();
                    String source = (props.get("source") == null) ? null : 
                        (String)props.get("source").getValue();
                    String target = (props.get("target") == null) ? null : 
                        (String)props.get("target").getValue();
                    String value = (props.get("value") == null) ? null : 
                        (String)props.get("value").getValue();
                    if ((StringUtils.isEmpty(source)) ||
                        (StringUtils.isEmpty(target)) ||
                        (StringUtils.isEmpty(value))) {
                        throw new LookupHelperException(
                                LookupHelperException.ErrorCode.InvalidSourceLookupSpec);
                    }
                    
                    // we need to get the value
                    String tarVal = (String)JXPathContext.newContext(object).getValue(value);
                    Lookup lookup = ArchetypeQueryHelper.getLookup(service, target, tarVal);
                    if (lookup != null) {
                        lookups = new ArrayList<Lookup>((List)ArchetypeQueryHelper
                            .getSourceLookups(service, lookup, source, 0, -1).getRows());
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
}
