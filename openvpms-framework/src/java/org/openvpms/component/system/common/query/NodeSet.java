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

package org.openvpms.component.system.common.query;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Set of nodes associated with an {@link IMObject}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NodeSet implements Serializable {

    /**
     * Serial version identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * A reference to the object that the nodes belong to.
     */
    private IMObjectReference reference;

    /**
     * The nodes.
     */
    private Map<String, Object> nodes = new HashMap<String, Object>();


    /**
     * Constructs a new <code>NodeSet</code>.
     *
     * @param reference a reference to the object that the nodes belong to.
     */
    public NodeSet(IMObjectReference reference) {
        this.reference = reference;
    }

    /**
     * Constructs a new <code>NodeSet</code>. This is provided for serialization
     * purposes.
     */
    protected NodeSet() {
    }

    /**
     * Returns a reference to the object that the nodes belong to.
     *
     * @return the object reference
     */
    public IMObjectReference getObjectReference() {
        return reference;
    }

    /**
     * Returns the node names.
     *
     * @return the node names
     */
    public Set<String> getNames() {
        return nodes.keySet();
    }

    /**
     * Adds a node.
     *
     * @param name  the  node name
     * @param value the node value
     */
    public void set(String name, Object value) {
        nodes.put(name, value);
    }

    /**
     * Returns the value of a node.
     *
     * @param name the node name
     * @return the node value. May be <code>null</code>
     */
    public Object get(String name) {
        return nodes.get(name);
    }

}
