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
 *  Copyright 2007-2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.doc;

import echopointng.layout.TableLayoutDataEx;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.doc.DocumentRules;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.act.ActAmountTableModel;
import org.openvpms.web.component.util.LabelFactory;


/**
 * Table model for document acts.
 *
 * @author Tim Anderson
 */
public class DocumentActTableModel extends ActAmountTableModel<DocumentAct> {

    /**
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * The fileName/reference model index.
     */
    private int docIndex;

    /**
     * The versions model index.
     */
    private int versionsIndex = -1;


    /**
     * Constructs a {@code DocumentActTableModel}.
     *
     * @param context the layout context
     */
    public DocumentActTableModel(LayoutContext context) {
        this(true, true, true, context);
    }

    /**
     * Constructs a {@code DocumentActTableModel}.
     *
     * @param showArchetype determines if the archetype column should be displayed
     * @param showStatus    determines if the status column should be displayed
     * @param showVersions  determines if the versions column should be displayed
     * @param context       the layout context
     */
    public DocumentActTableModel(boolean showArchetype, boolean showStatus, boolean showVersions,
                                 LayoutContext context) {
        super(showArchetype, showStatus, false);
        this.context = context;

        if (showVersions) {
            DefaultTableColumnModel model = (DefaultTableColumnModel) getColumnModel();
            versionsIndex = getNextModelIndex(model);
            model.addColumn(createTableColumn(versionsIndex, "document.acttablemodel.versions"));
        }
    }


    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param act    the object the object
     * @param column the table column
     * @param row    the table row
     * @return the value at the given coordinate
     */
    @Override
    protected Object getValue(DocumentAct act, TableColumn column, int row) {
        Object result;
        int index = column.getModelIndex();
        if (index == docIndex) {
            DocumentViewer viewer = new DocumentViewer(act, true, context);
            viewer.setShowNoDocument(false);
            result = viewer.getComponent();
        } else if (index == versionsIndex) {
            result = getVersions(act);
        } else {
            result = super.getValue(act, column, row);
        }
        return result;
    }

    private Object getVersions(DocumentAct act) {
        Object result;
        ActBean bean = new ActBean(act);
        if (bean.hasNode(DocumentRules.VERSIONS)) {
            int versions = bean.getValues(DocumentRules.VERSIONS).size();
            if (versions > 0) {
                Label label = LabelFactory.create();
                label.setText(Integer.toString(versions));
                TableLayoutData layout = new TableLayoutDataEx();
                Alignment right = new Alignment(Alignment.RIGHT, Alignment.DEFAULT);
                layout.setAlignment(right);
                label.setLayoutData(layout);
                result = versions;
            } else {
                result = null;
            }
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Helper to create a column model.
     * Adds a customer column before the amount index.
     *
     * @param showArchetype determines if the showArchetype column should be displayed
     * @param showStatus    determines if the status column should be displayed
     * @param showAmount    determines if the credit/debit amount should be displayed
     * @return a new column model
     */
    @Override
    protected TableColumnModel createColumnModel(boolean showArchetype, boolean showStatus,
                                                 boolean showAmount) {
        DefaultTableColumnModel model
                = (DefaultTableColumnModel) super.createColumnModel(showArchetype, showStatus,
                                                                    showAmount);
        docIndex = getNextModelIndex(model);
        model.addColumn(createTableColumn(docIndex, "document.acttablemodel.doc"));
        if (showAmount) {
            model.moveColumn(model.getColumnCount() - 1,
                             getColumnOffset(model, AMOUNT_INDEX));
        }
        return model;
    }


}
