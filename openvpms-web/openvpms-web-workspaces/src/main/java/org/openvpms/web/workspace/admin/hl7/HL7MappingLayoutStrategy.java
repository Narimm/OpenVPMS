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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.hl7;

import nextapp.echo2.app.SelectField;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.bound.BoundSelectFieldFactory;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.ShortNameListCellRenderer;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Layout strategy for <em>entity.HL7Mapping</em>.
 *
 * @author Tim Anderson
 */
public class HL7MappingLayoutStrategy extends AbstractLayoutStrategy {

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
            Property property = properties.get("speciesMapping");
            if (property != null && !property.isReadOnly()) {
                List<String> shortNames = new ArrayList<>();
                String[] mappings = DescriptorHelper.getNodeShortNames(PatientArchetypes.SPECIES, "mapping");
                for (String mapping : mappings) {
                    shortNames.addAll(Arrays.asList(DescriptorHelper.getNodeShortNames(mapping, "target")));
                }
                ShortNameListModel model = new ShortNameListModel(shortNames, false, !property.isRequired(), true);
                SelectField field = BoundSelectFieldFactory.create(property, model);
                field.setCellRenderer(new ShortNameListCellRenderer());
                addComponent(new ComponentState(field, property));
            }
        }
        return super.apply(object, properties, parent, context);
    }
}
