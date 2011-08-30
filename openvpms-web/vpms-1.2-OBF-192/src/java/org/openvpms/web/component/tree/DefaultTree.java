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

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
package org.openvpms.web.component.tree;

import echopointng.Tree;
import echopointng.tree.TreeNode;
import org.openvpms.web.resource.util.Styles;


/**
 * Default tree implementation.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DefaultTree extends Tree {


    /**
     * Constructs a <code>DefaultTree</code> with the specified
     * <code>TreeNode</code> as its root.
     */
    public DefaultTree(TreeNode root) {
        super(root);
        StyleableTreeIcons icons = new StyleableTreeIcons();
        icons.setStyleName(Styles.getStyle(icons.getClass(), "default"));
        setTreeIcons(icons);

        StyleableTreeCellRenderer renderer = new StyleableTreeCellRenderer();
        renderer.setStyleName(Styles.getStyle(renderer.getClass(), "default"));
        setCellRenderer(renderer);
    }

}
