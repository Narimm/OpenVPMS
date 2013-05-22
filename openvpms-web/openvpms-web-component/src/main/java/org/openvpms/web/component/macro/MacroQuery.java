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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.macro;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractIMObjectQuery;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.focus.FocusHelper;


/**
 * Query for <em>lookup.macro</em> lookups.
 *
 * @author Tim Anderson
 */
public class MacroQuery extends AbstractIMObjectQuery<Lookup> {

    /**
     * Determines if the "Show Inactive" checkbox should be displayed.
     */
    private boolean showInactive = true;

    /**
     * The archetype short names to query.
     */
    public static final String[] SHORT_NAMES = new String[]{"lookup.macro", "lookup.macroReport"};


    /**
     * Constructs a {@code MacroQuery}.
     *
     * @throws org.openvpms.component.system.common.query.ArchetypeQueryException
     *          if the short names don't match any archetypes
     */
    public MacroQuery() {
        super(SHORT_NAMES, Lookup.class);
        setDefaultSortConstraint(new NodeSortConstraint[]{new NodeSortConstraint("code")});
    }

    /**
     * Determines if the inactive check box should be displayed.
     * <tt/>
     * Defaults to {@code true}.
     * Must be invoked prior to the component being created
     *
     * @param show if {@code true}, show the inactive check box when the component is created, otherwise leave it out
     */
    public void setShowInactive(boolean show) {
        this.showInactive = show;
    }

    /**
     * Lays out the component in a container, and sets focus on the search field name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        addSearchField(container);
        if (showInactive) {
            addInactive(container);
        }
        FocusHelper.setFocus(getSearchField());
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be <code>null</code>
     * @return a new result set
     */
    @Override
    protected ResultSet<Lookup> createResultSet(SortConstraint[] sort) {
        return new MacroResultSet(getArchetypeConstraint(), getValue(), sort, getMaxResults());
    }
}

