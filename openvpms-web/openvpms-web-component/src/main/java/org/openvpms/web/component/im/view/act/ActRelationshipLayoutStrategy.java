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

package org.openvpms.web.component.im.view.act;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.LabelFactory;


/**
 * {@link ActRelationship} layout strategy. This resolves and displays the
 * 'target' node.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActRelationshipLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <tt>Component</tt>, using a factory to
     * create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object
     * @param context    the layout context
     * @return the component containing the rendered <tt>object</tt>
     */
    public ComponentState apply(IMObject object, PropertySet properties,
                                IMObject parent, LayoutContext context) {
        String node;
        if (parent == null) {
            node = "target";
        } else {
            IMObjectRelationship relationship = (IMObjectRelationship) object;
            IMObjectReference target = relationship.getTarget();
            if (target != null && !target.equals(parent.getObjectReference())) {
                node = "target";
            } else {
                node = "source";
            }
        }
        Property property = properties.get(node);
        ComponentState result = null;
        if (property != null) {
            IMObjectReference ref = (IMObjectReference) property.getValue();
            IMObjectComponentFactory factory = context.getComponentFactory();
            if (!context.isRendered(ref)) {
                IMObject toRender = context.getCache().get(ref);
                if (toRender != null) {
                    result = factory.create(toRender, parent);
                }
            } else {
                Component component = LabelFactory.create(
                    "imobject.alreadyRendered");
                result = new ComponentState(component);
            }
        }
        if (result == null) {
            Component component = LabelFactory.create();
            result = new ComponentState(component);
        }
        return result;
    }

}
