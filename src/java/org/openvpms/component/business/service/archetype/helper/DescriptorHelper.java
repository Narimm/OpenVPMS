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

package org.openvpms.component.business.service.archetype.helper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;


/**
 * Helper class for working with {@link ArchetypeDescriptor} and {@link
 * NodeDescriptor}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-06-27 04:04:11Z $
 */
public final class DescriptorHelper {

    /**
     * Returns an archetype descriptor, given its shortname.
     *
     * @param shortName the shortname
     * @return the descriptor corresponding to <code>shortName</code>, or
     *         <code>null</code> if none exists
     * @throws ArchetypeServiceException for any error
     */
    public static ArchetypeDescriptor getArchetypeDescriptor(String shortName) {
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        return service.getArchetypeDescriptor(shortName);
    }

    /**
     * Returns an archetype descriptor, given a reference.
     *
     * @param reference the object reference.
     * @return the descriptor corresponding to <code>reference</code>, or
     *         <code>null</code> if none exists
     * @throws ArchetypeServiceException for any error
     */
    public static ArchetypeDescriptor getArchetypeDescriptor(
            IMObjectReference reference) {
        ArchetypeId id = reference.getArchetypeId();
        return getArchetypeDescriptor(id.getShortName());
    }

    /**
     * Returns the archetype descriptor for the specified object.
     *
     * @param object the object
     * @return the archetype descriptor corresponding to <code>object</code>
     * @throws ArchetypeServiceException for any error
     */
    public static ArchetypeDescriptor getArchetypeDescriptor(IMObject object) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        ArchetypeDescriptor descriptor;
        ArchetypeId archId = object.getArchetypeId();

        //TODO This is a work around until we resolve the current
        // problem with archetyping and archetype.
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
        return descriptor;
    }

    /**
     * Returns the archetype descriptors for an archetype range.
     *
     * @param range the archetype range. May contain wildcards
     * @return a list of archetype descriptors
     * @throws ArchetypeServiceException for any error
     */
    public static List<ArchetypeDescriptor> getArchetypeDescriptors(
            String[] range) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        List<ArchetypeDescriptor> result = new ArrayList<ArchetypeDescriptor>();
        for (String shortName : range) {
            result.addAll(service.getArchetypeDescriptors(shortName));
        }
        return result;
    }

    /**
     * Returns archetype short names from a descriptor.
     * This expands any wildcards. If the {@link NodeDescriptor#getFilter}
     * is non-null, matching shortnames are
     * returned, otherwise matching short names from
     * {@link NodeDescriptor#getArchetypeRange()} are returned.
     *
     * @param descriptor the node descriptor
     * @return a list of short names
     * @throws ArchetypeServiceException for any error
     */
    public static String[] getShortNames(NodeDescriptor descriptor) {
        String filter = descriptor.getFilter();
        String[] names;
        if (!StringUtils.isEmpty(filter)) {
            names = getShortNames(filter, false);
        } else {
            names = getShortNames(descriptor.getArchetypeRange(), false);
        }
        return names;
    }

    /**
     * Returns primary archetype short names matching the specified criteria.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     * @return a list of short names matching the criteria
     * @throws ArchetypeServiceException for any error
     */
    public static String[] getShortNames(String refModelName,
                                         String entityName,
                                         String conceptName) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        List<String> names = service.getArchetypeShortNames(refModelName,
                                                            entityName,
                                                            conceptName,
                                                            true);
        return names.toArray(new String[0]);
    }

    /**
     * Returns primary archetype short names matching the specified criteria.
     *
     * @param shortName the short name. May contain wildcards
     * @return a list of short names matching the criteria
     * @throws ArchetypeServiceException for any error
     */
    public static String[] getShortNames(String shortName) {
        return getShortNames(new String[]{shortName}, true);
    }

    /**
     * Returns archetype short names matching the specified criteria.
     *
     * @param shortName   the short name. May contain wildcards
     * @param primaryOnly if <code>true</code> only include primary archetypes
     * @return a list of short names matching the criteria
     * @throws ArchetypeServiceException for any error
     */
    public static String[] getShortNames(String shortName,
                                         boolean primaryOnly) {
        return getShortNames(new String[]{shortName}, primaryOnly);
    }

    /**
     * Returns primary archetype short names matching the specified criteria.
     *
     * @param shortNames the short names. May contain wildcards
     * @return a list of short names matching the criteria
     * @throws ArchetypeServiceException for any error
     */
    public static String[] getShortNames(String[] shortNames) {
        return getShortNames(shortNames, true);
    }

    /**
     * Returns archetype short names matching the specified criteria.
     *
     * @param shortNames  the shortNames. May contain wildcards
     * @param primaryOnly if <code>true</code> only include primary archetypes
     * @return a list of short names matching the criteria
     * @throws ArchetypeServiceException for any error
     */
    public static String[] getShortNames(String[] shortNames,
                                         boolean primaryOnly) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        Set<String> result = new LinkedHashSet<String>();
        for (String shortName : shortNames) {
            List<String> matches = service.getArchetypeShortNames(
                    shortName, primaryOnly);
            result.addAll(matches);
        }
        return result.toArray(new String[0]);
    }

    /**
     * Returns the display name for an archetype.
     *
     * @param shortName the archetype short name
     * @return the archetype display name, or <code>null</code> if none exists
     * @throws ArchetypeServiceException for any error
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
     * @throws ArchetypeServiceException for any error
     */
    public static String getDisplayName(IMObject object) {
        ArchetypeDescriptor descriptor = getArchetypeDescriptor(object);
        return (descriptor != null) ? descriptor.getDisplayName() : null;
    }

    /**
     * Returns the display name for a node.
     *
     * @param object the object
     * @param node   the node
     * @return a display name for the node, or <code>null</code> if none eixsts
     * @throws ArchetypeServiceException for any error
     */
    public static String getDisplayName(IMObject object, String node) {
        String result = null;
        ArchetypeDescriptor archetype = getArchetypeDescriptor(object);
        if (archetype != null) {
            NodeDescriptor descriptor = archetype.getNodeDescriptor(node);
            if (descriptor != null) {
                result = descriptor.getDisplayName();
            }
        }
        return result;
    }

}
