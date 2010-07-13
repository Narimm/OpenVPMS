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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.patient.mr;

import echopointng.TabbedPane;
import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.TabPaneModel;
import org.openvpms.web.component.util.TabbedPaneFactory;

import java.util.List;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class PatientRecordLayoutStrategy extends ActLayoutStrategy {

    /**
     * Constructs a <tt>PatientRecordLayoutStrategy</tt>.
     */
    public PatientRecordLayoutStrategy() {
        this(null);
    }

    /**
     * Constructs a <tt>PatientRecordLayoutStrategy</tt>.
     *
     * @param showItems if <tt>true</tt>, show the items node
     */
    public PatientRecordLayoutStrategy(boolean showItems) {
        super(showItems);
    }

    /**
     * Constructs a <tt>PatientRecordLayoutStrategy</tt> with a custom editor for the "items" node.
     *
     * @param editor the act items editor. If <tt>null</tt>, creates a default editor.
     */
    public PatientRecordLayoutStrategy(IMObjectCollectionEditor editor) {
        super(editor);
    }

    /**
     * Lays out each child component in a tabbed pane.
     * <p/>
     * This implementation displays a patient record summary in a tab if there is no parent object.
     * This helps provide context to the user when the act is being edited stand-alone.
     *
     * @param object      the object to lay out
     * @param parent      the parent object. May be <tt>null</tt>
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param container   the container to use
     * @param context     the layout context
     */
    @Override
    protected void doComplexLayout(IMObject object, IMObject parent, List<NodeDescriptor> descriptors,
                                   PropertySet properties, Component container, LayoutContext context) {
        if (parent == null) {
            boolean shortcuts = (context.getLayoutDepth() == 0);
            TabPaneModel model = doTabLayout(object, descriptors, properties, container, context, shortcuts);
            TabbedPane pane = TabbedPaneFactory.create(model);
            PatientRecordSummaryTab summary = new PatientRecordSummaryTab();
            summary.addTab((Act) object, pane, shortcuts);
            pane.setSelectedIndex(0);
            container.add(pane);
        } else {
            super.doComplexLayout(object, parent, descriptors, properties, container, context);
        }
    }

}
