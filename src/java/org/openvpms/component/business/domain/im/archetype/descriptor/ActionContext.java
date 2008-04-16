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

package org.openvpms.component.business.domain.im.archetype.descriptor;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;


/**
 * Context information passed to an assertion action.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActionContext {

    /**
     * The assertion.
     */
    private final AssertionDescriptor assertion;

    /**
     * The parent object. May be <tt>null</tt>
     */
    private final IMObject parent;

    /**
     * The node descriptor.
     */
    private final NodeDescriptor node;

    /**
     * The value to that is the subject of the action.
     */
    private final Object value;


    /**
     * Creates a new <tt>ActionContext</tt>.
     *
     * @param assertion the assertion descriptor
     * @param parent    the parent object. May be <tt>null</tt>
     * @param node      the node descriptor
     * @param value     the value that is the subject of the action.
     *                  May be <tt>null</tt>
     */
    public ActionContext(AssertionDescriptor assertion,
                         IMObject parent, NodeDescriptor node, Object value) {
        this.assertion = assertion;
        this.parent = parent;
        this.value = value;
        this.node = node;
    }

    /**
     * Returns the assertion descriptor.
     *
     * @return the assertion descriptor
     */
    public AssertionDescriptor getAssertion() {
        return assertion;
    }

    /**
     * Returns the parent object.
     *
     * @return the parent object, or <tt>null</tt> if there is no parent
     */
    public IMObject getParent() {
        return parent;
    }

    /**
     * Returns the node descriptor.
     *
     * @return the node descriptor
     */
    public NodeDescriptor getNode() {
        return node;
    }

    /**
     * Returns the value that is the subject of the action.
     *
     * @return the value. May be <tt>null</tt>
     */
    public Object getValue() {
        return value;
    }

    /**
     * Helper to return an assertion property.
     *
     * @param name the property name
     * @return the property, or <tt>null</tt> if none is found
     */
    public NamedProperty getProperty(String name) {
        return assertion.getProperty(name);
    }

}
