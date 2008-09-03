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

package org.openvpms.web.component.im.doc;

import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;
import org.openvpms.web.component.property.Property;


/**
 * A layout strategy for {@link DocumentAct}s that enables the document to
 * be viewed.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentActViewLayoutStrategy extends ActLayoutStrategy {

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
        String name = property.getName();
        ComponentState result;
        if (name.equals("documentTemplate") || name.equals("docReference")) {
            DocumentViewer viewer = new DocumentViewer((DocumentAct) parent,
                                                       true);
            result = new ComponentState(viewer.getComponent(), property);
        } else {
            result = super.createComponent(property, parent, context);
        }
        return result;
    }

    /**
     * Returns a node filter to filter nodes. This implementation filters the
     * "documentTemplate" node if there is also "docReference" node.
     *
     * @param object  the object to filter nodes for
     * @param context the context
     * @return a node filter to filter nodes
     */
    @Override
    protected NodeFilter getNodeFilter(IMObject object, LayoutContext context) {
        NodeFilter filter;
        IMObjectBean bean = new IMObjectBean(object);
        if (bean.hasNode("documentTemplate") && bean.hasNode("docReference")) {
            filter = getNodeFilter(context,
                                   new NamedNodeFilter("documentTemplate"));
        } else {
            filter = super.getNodeFilter(object, context);
        }
        return filter;
    }
}

