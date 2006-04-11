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

package org.openvpms.web.component.edit;

import java.util.List;

import org.openvpms.component.business.service.archetype.ValidationError;


/**
 * General validation interface..
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $Revision$ $Date$
 */
public interface Validator {

    /**
     * Perform validation, returning a list of errors if the object is invalid.
     *
     * @param value the value to validate
     * @return a list of error messages if the object is invalid; or an empty
     *         list if valid
     */
    List<ValidationError> validate(Object value);

    /**
     * Determines if the object is valid.
     *
     * @param value the value to validate
     * @return <code>true</code> if the object is valid; otherwise
     *         <code>false</code>
     */
    boolean isValid(Object value);

}
