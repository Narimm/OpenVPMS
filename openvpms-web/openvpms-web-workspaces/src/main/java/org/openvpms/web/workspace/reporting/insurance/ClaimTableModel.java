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

package org.openvpms.web.workspace.reporting.insurance;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.insurance.InsuranceArchetypes;
import org.openvpms.archetype.rules.insurance.InsuranceRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.Reference;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.DescriptorTableModel;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;

/**
 * Insurance claim table model that displays the customer and patient.
 *
 * @author Tim Anderson
 */
public class ClaimTableModel extends DescriptorTableModel<Act> {

    /**
     * The archetype service.
     */
    private final IArchetypeRuleService service;

    /**
     * The nodes to display.
     */
    private final ArchetypeNodes nodes;

    /**
     * The insurance rules.
     */
    private final InsuranceRules rules;

    /**
     * The insurer column index.
     */
    private int insurerIndex;

    /**
     * The customer column inde.
     */
    private int customerIndex;

    /**
     * The policy state for the current row.
     */
    private State state;

    /**
     * Constructs a {@link DescriptorTableModel}.
     *
     * @param context the layout context
     */
    public ClaimTableModel(LayoutContext context) {
        super(context);
        service = ServiceHelper.getArchetypeService();
        rules = ServiceHelper.getBean(InsuranceRules.class);

        nodes = ArchetypeNodes.nodes("id", "startTime", "patient", "location", "policy", "insurerId", "status",
                                     "amount", "gapClaim", "status2", "benefitAmount", "paid", "clinician");
        setTableColumnModel(createColumnModel(new String[]{InsuranceArchetypes.CLAIM}, context));
    }

    /**
     * Returns an {@link ArchetypeNodes} that determines what nodes appear in the table.
     * This is only used when {@link #getNodeNames()} returns null or empty.
     *
     * @return the nodes to include
     */
    @Override
    protected ArchetypeNodes getArchetypeNodes() {
        return nodes;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the table column
     * @param row    the table row
     */
    @Override
    protected Object getValue(Act object, TableColumn column, int row) {
        Object result;
        LayoutContext layoutContext = getLayoutContext();
        if (column.getModelIndex() == customerIndex) {
            Reference customer = getState(object, row).customer;
            result = new IMObjectReferenceViewer(customer, layoutContext.getContextSwitchListener(),
                                                 layoutContext.getContext()).getComponent();
        } else if (column.getModelIndex() == insurerIndex) {
            Reference insurer = getState(object, row).insurer;
            result = new IMObjectReferenceViewer(insurer, layoutContext.getContextSwitchListener(),
                                                 layoutContext.getContext()).getComponent();
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }

    /**
     * Returns a value for a given column.
     *
     * @param object the object to operate on
     * @param column the column
     * @param row    the row
     * @return the value for the column
     */
    @Override
    protected Object getValue(Act object, DescriptorTableColumn column, int row) {
        Object result;
        if ("policy".equals(column.getName())) {
            result = getState(object, row).policyNumber;
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }

    /**
     * Creates a column model for one or more archetypes.
     * If there are multiple archetypes, the intersection of the descriptors
     * will be used.
     *
     * @param archetypes the archetypes
     * @param context    the layout context
     * @return a new column model
     */
    @Override
    protected TableColumnModel createColumnModel(List<ArchetypeDescriptor> archetypes, LayoutContext context) {
        DefaultTableColumnModel model = (DefaultTableColumnModel) super.createColumnModel(archetypes, context);
        DescriptorTableColumn policy = getColumn(model, "policy");
        if (policy != null) {
            policy.setHeaderValue(DescriptorHelper.getDisplayName(InsuranceArchetypes.POLICY, "insurerId"));
        }
        customerIndex = getNextModelIndex(model);
        insurerIndex = customerIndex + 1;
        TableColumn customer = new TableColumn(customerIndex);
        TableColumn insurer = new TableColumn(insurerIndex);
        customer.setHeaderValue(DescriptorHelper.getDisplayName(InsuranceArchetypes.POLICY, "customer"));
        insurer.setHeaderValue(DescriptorHelper.getDisplayName(InsuranceArchetypes.POLICY, "insurer"));
        addColumnAfter(customer, getModelIndex(model, "startTime"), model);
        addColumnAfter(insurer, getModelIndex(model, "location"), model);
        return model;
    }

    /**
     * Returns the policy state for the specified row.
     *
     * @param claim the claim
     * @param row   the row being rendered
     * @return the policy state
     */
    private State getState(Act claim, int row) {
        if (state == null || row != state.row) {
            state = new State(claim, row);
        }
        return state;
    }

    /**
     * Policy details for a row in the table.
     */
    private class State {

        private final String policyNumber;

        private final Reference customer;

        private final Reference insurer;

        private final int row;

        public State(Act claim, int row) {
            Act policy = service.getBean(claim).getTarget("policy", Act.class);
            if (policy != null) {
                policyNumber = rules.getPolicyNumber(policy);
                IMObjectBean bean = service.getBean(policy);
                customer = bean.getTargetRef("customer");
                insurer = bean.getTargetRef("insurer");
            } else {
                policyNumber = null;
                customer = null;
                insurer = null;
            }
            this.row = row;
        }
    }
}
