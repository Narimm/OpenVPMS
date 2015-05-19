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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment.repeat;

import org.openvpms.web.component.edit.AbstractPropertyEditor;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.focus.FocusGroup;

/**
 * Abstract implementation of the {@link RepeatUntilEditor} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractRepeatUntilEditor extends AbstractPropertyEditor implements RepeatUntilEditor {

    /**
     * The focus group.
     */
    private final FocusGroup group = new FocusGroup(getClass().getSimpleName());

    /**
     * Constructs an {@link AbstractRepeatUntilEditor}.
     *
     * @param property the property being edited
     */
    public AbstractRepeatUntilEditor(Property property) {
        super(property);
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    @Override
    public FocusGroup getFocusGroup() {
        return group;
    }
}
