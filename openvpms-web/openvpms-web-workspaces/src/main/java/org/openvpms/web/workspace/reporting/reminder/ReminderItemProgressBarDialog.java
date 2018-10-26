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

package org.openvpms.web.workspace.reporting.reminder;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import org.openvpms.archetype.component.processor.BatchProcessorListener;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.style.Styles;

/**
 * Displays a {@link ReminderItemProgressBarProcessor} in a popup dialog, and supports cancellation.
 *
 * @author Tim Anderson
 */
class ReminderItemProgressBarDialog extends PopupDialog {

    /**
     * The processor.
     */
    private final ReminderItemProgressBarProcessor processor;

    /**
     * Constructs a {@link ReminderItemProgressBarDialog}.
     *
     * @param processor the processor
     */
    public ReminderItemProgressBarDialog(String title, String message, ReminderItemProgressBarProcessor processor) {
        super(title, CANCEL);
        this.processor = processor;
        setModal(true);
        Label label = LabelFactory.create();
        label.setText(message);
        Column column = ColumnFactory.create(Styles.WIDE_CELL_SPACING, label, processor.getComponent());
        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, column));
    }

    /**
     * Show the window.
     */
    @Override
    public void show() {
        super.show();
        processor.setListener(new BatchProcessorListener() {
            @Override
            public void completed() {
                close();
            }

            @Override
            public void error(Throwable exception) {
                ErrorHelper.show(exception);
            }
        });
        processor.process();
    }

    /**
     * Invoked when the 'cancel' button is pressed.
     */
    @Override
    protected void onCancel() {
        processor.cancel();
        super.onCancel();
    }
}
