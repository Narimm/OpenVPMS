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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.ColumnLayoutData;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMTable;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.focus.FocusHelper;
import org.openvpms.web.echo.style.Styles;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;

/**
 * An {@link Browser} that renders its results in a table.
 *
 * @author Tim Anderson
 */
public abstract class AbstractTableBrowser<T> extends AbstractBrowser<T> {

    /**
     * The browser component.
     */
    private Component component;

    /**
     * The paged table.
     */
    private PagedIMTable<T> table;

    /**
     * The model to render results.
     */
    private IMTableModel<T> model;

    /**
     * The layout context.
     */
    private final LayoutContext context;

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
     * Style name for this.
     */
    protected static final String STYLE = "Browser";

    /**
     * Constructs an {@link AbstractTableBrowser}.
     *
     * @param model   the table model. If {@code null}, one will be created on each query
     * @param context the layout context
     */
    public AbstractTableBrowser(IMTableModel<T> model, LayoutContext context) {
        this.model = model;
        this.context = context;
        createModel = (model == null);
        if (model != null) {
            initTableModel(model);
        }
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or {@code null} if none has been selected.
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
        return (model != null) ? model.getObjects() : Collections.<T>emptyList();
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
     * Returns the browser component.
     *
     * @return the browser component
     */
    public Component getComponent() {
        if (component == null) {
            doLayout();
        }
        return component;
    }

    /**
     * Lay out this component.
     */
    protected void doLayout() {
        Column container = ColumnFactory.create(STYLE);
        doLayout(container);
        setComponent(container);
    }

    /**
     * Registers the browser component.
     *
     * @param component the component
     */
    protected void setComponent(Component component) {
        this.component = component;
    }

    /**
     * Lays out this component.
     *
     * @param container the container
     */
    protected abstract void doLayout(Component container);

    /**
     * Determines if a result set has results.
     *
     * @param set the result set
     * @return {@code true} if the result set has results
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
        Label label = LabelFactory.create("browser.noresults", Styles.BOLD);
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
                model = createTableModel(context);
                initTableModel(model);
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
     * @param context the layout context
     * @return a table model
     */
    protected IMTableModel<T> createTableModel(LayoutContext context) {
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
     * Initialises the table model. This is invoked at construction, and after {@link #createTableModel} is invoked.
     * <p/>
     * This implementation is a no-op.
     *
     * @param model the model
     */
    protected void initTableModel(IMTableModel<T> model) {
        // do nothing
    }

    /**
     * Returns the layout context.
     *
     * @return the layout context
     */
    protected LayoutContext getContext() {
        return context;
    }

    /**
     * Adds the table to the browser container.
     *
     * @param container  the browser container
     * @param hasResults determines if there are results to display
     */
    protected void doLayout(Component container, boolean hasResults) {
        if (createModel) {
            // Destroy any existing model and table. These will be recreated in getTable().
            destroyTable();
            initialLayout = true;
        }

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
     * @param results   if {@code true} denotes that there are results to display
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
     * Updates the selected IMObject from the table, and notifies any listeners.
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
