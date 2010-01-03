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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.admin.organisation;

import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.DefaultListModel;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.print.PrintHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.SelectFieldFactory;

/**
 * @author tony
 *
 */
public class OrganisationLocationEditor extends AbstractIMObjectEditor {

	/**
	 * @param object
	 * @param parent
	 * @param layoutContext
	 */
	public OrganisationLocationEditor(IMObject object, IMObject parent,
			LayoutContext layoutContext) {
		super(object, parent, layoutContext);
	}
    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LocationLayoutStrategy();
    }

    private class LocationLayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Creates a component for a property.
         *
         * @param property the property
         * @param parent   the parent object
         * @param context  the layout context
         * @return a component to display <code>property</code>
         */
        @Override
        protected ComponentState createComponent(Property property,
                                                 IMObject parent,
                                                 LayoutContext context) {
            ComponentState result;
            if (property.getName().equals("defaultPrinter")) {
                DefaultListModel model
                        = new DefaultListModel(PrintHelper.getPrinters());
                SelectField field = SelectFieldFactory.create(property, model);
                result = new ComponentState(field, property);
            } else {
                result = super.createComponent(property, parent, context);
            }
            return result;
        }
    }

}
