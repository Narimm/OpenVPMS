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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.insurance.claim;

import nextapp.echo2.app.Component;
import org.apache.commons.collections.PredicateUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActIdentity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.insurance.service.InsuranceService;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.resource.i18n.Messages;

/**
 * Layout strategy for <em>act.patientInsuranceClaim</em>.
 *
 * @author Tim Anderson
 */
public class ClaimLayoutStrategy extends AbstractClaimLayoutStrategy {

    /**
     * Determines if the insuranceId node should be read-only.
     */
    private boolean idReadOnly;

    /**
     * Determines if the insuranceId node should be read-only.
     * <p>
     * It should be read-only when the claim is to be submitted via an {@link InsuranceService}, as the service
     * is responsible for assigning the identifier.
     *
     * @param readOnly if {@code true}, the insuranceId node should be read-only
     */
    public void setInsuranceIdReadOnly(boolean readOnly) {
        this.idReadOnly = readOnly;
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
        IMObjectComponentFactory factory = context.getComponentFactory();

        // render the claim identifier
        CollectionProperty insuranceId = (CollectionProperty) properties.get("insuranceId");
        if (idReadOnly && context.isEdit()) {
            if (insuranceId.isEmpty()) {
                addComponent(createDummyInsuranceId(object, insuranceId, factory));
            } else {
                addComponent(factory.create(createReadOnly(insuranceId), object));
            }
        } else if (!context.isEdit() && insuranceId.isEmpty()) {
            addComponent(createDummyInsuranceId(object, insuranceId, factory));
        }

        // render the policy
        Act policy = getPolicy((Act) object);
        addComponent(createPolicy(policy, object, properties, factory));

        addComponent(createNotes(object, properties, context));
        return super.apply(object, properties, parent, context);
    }

    /**
     * Creates a place-holder for the insuranceId node when the collection is empty.
     * <p>
     * This is a workaround for the default single collection rendering that displays a short field containing 'None'.
     *
     * @param object      the parent object
     * @param insuranceId the insuranceId node
     * @param factory     the component factory
     * @return the place-holder
     */
    private ComponentState createDummyInsuranceId(IMObject object, CollectionProperty insuranceId,
                                                  IMObjectComponentFactory factory) {
        SimpleProperty dummy = new SimpleProperty("dummy", String.class);
        dummy.setMaxLength(insuranceId.getMaxLength());
        dummy.setReadOnly(true);
        ComponentState state = factory.create(dummy, object);
        return new ComponentState(state.getComponent(), insuranceId);
    }

    /**
     * Creates a component representing the policy.
     *
     * @param policy     the policy. May be {@code null}
     * @param object     the parent object
     * @param properties the properties
     * @param factory    the component factory
     * @return the policy component
     */
    private ComponentState createPolicy(Act policy, IMObject object, PropertySet properties,
                                        IMObjectComponentFactory factory) {
        String value;
        if (policy != null) {
            ActBean bean = new ActBean(policy);
            Party insurer = (Party) bean.getNodeParticipant("insurer");
            String insurerName = (insurer != null) ? insurer.getName() : null;
            ActIdentity identity = bean.getValue("insuranceId", PredicateUtils.truePredicate(), ActIdentity.class);
            String insuranceId = (identity != null) ? identity.getIdentity() : null;
            value = Messages.format("patient.insurance.policy", insurerName, insuranceId);
        } else {
            value = Messages.get("imobject.none");
        }
        SimpleProperty dummy = new SimpleProperty("dummy", value, String.class);
        dummy.setReadOnly(true);
        Component component = factory.create(dummy, object).getComponent();
        return new ComponentState(component, properties.get("policy"));
    }

    /**
     * Returns the claim policy.
     *
     * @param claim the claim
     * @return the policy, or {@code null} if none exists
     */
    private Act getPolicy(Act claim) {
        ActBean claimBean = new ActBean(claim);
        return (Act) claimBean.getNodeTargetObject("policy");
    }

}
