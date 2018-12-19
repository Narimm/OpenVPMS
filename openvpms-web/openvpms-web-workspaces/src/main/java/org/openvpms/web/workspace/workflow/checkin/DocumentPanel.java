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

package org.openvpms.web.workspace.workflow.checkin;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;

import java.util.Collection;

/**
 * Check-In documents.
 *
 * @author Tim Anderson
 */
class DocumentPanel {

    /**
     * The schedule. May be {@code null}
     */
    private final Entity schedule;

    /**
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * The maximum no. of results to display.
     */
    private final int maxResults;

    /**
     * The focus group.
     */
    private final FocusGroup focusGroup = new FocusGroup("DocumentPanel");

    /**
     * The container.
     */
    private Component container = RowFactory.create();

    /**
     * The document browser.
     */
    private PatientDocumentTemplateBrowser browser;

    /**
     * Constructs a {@link PatientDocumentTemplateBrowser}.
     *
     * @param schedule   the schedule. May be {@code null}
     * @param worklist   the work list. May be {@code null}
     * @param context    the layout context
     * @param maxResults the maximum no. of results to display
     */
    DocumentPanel(Entity schedule, Entity worklist, LayoutContext context, int maxResults) {
        this.schedule = schedule;
        this.context = context;
        this.maxResults = maxResults;
        addBrowser(worklist);
    }

    /**
     * Sets the work list.
     *
     * @param worklist the work list. May be {@code null}
     */
    public void setWorkList(Entity worklist) {
        addBrowser(worklist);
    }

    /**
     * Returns the selected templates.
     *
     * @return a list of <em>entity.documentTemplate</em>
     */
    public Collection<Entity> getTemplates() {
        return browser.getSelections();
    }

    /**
     * Returns the panel component.
     *
     * @return the component
     */
    public Component getComponent() {
        return container;
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return focusGroup;
    }

    /**
     * Adds a browser.
     *
     * @param worklist the work list. May be {@code null}
     */
    private void addBrowser(Entity worklist) {
        if (browser != null) {
            focusGroup.remove(browser.getFocusGroup());
        }
        ScheduleDocumentTemplateQuery query = new ScheduleDocumentTemplateQuery(schedule, worklist);
        query.setMaxResults(maxResults);
        browser = new PatientDocumentTemplateBrowser(query, context) {
            /**
             * Lay out this component.
             */
            @Override
            protected void doLayout() {
                Column container = ColumnFactory.create();
                doLayout(container);
                setComponent(container);
                query();
            }

            /**
             * Creates a new paged table.
             *
             * @param model the table model
             * @return a new paged table
             */
            @Override
            protected PagedIMTable<Entity> createTable(IMTableModel<Entity> model) {
                return new PagedIMTable<>(model, false);
            }
        };
        container.removeAll();
        container.add(browser.getComponent());
        focusGroup.add(browser.getFocusGroup());
    }


}
