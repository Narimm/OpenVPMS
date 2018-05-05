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


package org.openvpms.component.service.archetype;

import org.openvpms.component.model.object.Reference;


/**
 * A validation error is generated when an object doesn't comply with its archetype description.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public interface ValidationError {

    /**
     * Returns a reference to the object.
     *
     * @return the object reference
     */
    Reference getReference();

    /**
     * Returns the archetype.
     *
     * @return the archetype
     */
    String getArchetype();

    /**
     * Returns the node name.
     *
     * @return the node name. May be {@code null}
     */
    String getNode();

    /**
     * Returns the error message.
     *
     * @return the error message
     */
    String getMessage();

}
