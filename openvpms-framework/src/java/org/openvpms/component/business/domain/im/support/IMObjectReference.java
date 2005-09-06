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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */


package org.openvpms.component.business.domain.im.support;

// java core
import java.io.Serializable;


/**
 * This class holds a reference to another object. The reference consists 
 * of a namespace and a unique identity
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class IMObjectReference implements Serializable {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * A namespace is used to qualify the identity and assists in resolving
     * the reference to an object.
     */
    private String namespace;
    
    /**
     * The unique identity of the object.
     */
    private String uid;
    
    /**
     * Default constructor
     */
    public IMObjectReference() {
    }

    /**
     * Costruct an object using the supplied namespace and id.
     * 
     * @param namespace
     *            the namespace associated with reference.
     * @param id
     *            the unique identity                       
     */
    public IMObjectReference(String namespace, String uid) {
        this.namespace = namespace;
        this.uid = uid;
    }

    /**
     * @return Returns the uid.
     */
    protected String getUid() {
        return uid;
    }

    /**
     * @param uid The id to set.
     */
    protected void setId(String uid) {
        this.uid = uid;
    }

    /**
     * @return Returns the namespace.
     */
    protected String getNamespace() {
        return namespace;
    }

    /**
     * @param namespace The namespace to set.
     */
    protected void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
