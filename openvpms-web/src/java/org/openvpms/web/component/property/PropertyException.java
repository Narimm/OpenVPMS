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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.property;

import org.openvpms.component.system.common.exception.OpenVPMSException;


/**
 * Exception for {@link Property} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PropertyException extends OpenVPMSException {

    /**
     * The property.
     */
    private final Property property;


    /**
     * Constructs a new <tt>PropertyException</tt>.
     *
     * @param property the property
     * @param message  the message
     */
    public PropertyException(Property property, String message) {
        this(property, message, null);
    }

    /**
     * Constructs a new <tt>PropertyException</tt>.
     *
     * @param property the property
     * @param message  the message
     * @param cause    the cause of the exception. May be <tt>null</tt>
     */
    public PropertyException(Property property, String message,
                             Throwable cause) {
        super(message, cause);
        this.property = property;
    }

    /**
     * Returns the property that triggered the exception.
     *
     * @return the property
     */
    public Property getProperty() {
        return property;
    }
}
