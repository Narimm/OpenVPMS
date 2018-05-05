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

package org.openvpms.smartflow.client;

/**
 * Smart Flow Sheet synchronisation state.
 *
 * @author Tim Anderson
 */
public class SyncState {

    private final int added;

    private final int updated;

    private final int removed;

    /**
     * Constructs a {@link SyncState}.
     *
     * @param added   the number of added objects
     * @param updated the number of updated objects
     * @param removed the number of removed objects
     */
    public SyncState(int added, int updated, int removed) {
        this.added = added;
        this.updated = updated;
        this.removed = removed;
    }

    /**
     * Returns the number of added objects.
     *
     * @return the number of added objects
     */
    public int getAdded() {
        return added;
    }

    /**
     * Returns the number of updated objects.
     *
     * @return the number of updated objects
     */
    public int getUpdated() {
        return updated;
    }

    /**
     * Returns the number of removed objects.
     *
     * @return the number of removed objects
     */
    public int getRemoved() {
        return removed;
    }

    /**
     * Determines if synchronisation changed anything.
     *
     * @return {@code true} if there were changes
     */
    public boolean changed() {
        return added != 0 || updated != 0 || removed != 0;
    }
}
