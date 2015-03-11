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

package org.openvpms.web.component.im.act;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.act.Act;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Flattens an act hierarchy into a list.
 *
 * @author Tim Anderson
 */
public class ActHierarchyLister<T extends Act> {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ActHierarchyLister.class);

    /**
     * Flattens the tree of child acts beneath the specified root.
     * <p/>
     * Child acts are filtered using the filter, and recursively processed up to depth maxDepth.
     * The result is an in-order traversal of the tree.
     *
     * @param root the root element
     * @return the flattened tree
     */
    public List<T> list(T root, ActFilter<T> filter, int maxDepth) {
        Node<T> tree = new Node<T>(root);
        Map<T, Node<T>> nodes = new HashMap<T, Node<T>>();
        buildTree(root, root, filter, 2, maxDepth, tree, nodes); // root elements are depth = 1
        List<T> result = new ArrayList<T>();
        return flattenTree(tree, result, filter);
    }

    /**
     * Performs an in-order traversal of a tree, collecting the values in the passed list.
     *
     * @param tree   the tree
     * @param values the collected values
     * @param filter the filter
     * @return the collected values
     */
    protected List<T> flattenTree(Node<T> tree, List<T> values, ActFilter<T> filter) {
        values.add(tree.value);
        Comparator<T> comparator = filter.getComparator(tree.value);
        for (Node<T> child : sort(tree.children, comparator)) {
            flattenTree(child, values, filter);
        }
        return values;
    }

    /**
     * Sorts tree nodes.
     *
     * @param nodes      the nodes to sort
     * @param comparator the comparator to sort nodes
     */
    protected List<Node<T>> sort(List<Node<T>> nodes, final Comparator<T> comparator) {
        Comparator<Node<T>> nodeComparator = new Comparator<Node<T>>() {
            @Override
            public int compare(Node<T> o1, Node<T> o2) {
                return comparator.compare(o1.value, o2.value);
            }
        };
        Collections.sort(nodes, nodeComparator);
        return nodes;
    }

    /**
     * Builds a tree of acts given a parent. Where acts are linked to multiple parent acts, only one instance
     * will be recorded in the resulting tree; that which has the maximum depth.
     *
     * @param act      the parent act
     * @param root     the root act
     * @param depth    the current depth
     * @param maxDepth the maximum depth to build to, or {@code -1} if there is no depth restriction
     * @param parent   the parent node
     * @param nodes    a map of value to node, for quick searches
     */
    private void buildTree(T act, T root, ActFilter<T> filter, int depth, int maxDepth, Node<T> parent,
                           Map<T, Node<T>> nodes) {
        List<T> children = filter.filter(act, root);
        for (T child : children) {
            Node<T> node = nodes.get(child);
            if (node != null) {
                if (node == parent) {
                    log.warn("Attempt to add node to itself: " + child.getObjectReference());
                } else if (node.getDepth() < depth) {
                    // if the node already exists at a shallower depth, move it deeper so it is not duplicated.
                    node.remove();
                    parent.add(node);
                }
            } else {
                node = new Node<T>(parent, child);
                nodes.put(child, node);
                if (depth < maxDepth || maxDepth == -1) {
                    buildTree(child, root, filter, depth + 1, maxDepth, node, nodes);
                }
            }
        }
    }

    /**
     * Tree node.
     */
    static class Node<T> {

        /**
         * The parent node, or {@code null} if this is a root node.
         */
        private Node parent;

        /**
         * The node value.
         */
        final T value;

        /**
         * The child nodes.
         */
        final List<Node<T>> children = new ArrayList<Node<T>>();

        /**
         * Constructs a parent {@link Node}.
         *
         * @param value the value
         */
        public Node(T value) {
            this(null, value);
        }

        /**
         * Constructs a {@link Node}.
         *
         * @param parent the parent node. May be {@code null}
         * @param value  the value
         */
        public Node(Node<T> parent, T value) {
            this.value = value;
            if (parent != null) {
                parent.add(this);
            }
        }

        /**
         * Adds a child node.
         *
         * @param child the child node
         */
        public void add(Node<T> child) {
            children.add(child);
            child.parent = this;
        }

        /**
         * Removes a child node.
         */
        public void remove() {
            if (parent != null) {
                parent.children.remove(this);
                parent = null;
            }
        }

        /**
         * Returns the depth of the node.
         *
         * @return the node depth
         */
        public int getDepth() {
            Node p = parent;
            int depth = 0;
            while (p != null) {
                depth++;
                p = p.parent;
            }
            return depth;
        }

        /**
         * Returns the parent node.
         *
         * @return the parent. May be {@code null}
         */
        public Node getParent() {
            return parent;
        }

    }
}
