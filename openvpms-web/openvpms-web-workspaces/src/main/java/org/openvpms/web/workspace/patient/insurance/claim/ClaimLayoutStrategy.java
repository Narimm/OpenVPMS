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

package org.openvpms.web.workspace.patient.insurance.claim;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.edit.PropertyEditor;
import org.openvpms.web.component.im.layout.IMObjectTabPane;
import org.openvpms.web.component.im.layout.IMObjectTabPaneModel;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.patient.history.PatientHistoryBrowser;
import org.openvpms.web.workspace.patient.history.PatientHistoryQuery;

import java.util.List;

/**
 * Layout strategy for editing <em>act.patientInsuranceClaim</em>.
 *
 * @author Tim Anderson
 */
public class ClaimLayoutStrategy extends AbstractClaimLayoutStrategy {

    /**
     * The patient.
     */
    private final Party patient;

    /**
     * The insurer.
     */
    private final PropertyEditor insurer;

    /**
     * The policy number.
     */
    private final PropertyEditor policyNumber;

    /**
     * The items editor.
     */
    private final ClaimItemCollectionEditor items;

    /**
     * The attachments editor.
     */
    private final AttachmentCollectionEditor attachments;

    /**
     * The tab pane.
     */
    private IMObjectTabPane pane;

    /**
     * Constructs a {@link ClaimLayoutStrategy}.
     *
     * @param patient      the patient
     * @param insurer      the insurer. If {@code null}, get the insurer from the policy
     * @param policyNumber the policy number. If {@code null}, get the policy number from the policy
     * @param items        the claim items
     * @param attachments  the attachments editor
     * @param showGapClaim if {@code true}, show the gap claim node
     */
    public ClaimLayoutStrategy(Party patient, PropertyEditor insurer, PropertyEditor policyNumber,
                               ClaimItemCollectionEditor items, AttachmentCollectionEditor attachments,
                               boolean showGapClaim) {
        super(showGapClaim);
        this.insurer = insurer;
        this.policyNumber = policyNumber;
        this.items = items;
        this.attachments = attachments;
        this.patient = patient;
    }

    /**
     * Selects the attachments tab.
     */
    public void selectAttachments() {
        if (pane != null) {
            pane.setSelectedIndex(1);
        }
    }

    /**
     * Apply the layout strategy.
     * <p>
     * This renders an object in a {@code Component}, using a factory to create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param context    the layout context
     * @return the component containing the rendered {@code object}
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
        addComponent(new ComponentState(insurer));
        addComponent(new ComponentState(policyNumber));
        addComponent(new ComponentState(items));
        addComponent(new ComponentState(attachments));
        return super.apply(object, properties, parent, context);
    }

    /**
     * Returns the insurer property.
     *
     * @return the insurer
     */
    @Override
    protected Property getInsurer() {
        return insurer.getProperty();
    }

    /**
     * Returns the policy number property.
     *
     * @return the policy number
     */
    @Override
    protected Property getPolicyNumber() {
        return policyNumber.getProperty();
    }

    /**
     * Lays out each child component in a tabbed pane.
     *
     * @param object     the object to lay out
     * @param parent     the parent object. May be {@code null}
     * @param properties the properties
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doComplexLayout(IMObject object, IMObject parent, List<Property> properties, Component container,
                                   LayoutContext context) {
        IMObjectTabPaneModel model = doTabLayout(object, properties, container, context, false);
        if (patient != null) {
            PatientHistoryQuery query = new PatientHistoryQuery(patient, context.getPreferences());
            PatientHistoryBrowser history = new PatientHistoryBrowser(query, context);
            String label = Messages.get("patient.insurance.history");
            label = getShortcut(label, model.size() + 1);
            model.addTab(label, history.getComponent());
        }
        pane = new IMObjectTabPane(model);

        pane.setSelectedIndex(0);
        container.add(pane);
    }

}
