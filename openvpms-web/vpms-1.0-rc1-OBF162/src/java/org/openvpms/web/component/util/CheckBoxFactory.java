/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.util;

import nextapp.echo2.app.CheckBox;


/**
 * Factory for {@link CheckBox}es.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class CheckBoxFactory extends ComponentFactory {

    /**
     * Creates a new check box.
     *
     * @return a new check box
     */
    public static CheckBox create() {
        return new CheckBox();
    }

    /**
     * Creates a new check box with localised label.
     *
     * @param key the resource bundle key. May be <code>null</code>
     * @return a new check box
     */
    public static CheckBox create(String key) {
        CheckBox box = create();
        if (key != null) {
            box.setText(getString("label", key, false));
        }
        return box;
    }

    /**
     * Creates a new check box with localised label and initial value.
     *
     * @param key   the resource bundle key. May be <code>null</code>
     * @param value the initial value
     * @return a new check box
     */
    public static CheckBox create(String key, boolean value) {
        CheckBox box = create(key);
        box.setSelected(value);
        return box;
    }

    /**
     * Creates a new check box with initial value.
     *
     * @param value the initial value
     * @return a new check box
     */
    public static CheckBox create(boolean value) {
        CheckBox box = create();
        box.setSelected(value);
        return box;
    }

}
