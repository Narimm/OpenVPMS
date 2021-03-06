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

package org.openvpms.web.workspace.patient.mr;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.text.TextComponent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.bound.BoundTextArea;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.history.PatientHistoryDatingPolicy;

import java.util.List;


/**
 * Layout strategy for <em>act.patientClinicalNote</em> and <em>act.patientClinicalAddendum</em>.
 *
 * @author Tim Anderson
 */
public class PatientClinicalNoteLayoutStrategy extends AbstractLayoutStrategy {


    /**
     * Apply the layout strategy.
     * <p/>
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
        if (context.isEdit()) {
            Property startTime = properties.get("startTime");
            PatientHistoryDatingPolicy policy = ServiceHelper.getBean(PatientHistoryDatingPolicy.class);
            if (!startTime.isReadOnly() && !policy.canEditStartTime(((Act) object))) {
                IMObjectComponentFactory factory = context.getComponentFactory();
                addComponent(factory.create(createReadOnly(startTime), object));
            }
        }
        addComponent(createNote(properties, context, "PatientClinicalNote.note"));
        return super.apply(object, properties, parent, context);
    }

    /**
     * Lays out child components in a grid.
     *
     * @param object     the object to lay out
     * @param parent     the parent object. May be {@code null}
     * @param properties the properties
     * @param container  the container to use
     * @param context    the layout context
     */
    protected void doSimpleLayout(IMObject object, IMObject parent, List<Property> properties,
                                  Component container, LayoutContext context) {
        Grid grid = createGrid(createGrid(object, properties, context));
        grid.setWidth(Styles.FULL_WIDTH);
        grid.setColumnWidth(1, new Extent(90, Extent.PERCENT));
        container.add(ColumnFactory.create(Styles.INSET, grid));
    }

    /**
     * Returns the default focus component.
     * <p/>
     * This implementation returns the note component.
     *
     * @param components the components
     * @return the note component, or {@code null} if none is found
     */
    @Override
    protected Component getDefaultFocus(ComponentSet components) {
        return components.getFocusable("note");
    }

    /**
     * Creates a component to display the note.
     *
     * @param properties the properties
     * @param context    the layout context
     * @param styleName  the message style name
     * @return a component to display the message
     */
    protected ComponentState createNote(PropertySet properties, LayoutContext context, String styleName) {
        Property message = properties.get("note");
        TextComponent textArea = new BoundTextArea(message);
        if (message.getMaxLength() != -1) {
            textArea.setMaximumLength(message.getMaxLength());
        }
        if (!context.isEdit()) {
            textArea.setEnabled(false);
        }
        textArea.setStyleName(styleName);
        return new ComponentState(ColumnFactory.create("PatientClinicalNote.inset", textArea), message);
    }


}
