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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.view.act;

import org.openvpms.web.component.im.edit.IMObjectCollectionEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.view.ComponentState;


/**
 * Act layout strategy. Hides the items node.
 *
 * @author Tim Anderson
 */
public class ActLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The collection items node.
     */
    private final String itemsNode;

    /**
     * Constructs an {@link ActLayoutStrategy}.
     */
    public ActLayoutStrategy() {
        this(true);
    }

    /**
     * Constructs an {@link ActLayoutStrategy}.
     *
     * @param showItems if {@code true}, show the items node
     */
    public ActLayoutStrategy(boolean showItems) {
        this(null, showItems);
    }

    /**
     * Constructs an {@link ActLayoutStrategy}.
     *
     * @param node      the act items node
     * @param showItems if {@code true}, show the items node
     */
    public ActLayoutStrategy(String node, boolean showItems) {
        this(null, node, showItems, new ArchetypeNodes());
    }

    /**
     * Constructs an {@link ActLayoutStrategy}.
     *
     * @param editor the act items editor. May be {@code null}
     */
    public ActLayoutStrategy(IMObjectCollectionEditor editor) {
        this(editor, (String) null);
    }

    /**
     * Constructs an {@link ActLayoutStrategy}.
     *
     * @param editor the act items editor
     * @param nodes  the nodes to display
     */
    public ActLayoutStrategy(IMObjectCollectionEditor editor, ArchetypeNodes nodes) {
        this(editor, null, true, nodes);
    }

    /**
     * Constructs an {@link ActLayoutStrategy}.
     *
     * @param editor the act items editor
     * @param node   the node that the editor corresponds to
     */
    public ActLayoutStrategy(IMObjectCollectionEditor editor, String node) {
        this(editor, node, true, new ArchetypeNodes());
    }

    /**
     * Constructs an {@link ActLayoutStrategy}.
     *
     * @param editor    the act items editor. May be {@code null}
     * @param node      the node that editor corresponds to. May be {@code null}
     * @param showItems if {@code true}, show the items node
     * @param nodes     the nodes to display
     */
    private ActLayoutStrategy(IMObjectCollectionEditor editor, String node, boolean showItems, ArchetypeNodes nodes) {
        super(nodes);
        itemsNode = (node == null) ? "items" : node;
        if (!showItems) {
            nodes.exclude(itemsNode);
        }
        if (editor != null && showItems) {
            // pre-register the editor
            addComponent(new ComponentState(editor));
        }
    }

    /**
     * Returns the name of the items node.
     *
     * @return the items node name
     */
    protected String getItemsNode() {
        return itemsNode;
    }
}
