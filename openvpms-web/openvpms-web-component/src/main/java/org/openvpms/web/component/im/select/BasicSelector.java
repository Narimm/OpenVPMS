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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.select;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Component that provides a 'select' button and non-editable object summary.
 *
 * @author Tim Anderson
 */
public class BasicSelector<T extends IMObject> extends Selector<T> {

    /**
     * Constructs a {@link BasicSelector}.
     */
    public BasicSelector() {
        this(BUTTON_ID);
    }

    /**
     * Construct a {@link BasicSelector}.
     *
     * @param buttonId the button identifier
     */
    public BasicSelector(String buttonId) {
        this(buttonId, true);
    }

    /**
     * Construct a {@link BasicSelector}.
     *
     * @param buttonId        the button identifier
     * @param enableShortcuts if {@code true}, include button shortcuts
     */
    public BasicSelector(String buttonId, boolean enableShortcuts) {
        super(buttonId, ButtonStyle.LEFT, false, enableShortcuts);
    }

    /**
     * Sets the current object details.
     *
     * @param name        the object name. May be {@code null}
     * @param description the object description. May be {@code null}
     * @param active      determines if the object is active
     */
    @Override
    public void setObject(String name, String description, boolean active) {
        super.setObject(name, description, active);
    }
}
