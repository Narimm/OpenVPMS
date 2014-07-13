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
package org.openvpms.web.workspace.workflow.investigation;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.component.app.ContextSwitchListener;
import org.openvpms.web.component.im.doc.DocumentViewer;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableModel;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;
import java.util.List;


/**
 * Table model for <em>act.patientInvestigation</em> acts.
 *
 * @author Tim Anderson
 */
class InvestigationsTableModel extends DescriptorTableModel<Act> {

    /**
     * The patient rules.
     */
    private final PatientRules rules;

    /**
     * The index of the start time column.
     */
    private int startTimeIndex;

    /**
     * The index of the customer column.
     */
    private int customerIndex;

    /**
     * The index of the document column.
     */
    private int documentIndex;

    /**
     * The index of the supplier column.
     */
    private int supplierIndex;

    /**
     * The location index.
     */
    private int locationIndex;


    /**
     * Constructs a {@code InvestigationsTableModel}.
     *
     * @param context the layout context
     */
    public InvestigationsTableModel(LayoutContext context) {
        super(InvestigationsQuery.SHORT_NAMES, context);
        rules = ServiceHelper.getBean(PatientRules.class);
    }

    /**
     * Returns a list of descriptor names to include in the table.
     *
     * @return the list of descriptor names to include in the table
     */
    @Override
    protected String[] getNodeNames() {
        return new String[]{"startTime", "investigationType", "patient", "id", "status", "product", "clinician",
                            "location"};
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
        if (index == startTimeIndex) {
            result = getStartTime(act);
        } else if (index == customerIndex) {
            result = getCustomer(act);
        } else if (index == supplierIndex) {
            result = getSupplier(act);
        } else if (index == documentIndex) {
            DocumentViewer viewer = new DocumentViewer((DocumentAct) act, true, getLayoutContext());
            viewer.setShowNoDocument(false);
            result = viewer.getComponent();
        } else if (index == locationIndex) {
            ActBean bean = new ActBean(act);
            IMObjectReference ref = bean.getNodeParticipantRef("location");
            result = createViewer(ref, null, false);
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

        startTimeIndex = getColumnModelIndex(model, "startTime");
        customerIndex = getNextModelIndex(model);
        TableColumn customerColumn = createTableColumn(customerIndex, "investigationstablemodel.customer");
        model.addColumn(customerColumn);
        model.moveColumn(model.getColumnCount() - 1, getColumnOffset(model, "patient"));

        supplierIndex = getNextModelIndex(model);
        TableColumn supplierColumnn = createTableColumn(supplierIndex, "investigationstablemodel.supplier");
        model.addColumn(supplierColumnn);

        documentIndex = getNextModelIndex(model);
        TableColumn documentColumn = new TableColumn(documentIndex);
        String displayName = DescriptorHelper.getDisplayName(InvestigationArchetypes.PATIENT_INVESTIGATION, "document");
        documentColumn.setHeaderValue(displayName);
        model.addColumn(documentColumn);

        locationIndex = getColumnModelIndex(model, "location");
        return model;
    }

    /**
     * Creates a new column for a node.
     *
     * @param archetypes the archetypes
     * @param name       the node name
     * @param index      the index to assign the column
     * @return a new column
     */
    @Override
    protected TableColumn createColumn(List<ArchetypeDescriptor> archetypes, String name, int index) {
        TableColumn column = super.createColumn(archetypes, name, index);
        if ("id".equals(name)) {
            column.setHeaderValue(Messages.get("investigationstablemodel.requestId"));
        }
        return column;
    }

    /**
     * Returns a component representing the owner of the patient at the time the investigation was done
     *
     * @param act the act
     * @return the customer component. May be {@code null}
     */
    private Component getCustomer(Act act) {
        Component result = null;
        Party owner = rules.getOwner(act);
        if (owner != null) {
            result = createViewer(owner.getObjectReference(), owner.getName(), true);
        }
        return result;
    }

    /**
     * Returns a component representing the supplier for the specified investigation type associated with the act.
     *
     * @param act the act
     * @return the supplier component. May be {@code null}
     */
    private Component getSupplier(Act act) {
        Component result = null;
        ActBean bean = new ActBean(act);
        Entity investigationType = bean.getNodeParticipant("investigationType");
        if (investigationType != null) {
            EntityBean entityBean = new EntityBean(investigationType);
            List<IMObjectReference> refs = entityBean.getNodeTargetEntityRefs("supplier");
            if (!refs.isEmpty()) {
                result = createViewer(refs.get(0), null, true);
            }
        }
        return result;
    }

    /**
     * Creates a viewer for a reference.
     *
     * @param reference the reference
     * @param name      the name. May be {@code null}
     * @return a new viewer
     */
    private Component createViewer(IMObjectReference reference, String name, boolean link) {
        Component result;
        LayoutContext context = getLayoutContext();
        ContextSwitchListener listener = (link) ? context.getContextSwitchListener() : null;
        IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(
                reference, name, listener, context.getContext());
        result = viewer.getComponent();
        return result;
    }

    /**
     * Formats the start time for an investigation.
     *
     * @param act the investigation
     * @return the start time as a time if it is for today, or as a date/time for any other day
     */
    private String getStartTime(Act act) {
        String result;
        Date startTime = act.getActivityStartTime();
        if (startTime != null) {
            if (DateRules.compareDateToToday(startTime) == 0) {
                result = DateFormatter.formatTime(startTime, false);
            } else {
                result = DateFormatter.formatDateTime(startTime, false);
            }
        } else {
            result = null;
        }
        return result;
    }

}
