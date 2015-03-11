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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.workflow;

import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.event.WindowPaneListener;

/**
 * A task that displays an {@link InformationDialog}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractInformationTask extends AbstractTask {

    /**
     * Determines what to do when OK is clicked. If {@code false}, invoke {@link #notifyCompleted()}, else invoke
     * {@link #notifyCancelled()}.
     */
    private final boolean cancel;

    /**
     * Constructs an {@link AbstractInformationTask}.
     *
     * @param cancel if {@code false}, invoke {@link #notifyCompleted()} on OK, else invoke {@link #notifyCancelled()}
     */
    public AbstractInformationTask(boolean cancel) {
        this.cancel = cancel;
    }

    /**
     * Starts the task.
     *
     * @param context the task context
     */
    @Override
    public void start(TaskContext context) {
        InformationDialog dialog = new InformationDialog(getTitle(), getMessage());
        dialog.addWindowPaneListener(new WindowPaneListener() {
            @Override
            public void onClose(WindowPaneEvent event) {
                if (cancel) {
                    notifyCancelled();
                } else {
                    notifyCompleted();
                }
            }
        });
        dialog.show();
    }

    /**
     * Returns the title.
     *
     * @return the title
     */
    protected abstract String getTitle();

    /**
     * Returns the message.
     *
     * @return the message
     */
    protected abstract String getMessage();

}
