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

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.ColumnLayoutData;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.focus.FocusHelper;
import org.openvpms.web.component.im.table.IMTable;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.util.LabelFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;


/**
 * Implementation of {@link Browser} that renders results in a table.
 * .
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class TableBrowser<T> extends AbstractQueryBrowser<T> {

    /**
     * The selected action command.
     */
    public static final String SELECTED = "selected";

    /**
     * The paged table.
     */
    private PagedIMTable<T> table;

    /**
     * The model to render results.
     */
    private IMTableModel<T> model;

    /**
     * Determines if the model should be created for each query.
     */
    private boolean createModel;

    /**
     * Determines if the component has been laid out.
     */
    private boolean initialLayout = true;

    /**
     * If true, denotes that the current layout is the 'results layout'.
     * If false, denotes that the current layout is the 'no results layout'.
     */
    private boolean resultsLayout = false;


    /**
     * Construct a new <code>TableBrowser</code> that queries objects using the
     * specified query, displaying them in the table.
     *
     * @param query the query
     * @param sort  the sort criteria. May be <code>null</code>
     * @param model the table model. If <tt>null</tt>, one will be created on each query
     */
    public TableBrowser(Query<T> query, SortConstraint[] sort, IMTableModel<T> model) {
        super(query, sort);
        this.model = model;
        createModel = (model == null);
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <tt>null</tt> if none has been selected.
     */
    public T getSelected() {
        return (table != null) ? table.getTable().getSelected() : null;
    }

    /**
     * Select an object.
     *
     * @param object the object to select
     */
    public void setSelected(T object) {
        getTable().getTable().setSelected(object);
    }

    /**
     * Returns the objects matching the query.
     *
     * @return the objects matcing the query.
     */
    public List<T> getObjects() {
        return model.getObjects();
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    public void query() {
        Component component = getComponent();
        if (createModel) {
            // Destroy any existing model and tsble. These will be recreated in getTable().
            destroyTable();
            initialLayout = true;
        }

        ResultSet<T> set = doQuery();
        boolean hasResults = (set != null && hasResults(set));
        doLayout(component, hasResults);

        if (set == null) {
            set = new EmptyResultSet<T>(getQuery().getMaxResults());
        }
        PagedIMTable<T> table = getTable();
        table.setResultSet(set);
        setFocusOnResults();
    }

    /**
     * Returns the result set.
     *
     * @return the result set
     */
    public ResultSet<T> getResultSet() {
        ResultSet<T> set = getTable().getResultSet();
        try {
            if (set != null) {
                return set.clone();
            }
        } catch (CloneNotSupportedException exception) {
            throw new IllegalStateException(exception);
        }
        return null;
    }

    /**
     * Sets focus on the results.
     */
    public void setFocusOnResults() {
        IMTable<T> table = getTable().getTable();
        if (!table.getObjects().isEmpty() && table.isFocusTraversalParticipant()) {
            FocusHelper.setFocus(table);
        }
    }

    /**
     * Determines if a result set has results.
     *
     * @param set the result set
     * @return <tt>true</tt> if the result set has results
     */
    protected boolean hasResults(ResultSet<T> set) {
        boolean hasResults = false;
        if (set != null) {
            IPage<T> page = set.getPage(0);
            if (page != null) {
                hasResults = !page.getResults().isEmpty();
            }
        }
        return hasResults;
    }

    /**
     * Lays out the container to display results.
     *
     * @param container the container
     */
    protected void doLayoutForResults(Component container) {
        PagedIMTable<T> table = getTable();
        container.add(table);
        getFocusGroup().add(table);
    }

    /**
     * Lays out the container when there are no results to display.
     *
     * @param container the container
     */
    protected void doLayoutForNoResults(Component container) {
        Label label = LabelFactory.create("browser.noresults", "bold");
        ColumnLayoutData layout = new ColumnLayoutData();
        layout.setAlignment(Alignment.ALIGN_CENTER);
        label.setLayoutData(layout);

        container.add(label);
    }

    /**
     * Creates a new paged table.
     *
     * @param model the table model
     * @return a new paged table
     */
    protected PagedIMTable<T> createTable(IMTableModel<T> model) {
        return new PagedIMTable<T>(model);
    }

    /**
     * Returns the underlying table, creating it if it doesn't exist.
     *
     * @return the table
     */
    protected PagedIMTable<T> getTable() {
        if (table == null) {
            if (model == null) {
                model = createTableModel();
            }
            table = createTable(model);
            table.getTable().addActionListener(new ActionListener() {
                public void onAction(ActionEvent e) {
                    onSelect();
                }
            });
            table.getTable().addPropertyChangeListener(Table.SELECTION_CHANGED_PROPERTY, new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    onBrowse();
                }
            });
        }
        return table;
    }

    /**
     * Creates a table model.
     * <p/>
     * Subclasses must override this method if they do not specify a model at construction.
     *
     * @return a table model
     */
    protected IMTableModel<T> createTableModel() {
        throw new IllegalStateException("No table model has been registered");
    }

    /**
     * Returns the underlying table model.
     *
     * @return the table model
     */
    protected IMTableModel<T> getTableModel() {
        return model;
    }

    /**
     * Adds the table to the browser container.
     *
     * @param container  the browser container
     * @param hasResults determines if there are results to display
     */
    private void doLayout(Component container, boolean hasResults) {
        if (initialLayout || (hasResults != resultsLayout)) {
            switchLayout(container, hasResults);
            initialLayout = false;
        }
        resultsLayout = hasResults;
    }

    /**
     * Switches between the layout for displaying results and no results.
     *
     * @param container the container
     * @param results   if <tt>true</tt> denotes that there are results to display
     */
    private void switchLayout(Component container, boolean results) {
        destroyTable();
        container.removeAll();
        doLayout(container);
        if (results) {
            doLayoutForResults(container);
        } else {
            doLayoutForNoResults(container);
        }
    }

    /**
     * Destroys the existing table, if any.
     */
    private void destroyTable() {
        if (table != null) {
            getFocusGroup().remove(table);
            table = null;
        }
        if (createModel) {
            model = null;
        }
    }

    /**
     * Updates the selected IMObject from the table, and notifies any
     * listeners.
     */
    private void onSelect() {
        T selected = getSelected();
        if (selected != null) {
            notifySelected(selected);
        }
    }

    /**
     * Notifies any listeners when an object is browsed.
     */
    private void onBrowse() {
        T selected = getSelected();
        if (selected != null) {
            notifyBrowsed(selected);
        }
    }

}
