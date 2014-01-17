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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.macro;

/**
 * Tracks the changes in cursor position, when macros are being expanded in text.
 *
 * @author Tim Anderson
 */
public class Position {

    /**
     * The initial cursor position.
     */
    private int oldPosition;

    /**
     * The new cursor position.
     */
    private int newPosition;

    /**
     * Constructs a {@link Position}.
     *
     * @param position the initial position
     */
    public Position(int position) {
        this.oldPosition = position;
        this.newPosition = position;
    }

    /**
     * Returns the initial cursor position.
     *
     * @return the initial cursor position
     */
    public int getOldPosition() {
        return oldPosition;
    }

    /**
     * Returns the new cursor position.
     *
     * @return the new cursor position
     */
    public int getNewPosition() {
        return newPosition;
    }

    /**
     * Sets the new cursor position.
     *
     * @param position the new cursor position
     */
    public void setNewPosition(int position) {
        newPosition = position;
    }
}
