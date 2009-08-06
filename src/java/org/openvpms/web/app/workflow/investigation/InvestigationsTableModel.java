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
 *
 *  $Id$
 */
package org.openvpms.web.app.workflow.investigation;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;

import java.util.List;


/**
 * Table model for <em>act.patientInvestigation</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class InvestigationsTableModel extends AbstractActTableModel {

    /**
     * The index of the supplier column.
     */
    private int supplierIndex;


    /**
     * Creates a new <tt>InvestigationsTableModel</tt>.
     */
    public InvestigationsTableModel() {
        super(InvestigationsQuery.SHORT_NAMES);
    }

    /**
     * Returns a list of descriptor names to include in the table.
     *
     * @return the list of descriptor names to include in the table
     */
    @Override
    protected String[] getNodeNames() {
        return new String[]{"startTime", "investigationType", "patient", "status", "docReference"};
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
    protected Object getValue(Act act, TableColumn column, int row) {
        Object result;
        int index = column.getModelIndex();
        if (index == supplierIndex) {
            result = getSupplier(act);
        } else {
            result = super.getValue(act, column, row);
        }
        return result;
    }

    /**
     * Creates a column model for a set of archetypes.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context
     * @return a new column model
     */
    @Override
    protected TableColumnModel createColumnModel(String[] shortNames, LayoutContext context) {
        DefaultTableColumnModel model = (DefaultTableColumnModel) super.createColumnModel(shortNames, context);
        supplierIndex = getNextModelIndex(model);
        TableColumn supplierColumnn = createTableColumn(supplierIndex, "investigationstablemodel.supplier");
        model.addColumn(supplierColumnn);
        model.moveColumn(model.getColumnCount() - 1, model.getColumnCount() - 2);
        return model;
    }

    /**
     * Returns a component representing the supplier for the specified investigation type associated with the act.
     *
     * @param act the act
     * @return the supplier component
     */
    private Component getSupplier(Act act) {
        Component result = null;
        ActBean bean = new ActBean(act);
        Entity investigationType = bean.getNodeParticipant("investigationType");
        if (investigationType != null) {
            EntityBean entityBean = new EntityBean(investigationType);
            List<IMObjectReference> refs = entityBean.getNodeTargetEntityRefs("supplier");
            if (!refs.isEmpty()) {
                IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(refs.get(0), true);
                result = viewer.getComponent();
            }
        }
        return result;
    }

}
