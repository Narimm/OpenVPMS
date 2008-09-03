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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditor;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;


/**
 * Act layout strategy. Hides the items node.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ActLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The act item editor. May be <tt>null</tt>.
     */
    private final IMObjectCollectionEditor editor;

    /**
     * The collection items node.
     */
    private final String itemsNode;

    /**
     * Determines if the items node should be displayed.
     */
    private final boolean showItems;


    /**
     * Construct a new <tt>ActLayoutStrategy</tt>.
     */
    public ActLayoutStrategy() {
        this(true);
    }

    /**
     * Construct a new <tt>ActLayoutStrategy</tt>
     *
     * @param showItems if <tt>true</tt>, show the items node
     */
    public ActLayoutStrategy(boolean showItems) {
        this(null, showItems);
    }

    /**
     * Construct a new <tt>ActLayoutStrategy</tt>
     *
     * @param node      the act items node
     * @param showItems if <tt>true</tt>, show the items node
     */
    public ActLayoutStrategy(String node, boolean showItems) {
        this(null, node, showItems);
    }

    /**
     * Construct a new <tt>ActLayoutStrategy</tt>.
     *
     * @param editor the act items editor
     */
    public ActLayoutStrategy(IMObjectCollectionEditor editor) {
        this(editor, null);
    }

    /**
     * Construct a new <tt>ActLayoutStrategy</tt>.
     *
     * @param editor the act items editor
     * @param node   the node that the editor corresponds to
     */
    public ActLayoutStrategy(IMObjectCollectionEditor editor, String node) {
        this(editor, node, true);
    }

    /**
     * Construct a new <tt>ActLayoutStrategy</tt>.
     *
     * @param editor    the act items editor. May be <tt>null</tt>
     * @param node      the node that editor corresponds to. May be <tt>null</tt>
     * @param showItems if <tt>true</tt>, show the items node
     */
    private ActLayoutStrategy(IMObjectCollectionEditor editor,
                              String node, boolean showItems) {
        this.editor = editor;
        this.showItems = showItems;
        itemsNode = (node == null) ? "items" : node;
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
        String name = property.getName();
        ComponentState component;
        if (name.equals(itemsNode)) {
            if (editor != null) {
                component = new ComponentState(editor.getComponent(),
                                               editor.getFocusGroup());
            } else {
                component = createItems(property, parent, context);
            }
        } else {
            component = super.createComponent(property, parent, context);
        }
        return component;
    }

    /**
     * Creates a component for the items node.
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a component to display <tt>property</tt>
     */
    protected ComponentState createItems(Property property, IMObject parent,
                                         LayoutContext context) {
        return super.createComponent(property, parent, context);
    }

    /**
     * Returns a node filter to filter nodes. This implementation filters the
     * "items" node.
     *
     * @param object
     * @param context the context
     * @return a node filter to filter nodes
     */
    @Override
    protected NodeFilter getNodeFilter(IMObject object, LayoutContext context) {
        NodeFilter filter;
        if (!showItems) {
            filter = getNodeFilter(context, new NamedNodeFilter(itemsNode));
        } else {
            filter = super.getNodeFilter(object, context);
        }
        return filter;
    }

}
