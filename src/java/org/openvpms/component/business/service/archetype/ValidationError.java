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


package org.openvpms.component.business.service.archetype;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;


/**
 * A validation error is generated when a node doesn't comply with its
 * archetype description.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ValidationError implements Serializable {

    /**
     * Serialisation identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The short name of the archetype that the error refers to.
     */
    private String archetype;

    /**
     * The name of the node that the error refers to.
     */
    private String node;

    /**
     * The error message.
     */
    private String message;


    /**
     * Constructs a new <tt>ValidationError</tt>.
     */
    protected ValidationError() {
    }

    /**
     * Constructs a new <tt>ValidationError</tt>.
     *
     * @param archetype the archetype short name
     * @param node      the node name
     * @param message   the error message
     */
    public ValidationError(String archetype, String node, String message) {
        this.archetype = archetype;
        this.message = message;
        this.node = node;
    }

    /**
     * Returns the archetype short name.
     *
     * @return the archetype short name
     */
    public String getArchetype() {
        return archetype;
    }

    /**
     * Sets the archetype short name.
     *
     * @param archetype the archetype short name
     */
    public void setArchetype(String archetype) {
        this.archetype = archetype;
    }

    /**
     * Returns the node name.
     *
     * @return the node name
     */
    public String getNode() {
        return node;
    }

    /**
     * Sets the node name.
     *
     * @param node the node name
     */
    public void setNode(String node) {
        this.node = node;
    }

    /**
     * Returns the error message.
     *
     * @return the error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the error message.
     *
     * @param message the error message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("archetype", archetype)
                .append("node", node)
                .append("message", message)
                .toString();
    }

}
