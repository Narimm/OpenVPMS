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

package org.openvpms.web.app.patient.mr;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.im.doc.DocumentEditor;
import org.openvpms.web.component.im.doc.DocumentActLayoutStrategy;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.layout.PrintObjectLayoutHelper;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.ReadOnlyComponentFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.ErrorHelper;


/**
 * Layout strategy that includes a 'Print Form' button to print the act.
 */
public class PatientInvestigationActLayoutStrategy extends DocumentActLayoutStrategy {

    /**
     * Determines if the date node should be displayed read-only.
     */
    private boolean showDateReadOnly;

    /**
     * Print layout strategy to delegate to.
     */
    private PrintObjectLayoutHelper printLayout;


    /**
     * Constructs a <tt>PatientInvestigationActLayoutStrategy</tt>.
     */
    public PatientInvestigationActLayoutStrategy() {
        this(null, null);
    }

    /**
     * Constructs a <tt>PatientInvestigationActLayoutStrategy</tt>.
     *
     * @param editor         the document reference editor. May be <tt>null</tt>
     * @param versionsEditor the document version editor. May be <tt>null</tt>
     */
    public PatientInvestigationActLayoutStrategy(DocumentEditor editor,
                                                 ActRelationshipCollectionEditor versionsEditor) {
        super(editor, versionsEditor);
        printLayout = new PrintObjectLayoutHelper("button.printform");
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
     * Determines if the button should be enabled.
     *
     * @param enable if <tt>true</tt>, enable the button
     */
    public void setEnableButton(boolean enable) {
        printLayout.setEnableButton(enable);
    }

    /**
     * Lay out out the object in the specified container.
     *
     * @param object     the object to lay out
     * @param properties the object's properties
     * @param parent     the parent object. May be <tt>null</tt>
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doLayout(final IMObject object, PropertySet properties, IMObject parent, Component container,
                            LayoutContext context) {
        Button print = printLayout.doLayout(container);
        print.addActionListener(new ActionListener() {
            public void onAction(ActionEvent e) {
                onPrint(object);
            }
        });
        super.doLayout(object, properties, parent, container, context);
        getFocusGroup().add(print);
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

    /**
     * Invoked when the print button is pressed.
     *
     * @param object the object to print
     */
    private void onPrint(IMObject object) {
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
     * Returns the template associated with the act;s investigation type.
     *
     * @param object the act
     * @return the associated investigation template, or <tt>null</tt> if none is found
     */
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
        ReadOnlyComponentFactory factory = new ReadOnlyComponentFactory(context);
        return factory.create(property, parent);
    }

}