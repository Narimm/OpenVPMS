/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.admin.lookup;

import nextapp.echo2.app.SelectField;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.ShortNameListCellRenderer;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.SelectFieldFactory;


/**
 * An editor for <em>lookup.species</em> lookups.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SpeciesLookupEditor extends AbstractLookupEditor {

    /**
     * Creates a new <tt>SpeciesLookupEditor</tt>.
     *
     * @param object  the object to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context. May be <tt>null</tt>.
     */
    public SpeciesLookupEditor(Lookup object, IMObject parent,
                               LayoutContext context) {
        super(object, parent, context);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new SpeciesLayoutStrategy();
    }

    private class SpeciesLayoutStrategy extends AbstractLayoutStrategy {

        @Override
        protected ComponentState createComponent(Property property,
                                                 IMObject parent,
                                                 LayoutContext context) {
            ComponentState result;
            if ("customFields".equals(property.getName())) {
                SelectField field = createCustomFieldSelector(property);
                result = new ComponentState(field, property);
            } else {
                result = super.createComponent(property, parent, context);
            }
            return result;
        }

        /**
         * Creates a drop down of archetype short names for the customFields
         * node.
         *
         * @param property the customFields node property
         * @return a new drop down
         */
        private SelectField createCustomFieldSelector(final Property property) {
            String[] shortNames = DescriptorHelper.getShortNames(
                    "entity.customPatient*");
            ShortNameListModel model = new ShortNameListModel(shortNames,
                                                              false, true,
                                                              true);
            final SelectField field = SelectFieldFactory.create(property,
                                                                model);
            field.setCellRenderer(new ShortNameListCellRenderer());
            return field;
        }
    }


}
