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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.act;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;

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
        Node<T> tree = buildTree(root, filter, maxDepth);
        List<T> result = new ArrayList<>();
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
     * @param root     the root act
     * @param filter   the act filter
     * @param maxDepth the maximum depth to build to, or {@code -1} if there is no depth restriction
     * @return the tree
     */
    protected Node<T> buildTree(T root, ActFilter<T> filter, int maxDepth) {
        Node<T> tree = new Node<>(root);
        Map<T, Node<T>> nodes = new HashMap<>();
        Map<IMObjectReference, T> acts = new HashMap<>();
        buildTree(root, root, filter, 2, maxDepth, tree, nodes, acts); // root elements are depth = 1
        return tree;
    }

    /**
     * Builds a tree of acts given a parent. Where acts are linked to multiple parent acts, only one instance
     * will be recorded in the resulting tree; that which has the maximum depth.
     *
     * @param act      the parent act
     * @param root     the root act
     * @param filter   the act filter
     * @param depth    the current depth
     * @param maxDepth the maximum depth to build to, or {@code -1} if there is no depth restriction
     * @param parent   the parent node
     * @param nodes    a map of value to node, for quick searches
     * @param acts     the set of visited acts
     */
    private void buildTree(T act, T root, ActFilter<T> filter, int depth, int maxDepth, Node<T> parent,
                           Map<T, Node<T>> nodes, Map<IMObjectReference, T> acts) {
        List<T> children = filter.filter(act, root, acts);
        List<Node<T>> added = new ArrayList<>(); // new nodes that need subtrees built
        List<Node<T>> move = new ArrayList<>();  // existing nodes that may need moving
        for (T child : children) {
            Node<T> node = nodes.get(child);
            if (node != null) {
                if (node == parent) {
                    log.warn("Attempt to add node to itself: " + child.getObjectReference());
                } else {
                    move.add(node);
                }
            } else {
                node = new Node<>(parent, child);
                nodes.put(child, node);
                added.add(node);
            }
        }

        if (depth < maxDepth || maxDepth == -1) {
            for (Node<T> node : added) {
                buildTree(node.value, root, filter, depth + 1, maxDepth, node, nodes, acts);
            }
        }

        // go through the nodes that may need moving deeper in the tree, discarding those that are
        // already children of others
        List<Node<T>> list = new ArrayList<>(move);
        while (!list.isEmpty()) {
            Node<T> node1 = list.remove(0);
            for (Node<T> node2 : list) {
                if (node2.isChildOf(node1)) {
                    move.remove(node2);
                } else if (node1.isChildOf(node2)) {
                    move.remove(node1);
                    break;
                }
            }
        }

        // move any remaining nodes that can go deeper in the tree
        for (Node<T> node : move) {
            if (node.getDepth() < depth) {
                node.remove();
                parent.add(node);
            }
        }
    }

    /**
     * Tree node.
     */
    static class Node<T extends Act> {

        /**
         * The parent node, or {@code null} if this is a root node.
         */
        private Node parent;

        /**
         * The node value.
         */
        private final T value;

        /**
         * The child nodes.
         */
        private final List<Node<T>> children = new ArrayList<>();

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
                if (depth > 255) {
                    log.warn("getDepth() depth greater than expected, bailing out");
                    break;
                }
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

        /**
         * Determines if this node is a child of another.
         *
         * @param node the node
         * @return if this is a child of {@code node}
         */
        public boolean isChildOf(Node<T> node) {
            Node p = parent;
            int depth = 0;
            while (p != null && p != node) {
                depth++;
                p = p.getParent();
                if (depth > 255) {
                    log.warn("isChildOf() node depth greater than expected, bailing out");
                    break;
                }
            }
            return p == node;
        }

        /**
         * Returns a string representation of the node and its children.
         *
         * @return a string representation of the node and its children
         */
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(StringUtils.leftPad("", getDepth(), '-'));
            builder.append(value.getArchetypeId());
            builder.append(':');
            builder.append(value.getId());
            for (Node<T> child : children) {
                builder.append("\n");
                builder.append(child);
            }
            return builder.toString();
        }
    }
}
