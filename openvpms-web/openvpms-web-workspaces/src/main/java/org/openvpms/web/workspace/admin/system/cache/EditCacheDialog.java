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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.system.cache;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.RadioButton;
import nextapp.echo2.app.button.ButtonGroup;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.dialog.MessageDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.Messages;

/**
 * Prompts to change the maximum elements of a cache.
 *
 * @author Tim Anderson
 */
class EditCacheDialog extends MessageDialog {

    /**
     * The current cache size.
     */
    private final SimpleProperty currentSize;

    /**
     * The suggested cache size.
     */
    private final SimpleProperty suggestedSize;

    /**
     * The new cache size.
     */
    private final SimpleProperty newSize;

    /**
     * The suggested button.
     */
    private final RadioButton suggestedButton;

    /**
     * The new button.
     */
    private final RadioButton newButton;

    /**
     * Constructs a {@link EditCacheDialog}.
     *
     * @param cacheName the cache name
     * @param maxCount  the current maximum count
     * @param suggested the suggested count
     * @param help      the help context
     */
    public EditCacheDialog(String cacheName, long maxCount, long suggested, HelpContext help) {
        super(Messages.get("admin.system.cache.edit.title"),
              Messages.format("admin.system.cache.edit.message", cacheName), OK_CANCEL, help);
        currentSize = new SimpleProperty("current", maxCount, Long.class,
                                         Messages.get("admin.system.cache.edit.current"), true);
        suggestedSize = new SimpleProperty("suggested", suggested, Long.class,
                                           Messages.get("admin.system.cache.edit.suggested"), true);
        newSize = new SimpleProperty("new", null, Long.class, Messages.get("admin.system.cache.edit.new"));

        ButtonGroup group = new ButtonGroup();
        suggestedButton = ButtonFactory.create("admin.system.cache.edit.suggested", group, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                enableOK();
            }
        });
        newButton = ButtonFactory.create("admin.system.cache.edit.new", group);

        newSize.addModifiableListener(modifiable -> {
            newButton.setSelected(true);
            enableOK();
        });
        enableOK();
    }

    /**
     * Returns the new maximum count.
     *
     * @return the maximum count or <= 0 if one hasn't been entered
     */
    public long getCount() {
        return suggestedButton.isSelected() ? suggestedSize.getLong()
                                            : newButton.isSelected() ? newSize.getLong()
                                                                     : -1;
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Label content = LabelFactory.create(true, true);
        content.setText(getMessage());

        TextField current = BoundTextComponentFactory.create(currentSize, 10);
        current.setEnabled(false);

        TextField suggested = BoundTextComponentFactory.create(suggestedSize, 10);
        suggested.setEnabled(false);

        ComponentGrid grid = new ComponentGrid();
        grid.add(new ComponentState(current, currentSize));
        TextField input = BoundTextComponentFactory.create(newSize, 10);
        grid.add(suggestedButton, suggested);
        grid.add(newButton, input);
        FocusGroup group = getFocusGroup();
        group.add(suggested);
        group.add(newButton);
        group.add(input);

        Column column = ColumnFactory.create(Styles.WIDE_CELL_SPACING, content, grid.createGrid());
        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, column));
    }

    /**
     * Enables the OK button, if the new cache size is greater than 0.
     */
    private void enableOK() {
        boolean enabled = suggestedButton.isSelected() || (newButton.isSelected() && newSize.getLong() > 0);
        getButtons().setEnabled(OK_ID, enabled);
    }
}
