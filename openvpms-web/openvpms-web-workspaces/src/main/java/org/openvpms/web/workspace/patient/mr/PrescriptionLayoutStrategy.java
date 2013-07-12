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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.mr;

import nextapp.echo2.app.Grid;
import org.openvpms.archetype.rules.patient.prescription.PrescriptionRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.List;

/**
 * Layout strategy for <em>act.patientPrescription</em>.
 * <p/>
 * Renders a quantity dispensed field.
 *
 * @author Tim Anderson
 */
public class PrescriptionLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Lays out components in a grid.
     *
     * @param object      the object to lay out
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param context     the layout context
     */
    @Override
    protected Grid createGrid(IMObject object, List<NodeDescriptor> descriptors,
                              PropertySet properties, LayoutContext context, int columns) {
        ComponentSet set = createComponentSet(object, descriptors, properties, context);

        String displayName = Messages.get("patient.prescription.dispensed");
        SimpleProperty quantityDispensed = new SimpleProperty(
                "quantityDispensed", BigDecimal.ZERO, BigDecimal.class, displayName);
        quantityDispensed.setReadOnly(true);

        PrescriptionRules rules = ServiceHelper.getBean(PrescriptionRules.class);
        BigDecimal quantity = rules.getDispensedQuantity((Act) object);
        quantityDispensed.setValue(quantity);
        ComponentState dispensed = createComponent(quantityDispensed, object, context);
        set.add(dispensed);

        ComponentGrid grid = new ComponentGrid();
        grid.add(set, columns);
        return createGrid(grid);
    }


}
