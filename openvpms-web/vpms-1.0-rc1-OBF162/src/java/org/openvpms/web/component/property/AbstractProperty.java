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


/**
 * Abstract implementation of the {@link Property} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractProperty implements Property {

    /**
     * Determines if the underlying object is dirty.
     */
    private boolean dirty;

    /**
     * The listeners.
     */
    private ModifiableListeners listeners;

    /**
     * The property handler.
     */
    private PropertyTransformer transformer;


    /**
     * Determines if the underlying object has been modified.
     *
     * @return <tt>true</tt> if this has been modified; otherwise <tt>false</tt>
     */
    public boolean isModified() {
        return dirty;
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        dirty = false;
    }

    /**
     * Sets the property transformer.
     *
     * @param transformer the property transformer. May be <tt>null</tt>
     */
    public void setTransformer(PropertyTransformer transformer) {
        this.transformer = transformer;
    }

    /**
     * Returns the property transformer.
     * If none has been set, creates one using
     * {@link PropertyTransformerFactory#create}.
     *
     * @return the property transformer
     */
    public PropertyTransformer getTransformer() {
        if (transformer == null) {
            transformer = PropertyTransformerFactory.create(this);
        }
        return transformer;
    }

    /**
     * Notify any listeners that they need to refresh and marks this modified.
     */
    public void refresh() {
        dirty = true;
        if (listeners != null) {
            listeners.notifyListeners(this);
        }
    }

    /**
     * Add a listener to be notified when a this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        if (listeners == null) {
            listeners = new ModifiableListeners();
        }
        listeners.addListener(listener);
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        if (listeners != null) {
            listeners.removeListener(listener);
        }
    }

    /**
     * Determines if the object is valid.
     *
     * @return <tt>true</tt> if the object is valid; otherwise
     *         <tt>false</tt>
     */
    public boolean isValid() {
        Validator validator = new Validator();
        return validator.validate(this);
    }

    /**
     * Returns a hash code value for the object, based on the property name.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return <tt>true</tt> if this object is the same as the obj
     *         argument; <tt>false</tt> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Property) {
            Property p = (Property) obj;
            return getName().equals(p.getName());
        }
        return false;
    }

    /**
     * Verifies that the property can be modified.
     *
     * @throws UnsupportedOperationException if the property is read-only or
     *                                       derived
     */
    protected void checkReadOnly() {
        if (isDerived()) {
            throw new UnsupportedOperationException(
                    "Attenpt to modify read-only property: "
                            + getDisplayName());
        }
    }

}
