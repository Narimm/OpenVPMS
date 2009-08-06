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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.edit.investigation;

import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.layout.PrintObjectLayoutStrategy;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.ReadOnlyComponentFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.ErrorHelper;


/**
 * Layout strategy that includes a 'Print Form' button to print the act.
 */
public class PatientInvestigationActLayoutStrategy extends PrintObjectLayoutStrategy {

    /**
     * Determines if the date node should be displayed read-only.
     */
    private boolean showDateReadOnly;


    /**
     * Constructs a new <tt>PatientInvestigationActLayoutStrategy</tt>.
     */
    public PatientInvestigationActLayoutStrategy() {
        super("button.printform");
    }

    /**
     * Determines if the data should be displayed read-only.
     *
     * @param readOnly if <tt>true</tt> display the date read-only
     */
    public void setDateReadOnly(boolean readOnly) {
        showDateReadOnly = readOnly;
    }

    /**
     * Invoked when the print button is pressed.
     *
     * @param object the object to print
     */
    @Override
    protected void onPrint(IMObject object) {
        try {
            Entity template = getTemplate(object);
            if (template != null) {
                IMPrinter<IMObject> printer = new IMObjectReportPrinter<IMObject>(object, template);
                InteractiveIMPrinter<IMObject> iPrinter = new InteractiveIMPrinter<IMObject>(printer);
                iPrinter.print();
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Creates a component for a property.
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a component to display <tt>property</tt>
     */
    @Override
    protected ComponentState createComponent(Property property, IMObject parent,
                                             LayoutContext context) {
        ComponentState result;
        String name = property.getName();
        if (showDateReadOnly && name.equals("startTime")) {
            result = getReadOnlyComponent(property, parent, context);
        } else {
            result = super.createComponent(property, parent, context);
        }
        return result;
    }

    private Entity getTemplate(IMObject object) {
        Entity result = null;
        ActBean act = new ActBean((Act) object);
        Entity investigationType = act.getParticipant(InvestigationArchetypes.INVESTIGATION_TYPE_PARTICIPATION);
        if (investigationType != null) {
            EntityBean entity = new EntityBean(investigationType);
            result = entity.getNodeTargetEntity("template");
        }
        return result;
    }
    
    /**
     * Helper to return a read-only component.
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a read-only component to display the property
     */
    private ComponentState getReadOnlyComponent(Property property,
                                                IMObject parent,
                                                LayoutContext context) {
        ReadOnlyComponentFactory factory
                = new ReadOnlyComponentFactory(context);
        return factory.create(property, parent);
    }

}