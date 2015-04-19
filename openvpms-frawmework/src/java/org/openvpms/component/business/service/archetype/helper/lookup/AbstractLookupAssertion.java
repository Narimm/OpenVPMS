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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.helper.lookup;

import org.apache.commons.jxpath.JXPathContext;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.LookupHelperException;
import static org.openvpms.component.business.service.archetype.helper.LookupHelperException.ErrorCode.*;
import org.openvpms.component.business.service.lookup.ILookupService;

import java.util.Collection;
import java.util.List;


/**
 * Abstract implementation of the {@link LookupAssertion} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
abstract class AbstractLookupAssertion implements LookupAssertion {

    /**
     * The assertion descriptor.
     */
    private final AssertionDescriptor descriptor;

    /**
     * The assertion type.
     */
    private final String type;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookupService;


    /**
     * Constructs a new <code>AbstractLookupAssertion</code>.
     *
     * @param descriptor the assertion descriptor
     * @param type       the assertion type
     * @param service    the archetype service
     */
    public AbstractLookupAssertion(AssertionDescriptor descriptor,
                                   String type, IArchetypeService service,
                                   ILookupService lookupService) {
        this.descriptor = descriptor;
        this.type = type;
        this.service = service;
        this.lookupService = lookupService;
    }

    /**
     * Returns the assertion descriptor.
     *
     * @return the assertion descriptor
     */
    public AssertionDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Returns the type of the assertion.
     *
     * @return the type of the assertion
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the lookups for this assertion.
     * This implementation throws {@link LookupHelperException}.
     *
     * @return a list of lookups
     * @throws LookupHelperException if invoked
     */
    public List<Lookup> getLookups() {
        throw new LookupHelperException(InvalidLookupAssertion,
                                        new Object[]{descriptor.getName()});
    }

    /**
     * Returns the lookups for this assertion.
     * This implementation delegates to
     * {@link LookupAssertion#getLookups()}.
     *
     * @param context the context
     * @return a list of lookups
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Collection<Lookup> getLookups(IMObject context) {
        return getLookups();
    }

    /**
     * Returns the lookup with the specified code.
     * This implementation throws {@link LookupHelperException}.
     *
     * @param code the lookup code
     * @return the lookup matching <code>code</code>, or <code>null</code> if
     *         none is found
     * @throws LookupHelperException if invoked
     */
    public Lookup getLookup(String code) {
        throw new LookupHelperException(InvalidLookupAssertion,
                                        new Object[]{descriptor.getName()});
    }

    /**
     * Returns the lookup with the specified code.
     * This implementation delegates to
     * {@link LookupAssertion#getLookup(String)}.
     *
     * @param context the context
     * @return the lookup matching <code>code</code>, or <code>null</code> if
     *         none is found
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if this method is unsupported by the
     *                                   lookup type
     */
    public Lookup getLookup(IMObject context, String code) {
        return getLookup(code);
    }

    /**
     * Returns the name of the lookup with the specified code.
     * This implementation uses to {@link #getLookup(String)}.
     *
     * @param code the lookup code
     * @return the name of the lookup matching <code>code</code>, or
     *         <code>null</code> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if this method is unsupported by the
     *                                   lookup type
     */
    public String getName(String code) {
        Lookup lookup = getLookup(code);
        return (lookup != null) ? lookup.getName() : null;
    }

    /**
     * Returns the name of the lookup with the specified code.
     * This method delegates to {@link #getName(String)}.
     *
     * @param context the context. May be <code>null</code>
     * @return the name of the lookup matching <code>code</code>, or
     *         <code>null</code> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if this method is unsupported by the
     *                                   lookup type
     */
    public String getName(IMObject context, String code) {
        return getName(code);
    }

    /**
     * Returns the default lookup.
     *
     * @return the default lookup or <tt>null</tt> if there is no default
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Lookup getDefault() {
        for (Lookup lookup : getLookups()) {
            if (lookup.isDefaultLookup()) {
                return lookup;
            }
        }
        return null;
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getArchetypeService() {
        return service;
    }

    /**
     * Returns the lookup service.
     *
     * @return the lookup service
     */
    protected ILookupService getLookupService() {
        return lookupService;
    }

    /**
     * Returns the value of the named property from the assertion descriptor.
     *
     * @param name the property name
     * @return the property value, or <code>null</code> if it doesn't exist
     */
    protected String getProperty(String name) {
        return LookupAssertionHelper.getValue(descriptor, name);
    }

    /**
     * Returns the lookup at the end of a relationship.
     * The lookup code is derived from the supplied context and node path.
     *
     * @param context               the context object
     * @param nodePath              the jxpath to the lookup code
     * @param relationshipShortName the lookup relationship short name
     * @param relationshipNode      the lookup relationship node
     * @return the corresponding lookup, or <code>null</code> if none is found
     */
    protected Lookup getLookup(IMObject context, String nodePath,
                               String relationshipShortName,
                               String relationshipNode) {
        Lookup result = null;
        String code = getPathValue(context, nodePath);
        String[] shortNames = getArchetypeShortNames(relationshipShortName,
                                                     relationshipNode);
        for (String shortName : shortNames) {
            result = lookupService.getLookup(shortName, code);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    /**
     * Returns a list of short names given the relationship archetype and a node
     * name.
     *
     * @param relationship the relationship archetype
     * @param node         the node name
     * @return an array of archetype short names
     * @throws LookupHelperException if the relationship or node is doesn't
     *                               exist, or the node descriptor doesn't
     *                               specify an archetype range
     */
    protected String[] getArchetypeShortNames(String relationship,
                                              String node) {
        ArchetypeDescriptor archetype = service.getArchetypeDescriptor(
                relationship);
        if (archetype == null) {
            throw new LookupHelperException(
                    LookupRelationshipArchetypeNotDefined,
                    new Object[]{relationship});
        }
        NodeDescriptor ndesc = archetype.getNodeDescriptor(node);
        if (ndesc == null) {
            throw new LookupHelperException(
                    InvalidLookupRelationshipArchetypeDefinition,
                    new Object[]{relationship, node});
        }

        String[] types = ndesc.getArchetypeRange();
        if (types.length == 0) {
            throw new LookupHelperException(
                    NoArchetypeRangeInLookupRelationship,
                    new Object[]{relationship, node});
        }

        return types;
    }

    /**
     * Evaluates a jxpath to return its value.
     *
     * @param context the context object
     * @param path    the jxpath
     * @return the value of the jxpath
     */
    protected String getPathValue(IMObject context, String path) {
        return (String) JXPathContext.newContext(context).getValue(path);
    }

}
