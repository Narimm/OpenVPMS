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

// java core
import java.io.Serializable;

// commons-lang
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A validation error is generated when a node doesn't comply with its
 * archetype description.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ValidationError implements Serializable {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The name of the node that this error pertains too
     */
    private String nodeName;
    
    /**
     * The actual error message
     */
    private String errorMessage;

    /**
     * @param message
     * @param name
     */
    public ValidationError( String nodeName, String errorMessage) {
        this.errorMessage = errorMessage;
        this.nodeName = nodeName;
    }

    /**
     * @return Returns the errorMessage.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage The errorMessage to set.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * @return Returns the nodeName.
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * @param nodeName The nodeName to set.
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("node", nodeName)
            .append("error", errorMessage)
            .toString();
    }

}
