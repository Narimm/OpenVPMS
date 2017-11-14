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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.SimpleProperty;

/**
 * Layout strategy for <em>act.patientInsuranceClaimAttachment</em>.
 *
 * @author Tim Anderson
 */
public class AttachmentLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The nodes to display.
     */
    private static final ArchetypeNodes NODES = ArchetypeNodes.allSimple().excludeIfEmpty("error");

    /**
     * Constructs an {@link AttachmentLayoutStrategy}.
     */
    public AttachmentLayoutStrategy() {
        super(NODES);
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
        CollectionProperty id = (CollectionProperty) properties.get("insurerId");
        if (context.isEdit()) {
            if (id.isEmpty()) {
                addComponent(createDummyInsurerId(object, id, factory));
            } else {
                addComponent(factory.create(createReadOnly(id), object));
            }
        } else {
            if (id.isEmpty()) {
                addComponent(createDummyInsurerId(object, id, factory));
            }
        }
        return super.apply(object, properties, parent, context);
    }

    /**
     * Creates a place-holder for the insurerId node when the collection is empty.
     * <p>
     * This is a workaround for the default single collection rendering that displays a short field containing 'None'.
     *
     * @param object    the parent object
     * @param insurerId the insurerId node
     * @param factory   the component factory
     * @return the place-holder
     */
    private ComponentState createDummyInsurerId(IMObject object, CollectionProperty insurerId,
                                                IMObjectComponentFactory factory) {
        SimpleProperty dummy = new SimpleProperty("dummy", String.class);
        dummy.setMaxLength(insurerId.getMaxLength());
        dummy.setReadOnly(true);
        ComponentState state = factory.create(dummy, object);
        return new ComponentState(state.getComponent(), insurerId);
    }

}
