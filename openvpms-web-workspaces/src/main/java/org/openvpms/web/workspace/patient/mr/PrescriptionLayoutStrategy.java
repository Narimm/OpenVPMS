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

import org.openvpms.archetype.rules.patient.prescription.PrescriptionRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;

/**
 * Layout strategy for <em>act.patientPrescription</em>.
 * <p/>
 * Renders a field representing the no. of times dispensed.
 *
 * @author Tim Anderson
 */
public class PrescriptionLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The archetype nodes, with the dispensing node excluded. Used when the prescription hasn't been dispensed.
     */
    private static final ArchetypeNodes EXCLUDE_DISPENSING = new ArchetypeNodes().exclude("dispensing");

    /**
     * Lays out components in a grid.
     *
     * @param object     the object to lay out
     * @param properties the properties
     * @param context    the layout context
     */
    @Override
    protected ComponentGrid createGrid(IMObject object, List<Property> properties, LayoutContext context, int columns) {
        ComponentSet set = createComponentSet(object, properties, context);

        String displayName = Messages.get("patient.prescription.dispensed");
        SimpleProperty dispensed = new SimpleProperty("dispensed", 0, int.class, displayName);
        dispensed.setReadOnly(true);

        PrescriptionRules rules = ServiceHelper.getBean(PrescriptionRules.class);
        int value = rules.getDispensed((Act) object);
        dispensed.setValue(value);
        ComponentState state = createComponent(dispensed, object, context);
        set.add(state);

        ComponentGrid grid = new ComponentGrid();
        grid.add(set, columns);
        return grid;
    }

    /**
     * Returns {@link ArchetypeNodes} to determine which nodes will be displayed.
     *
     * @param object the object to display
     * @return the archetype nodes
     */
    @Override
    protected ArchetypeNodes getArchetypeNodes(IMObject object) {
        IMObjectBean bean = new IMObjectBean(object);
        if (bean.getValues("dispensing").isEmpty()) {
            return EXCLUDE_DISPENSING;
        }
        return super.getArchetypeNodes(object);
    }
}
