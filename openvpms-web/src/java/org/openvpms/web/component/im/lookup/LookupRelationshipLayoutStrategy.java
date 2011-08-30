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

package org.openvpms.web.component.im.lookup;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.web.component.app.ContextSwitchListener;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.GridFactory;


/**
 * {@link LookupRelationship} layout strategy.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupRelationshipLayoutStrategy extends AbstractLayoutStrategy {

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
    protected void doLayout(IMObject object, PropertySet properties, IMObject parent, Component container,
                            LayoutContext context) {
        Property source = properties.get("source");
        Property target = properties.get("target");
        IMObjectReference srcRef = (IMObjectReference) source.getValue();
        IMObjectReference tgtRef = (IMObjectReference) target.getValue();
        boolean srcLink = false;
        boolean tgtLink = false;

        if (parent == null) {
            srcLink = true;
            tgtLink = true;
        } else {
            IMObjectReference parentRef = parent.getObjectReference();
            if (srcRef != null && !srcRef.equals(parentRef)) {
                srcLink = true;
            }
            if (tgtRef != null && !tgtRef.equals(parentRef)) {
                tgtLink = true;
            }
        }

        ContextSwitchListener listener = context.getContextSwitchListener();
        IMObjectReferenceViewer sourceView = new IMObjectReferenceViewer(srcRef, (srcLink) ? listener : null);
        IMObjectReferenceViewer targetView = new IMObjectReferenceViewer(tgtRef, (tgtLink) ? listener : null);

        Grid grid = GridFactory.create(4);
        add(grid, new ComponentState(sourceView.getComponent(), source));
        add(grid, new ComponentState(targetView.getComponent(), target));
        container.add(grid);
    }

}
