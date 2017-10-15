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

import nextapp.echo2.app.Row;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;

/**
 * Layout strategy for <em>act.patientInsuranceClaimItem</em>.
 *
 * @author Tim Anderson
 */
public class ClaimItemLayoutStrategy extends AbstractClaimLayoutStrategy {

    /**
     * The euthanasia reason component
     */
    private ComponentState euthanasiaResson;

    /**
     * The nodes to display.
     */
    private static final ArchetypeNodes NODES = new ArchetypeNodes().exclude("euthanasiaReason");

    /**
     * Constructs a {@link ClaimItemLayoutStrategy}.
     */
    public ClaimItemLayoutStrategy() {
        super(NODES);
    }


    /**
     * Determines if the euthanasia reason should be displayed.
     *
     * @param show if {@code true} display the euthansia reason, otherwise hide it.
     */
    public void setShowEuthanasiaReason(boolean show) {
        if (euthanasiaResson != null) {
            euthanasiaResson.getLabel().setVisible(show);
            euthanasiaResson.getComponent().setVisible(show);
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
        Row statusRow = RowFactory.create(Styles.CELL_SPACING);
        Property status = properties.get("status");
        ComponentState statusState = createComponent(status, object, context);
        euthanasiaResson = createComponent(properties.get("euthanasiaReason"), object, context);
        statusRow.add(statusState.getComponent());
        statusRow.add(euthanasiaResson.getLabel());
        statusRow.add(euthanasiaResson.getComponent());
        addComponent(new ComponentState(statusRow, statusState.getProperty()));
        setShowEuthanasiaReason("EUTHANASED".equals(status.getString()));

        addComponent(createNotes(object, properties, context));
        return super.apply(object, properties, parent, context);
    }

}
