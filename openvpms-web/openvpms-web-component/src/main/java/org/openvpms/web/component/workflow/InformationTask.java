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

import org.openvpms.web.echo.dialog.InformationDialog;


/**
 * A task that displays an {@link InformationDialog}.
 *
 * @author Tim Anderson
 */
public class InformationTask extends AbstractInformationTask {

    /**
     * The dialog title.
     */
    private final String title;

    /**
     * The dialog message.
     */
    private final String message;


    /**
     * Constructs an {@link InformationTask}.
     *
     * @param title   the dialog title
     * @param message the dialog message
     */
    public InformationTask(String title, String message) {
        this(title, message, false);
    }

    /**
     * Constructs an {@link InformationTask}.
     *
     * @param title   the dialog title
     * @param message the dialog message
     * @param cancel  if {@code false}, invoke {@link #notifyCompleted()} on OK, else invoke {@link #notifyCancelled()}
     */
    public InformationTask(String title, String message, boolean cancel) {
        super(cancel);
        this.title = title;
        this.message = message;
    }

    /**
     * Returns the title.
     *
     * @return the title
     */
    @Override
    protected String getTitle() {
        return title;
    }

    /**
     * Returns the message.
     *
     * @return the message
     */
    @Override
    protected String getMessage() {
        return message;
    }
}
