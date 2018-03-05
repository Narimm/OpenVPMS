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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.supplier;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.workspace.QueryBrowserCRUDWorkspace;


/**
 * Supplier act workspace.
 *
 * @author Tim Anderson
 */
public abstract class SupplierActWorkspace<T extends Act>
        extends QueryBrowserCRUDWorkspace<Party, T> {

    /**
     * Constructs a {@link SupplierActWorkspace}.
     *
     * @param id          the workspace id
     * @param context     the context
     */
    public SupplierActWorkspace(String id, Context context) {
        this(id, null, context);
    }

    /**
     * Constructs a {@link SupplierActWorkspace}.
     *
     * @param id         the workspace id
     * @param archetypes the archetype short names that this operates on
     * @param context    the context
     */
    public SupplierActWorkspace(String id, Archetypes<T> archetypes, Context context) {
        super(id, null, archetypes, context);
        setArchetypes(Party.class, "party.supplier*");
        setMailContext(new SupplierMailContext(context, getHelpContext()));
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(Party object) {
        super.setObject(object);
        getContext().setSupplier(object);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Creates a new query to select an object.
     *
     * @return a new query
     */
    @Override
    protected Query<Party> createSelectQuery() {
        // uses the query handler for party.supplier* by default
        return QueryFactory.create(getArchetypes().getShortNames(), false, getContext(), getType());
    }

    /**
     * Renders the workspace summary.
     *
     * @return the component representing the workspace summary, or {@code null} if there is no summary
     */
    @Override
    public Component getSummary() {
        return SupplierSummary.getSummary(getObject());
    }

    /**
     * Returns the latest version of the current supplier context object.
     *
     * @return the latest version of the context object, or {@link #getObject()} if they are the same
     */
    @Override
    protected Party getLatest() {
        return getLatest(getContext().getSupplier());
    }

}
