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

package org.openvpms.web.component.im.query;

import java.util.Date;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.AndConstraint;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IConstraintContainer;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Result set for {@link Act}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ActResultSet extends AbstractArchetypeServiceResultSet<Act> {

    /**
     * The archetype query.
     */
    private final ArchetypeQuery _query;


    /**
     * Construct a new <code>ActResultSet</code>.
     *
     * @param entityId      the id of the entity to search for
     * @param participation the participation short name
     * @param archetypes    the act archetype constraint
     * @param from          the act start-from date. May be <code>null</code>
     * @param to            the act start-to date. May be <code>null</code>
     * @param statuses      the act statuses. If empty, indicates all acts
     * @param rows          the maximum no. of rows per page
     * @param sort          the sort criteria. May be <code>null</code>
     */
    public ActResultSet(IMObjectReference entityId,
                        String participation,
                        BaseArchetypeConstraint archetypes, Date from, Date to,
                        String[] statuses, int rows, SortConstraint[] sort) {
        this(entityId, participation, archetypes, from, to, statuses, false,
             rows, sort);
    }

    /**
     * Construct a new <code>ActResultSet</code>.
     *
     * @param entityId      the id of the entity to search for
     * @param participation the participation short name
     * @param archetypes    the act archetype constraint
     * @param from          the act start-from date. May be <code>null</code>
     * @param to            the act start-to date. May be <code>null</code>
     * @param statuses      the act statuses. If empty, indicates all acts
     * @param exclude       if <code>true</code> exclude acts with status in
     *                      <code>statuses</code>; otherwise include them.
     * @param rows          the maximum no. of rows per page
     * @param sort          the sort criteria. May be <code>null</code>
     */
    public ActResultSet(IMObjectReference entityId,
                        String participation,
                        BaseArchetypeConstraint archetypes, Date from, Date to,
                        String[] statuses, boolean exclude, int rows,
                        SortConstraint[] sort) {
        super(rows, sort);
        _query = new ArchetypeQuery(archetypes);

        if (statuses.length > 1) {
            IConstraintContainer constraint;
            RelationalOp op;
            if (exclude) {
                constraint = new AndConstraint();
                op = RelationalOp.NE;
            } else {
                constraint = new OrConstraint();
                op = RelationalOp.EQ;
            }
            for (String status : statuses) {
                constraint.add(new NodeConstraint("status", op, status));
            }
            _query.add(constraint);
        } else if (statuses.length == 1) {
            RelationalOp op = RelationalOp.EQ;
            if (exclude) {
                op = RelationalOp.NE;
            }
            _query.add(new NodeConstraint("status", op, statuses[0]));
        }

        if (from != null && to != null) {
            _query.add(new NodeConstraint("startTime", RelationalOp.BTW, from,
                                          to));
        }

        CollectionNodeConstraint participations = new CollectionNodeConstraint(
                "participants", participation, true, true)
                .add(new ObjectRefNodeConstraint("entity", entityId));

        _query.add(participations);
    }

    /**
     * Returns the specified page.
     *
     * @param firstRow the first row of the page to retrieve
     * @param maxRows  the maximun no of rows in the page
     * @return the page corresponding to <code>firstRow</code>, or
     *         <code>null</code> if none exists
     */
    protected IPage<Act> getPage(int firstRow, int maxRows) {
        IPage<Act> result = null;
        try {
            _query.setFirstRow(firstRow);
            _query.setNumOfRows(maxRows);

            for (SortConstraint sort : getSortConstraints()) {
                _query.add(sort);
            }
            IArchetypeService service = ServiceHelper.getArchetypeService();
            IPage<IMObject> page = service.get(_query);
            result = convert(page);
        } catch (OpenVPMSException exception) {
            ErrorDialog.show(exception);
        } finally {
            for (SortConstraint sort : getSortConstraints()) {
                _query.remove(sort);
            }
        }
        return result;
    }

}
