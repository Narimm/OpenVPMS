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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.admin.style;

import nextapp.echo2.app.Grid;
import org.openvpms.web.app.OpenVPMSApp;
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.echo.i18n.Messages;

import java.awt.*;


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
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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


    /**
     * Constructs a <tt>ChangeResolutionDialog</tt>.
     */
    public ChangeResolutionDialog() {
        super(Messages.get("stylesheet.changeResolution"), OK_CANCEL);
        setModal(true);
        Dimension size = ContextApplicationInstance.getInstance().getResolution();
        width = new SimpleProperty("width", size.width, Integer.class);
        height = new SimpleProperty("height", size.height, Integer.class);
        width.setValue(size.width);
        width.setMaxLength(4);
        height.setValue(size.height);
        height.setMaxLength(4);
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Grid grid = GridFactory.create(2);
        StyleHelper.addProperty(grid, width);
        StyleHelper.addProperty(grid, height);
        getLayout().add(grid);
    }

    /**
     * Invoked when the 'OK' button is pressed. This sets the action and closes
     * the window.
     */
    @Override
    protected void onOK() {
        if (width.isValid() && height.isValid()) {
            int w = (Integer) width.getValue();
            int h = (Integer) height.getValue();
            // ContextApplicationInstance.getInstance().setStyleSheet(w, h);
            OpenVPMSApp.getInstance().createWindow(w, h);
        }
        super.onOK();
    }
}
