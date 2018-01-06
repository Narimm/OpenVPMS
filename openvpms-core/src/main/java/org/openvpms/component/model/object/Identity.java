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
 * An identifier for an {@link IMObject}.
 * <p>
 * These are used to assign internal and external identifiers to objects.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public interface Identity extends IMObject {

    /**
     * Sets the identity.
     *
     * @param identity the identity
     */
    void setIdentity(String identity);

    /**
     * Returns the identity.
     *
     * @return the identity
     */
    String getIdentity();
}
