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
import echopointng.tree.MutableTreeNode;
import echopointng.tree.TreeNode;
import echopointng.tree.TreePath;
import echopointng.tree.TreeSelectionEvent;
import echopointng.tree.TreeSelectionListener;
import echopointng.tree.TreeSelectionModel;
import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.tree.IMObjectTreeNode;
import org.openvpms.web.component.im.tree.TreeBuilder;
import org.openvpms.web.component.tree.DefaultTree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;


/**
 * Browser that displays objects in a tree.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractTreeBrowser<T extends IMObject>
        extends AbstractQueryBrowser<T> implements TreeBrowser<T> {

    /**
     * The tree builder.
     */
    private final TreeBuilder<T> _builder;

    /**
     * The tree.
     */
    private Tree _tree;


    /**
     * Construct a new <code>AbstractTreeBrowser</code> that queries IMObjects
     * using the specified query.
     *
     * @param query   the query
     * @param sort    the sort criteria. May be <code>null</code>
     * @param builder the tree builder
     */
    public AbstractTreeBrowser(Query<T> query, SortConstraint[] sort,
                               TreeBuilder<T> builder) {
        super(query, sort);
        _builder = builder;
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <code>null</code> if none has been
     *         selected.
     */
    public T getSelected() {
        T result = null;
        if (_tree != null) {
            TreePath path = _tree.getSelectionPath();
            if (path != null) {
                Object last = path.getLastPathComponent();
                if (last instanceof IMObjectTreeNode) {
                    result = ((IMObjectTreeNode<T>) last).getObject();
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
    public void setSelected(T object) {
        if (_tree != null) {
            MutableTreeNode root = (MutableTreeNode) _tree.getModel().getRoot();
            TreePath path = getPath(object, root);
            if (path != null) {
                if (path.getPathCount() >= 2) {
                    // expand all nodes related to the object
                    Object[] subPath = new Object[2];
                    System.arraycopy(path.getPath(), 0, subPath, 0, 2);
                    _tree.toggleAllNodes(new TreePath(subPath), true);
                }
                _tree.setSelectionPath(path);
            }
        }
    }

    /**
     * Returns the objects matching the query.
     *
     * @return the objects matcing the query.
     */
    public List<T> getObjects() {
        List<T> result = new ArrayList<T>();
        if (_tree != null) {
            MutableTreeNode root = (MutableTreeNode) _tree.getModel().getRoot();
            Enumeration chilren = root.children();
            while (chilren.hasMoreElements()) {
                Object child = chilren.nextElement();
                if (child instanceof IMObjectTreeNode) {
                    IMObjectTreeNode<T> node = (IMObjectTreeNode<T>) child;
                    T object = node.getObject();
                    if (object != null) {
                        result.add(object);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Query using the specified criteria, and populate the browser with
     * matches.
     */
    public void query() {
        Component component = getComponent();

        ResultSet<T> set = doQuery();
        MutableTreeNode root = createTree(set);
        if (_tree != null) {
            component.remove(_tree);
        }
        _tree = new DefaultTree(root);
        _tree.setShowsRootHandles(false);
        _tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        _tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent event) {
                onSelected();
            }
        });
        component.add(_tree);
    }

    /**
     * Returns the parent of an object.
     *
     * @param object the object
     * @return the parent of object, or <code>null</code> if the object has
     *         no parent
     */
    public T getParent(T object) {
        MutableTreeNode root = (MutableTreeNode) _tree.getModel().getRoot();
        TreePath path = getPath(object, root);
        if (path != null && path.getParentPath() != null) {
            Object parent = path.getParentPath().getLastPathComponent();
            if (parent instanceof IMObjectTreeNode) {
                return ((IMObjectTreeNode<T>) parent).getObject();
            }
        }
        return null;
    }

    /**
     * Creates a tree from a result set.
     *
     * @param set the result set
     * @return the root of the tree
     */
    protected MutableTreeNode createTree(ResultSet<T> set) {
        _builder.create(set.getSortConstraints());
        while (set.hasNext()) {
            IPage<T> page = set.next();
            for (T object : page.getResults()) {
                _builder.add(object);
            }
        }
        return _builder.getTree();
    }

    /**
     * Invoked when a node is selected. Notifies any registered listeners.
     */
    protected void onSelected() {
        T selected = getSelected();
        if (selected != null) {
            notifySelected(selected);
        }
    }

    /**
     * Returns a path to an object.
     *
     * @param object the object
     * @return the path to an object, or <code>null</code> if the object can't be found
     */
    protected TreePath getPath(IMObject object, MutableTreeNode node) {
        if (node instanceof IMObjectTreeNode) {
            IMObjectTreeNode objNode = (IMObjectTreeNode) node;
            if (object.equals(objNode.getObject())) {
                List<TreeNode> nodes = new LinkedList<TreeNode>();
                TreeNode treeNode = node;
                while (treeNode != null) {
                    nodes.add(0, treeNode);
                    treeNode = treeNode.getParent();
                }
                return new TreePath(nodes.toArray());
            }
        }
        Enumeration children = node.children();
        while (children.hasMoreElements()) {
            Object next = children.nextElement();
            if (next instanceof MutableTreeNode) {
                TreePath path = getPath(object, (MutableTreeNode) next);
                if (path != null) {
                    return path;
                }
            }
        }
        return null;
    }
}
