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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.system.common.query.criteria;

import org.openvpms.component.model.archetype.NodeDescriptor;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.query.criteria.Path;

/**
 * Default implementation of {@link Path}.
 *
 * @author Tim Anderson
 */
public class PathImpl<X> extends ExpressionImpl<X> implements Path<X> {

    /**
     * The parent path. May be {@code null}
     */
    private final PathImpl<?> parent;

    /**
     * Constructs a {@link PathImpl}.
     *
     * @param type    the type of the path
     * @param parent  the parent path. May be {@code null}
     * @param context the context
     */
    public PathImpl(Type<X> type, PathImpl<?> parent, Context context) {
        super(type, context);
        this.parent = parent;
    }

    /**
     * Sets the path alias.
     *
     * @param alias the alias
     * @return the path
     */
    @Override
    public Path<X> alias(String alias) {
        return (Path<X>) super.alias(alias);
    }

    /**
     * Returns a path corresponding to an archetype node.
     *
     * @param name the node name
     * @return a path corresponding to the node
     */
    @Override
    public <Y> PathImpl<Y> get(String name) {
        Context context = getContext();
        Type<Y> type = context.getTypeForNode(getType(), name);
        return new PathImpl<Y>(type, this, context);
    }

    /**
     * Returns a path corresponding to an archetype node.
     *
     * @param name the node name
     * @param type the type
     * @return a path corresponding to the node
     */
    @Override
    @SuppressWarnings("unchecked")
    public <Y> Path<Y> get(String name, Class<Y> type) {
        PathImpl<?> path = get(name);
        if (!type.isAssignableFrom(path.getJavaType())) {
            throw new IllegalArgumentException("Node " + name + " has type: " + type);
        }
        return (Path<Y>) path;
    }

    /**
     * Returns a path that is the reference to the instance.
     *
     * @return the path
     */
    @Override
    public Path<Reference> reference() {
        if (IMObject.class.isAssignableFrom(getJavaType())) {
            Type<Reference> type = new Type<>(Reference.class, null, null);
            return new PathImpl<>(type, this, getContext());
        }
        return null;
    }

    /**
     * Returns the parent path.
     *
     * @return the parent path, or {@code null} if this has no parent
     */
    public PathImpl<?> getParent() {
        return parent;
    }

    /**
     * Returns the node name.
     *
     * @return the node name. May be {@code null}
     */
    public String getName() {
        NodeDescriptor node = getNode();
        return node != null ? node.getName() : null;
    }

    /**
     * Returns the node descriptor.
     *
     * @return the node descriptor. May be {@code null}
     */
    public NodeDescriptor getNode() {
        return getType().getNode();
    }

    /**
     * Determines if the path is a {@link Reference}.
     *
     * @return {@code true} if the path is a reference
     */
    public boolean isReference() {
        return Reference.class.isAssignableFrom(getJavaType());
    }

}
