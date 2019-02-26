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
import nextapp.echo2.app.Extent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.insurance.InsuranceArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActIdentity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.DelegatingProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.text.TextArea;

/**
 * Layout strategy for viewing <em>act.patientInsuranceClaim</em>.
 *
 * @author Tim Anderson
 */
public class ClaimViewLayoutStrategy extends AbstractClaimLayoutStrategy {

    /**
     * The insurer.
     */
    private final SimpleProperty insurer = new SimpleProperty("insurer", IMObjectReference.class);

    /**
     * The policy number.
     */
    private final SimpleProperty policyNumber = new SimpleProperty("policyNumber", String.class);

    /**
     * Gap status node name.
     */
    private static final String GAP_STATUS = "status2";

    /**
     * Benefit amount node name.
     */
    private static final String BENEFIT_AMOUNT = "benefitAmount";

    /**
     * Paid amount node name.
     */
    private static final String PAID = "paid";

    /**
     * Benefit notes node name.
     */
    private static final String BENEFIT_NOTES = "benefitNotes";


    /**
     * Constructs a {@link ClaimViewLayoutStrategy}.
     */
    public ClaimViewLayoutStrategy() {
        super(true);
        insurer.setDisplayName(DescriptorHelper.getDisplayName(InsuranceArchetypes.POLICY, "insurer"));
        insurer.setArchetypeRange(new String[]{SupplierArchetypes.INSURER});
        policyNumber.setDisplayName(DescriptorHelper.getDisplayName(InsuranceArchetypes.POLICY, "insurerId"));
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
        IMObjectBean bean = getBean(object);
        if (bean.getBoolean(GAP_CLAIM)) {
            getArchetypeNodes().simple(GAP_STATUS, BENEFIT_AMOUNT, PAID, BENEFIT_NOTES).hidden(true);

            Property notes = properties.get(BENEFIT_NOTES);
            ComponentState state;
            int lines = StringUtils.countMatches(notes.getString(), "\n") + 1;
            if (lines > 1) {
                state = createComponent(notes, object, context);
                Component component = state.getComponent();
                if (component instanceof TextArea) {
                    TextArea text = (TextArea) component;
                    text.setHeight(new Extent(lines + 1, Extent.EM));
                }
            } else {
                // force it to display in one line
                DelegatingProperty p = new DelegatingProperty(notes) {
                    @Override
                    public int getMaxLength() {
                        return 255;
                    }
                };
                state = createComponent(p, object, context);
            }
            addComponent(state);
        }

        IMObjectBean policy = getPolicy(bean);
        if (policy != null) {
            insurer.setValue(policy.getTargetRef("insurer"));
            ActIdentity insurerId = policy.getObject("insurerId", ActIdentity.class);
            if (insurerId != null) {
                policyNumber.setValue(insurerId.getIdentity());
            }
        }
        return super.apply(object, properties, parent, context);
    }

    /**
     * Returns the insurer property.
     *
     * @return the insurer
     */
    @Override
    protected Property getInsurer() {
        return insurer;
    }

    /**
     * Returns the policy number property.
     *
     * @return the policy number
     */
    @Override
    protected Property getPolicyNumber() {
        return policyNumber;
    }

    /**
     * Returns the claim policy.
     *
     * @param claim the claim
     * @return the policy, or {@code null} if none exists
     */
    private IMObjectBean getPolicy(IMObjectBean claim) {
        Act policy = claim.getTarget("policy", Act.class);
        return policy != null ? getBean(policy) : null;
    }

}
