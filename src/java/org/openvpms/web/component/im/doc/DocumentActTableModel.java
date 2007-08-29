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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.doc;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.act.ActAmountTableModel;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.property.IMObjectProperty;
import org.openvpms.web.component.property.Property;


/**
 * Table model for document acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentActTableModel extends ActAmountTableModel<DocumentAct> {

    /**
     * The fileName/reference model index.
     */
    private int docIndex;

    /**
     * Construct a new <code>DocumentActTableModel</code>.
     */
    public DocumentActTableModel() {
        super(true, false);
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
        Object result = null;
        int index = column.getModelIndex();
        if (index == docIndex) {
            ActBean bean = new ActBean(act);
            if (bean.hasNode("documentTemplate")) {
                NodeDescriptor descriptor
                        = bean.getDescriptor("documentTemplate");
                LayoutContext context = new DefaultLayoutContext();
                TableComponentFactory factory = new TableComponentFactory(
                        context);
                context.setComponentFactory(factory);
                Property property = new IMObjectProperty(act, descriptor);
                result = factory.create(property, act).getComponent();
            } else if (bean.hasNode("fileName")) {
                DocumentViewer viewer = new DocumentViewer(act, true);
                result = viewer.getComponent();
            }
        } else {
            result = super.getValue(act, column, row);
        }
        return result;
    }

    /**
     * Helper to create a column model.
     * Adds a customer column before the amount index.
     *
     * @param showStatus determines if the status colunn should be displayed
     * @param showAmount determines if the credit/debit amount should be
     *                   displayed
     * @return a new column model
     */
    @Override
    protected TableColumnModel createColumnModel(boolean showStatus,
                                                 boolean showAmount) {
        DefaultTableColumnModel model
                = (DefaultTableColumnModel) super.createColumnModel(showStatus,
                                                                    showAmount);
        docIndex = getNextModelIndex(model);
        TableColumn column = createTableColumn(docIndex,
                                               "document.acttablemodel.doc");
        model.addColumn(column);
        if (showAmount) {
            model.moveColumn(model.getColumnCount() - 1,
                             getColumnOffset(model, AMOUNT_INDEX));
        }
        return model;
    }


}
