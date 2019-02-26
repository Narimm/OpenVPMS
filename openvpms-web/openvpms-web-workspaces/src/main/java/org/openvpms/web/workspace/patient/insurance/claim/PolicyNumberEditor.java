package org.openvpms.web.workspace.patient.insurance.claim;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.insurance.InsuranceArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ParticipationConstraint;
import org.openvpms.web.component.bound.BoundTextField;
import org.openvpms.web.component.edit.AbstractPropertyEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ArchetypeQueryResultSet;
import org.openvpms.web.component.im.query.DefaultQueryExecutor;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.DefaultDescriptorTableModel;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.popup.DropDown;
import org.openvpms.web.echo.style.Styles;

import java.util.List;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.sort;
import static org.openvpms.component.system.common.query.ParticipationConstraint.Field.ActShortName;

/**
 * Editor for insurance claim policy numbers.
 *
 * @author Tim Anderson
 */
public class PolicyNumberEditor extends AbstractPropertyEditor {

    /**
     * The claim context.
     */
    private final ClaimContext claimContext;

    /**
     * The layout context.
     */
    private final LayoutContext layoutContext;

    /**
     * The focus group.
     */
    private final FocusGroup focusGroup;

    /**
     * Input listener.
     */
    private final ModifiableListener listener;

    /**
     * The policy drop down component.
     */
    private DropDown dropDown;

    /**
     * Constructs a {@link PolicyNumberEditor}.
     *
     * @param property      the property being edited
     * @param claimContext  the claim context
     * @param layoutContext the layout context
     */
    public PolicyNumberEditor(Property property, ClaimContext claimContext, LayoutContext layoutContext) {
        super(property);
        this.claimContext = claimContext;
        this.layoutContext = layoutContext;
        focusGroup = new FocusGroup(getClass().getSimpleName());
        BoundTextField inputField = new BoundTextField(property);
        inputField.setStyleName("Selector");
        dropDown = new DropDown();
        dropDown.setStyleName(Styles.DEFAULT);
        dropDown.setTarget(inputField);
        dropDown.setPopUpAlwaysOnTop(true);
        dropDown.setFocusOnExpand(true);
        listener = modifiable -> updatePolicies(true);
        focusGroup.add(inputField);
        updateText();
        updatePolicies(false);
        property.addModifiableListener(listener);
    }

    /**
     * Refreshes the policy number from the context.
     */
    public void refresh() {
        updateText();
    }

    /**
     * Returns the edit component.
     *
     * @return the edit component
     */
    @Override
    public Component getComponent() {
        return dropDown;
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group, or {@code null} if the editor hasn't been rendered
     */
    @Override
    public FocusGroup getFocusGroup() {
        return focusGroup;
    }

    /**
     * Sets the policy.
     *
     * @param policy     the policy. May be {@code null}
     * @param updateText if {@code true}, updates the text field
     * @return {@code true} if the policy or insurer changed, otherwise {@code false}
     */
    private boolean setPolicy(Act policy, boolean updateText) {
        Act existingPolicy = (Act) claimContext.getPolicy();
        Party existingInsurer = claimContext.getInsurer();

        claimContext.setPolicy(policy);
        if (updateText) {
            updateText();
        }
        return !ObjectUtils.equals(existingPolicy, claimContext.getPolicy())
               || !ObjectUtils.equals(existingInsurer, claimContext.getInsurer());
    }

    /**
     * Updates the field with the current policy number.
     */
    private void updateText() {
        Property input = getProperty();
        try {
            input.removeModifiableListener(listener);
            input.setValue(claimContext.getPolicyNumber());
        } finally {
            input.addModifiableListener(listener);
        }
    }

    /**
     * Updates the policies.
     * <p>
     * If the patient has multiple policies, renders a drop down containing them beside the text field.
     *
     * @param showPopup if {@code true} display the popup if there is more than one match
     */
    private void updatePolicies(boolean showPopup) {
        boolean modified = false;
        PagedIMTable<Act> table = null;
        Act policy = (Act) claimContext.getPolicy();
        Party insurer = claimContext.getInsurer();
        String policyNumber = getProperty().getString();
        if (insurer != null) {
            boolean useDefault = showPopup;
            if (policy != null && ObjectUtils.equals(policyNumber, claimContext.getPolicyNumber())) {
                // already have a policy, so list all policies
                policyNumber = null;
            } else if (policy != null && policyNumber == null) {
                // input text has been cleared. Clear the existing reference
                useDefault = false;
                modified = setPolicy(null, false);
            }
            ResultSet<Act> set = createResultSet(policyNumber);
            if (set.hasNext()) {
                IPage<Act> next = set.next();
                List<Act> results = next.getResults();
                if (results.size() >= 1) {
                    set.previous(); // reset() will shed caches
                    table = createTable(set);
                    if (useDefault && (policy == null || results.size() == 1)) {
                        modified = setPolicy(results.get(0), true);
                        showPopup = false;
                    }
                }
            } else if (!StringUtils.isEmpty(policyNumber)) {
                // no match on the input policy number, so create a dropdown of all policies
                modified |= claimContext.setPolicyNumber(policyNumber);
                set = createResultSet(null);
                if (set.hasNext()) {
                    table = createTable(set);
                }
            }
        }
        if (table != null) {
            table.getTable().setSelected((Act) claimContext.getPolicy());
            Column newValue = ColumnFactory.create(Styles.INSET, table.getComponent());
            dropDown.setPopUp(newValue);
            dropDown.setFocusComponent(table.getComponent());
        } else {
            Label component = LabelFactory.create("imobject.none");
            Column newValue = ColumnFactory.create(Styles.INSET, component);
            dropDown.setPopUp(newValue);
            dropDown.setFocusComponent(null);
        }
        if (showPopup) {
            dropDown.setExpanded(true);
        }
        if (modified) {
            // notify listenerr
            modified();
        }
    }

    /**
     * Returns a result set matching all policies for the customer and patient.
     *
     * @param policyNumber the policy number to match. If {@code null}, match all policies, otherwise perform a
     *                     starts-with match
     * @return a new result set
     */
    private ResultSet<Act> createResultSet(String policyNumber) {
        ArchetypeQuery query = new ArchetypeQuery(Constraints.shortName("act", InsuranceArchetypes.POLICY));
        query.add(join("customer").add(eq("entity", claimContext.getCustomer())).add(
                new ParticipationConstraint(ActShortName, InsuranceArchetypes.POLICY)));
        query.add(join("patient").add(eq("entity", claimContext.getPatient())).add(
                new ParticipationConstraint(ActShortName, InsuranceArchetypes.POLICY)));
        if (policyNumber != null) {
            if (!policyNumber.endsWith("*")) {
                policyNumber = policyNumber + "*";
            }
            query.add(join("insurerId").add(eq("identity", policyNumber)));
        }
        query.add(sort("startTime", false));
        query.add(sort("id", false));
        return new ArchetypeQueryResultSet<>(query, 5, new DefaultQueryExecutor<>());
    }

    /**
     * Creates a table of policies.
     *
     * @param set the policy result set
     * @return a new table
     */
    private PagedIMTable<Act> createTable(ResultSet<Act> set) {
        String[] archetypes = {InsuranceArchetypes.POLICY};
        IMTableModel<Act> model = new DefaultDescriptorTableModel<>(
                archetypes, layoutContext, "insurer", "insurerId", "startTime", "endTime");
        PagedIMTable<Act> table = new PagedIMTable<>(model, set);
        table.getTable().addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                setPolicy(table.getTable().getSelected(), true);
                dropDown.setExpanded(false);
            }
        });
        return table;
    }

}
