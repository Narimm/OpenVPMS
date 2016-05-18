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

package org.openvpms.web.workspace.admin.style;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.DefaultListModel;
import nextapp.echo2.app.list.ListCellRenderer;
import nextapp.echo2.app.list.ListModel;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.resource.i18n.Messages;

import java.awt.Dimension;

/**
 * Select field for browser resolutions.
 *
 * @author Tim Anderson
 */
public class ResolutionSelectField extends SelectField {

    /**
     * Constructs a {@link ResolutionSelectField}.
     *
     * @param resolutions the resolutions
     */
    public ResolutionSelectField(Dimension[] resolutions) {
        SelectFieldFactory.setDefaultStyle(this);
        ListModel model = createModel(resolutions);
        setModel(model);
        if (model.size() != 0) {
            setSelectedIndex(0);
        }
        setCellRenderer(new ListCellRenderer() {
            public Object getListCellRendererComponent(Component list, Object value, int index) {
                Dimension size = (Dimension) value;
                if (size.equals(StyleHelper.ANY_RESOLUTION)) {
                    return Messages.get("stylesheet.anyresolution");
                }
                return Messages.format("stylesheet.size", size.width, size.height);
            }
        });
    }

    public boolean contains(Dimension resolution) {
        return ((DefaultListModel) getModel()).indexOf(resolution) != -1;
    }

    public static ListModel createModel(Dimension[] resolutions) {
        return new DefaultListModel(resolutions);
    }

}
