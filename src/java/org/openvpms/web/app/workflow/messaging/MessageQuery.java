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

package org.openvpms.web.app.workflow.messaging;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.component.im.select.IMObjectSelectorListener;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Query for <em>act.userMessage</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MessageQuery extends DateRangeActQuery<Act> {

    /**
     * The clinician selector.
     */
    private final IMObjectSelector<Entity> clinician;

    /**
     * The act statuses.
     */
    private static final ActStatuses STATUSES
            = new ActStatuses("act.userMessage");


    /**
     * Constructs a new <tt>MessageQuery</tt>.
     */
    public MessageQuery(Entity user) {
        super(user, "to", "participation.user", new String[]{"act.userMessage"},
              STATUSES, Act.class);
        setStatus("PENDING");

        clinician = new IMObjectSelector<Entity>(Messages.get("messaging.user"),
                                                 "security.user");
        clinician.setListener(new IMObjectSelectorListener<Entity>() {
            public void selected(Entity object) {
                setEntity(object);
                onQuery();
            }

            public void create() {
                // no-op
            }
        });
        clinician.setObject(user);
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <tt>null</tt>
     * @return the query result set. May be <tt>null</tt>
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public ResultSet<Act> query(SortConstraint[] sort) {
        ResultSet<Act> result = null;
        ParticipantConstraint[] participants;
        if (clinician.isValid()) {
            if (getEntityId() != null) {
                participants = new ParticipantConstraint[]{
                        getParticipantConstraint()};
            } else {
                participants = new ParticipantConstraint[0];
            }
            result = new ActResultSet<Act>(getArchetypeConstraint(),
                                           participants,
                                           getFrom(), getTo(),
                                           getStatuses(), excludeStatuses(),
                                           getConstraints(), getMaxResults(),
                                           sort);
        }
        return result;
    }

    /**
     * Lays out the component in a container, and sets focus on the instance
     * name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        super.doLayout(container);
        container.add(LabelFactory.create("messaging.user"));
        container.add(clinician.getComponent());
        getFocusGroup().add(clinician.getFocusGroup());
    }
}
