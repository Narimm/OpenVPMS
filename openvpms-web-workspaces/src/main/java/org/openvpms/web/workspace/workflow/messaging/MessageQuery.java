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
 */

package org.openvpms.web.workspace.workflow.messaging;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.workflow.MessageArchetypes;
import org.openvpms.archetype.rules.workflow.MessageStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.select.AbstractIMObjectSelectorListener;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Query for <em>act.userMessage</em> and <em>act.systemMessage</em> acts.
 *
 * @author Tim Anderson
 */
public class MessageQuery extends DateRangeActQuery<Act> {

    /**
     * The archetypes to query.
     */
    public static final String[] ARCHETYPES = {MessageArchetypes.USER, MessageArchetypes.SYSTEM};

    /**
     * The user selector.
     */
    private final IMObjectSelector<Entity> user;

    /**
     * The act statuses. Exclude the <em>READ</em> status, as it will be handled explicitly whenever <em>PENDING</em>
     * is selected.
     */
    private static final ActStatuses STATUSES = new ActStatuses(MessageArchetypes.USER);

    /**
     * The default statuses to query.
     */
    private static final String[] DEFAULT_STATUSES = {MessageStatus.PENDING, MessageStatus.READ};


    /**
     * Constructs a {@code MessageQuery}.
     *
     * @param user    the user to query messages for. May be {@code null}
     * @param context the layout context
     */
    public MessageQuery(Entity user, LayoutContext context) {
        super(user, "to", "participation.user", ARCHETYPES, STATUSES, Act.class);

        this.user = new IMObjectSelector<Entity>(Messages.get("messaging.user"), context, UserArchetypes.USER);
        this.user.setListener(new AbstractIMObjectSelectorListener<Entity>() {
            public void selected(Entity object) {
                setEntity(object);
                onQuery();
            }
        });
        this.user.setObject(user);
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be {@code null}
     * @return the query result set. May be {@code null}
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public ResultSet<Act> query(SortConstraint[] sort) {
        ResultSet<Act> result = null;
        ParticipantConstraint[] participants;
        if (user.isValid()) {
            if (getEntityId() != null) {
                participants = new ParticipantConstraint[]{getParticipantConstraint()};
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
     * Returns the user that messages are being queried for.
     *
     * @return the user. May be {@code null}
     */
    public Entity getUser() {
        return user.getObject();
    }

    /**
     * Creates a container component to lay out the query component in.
     *
     * @return a new container
     * @see #doLayout(nextapp.echo2.app.Component)
     */
    @Override
    protected Component createContainer() {
        return ColumnFactory.create("WideCellSpacing");
    }

    /**
     * Lays out the component in a container, and sets focus on the instance
     * name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        Row row1 = RowFactory.create("CellSpacing");
        super.doLayout(row1);
        Row row2 = RowFactory.create("CellSpacing", LabelFactory.create("messaging.user"), user.getComponent());
        container.add(row1);
        container.add(row2);
        getFocusGroup().add(user.getFocusGroup());
    }

    /**
     * Returns the act statuses to query.
     * <p/>
     * If the status is <em>PENDING</em>, this also includes <em>READ</em> acts.
     *
     * @return the act statuses to query
     */
    @Override
    protected String[] getStatuses() {
        String[] statuses = super.getStatuses();
        if (statuses.length == 1 && statuses[0].equals(MessageStatus.PENDING)) {
            statuses = DEFAULT_STATUSES;
        }
        return statuses;
    }
}
