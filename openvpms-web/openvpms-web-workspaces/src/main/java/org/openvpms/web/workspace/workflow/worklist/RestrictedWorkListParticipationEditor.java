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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.worklist;

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

/**
 * An editor for <em>participation.worklist</em> associated with a task that restricts the available
 * work lists to those provided by a custom query.
 *
 * @author Tim Anderson
 */
public abstract class RestrictedWorkListParticipationEditor extends ParticipationEditor<Entity> {

    /**
     * Constructs a {@link RestrictedWorkListParticipationEditor}.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param context       the layout context
     */
    public RestrictedWorkListParticipationEditor(Participation participation, Act parent, LayoutContext context) {
        super(participation, parent, context);
    }

    /**
     * Creates a query to query work lists.
     *
     * @param name the name to filter on. May be {@code null}
     * @return a new query
     */
    protected abstract Query<Entity> createWorkListQuery(String name);

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
                return createWorkListQuery(name);
            }

            @Override
            protected Browser<Entity> createBrowser(Query<Entity> query) {
                return new DefaultIMObjectTableBrowser<>(query, getLayoutContext());
            }
        };
    }

}
