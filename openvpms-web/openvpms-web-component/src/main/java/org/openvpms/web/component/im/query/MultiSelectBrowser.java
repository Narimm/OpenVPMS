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

package org.openvpms.web.component.im.query;

import java.util.Collection;

/**
 * A {@link Browser} that supports multiple object selection.
 *
 * @author Tim Anderson
 */
public interface MultiSelectBrowser<T> extends Browser<T> {

    /**
     * Returns the selections.
     *
     * @return the selections
     */
    Collection<T> getSelections();

    /**
     * Clears the selections.
     */
    void clearSelections();
}
