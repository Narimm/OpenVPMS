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
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.view.ReadOnlyComponentFactory;
import org.openvpms.web.component.property.PropertySet;

/**
 * Layout strategy for <em>act.patientInsuranceClaim</em>.
 *
 * @author Tim Anderson
 */
public class ClaimLayoutStrategy extends AbstractClaimLayoutStrategy {

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
        ActBean claimBean = new ActBean((Act) object);
        Act policy = (Act) claimBean.getNodeTargetObject("policy");
        if (policy != null) {
            ActBean policyBean = new ActBean(policy);
            ActIdentity identity = policyBean.getValue("insuranceId", PredicateUtils.truePredicate(),
                                                       ActIdentity.class);
            if (identity != null) {
                IMObjectComponentFactory factory = context.getComponentFactory();
                if (!(factory instanceof ReadOnlyComponentFactory)) {
                    factory = new ReadOnlyComponentFactory(context);
                }
                Component component = factory.create(identity, object).getComponent();
                addComponent(new ComponentState(component, properties.get("policy")));
            }
        }

        addComponent(createNotes(object, properties, context));
        return super.apply(object, properties, parent, context);
    }
}
