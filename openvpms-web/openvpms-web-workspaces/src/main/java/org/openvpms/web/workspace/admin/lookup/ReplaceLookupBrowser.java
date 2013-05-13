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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.workspace.admin.lookup;

import echopointng.GroupBox;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.AbstractFilteredResultSet;
import org.openvpms.web.component.im.query.IMObjectTableBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.GroupBoxFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;


/**
 * A browser that prompts for a replacement to a lookup.
 *
 * @author Tim Anderson
 */
class ReplaceLookupBrowser extends IMObjectTableBrowser<Lookup> {

    /**
     * The lookup to replace.
     */
    private final Lookup replace;

    /**
     * Determines if the lookup should be deleted.
     */
    private final CheckBox deleteSource;

    /**
     * Component containing the source and target lookups.
     */
    private Component gridContainer;

    /**
     * Component containing the query and results.
     */
    private Component queryContainer;


    /**
     * Constructs a {@code ReplaceLookupBrowser}.
     *
     * @param query   the query
     * @param lookup  the lookup to replace
     * @param context the layout context
     */
    public ReplaceLookupBrowser(Query<Lookup> query, Lookup lookup, LayoutContext context) {
        super(query, context);
        query.setMaxResults(15);
        this.replace = lookup;
        deleteSource = CheckBoxFactory.create("lookup.replace.delete", false);
        gridContainer = ColumnFactory.create("Inset");
        queryContainer = ColumnFactory.create("WideCellSpacing");
    }

    /**
     * Determines if the source lookup should be deleted.
     *
     * @return {@code true} if the source lookup should be deleted
     */
    public boolean deleteLookup() {
        return deleteSource.isSelected();
    }

    /**
     * Notifies listeners when an object is selected.
     *
     * @param selected the selected object
     */
    @Override
    protected void notifySelected(Lookup selected) {
        gridContainer.removeAll();
        gridContainer.add(createGrid());
        super.notifySelected(selected);
    }

    /**
     * Lays out this component.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        gridContainer.removeAll();
        gridContainer.add(createGrid());
        container.add(gridContainer);

        queryContainer.removeAll();
        queryContainer.add(doQueryLayout());
        GroupBox box = GroupBoxFactory.create("lookup.replace.select", queryContainer);
        container.add(box);
    }

    /**
     * Lays out the container to display results.
     *
     * @param container the container
     */
    @Override
    protected void doLayoutForResults(Component container) {
        super.doLayoutForResults(queryContainer);
    }

    /**
     * Lays out the container when there are no results to display.
     *
     * @param container the container
     */
    @Override
    protected void doLayoutForNoResults(Component container) {
        super.doLayoutForNoResults(queryContainer);
    }

    /**
     * Creates a grid displaying the lookup being replaced, and the lookup to replace it with.
     *
     * @return a new grid
     */
    private Grid createGrid() {
        Grid grid = GridFactory.create(4);
        grid.add(LabelFactory.create());
        grid.add(LabelFactory.create("table.imobject.name", "bold"));
        grid.add(LabelFactory.create("table.imobject.description", "bold"));
        grid.add(LabelFactory.create());

        grid.add(LabelFactory.create("lookup.replace.replace", "bold"));
        grid.add(createLabel(replace.getName()));
        grid.add(createLabel(replace.getDescription()));
        grid.add(deleteSource);

        grid.add(LabelFactory.create("lookup.replace.with", "bold"));
        Lookup selected = getSelected();
        String name = (selected != null) ? selected.getName() : Messages.get("imobject.none");
        String description = (selected != null) ? selected.getDescription() : "";
        grid.add(createLabel(name));
        grid.add(createLabel(description));
        return grid;
    }

    /**
     * Performs the query.
     *
     * @return the query result set
     */
    @Override
    protected ResultSet<Lookup> doQuery() {
        ResultSet<Lookup> set = super.doQuery();
        return new AbstractFilteredResultSet<Lookup>(set) {

            protected void filter(Lookup lookup, List<Lookup> results) {
                if (!lookup.equals(replace)) {
                    results.add(lookup);
                }
            }
        };
    }

    /**
     * Helper to create a label.
     *
     * @param text the text
     * @return a new label
     */
    private Label createLabel(String text) {
        Label label = LabelFactory.create();
        label.setText(text);
        return label;
    }
}
