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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.insurance.service;

import java.util.List;

/**
 * Records changes made when synchronising data.
 *
 * @author Tim Anderson
 */
public class Changes<T> {

    /**
     * The added objects.
     */
    private final List<T> added;

    /**
     * The updated objects.
     */
    private final List<T> updated;

    /**
     * The deactivated objects.
     */
    private final List<T> deactivated;

    /**
     * Constructs a {@link Changes}.
     *
     * @param added       the added objects
     * @param updated     the updated objects
     * @param deactivated the deactivated objects
     */
    public Changes(List<T> added, List<T> updated, List<T> deactivated) {
        this.added = added;
        this.updated = updated;
        this.deactivated = deactivated;
    }

    /**
     * Returns the added objects.
     *
     * @return the added objects
     */
    public List<T> getAdded() {
        return added;
    }

    /**
     * Returns the updated objects.
     *
     * @return the updated objects
     */
    public List<T> getUpdated() {
        return updated;
    }

    /**
     * Returns the deactivated objects.
     *
     * @return the deactivated objects
     */
    public List<T> getDeactivated() {
        return deactivated;
    }
}
