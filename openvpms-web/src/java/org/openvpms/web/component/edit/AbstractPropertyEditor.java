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
 *  $Id: AbstractPropertyEditor.java 1535 2006-11-14 04:41:04Z tanderson $
 */

package org.openvpms.web.component.edit;

import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.AbstractModifiable;


/**
 * Abstract implementation of the {@link PropertyEditor} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-11-14 04:41:04Z $
 */
public abstract class AbstractPropertyEditor extends AbstractModifiable implements PropertyEditor {

    /**
     * The property being edited.
     */
    private final Property property;


    /**
     * Constructs an <tt>AbstractPropertyEditor</tt>.
     *
     * @param property the property being edited
     */
    public AbstractPropertyEditor(Property property) {
        this.property = property;
    }

    /**
     * Returns the property being edited.
     *
     * @return the property being edited
     */
    public Property getProperty() {
        return property;
    }

    /**
     * Determines if the object has been modified.
     *
     * @return <tt>true</tt> if the object has been modified
     */
    public boolean isModified() {
        return getProperty().isModified();
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        getProperty().clearModified();
    }

    /**
     * Add a listener to be notified when a this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        getProperty().addModifiableListener(listener);
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        getProperty().removeModifiableListener(listener);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the object and its descendants are valid otherwise <tt>false</tt>
     */
    protected boolean doValidation(Validator validator) {
        return getProperty().validate(validator);
    }

    /**
     * Resets the cached validity state of the object.
     *
     * @param descendants if <tt>true</tt> reset the validity state of any descendants as well.
     */
    @Override
    protected void resetValid(boolean descendants) {
        super.resetValid(descendants);
        getProperty().resetValid();
    }

}
