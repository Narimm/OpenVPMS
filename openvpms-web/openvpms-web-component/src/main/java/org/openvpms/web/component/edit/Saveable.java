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

package org.openvpms.web.component.edit;


import org.openvpms.component.exception.OpenVPMSException;

/**
 * Interface to track the saved status of an object.
 *
 * @author Tim Anderson
 */
public interface Saveable {

    /**
     * Save any edits.
     *
     * @throws OpenVPMSException if the save fails
     */
    void save();

    /**
     * Determines if any edits have been saved.
     *
     * @return {@code true} if edits have been saved.
     */
    boolean isSaved();

}
