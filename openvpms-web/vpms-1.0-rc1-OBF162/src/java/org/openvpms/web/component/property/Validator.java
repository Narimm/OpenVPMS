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

package org.openvpms.web.component.property;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Validates an {@link Modifiable} heirarchy.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Validator {

    /**
     * Modifiable instances with their corresponding errors.
     */
    private Map<Modifiable, List<ValidatorError>> errors
            = new HashMap<Modifiable, List<ValidatorError>>();


    /**
     * Validates an object.
     *
     * @param modifiable the object to validate
     * @return <tt>true</tt> if the object is valid; otherwise <tt>false</tt>
     */
    public boolean validate(Modifiable modifiable) {
        return modifiable.validate(this);
    }

    /**
     * Adds validation errors for an object.
     *
     * @param modifiable the object
     * @param errors     the validation errors
     */
    public void add(Modifiable modifiable, List<ValidatorError> errors) {
        if (!errors.isEmpty()) {
            this.errors.put(modifiable, errors);
        }
    }

    /**
     * Returns all invalid objects.
     *
     * @return all invalid objects
     */
    public Collection<Modifiable> getInvalid() {
        return errors.keySet();
    }

    /**
     * Returns any errors for an object.
     *
     * @param modifiable the object
     * @return errors associated with <tt>modifiable</tt>, or <tt>null</tt>
     *         if there are no errors
     */
    public List<ValidatorError> getErrors(Modifiable modifiable) {
        return errors.get(modifiable);
    }

}
