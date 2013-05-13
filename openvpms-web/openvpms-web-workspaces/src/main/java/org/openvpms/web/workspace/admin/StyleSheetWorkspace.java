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
 */
package org.openvpms.web.workspace.admin;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.openvpms.web.component.style.UserStyleSheets;
import org.openvpms.web.component.subsystem.AbstractWorkspace;
import org.openvpms.web.echo.button.ButtonRow;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.OpenVPMSApp;
import org.openvpms.web.workspace.admin.style.ChangeResolutionDialog;
import org.openvpms.web.workspace.admin.style.StyleBrowser;
import org.openvpms.web.workspace.admin.style.StyleEditor;
import org.openvpms.web.workspace.admin.style.StyleHelper;

import java.awt.*;
import java.util.Iterator;
import java.util.Map;


/**
 * An experimental workspace for testing changes to style sheets.
 *
 * @author Tim Anderson
 */
public class StyleSheetWorkspace extends AbstractWorkspace {

    /**
     * The user style sheets.
     */
    private final UserStyleSheets styles;

    /**
     * The style browser.
     */
    private final StyleBrowser browser;


    /**
     * Constructs a {@code StyleSheetWorkspace}.
     */
    public StyleSheetWorkspace(Context context) {
        super("admin", "stylesheet", context);
        ContextApplicationInstance app = ContextApplicationInstance.getInstance();
        styles = app.getStyleSheets();
        browser = new StyleBrowser(styles, app.getResolution());
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
        FocusGroup group = new FocusGroup("StyleSheetWorkspace");
        ButtonRow buttons = new ButtonRow(group, "ControlRow", "default");
        buttons.addButton("add", new ActionListener() {
            public void onAction(ActionEvent event) {
                addResolution();
            }
        });
        buttons.addButton("edit", new ActionListener() {
            public void onAction(ActionEvent event) {
                editResolution();
            }
        });
        buttons.addButton("changeResolution", new ActionListener() {
            public void onAction(ActionEvent event) {
                changeResolution();
            }
        });
        buttons.addButton("revert", new ActionListener() {
            public void onAction(ActionEvent event) {
                revertChanges();
            }
        });
        buttons.addButton("export", new ActionListener() {
            public void onAction(ActionEvent event) {
                exportResolution();
            }
        });
        SplitPane content = SplitPaneFactory.create(
            SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
            "SplitPaneWithButtonRow", buttons, browser.getComponent());

        root.add(content);
        return root;
    }

    /**
     * Returns the class type that this operates on.
     *
     * @return the class type that this operates on
     */
    protected Class getType() {
        return Object.class;
    }

    /**
     * Adds properties for a resolution, using the current resolution as a template.
     */
    private void addResolution() {
        OpenVPMSApp app = OpenVPMSApp.getInstance();
        Map<String, String> properties = StyleHelper.getProperties(styles, browser.getSelectedResolution(), false);
        Dimension size = app.getResolution();
        editProperties(size, properties, true);
    }

    /**
     * Edits properties for the selected resolution.
     */
    private void editResolution() {
        Dimension size = browser.getSelectedResolution();
        Map<String, String> properties = StyleHelper.getProperties(styles, size, false);
        editProperties(size, properties, false);
    }

    /**
     * Edits properties for a resolution.
     *
     * @param size       the screen resolution
     * @param properties the properties to edit
     * @param editSize   determines if the screen resolution can be edited
     */
    private void editProperties(Dimension size, Map<String, String> properties, boolean editSize) {
        if (properties != null) {
            final StyleEditor editor = new StyleEditor(size, properties, styles, editSize);
            editor.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    Dimension size = editor.getSize();
                    if (size != null) {
                        OpenVPMSApp app = OpenVPMSApp.getInstance();
                        if (StyleHelper.ANY_RESOLUTION.equals(size)) {
                            styles.setDefaultProperties(editor.getProperties());
                        } else {
                            styles.setProperties(editor.getProperties(), size.width, size.height);
                        }
                        app.setStyleSheet(); // refresh the display with the style sheet changes (if any)
                        browser.refresh();
                    }
                }
            });
            editor.show();
        }
    }

    /**
     * Changes the screen resolution.
     */
    private void changeResolution() {
        ChangeResolutionDialog dialog = new ChangeResolutionDialog();
        dialog.show();
    }

    /**
     * Reverts style sheet changes.
     */
    private void revertChanges() {
        ConfirmationDialog dialog = new ConfirmationDialog(Messages.get("stylesheet.revert.title"),
                                                           Messages.get("stylesheet.revert.message"));
        dialog.show();
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                styles.reset();
                browser.refresh();
                ContextApplicationInstance.getInstance().setStyleSheet();
            }
        });
    }

    /**
     * Exports the selected properties.
     */
    private void exportResolution() {
        Dimension size = browser.getSelectedResolution();
        Map<String, String> properties = StyleHelper.getProperties(styles, size, false);
        StringBuilder builder = new StringBuilder();

        if (!StyleHelper.ANY_RESOLUTION.equals(size)) {
            builder.append("# Properties for screen resolution: ").append(size.width).append("x").append(size.height);

            Map<String, String> defaults = styles.getDefaultProperties();
            Iterator<Map.Entry<String, String>> iter = properties.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> entry = iter.next();
                String value = defaults.get(entry.getKey());
                if (ObjectUtils.equals(entry.getValue(), value)) {
                    iter.remove();
                }
            }
        } else {
            builder.append("# Default properties for all screen resolutions");
        }
        builder.append('\n');
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            builder.append(entry.getKey());
            builder.append(" = ");
            builder.append(entry.getValue());
            builder.append('\n');
        }
        InformationDialog.show(builder.toString());
    }


}
