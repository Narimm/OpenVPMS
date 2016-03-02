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

package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.Component;
import org.openvpms.web.component.edit.AlertListener;
import org.openvpms.web.echo.message.InformationMessage;

/**
 * Manages the display of alerts.
 *
 * @author Tim Anderson
 */
public class AlertManager {

    /**
     * The container.
     */
    private final Component container;

    /**
     * The listener to handle alert messages. These are added to the container.
     */
    private final AlertListener listener;

    /**
     * Constructs an {@link AlertManager}.
     *
     * @param container the container for alerts. This may hold other components; the alerts will be displayed at the head
     */
    public AlertManager(Component container) {
        this.container = container;
        listener = new AlertListener() {
            @Override
            public long onAlert(String message) {
                return showAlert(message);
            }

            @Override
            public void cancel(long id) {
                cancelAlert(id);
            }
        };
    }

    /**
     * Returns the alert listener.
     *
     * @return the alert listener
     */
    public AlertListener getListener() {
        return listener;
    }

    /**
     * Invoked to display an alert.
     *
     * @param message the alert message
     * @return a handle to cancel the alert
     */
    protected long showAlert(String message) {
        InformationMessage alert = new InformationMessage(message);
        container.add(alert, 0);
        return System.identityHashCode(alert);
    }

    /**
     * Invoked to cancel an alert.
     *
     * @param id the alert identifier
     */
    protected void cancelAlert(long id) {
        for (Component component : container.getComponents()) {
            if (component instanceof InformationMessage && System.identityHashCode(component) == id) {
                container.remove(component);
                break;
            }
        }
    }

}
