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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.component.property;

/**
 * Abstract implementation of the {@link Modifiable} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public abstract class AbstractModifiable implements Modifiable {

    /**
     * Cached valid state. If <tt>false</tt> indicates that valid status needs to be re-evaluated.
     */
    private boolean valid = false;


    /**
     * Determines if the object is valid.
     *
     * @return <tt>true</tt> if the object is valid; otherwise <tt>false</tt>
     */
    public boolean isValid() {
        return new Validator().validate(this);
    }

    /**
     * Validates the object.
     * <p/>
     * This implementation caches the result of the validation. If valid, subsequent invocations will return this
     * result, otherwise {@link #doValidation(Validator)} will be invoked.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the object and its descendants are valid otherwise <tt>false</tt>
     */
    public boolean validate(Validator validator) {
        if (!valid) {
            valid = doValidation(validator);
        }
        return valid;
    }

    /**
     * Resets the cached validity state of the object, to force revalidation of the object and its descendants.
     */
    public void resetValid() {
        resetValid(true);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the object and its descendants are valid otherwise <tt>false</tt>
     */
    protected abstract boolean doValidation(Validator validator);

    /**
     * Resets the cached validity state of the object.
     *
     * @param descendants if <tt>true</tt> reset the validity state of any descendants as well.
     */
    protected void resetValid(boolean descendants) {
        valid = false;
    }

}
