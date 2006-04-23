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


package org.openvpms.component.system.common.exception;

/**
 * This is the base exception for all OpenVPMS exceptions. 
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public abstract class OpenVPMSException extends RuntimeException implements
        IOpenVPMSException {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor, which delegates to the super class
     */
    public OpenVPMSException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * Delegate to the super class
     * 
     * @param msg
     *            the message             
     */
    public OpenVPMSException(String msg) {
        super(msg);
    }

    /**
     * Delegate to the super class
     * 
     * @param msg
     *            the message
     * @param exception
     *            the exception
     */
    public OpenVPMSException(String msg, Throwable exception) {
        super(msg, exception);
    }

    /**
     * Delegate to the super class
     * 
     * @param exception
     *            the exception
     */
    public OpenVPMSException(Throwable exception) {
        super(exception);
    }
}
