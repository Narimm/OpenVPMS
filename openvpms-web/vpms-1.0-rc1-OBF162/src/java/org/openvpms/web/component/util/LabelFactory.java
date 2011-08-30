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

import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.Label;


/**
 * Factory for {@link Label}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public final class LabelFactory extends ComponentFactory {

    /**
     * Component type.
     */
    private static final String TYPE = "label";


    /**
     * Create a new label, with the default style.
     *
     * @return a new label
     */
    public static Label create() {
        Label label = new Label();
        setDefaultStyle(label);
        return label;
    }

    /**
     * Create a new label with an image.
     *
     * @param image the image
     * @return a new label.
     */
    public static Label create(ImageReference image) {
        Label label = create();
        label.setIcon(image);
        return label;
    }

    /**
     * Create a new label with localised text, and default style.
     *
     * @param key the resource bundle key. May be <code>null</code>
     * @return a new label
     */
    public static Label create(String key) {
        Label label = create();
        if (key != null) {
            label.setText(getString(TYPE, key, false));
        }
        return label;
    }

    /**
     * Create a new label with localised text, and specific style.
     *
     * @param key the resource bundle key. May be <code>null</code>
     * @return a new label
     */
    public static Label create(String key, String style) {
        Label label = create(key);
        setStyle(label, style);
        return label;
    }

}
