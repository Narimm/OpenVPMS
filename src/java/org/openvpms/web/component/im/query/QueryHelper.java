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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.IdConstraint;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;


/**
 * Query helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class QueryHelper {

    /**
     * Determines if a node is a participation node.
     *
     * @param descriptor the node descriptor
     */
    public static boolean isParticipationNode(NodeDescriptor descriptor) {
        return descriptor.isCollection() && "/participations".equals(
                descriptor.getPath());
    }

    /**
     * Helper to return the descriptor referred to by a constraint.
     *
     * @param archetypes the archetype constraint
     * @param node       the node name
     * @return the corresponding descriptor or <tt>null</tt>
     */
    public static NodeDescriptor getDescriptor(ShortNameConstraint archetypes,
                                               String node) {
        String[] shortNames = archetypes.getShortNames();
        if (shortNames.length > 0) {
            ArchetypeDescriptor archetype
                    = DescriptorHelper.getArchetypeDescriptor(shortNames[0]);
            if (archetype != null) {
                return archetype.getNodeDescriptor(node);
            }
        }
        return null;
    }

    /**
     * Adds a sort constraint on a participation node.
     *
     * @param acts       the act short names constraint. Must specify an alias
     * @param query      the query. Must reference <tt>acts</tt>
     * @param descriptor the participation node descriptor
     * @param ascending  if <tt>true</tt> sort ascending
     */
    public static void addSortOnParticipation(ShortNameConstraint acts,
                                              ArchetypeQuery query,
                                              NodeDescriptor descriptor,
                                              boolean ascending) {
        String[] particShortNames = DescriptorHelper.getShortNames(descriptor);
        String particAlias = getAlias("partic", query);
        ShortNameConstraint participation = new ShortNameConstraint(
                particAlias, particShortNames, false, false);
        acts.add(new CollectionNodeConstraint(descriptor.getName(),
                                              participation)
                .setJoinType(JoinConstraint.JoinType.LeftOuterJoin));
        ShortNameConstraint entity = new ShortNameConstraint(
                getAlias("entity", query),
                getEntityShortNames(particShortNames), false, false);
        query.add(entity);
        query.add(new IdConstraint(acts.getAlias(), particAlias + ".act"));
        query.add(new IdConstraint(entity.getAlias(), particAlias + ".entity"));
        query.add(new NodeSortConstraint(entity.getAlias(), "name", ascending));
    }

    /**
     * Returns a unique alias for an entity constraint.
     *
     * @return the alias
     */
    private static String getAlias(String prefix, ArchetypeQuery query) {
        return prefix + getAliasSuffix(prefix, query.getArchetypeConstraint(),
                                       0);
    }

    private static int getAliasSuffix(String prefix, IConstraint constraint,
                                      int maxId) {
        if (constraint instanceof BaseArchetypeConstraint) {
            BaseArchetypeConstraint arch = (BaseArchetypeConstraint) constraint;
            String alias = arch.getAlias();
            if (alias != null && alias.startsWith(prefix)) {
                String suffix = alias.substring(prefix.length());
                if (suffix != null) {
                    try {
                        int id = Integer.valueOf(suffix);
                        if (id > maxId) {
                            maxId = id + 1;
                        }
                    } catch (NumberFormatException ignore) {
                        // do nothing
                    }
                }
                for (IConstraint child : arch.getConstraints()) {
                    maxId = getAliasSuffix(prefix, child, maxId);
                }
            }
        }
        return maxId;
    }

    private static String[] getEntityShortNames(String[] shortNames) {
        return DescriptorHelper.getNodeShortNames(shortNames, "entity");
    }

}
