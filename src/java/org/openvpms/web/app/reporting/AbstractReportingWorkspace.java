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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.reporting;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.subsystem.AbstractWorkspace;
import org.openvpms.web.component.util.ButtonRow;
import org.openvpms.web.component.util.SplitPaneFactory;


/**
 * Workspace for reporting purposes.
 *
 * @author Tim Anderson
 */
public abstract class AbstractReportingWorkspace<T extends IMObject>
        extends AbstractWorkspace<T> {

    /**
     * The supported workspace type.
     */
    private Class<T> type;

    /**
     * The action button row.
     */
    private ButtonRow buttons;


    /**
     * Construct a new {@code AbstractReportingWorkspace}.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identifier
     * @param type        the supported workspace type
     * @param context     the context
     */
    public AbstractReportingWorkspace(String subsystemId, String workspaceId, Class<T> type, Context context) {
        super(subsystemId, workspaceId, context);
        this.type = type;
    }

    /**
     * Sets the object to be viewed/edited by the workspace.
     *
     * @param object the object. May be {@code null}
     */
    public void setObject(T object) {
        super.setObject(object);
        if (object != null) {
            enableButtons(buttons.getButtons(), true);
        } else {
            enableButtons(buttons.getButtons(), false);
        }
    }

    /**
     * Returns the class type that this operates on.
     *
     * @return the class type that this operates on
     */
    protected Class<T> getType() {
        return type;
    }

    /**
     * Lays out the component.
     *
     * @return the component
     */
    @Override
    protected Component doLayout() {
        SplitPane root = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL, "SplitPaneWithButtonRow");
        Component heading = super.doLayout();
        root.add(heading);
        FocusGroup group = new FocusGroup("AbstractReportingWorkspace");
        buttons = new ButtonRow(group, "ControlRow", "default");
        layoutButtons(buttons.getButtons());
        enableButtons(buttons.getButtons(), false);
        SplitPane content = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP, "SplitPaneWithButtonRow",
                                                    buttons);
        doLayout(content, group);
        root.add(content);
        return root;
    }

    /**
     * Lays out the components.
     *
     * @param container the container
     * @param group     the focus group
     */
    protected void doLayout(Component container, FocusGroup group) {
    }

    /**
     * Returns the buttons.
     *
     * @return the buttons
     */
    protected ButtonSet getButtons() {
        return buttons.getButtons();
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button set
     */
    protected void layoutButtons(ButtonSet buttons) {
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    protected void enableButtons(ButtonSet buttons, boolean enable) {
    }
}
