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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.hl7;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.hl7.io.HL7DocumentHandler;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextArea;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;

/**
 * Layout strategy for <em>act.HL7Message</em>.
 *
 * @author Tim Anderson
 */
public class HL7MessageLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Excludes the document and error nodes as these are handled explicitly.
     */
    private static final ArchetypeNodes NODES = new ArchetypeNodes().exclude("document", "error");

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
        Property error = properties.get("error");
        if (error.getString() != null) {
            addComponent(createComponent(error, parent, context));
        }
        addComponent(getMessage(object, properties));
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
    @Override
    protected void doSimpleLayout(IMObject object, IMObject parent, List<Property> properties, Component container,
                                  LayoutContext context) {
        ComponentGrid grid = createGrid(object, properties, context);

        ComponentState error = getComponent("error");
        if (error != null) {
            grid.add(error, 2);
        }
        grid.add(getComponent("document"), 2);
        Component component = createGrid(grid);
        container.add(ColumnFactory.create(Styles.INSET, component));
    }

    /**
     * Returns {@link ArchetypeNodes} to determine which nodes will be displayed.
     *
     * @return the archetype nodes
     */
    @Override
    protected ArchetypeNodes getArchetypeNodes() {
        return NODES;
    }

    /**
     * Returns a component for the message.
     *
     * @param object     the <em>act.HL7Message</em>
     * @param properties the properties
     * @return a component to display the message
     */
    private ComponentState getMessage(IMObject object, PropertySet properties) {
        DocumentAct act = (DocumentAct) object;
        Document document = (Document) IMObjectHelper.getObject(act.getDocument(), null);
        String content = null;
        if (document != null) {
            HL7DocumentHandler handler = new HL7DocumentHandler(ServiceHelper.getArchetypeService());
            content = handler.getStringContent(document);
        }
        TextArea text = TextComponentFactory.createTextArea();
        text.setStyleName("monospace");
        text.setWidth(Styles.FULL_WIDTH);
        text.setHeight(new Extent(20, Extent.EM));
        text.setText(content);
        return new ComponentState(text, properties.get("document"));
    }

}
