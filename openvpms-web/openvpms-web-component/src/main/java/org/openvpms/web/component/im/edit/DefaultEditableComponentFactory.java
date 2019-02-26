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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit;


import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.style.Styles;

import java.util.function.Consumer;

/**
 * A factory for editable components.
 *
 * @author Tim Anderson
 */
public class DefaultEditableComponentFactory extends AbstractEditableComponentFactory {

    /**
     * Constructs a {@link DefaultEditableComponentFactory}.
     *
     * @param context the layout context.
     */
    public DefaultEditableComponentFactory(LayoutContext context) {
        super(context, Styles.EDIT);
    }

    /**
     * Constructs a {@link DefaultEditableComponentFactory}.
     *
     * @param context        the layout context.
     * @param editorListener invoked when an editor is created. May be {@code null}
     */
    public DefaultEditableComponentFactory(LayoutContext context, Consumer<Editor> editorListener) {
        super(context, Styles.EDIT, editorListener);
    }

}
