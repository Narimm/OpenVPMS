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

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IdConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractArchetypeServiceResultSet;

import java.util.Date;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class EntityRelationshipResultSet
        extends AbstractArchetypeServiceResultSet<IMObject> {

    private final IMObject entity;
    private final String[] relationshipShortNames;
    private final String[] shortNames;
    private final boolean active;

    private final String primaryNode;
    private final String secondaryNode;

    public EntityRelationshipResultSet(IMObject entity,
                                       String[] relationshipShortNames,
                                       boolean active, int pageSize) {
        super(pageSize, null);
        this.entity = entity;
        this.relationshipShortNames = relationshipShortNames;
        this.active = active;
        String[] sourceShortNames = DescriptorHelper.getNodeShortNames(
                relationshipShortNames, "source");
        boolean source = TypeHelper.isA(entity, sourceShortNames);
        if (source) {
            shortNames = sourceShortNames;
            primaryNode = "source";
            secondaryNode = "target";
        } else {
            primaryNode = "target";
            secondaryNode = "source";
            shortNames = DescriptorHelper.getNodeShortNames(
                    relationshipShortNames, primaryNode);
        }
    }

    /**
     * Sets the sort criteria.
     *
     * @param sort the sort criteria. May be <tt>null</tt>
     */
    @Override
    protected void setSortConstraint(SortConstraint[] sort) {
        if (sort != null) {
            SortConstraint[] order = new SortConstraint[sort.length];
            for (int i = 0; i < sort.length; ++i) {
                SortConstraint s = sort[i];
                if (s instanceof NodeSortConstraint) {
                    NodeSortConstraint node = (NodeSortConstraint) s;
                    String nodeName = node.getNodeName();
                    if ("name".equals(nodeName)
                            || "description".equals(nodeName)) {
                        s = new NodeSortConstraint("target", nodeName);
                    }
                }
                order[i] = s;
            }
            sort = order;
        }
        super.setSortConstraint(sort);
    }

    /**
     * Creates a new archetype query.
     *
     * @return a new archetype query
     */
    protected ArchetypeQuery createQuery() {
        ShortNameConstraint relationships = new ShortNameConstraint(
                "rel", relationshipShortNames, false, false);
        ShortNameConstraint secondary = new ShortNameConstraint(
                secondaryNode, shortNames, false, false);

        ObjectRefConstraint source = new ObjectRefConstraint(
                primaryNode, entity.getObjectReference());

        ArchetypeQuery query = new ArchetypeQuery(relationships);
        query.add(source);
        query.add(secondary);
        query.add(new IdConstraint("rel.source", "source"));
        query.add(new IdConstraint("rel.target", "target"));
        if (active) {
            OrConstraint or = new OrConstraint();
            or.add(new NodeConstraint("rel.activeEndTime",
                                      RelationalOp.IsNULL));
            or.add(new NodeConstraint("rel.activeEndTime", RelationalOp.GT,
                                      new Date()));
            query.add(or);
            query.add(new NodeConstraint("target.active", true));
        }
        return query;
    }

}
