/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.worklist;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.web.component.im.edit.DefaultIMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.DefaultIMObjectTableBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.property.Property;

import java.util.List;

/**
 * An editor for <em>participation.worklist</em> associated with a follow-up task.
 *
 * @author Tim Anderson
 */
public class FollowUpWorkListParticipationEditor extends ParticipationEditor<Entity> {

    /**
     * The work lists for follow-up tasks.
     */
    private final List<Entity> workLists;

    /**
     * Constructs a {@link FollowUpWorkListParticipationEditor}.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param context       the layout context
     * @param workLists     the work lists for follow-up tasks
     */
    public FollowUpWorkListParticipationEditor(Participation participation, Act parent, LayoutContext context,
                                               List<Entity> workLists) {
        super(participation, parent, context);
        this.workLists = workLists;
    }

    /**
     * Creates a new object reference editor for the participation entity.
     *
     * @param property the reference property
     * @return a new object reference editor
     */
    @Override
    protected IMObjectReferenceEditor<Entity> createEntityEditor(Property property) {
        return new DefaultIMObjectReferenceEditor<Entity>(property, getObject(), getLayoutContext()) {
            @Override
            protected Query<Entity> createQuery(String name) {
                FollowUpWorkListQuery query = new FollowUpWorkListQuery(workLists);
                if (name != null) {
                    Entity entity = getEntity();
                    if (entity == null || !StringUtils.equals(entity.getName(), name)) {
                        query.setValue(name);
                    }
                }
                return query;
            }

            @Override
            protected Browser<Entity> createBrowser(Query<Entity> query) {
                return new DefaultIMObjectTableBrowser<>(query, getLayoutContext());
            }
        };
    }

}
