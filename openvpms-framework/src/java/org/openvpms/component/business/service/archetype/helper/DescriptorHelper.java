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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


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
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        return getArchetypeDescriptor(shortName, service);
    }

    /**
     * Returns an archetype descriptor, given its shortname.
     *
     * @param shortName the shortname
     * @param service   the archetype service
     * @return the descriptor corresponding to <code>shortName</code>, or
     *         <code>null</code> if none exists
     * @throws ArchetypeServiceException for any error
     */
    public static ArchetypeDescriptor getArchetypeDescriptor(
            String shortName, IArchetypeService service) {
        return service.getArchetypeDescriptor(shortName);
    }

    /**
     * Returns an archetype descriptor, given a reference.
     *
     * @param reference the object reference
     * @return the descriptor corresponding to <code>reference</code>, or
     *         <code>null</code> if none exists
     * @throws ArchetypeServiceException for any error
     */
    public static ArchetypeDescriptor getArchetypeDescriptor(
            IMObjectReference reference) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        return getArchetypeDescriptor(reference, service);
    }

    /**
     * Returns an archetype descriptor, given a reference.
     *
     * @param reference the object reference
     * @param service   the archetype service
     * @return the descriptor corresponding to <code>reference</code>, or
     *         <code>null</code> if none exists
     * @throws ArchetypeServiceException for any error
     */
    public static ArchetypeDescriptor getArchetypeDescriptor(
            IMObjectReference reference, IArchetypeService service) {
        ArchetypeId id = reference.getArchetypeId();
        return getArchetypeDescriptor(id.getShortName(), service);
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
        return getArchetypeDescriptor(object, service);
    }

    /**
     * Returns the archetype descriptor for the specified object.
     *
     * @param object  the object
     * @param service the archetype service
     * @return the archetype descriptor corresponding to <code>object</code>
     * @throws ArchetypeServiceException for any error
     */
    public static ArchetypeDescriptor getArchetypeDescriptor(
            IMObject object, IArchetypeService service) {
        ArchetypeDescriptor descriptor;
        ArchetypeId archId = object.getArchetypeId();

        //TODO This is a work around until we resolve the current
        // problem with archetyping and archetype.
        if (object instanceof AssertionDescriptor) {
            AssertionTypeDescriptor atDesc = service.getAssertionTypeDescriptor(object.getName());
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
     * Returns the archetype descriptors for an archetype short name.
     *
     * @param shortName the archetype short name. May contain wildcards
     * @return a list of archetype descriptors
     * @throws ArchetypeServiceException for any error
     */
    public static List<ArchetypeDescriptor> getArchetypeDescriptors(String shortName) {
        return getArchetypeDescriptors(new String[]{shortName});
    }

    /**
     * Returns the archetype descriptors for an archetype short name.
     *
     * @param shortName the archetype short name. May contain wildcards
     * @param service   the archetype service
     * @return a list of archetype descriptors
     * @throws ArchetypeServiceException for any error
     */
    public static List<ArchetypeDescriptor> getArchetypeDescriptors(String shortName, IArchetypeService service) {
        return getArchetypeDescriptors(new String[]{shortName}, service);
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
        return getArchetypeDescriptors(
                range, ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Returns the archetype descriptors for an archetype range.
     *
     * @param range   the archetype range. May contain wildcards
     * @param service the archetype service
     * @return a list of archetype descriptors
     * @throws ArchetypeServiceException for any error
     */
    public static List<ArchetypeDescriptor> getArchetypeDescriptors(
            String[] range, IArchetypeService service) {
        List<ArchetypeDescriptor> result = new ArrayList<ArchetypeDescriptor>();
        for (String shortName : range) {
            result.addAll(service.getArchetypeDescriptors(shortName));
        }
        return result;
    }

    /**
     * Returns archetype short names from a descriptor.
     * <p/>
     * This uses short names from {@link NodeDescriptor#getArchetypeRange()}, or
     * if none are present, those from {@link NodeDescriptor#getFilter()}.
     * <p/>
     * Any wildcards are expanded.
     *
     * @param descriptor the node descriptor
     * @return a list of short names
     * @throws ArchetypeServiceException for any error
     */
    public static String[] getShortNames(NodeDescriptor descriptor) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        return getShortNames(descriptor, service);
    }

    /**
     * Returns archetype short names from a descriptor.
     * <p/>
     * This uses short names from {@link NodeDescriptor#getArchetypeRange()}, or
     * if none are present, those from {@link NodeDescriptor#getFilter()}.
     * <p/>
     * Any wildcards are expanded.
     *
     * @param descriptor the node descriptor
     * @param service    the archetype service
     * @return a list of short names
     * @throws ArchetypeServiceException for any error
     */
    public static String[] getShortNames(NodeDescriptor descriptor,
                                         IArchetypeService service) {
        String[] names = getShortNames(descriptor.getArchetypeRange(), false,
                                       service);
        if (names.length == 0 && !StringUtils.isEmpty(descriptor.getFilter())) {
            names = getShortNames(descriptor.getFilter(), false, service);
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
     * @deprecated see {@link #getShortNames(String, String)}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public static String[] getShortNames(String refModelName,
                                         String entityName,
                                         String conceptName) {
        return getShortNames(refModelName, entityName, conceptName,
                             ArchetypeServiceHelper.getArchetypeService());
    }


    /**
     * Returns primary archetype short names matching the specified criteria.
     *
     * @param entityName  the archetype entity name
     * @param conceptName the archetype concept name
     * @return a list of short names matching the criteria
     * @throws ArchetypeServiceException for any error
     */
    public static String[] getShortNames(String entityName,
                                         String conceptName) {
        return getShortNames(entityName, conceptName,
                             ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Returns primary archetype short names matching the specified criteria.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     * @param service      the archetype service
     * @return a list of short names matching the criteria
     * @throws ArchetypeServiceException for any error
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public static String[] getShortNames(String refModelName,
                                         String entityName,
                                         String conceptName,
                                         IArchetypeService service) {
        List<String> names = service.getArchetypeShortNames(refModelName,
                                                            entityName,
                                                            conceptName,
                                                            true);
        return names.toArray(new String[names.size()]);
    }

    /**
     * Returns primary archetype short names matching the specified criteria.
     *
     * @param entityName  the archetype entity name
     * @param conceptName the archetype concept name
     * @param service     the archetype service
     * @return a list of short names matching the criteria
     * @throws ArchetypeServiceException for any error
     */
    public static String[] getShortNames(String entityName, String conceptName,
                                         IArchetypeService service) {
        List<String> names = service.getArchetypeShortNames(
                entityName, conceptName, true);
        return names.toArray(new String[names.size()]);
    }

    /**
     * Returns primary archetype short names matching the specified criteria.
     *
     * @param shortName the short name. May contain wildcards
     * @return a list of short names matching the criteria
     * @throws ArchetypeServiceException for any error
     */
    public static String[] getShortNames(String shortName) {
        return getShortNames(shortName,
                             ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Returns primary archetype short names matching the specified criteria.
     *
     * @param shortName the short name. May contain wildcards
     * @param service   the archetype service
     * @return a list of short names matching the criteria
     * @throws ArchetypeServiceException for any error
     */
    public static String[] getShortNames(String shortName,
                                         IArchetypeService service) {
        return getShortNames(shortName, true, service);
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
        return getShortNames(shortName, primaryOnly,
                             ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Returns archetype short names matching the specified criteria.
     *
     * @param shortName   the short name. May contain wildcards
     * @param primaryOnly if <code>true</code> only include primary archetypes
     * @param service     the archetype service
     * @return a list of short names matching the criteria
     * @throws ArchetypeServiceException for any error
     */
    public static String[] getShortNames(String shortName,
                                         boolean primaryOnly,
                                         IArchetypeService service) {
        return getShortNames(new String[]{shortName}, primaryOnly, service);
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
        return getShortNames(shortNames, primaryOnly, service);
    }

    /**
     * Returns archetype short names matching the specified criteria.
     *
     * @param shortNames  the shortNames. May contain wildcards
     * @param primaryOnly if <code>true</code> only include primary archetypes
     * @param service     the archetype service
     * @return a list of short names matching the criteria
     * @throws ArchetypeServiceException for any error
     */
    public static String[] getShortNames(String[] shortNames,
                                         boolean primaryOnly,
                                         IArchetypeService service) {
        Set<String> result = new LinkedHashSet<String>();
        for (String shortName : shortNames) {
            List<String> matches = service.getArchetypeShortNames(
                    shortName, primaryOnly);
            result.addAll(matches);
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Returns archetype short names from a node for the matching archetype where
     * the node is present. This expands any wildcards.
     * <p/>
     * If the {@link NodeDescriptor#getFilter} is non-null, matching short names
     * are returned, otherwise matching short names from
     * {@link NodeDescriptor#getArchetypeRange()} are returned.
     *
     * @param shortName the archetype short name. May contain wildcards
     * @param node      the node name
     * @return a list of short names, with any wildcards expanded
     * @throws ArchetypeServiceException for any error
     */
    public static String[] getNodeShortNames(String shortName, String node) {
        return getNodeShortNames(shortName, node, ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Returns archetype short names from a node for each archetype where
     * the node is present. This expands any wildcards.
     * <p/>
     * If the {@link NodeDescriptor#getFilter} is non-null, matching shortnames
     * are returned, otherwise matching short names from
     * {@link NodeDescriptor#getArchetypeRange()} are returned.
     *
     * @param shortNames the archetype short names. May contain wildcards
     * @param node       the node name
     * @return a list of short names, with any wildcards expanded
     * @throws ArchetypeServiceException for any error
     */
    public static String[] getNodeShortNames(String[] shortNames, String node) {
        return getNodeShortNames(shortNames, node, ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Returns archetype short names from a node for each archetype matching <tt>shortName</tt> where
     * the specified node is present. This expands any wildcards.
     * <p/>
     * This uses short names from {@link NodeDescriptor#getArchetypeRange()}, or
     * if none are present, those from {@link NodeDescriptor#getFilter()}.
     *
     * @param shortName the archetype short name. May contain wildcards
     * @param node      the node name
     * @param service   the archetype service
     * @return a list of short names
     * @throws ArchetypeServiceException for any error
     */
    public static String[] getNodeShortNames(String shortName, String node,
                                             IArchetypeService service) {
        return getNodeShortNames(new String[]{shortName}, node, service);
    }

    /**
     * Returns archetype short names from a node for each archetype where
     * the specified node is present. This expands any wildcards.
     * <p/>
     * This uses short names from {@link NodeDescriptor#getArchetypeRange()}, or
     * if none are present, those from {@link NodeDescriptor#getFilter()}.
     *
     * @param shortNames the archetype short names. May contain wildcards
     * @param node       the node name
     * @param service    the archetype service
     * @return a list of short names
     * @throws ArchetypeServiceException for any error
     */
    public static String[] getNodeShortNames(String[] shortNames, String node,
                                             IArchetypeService service) {
        Set<String> matches = new LinkedHashSet<String>();
        String[] expanded = getShortNames(shortNames, false, service);
        for (String shortName : expanded) {
            ArchetypeDescriptor archetype
                    = getArchetypeDescriptor(shortName, service);
            if (archetype != null) {
                NodeDescriptor desc = archetype.getNodeDescriptor(node);
                if (desc != null) {
                    matches.addAll(Arrays.asList(getShortNames(desc, service)));
                }
            }
        }
        return matches.toArray(new String[matches.size()]);
    }

    /**
     * Returns the display name for an archetype.
     *
     * @param shortName the archetype short name
     * @return the archetype display name, or <code>null</code> if none exists
     * @throws ArchetypeServiceException for any error
     */
    public static String getDisplayName(String shortName) {
        return getDisplayName(shortName,
                              ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Returns the display name for an archetype.
     *
     * @param shortName the archetype short name
     * @param service   the archetype service
     * @return the archetype display name, or <code>null</code> if none exists
     * @throws ArchetypeServiceException for any error
     */
    public static String getDisplayName(String shortName,
                                        IArchetypeService service) {
        ArchetypeDescriptor descriptor = getArchetypeDescriptor(shortName,
                                                                service);
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
        return getDisplayName(object,
                              ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Returns the display name for an object.
     *
     * @param object  the object
     * @param service the archeype service
     * @return a display name for the object, or <code>null</code> if none
     *         exists
     * @throws ArchetypeServiceException for any error
     */
    public static String getDisplayName(IMObject object,
                                        IArchetypeService service) {
        ArchetypeDescriptor descriptor = getArchetypeDescriptor(object,
                                                                service);
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
        return getDisplayName(object, node,
                              ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Returns the display name for a node.
     *
     * @param object  the object
     * @param node    the node
     * @param service the archetype service
     * @return a display name for the node, or <code>null</code> if none eixsts
     * @throws ArchetypeServiceException for any error
     */
    public static String getDisplayName(IMObject object, String node,
                                        IArchetypeService service) {
        return getDisplayName(object.getArchetypeId().getShortName(), node,
                              service);
    }

    /**
     * Returns the display name for a node.
     *
     * @param shortName the archetype short name
     * @param node      the node
     * @return a display name for the node, or <code>null</code> if none eixsts
     * @throws ArchetypeServiceException for any error
     */
    public static String getDisplayName(String shortName, String node) {
        return getDisplayName(shortName, node, ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Returns the display name for a node.
     *
     * @param shortName the archetype short name
     * @param node      the node
     * @param service   the archetype service
     * @return a display name for the node, or <code>null</code> if none eixsts
     * @throws ArchetypeServiceException for any error
     */
    public static String getDisplayName(String shortName, String node,
                                        IArchetypeService service) {
        String result = null;
        ArchetypeDescriptor archetype
                = getArchetypeDescriptor(shortName, service);
        if (archetype != null) {
            NodeDescriptor descriptor = archetype.getNodeDescriptor(node);
            if (descriptor != null) {
                result = descriptor.getDisplayName();
            }
        }
        return result;
    }

    /**
     * Returns node names common to a set of archetypes.
     *
     * @param shortNames the archetype short names. May contain wildcards.
     * @param service    the archetype service
     * @return node names common to the archetypes
     */
    public static String[] getCommonNodeNames(String shortNames, IArchetypeService service) {
        return getCommonNodeNames(shortNames, null, service);
    }

    /**
     * Returns node names common to a set of archetypes.
     *
     * @param shortNames the archetype short names. May contain wildcards.
     * @param service    the archetype service
     * @return node names common to the archetypes
     */
    public static String[] getCommonNodeNames(String[] shortNames, IArchetypeService service) {
        return getCommonNodeNames(shortNames, null, service);
    }

    /**
     * Returns node names common to a set of archetypes.
     *
     * @param shortNames the archetype short names. May contain wildcards.
     * @param nodes      node names to check. If <tt>null</tt>, all nodes common to the archetypes will be returned.
     * @param service    the archetype service
     * @return node names common to the archetypes
     */
    public static String[] getCommonNodeNames(String shortNames, String[] nodes, IArchetypeService service) {
        return getCommonNodeNames(new String[]{shortNames}, nodes, service);
    }

    /**
     * Returns node names common to a set of archetypes.
     *
     * @param shortNames the archetype short names. May contain wildcards.
     * @param nodes      node names to check. If <tt>null</tt>, all nodes common to the archetypes will be returned.
     * @param service    the archetype service
     * @return node names common to the archetypes
     */
    public static String[] getCommonNodeNames(String[] shortNames, String[] nodes, IArchetypeService service) {
        Set<String> result = new LinkedHashSet<String>();

        boolean init = false;
        if (nodes != null && nodes.length != 0) {
            result.addAll(Arrays.asList(nodes));
        } else {
            init = true;
        }
        for (ArchetypeDescriptor descriptor : getArchetypeDescriptors(shortNames, service)) {
            if (init) {
                for (NodeDescriptor node : descriptor.getAllNodeDescriptors()) {
                    result.add(node.getName());
                }
                init = false;
            } else {
                for (String node : new ArrayList<String>(result)) {
                    if (descriptor.getNodeDescriptor(node) == null) {
                        result.remove(node);
                    }
                }
            }
        }
        return result.toArray(new String[result.size()]);
    }

}

