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

package org.openvpms.web.component.im.query;

import echopointng.Tree;
import echopointng.tree.*;
import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.tree.IMObjectTreeNode;
import org.openvpms.web.component.im.tree.IMObjectTreeNodeFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Enter description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class TreeBrowser extends AbstractBrowser {

    /**
     * The tree.
     */
    private Tree _tree;

    /**
     * The tree node factory.
     */
    private final IMObjectTreeNodeFactory _factory;


    /**
     * Construct a new <code>TreeBrowser</code> that queries IMObjects using the
     * specified query.
     *
     * @param query   the query
     * @param sort    the sort criteria. May be <code>null</code>
     * @param factory factory for tree nodes
     */
    public TreeBrowser(Query query, SortConstraint[] sort,
                       IMObjectTreeNodeFactory factory) {
        super(query, sort);
        _factory = factory;
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <code>null</code> if none has been
     *         selected.
     */
    public IMObject getSelected() {
        IMObject result = null;
        if (_tree != null) {
            TreePath path = _tree.getSelectionPath();
            if (path != null) {
                Object last = path.getLastPathComponent();
                if (last instanceof IMObjectTreeNode) {
                    result = ((IMObjectTreeNode) last).getObject();
                }
            }
        }
        return result;
    }

    /**
     * Select an object.
     *
     * @param object the object to select
     */
    public void setSelected(IMObject object) {
    }

    /**
     * Returns the objects matching the query.
     *
     * @return the objects matcing the query.
     */
    public List<IMObject> getObjects() {
        return new ArrayList<IMObject>();
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    public void query() {
        Component component = getComponent();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(null, true);

        ResultSet<IMObject> set = doQuery();
        while (set.hasNext()) {
            IPage<IMObject> page = set.next();
            for (IMObject object : page.getRows()) {
                MutableTreeNode child = _factory.create(object);
                root.add(child);
            }
        }
        if (_tree == null) {
            _tree = new Tree(root);
            _tree.setShowsRootHandles(false);
            _tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            _tree.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent event) {
                    onSelected();
                }
            });
            component.add(_tree);
        } else {
            _tree.setModel(new DefaultTreeModel(root));
        }
    }

    /**
     * Invoked when a node is selected. Notifies any registered listeners.
     */
    protected void onSelected() {
        IMObject selected = getSelected();
        if (selected != null) {
            notifySelected(selected);
        }
    }

}
