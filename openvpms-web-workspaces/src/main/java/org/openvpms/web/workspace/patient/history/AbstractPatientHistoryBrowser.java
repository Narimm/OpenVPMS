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

package org.openvpms.web.workspace.patient.history;

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.IMObjectTableBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.table.IMTable;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.echo.event.ActionListener;

import java.util.List;

/**
 * Patient history record browser.
 *
 * @author Tim Anderson
 */
public class AbstractPatientHistoryBrowser extends IMObjectTableBrowser<Act> {

    /**
     * Constructs an {@link AbstractPatientHistoryBrowser} that queries acts using the specified query, displaying them
     * in the table.
     *
     * @param query   the query
     * @param model   the table model
     * @param context the layout context
     */
    public AbstractPatientHistoryBrowser(Query<Act> query, AbstractPatientHistoryTableModel model,
                                         LayoutContext context) {
        super(query, model, context);
    }

    /**
     * Select an object.
     *
     * @param object the object to select
     */
    @Override
    public void setSelected(Act object) {
        super.setSelected(object);
        onSelected();
    }

    /**
     * Returns the parent act of the selected act.
     *
     * @return the parent act, or {@code null} if none is found
     */
    public Act getSelectedParent() {
        return getParent(getSelected());
    }

    /**
     * Returns the parent of the supplied act.
     *
     * @param act the act. May be {@code null}
     * @return the parent, or {@code null} if none is found
     */
    public Act getParent(Act act) {
        AbstractPatientHistoryTableModel model = getTableModel();
        String shortName = model.getParentShortName();
        boolean found = false;
        if (act != null) {
            List<Act> acts = getObjects();
            int index = acts.indexOf(act);
            while (!(found = TypeHelper.isA(act, shortName)) && index > 0) {
                act = acts.get(--index);
            }
        }
        return (found) ? act : null;
    }

    /**
     * Creates a new paged table.
     *
     * @param model the table model
     * @return a new paged table
     */
    @Override
    protected PagedIMTable<Act> createTable(IMTableModel<Act> model) {
        PagedIMTable<Act> result = super.createTable(model);
        initTable(result);
        return result;
    }

    /**
     * Initialises a paged table.
     *
     * @param table the table
     */
    protected void initTable(PagedIMTable<Act> table) {
        IMTable<Act> t = table.getTable();
        t.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onSelected();
            }
        });
        t.setDefaultRenderer(Object.class, new PatientHistoryTableCellRenderer());
        t.setHeaderVisible(false);
        t.setStyleName("MedicalRecordSummary");
    }

    /**
     * Returns the underlying table model.
     *
     * @return the table model
     */
    @Override
    protected AbstractPatientHistoryTableModel getTableModel() {
        return (AbstractPatientHistoryTableModel) super.getTableModel();
    }

    /**
     * Invoked when an act is selected. Highlights the associated parent act.
     */
    private void onSelected() {
        AbstractPatientHistoryTableModel model = getTableModel();
        String shortName = model.getParentShortName();
        IMTable<Act> table = getTable().getTable();
        int index = table.getSelectionModel().getMinSelectedIndex();
        while (index >= 0) {
            Act act = table.getObjects().get(index);
            if (TypeHelper.isA(act, shortName)) {
                break;
            } else {
                --index;
            }
        }
        model.setSelectedParent(index);
    }
}
