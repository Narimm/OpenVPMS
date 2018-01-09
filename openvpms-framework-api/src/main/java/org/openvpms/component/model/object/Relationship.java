/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.model.object;

/**
 * A relationship between two {@link IMObject}s.
 *
 * @author Tim Anderson
 */
public interface Relationship extends IMObject {

    /**
     * Returns a reference to the source object.
     *
     * @return the source object reference
     */
    Reference getSource();

    /**
     * Sets the source object reference.
     *
     * @param source the source object reference
     */
    void setSource(Reference source);

    /**
     * Returns a reference to the target object.
     *
     * @return the target object reference
     */
    Reference getTarget();

    /**
     * Sets the target object reference.
     *
     * @param target the target object reference
     */
    void setTarget(Reference target);

}
