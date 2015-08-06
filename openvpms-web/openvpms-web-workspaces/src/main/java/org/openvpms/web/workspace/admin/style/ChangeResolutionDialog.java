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
package org.openvpms.web.workspace.admin.style;

import nextapp.echo2.app.Grid;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.OpenVPMSApp;

import java.awt.Dimension;


/**
 * A dialog that changes the effective screen resolution.
 * <p/>
 * This prompts for a new screen height and width, and when selected:
 * <ol>
 * <li>switches the application style to that of the selected resolution
 * <li>launches a new browser window with the selected resolution
 * </ol>
 * The resolution may be smaller or larger than the physical screen size.
 *
 * @author Tim Anderson
 */
public class ChangeResolutionDialog extends PopupDialog {

    /**
     * The screen width.
     */
    private final SimpleProperty width;

    /**
     * The screen height.
     */
    private final SimpleProperty height;
    private final ResolutionSelectField field;


    /**
     * Constructs a {@link ChangeResolutionDialog}.
     *
     * @param resolutions the available resolutions
     */
    public ChangeResolutionDialog(Dimension[] resolutions) {
        super(Messages.get("stylesheet.changeResolution"), OK_CANCEL);
        setModal(true);
        field = new ResolutionSelectField(resolutions);
        width = new SimpleProperty("width", Integer.class);
        width.setRequired(true);
        width.setMaxLength(4);
        height = new SimpleProperty("height", Integer.class);
        height.setRequired(true);
        height.setMaxLength(4);
        field.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                Dimension selected = (Dimension) field.getSelectedItem();
                setResolution(selected);
            }
        });
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Grid grid = GridFactory.create(2);
        grid.add(LabelFactory.create("stylesheet.resolution"));
        grid.add(field);
        StyleHelper.addProperty(grid, width);
        StyleHelper.addProperty(grid, height);
        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, grid));
    }

    /**
     * Invoked when the 'OK' button is pressed. This sets the action and closes
     * the window.
     */
    @Override
    protected void onOK() {
        if (width.isValid() && height.isValid()) {
            int w = width.getInt();
            int h = height.getInt();
            OpenVPMSApp.getInstance().createWindow(w, h);
            super.onOK();
        }
    }

    /**
     * Sets the resolution.
     *
     * @param selected the selected resolution
     */
    protected void setResolution(Dimension selected) {
        width.setValue(selected.getWidth());
        height.setValue(selected.getHeight());
    }

}
