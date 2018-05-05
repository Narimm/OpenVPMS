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

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.layout.GridLayoutData;
import org.apache.commons.lang.StringUtils;
import org.openvpms.web.component.edit.AlertListener;
import org.openvpms.web.component.util.StyleSheetHelper;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.style.Styles;

import java.util.ArrayList;
import java.util.List;

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
     * The listener to handle alert messages.
     */
    private final AlertListener listener;

    /**
     * The maximum number of alerts to display.
     */
    private final int maxAlerts;

    /**
     * The current alerts.
     */
    private List<WindowPane> alerts = new ArrayList<>();

    /**
     * The default offset for alerts.
     */
    private static final int OFFSET = 5;

    /**
     * Constructs an {@link AlertManager}.
     *
     * @param container the container used to locate the parent for alerts; Alerts will be registered on the nearest
     *                  parent ContentPane
     */
    public AlertManager(Component container, int maxAlerts) {
        this.container = container;
        this.maxAlerts = maxAlerts;
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
     * Clear any alerts.
     */
    public void clear() {
        for (WindowPane alert : alerts.toArray(new WindowPane[alerts.size()])) {
            alert.userClose();
        }
    }

    /**
     * Invoked to display an alert.
     *
     * @param message the alert message
     * @return a handle to cancel the alert
     */
    protected long showAlert(String message) {
        if (alerts.size() < maxAlerts) {
            final WindowPane pane = new InformationMessage(message);
            int y = OFFSET;
            for (WindowPane alert : alerts) {
                y = getNextY(alert);
            }
            pane.setPositionY(new Extent(y));
            getContentPane().add(pane);
            pane.addWindowPaneListener(new WindowPaneListener() {
                @Override
                public void onClose(WindowPaneEvent event) {
                    remove(pane);
                }
            });
            alerts.add(pane);
            return System.identityHashCode(pane);
        }
        return -1;
    }

    /**
     * Returns the Y position of the next alert.
     *
     * @param alert the alert to position relative to
     * @return the next Y position
     */
    private int getNextY(WindowPane alert) {
        Extent position = alert.getPositionY();
        int y = (position != null) ? position.getValue() : OFFSET;
        Extent height = alert.getHeight();
        if (height != null) {
            y += height.getValue() + OFFSET;
        } else {
            y += OFFSET;
        }
        return y;
    }

    /**
     * Removes an alert and repositions the remaining alerts.
     *
     * @param alert the alert to remove
     */
    private void remove(WindowPane alert) {
        alerts.remove(alert);
        shuffle();
    }

    /**
     * Adjusts the positions of the alerts.
     */
    private void shuffle() {
        int y = OFFSET;
        for (WindowPane alert : alerts) {
            alert.setPositionY(new Extent(y));
            y = getNextY(alert);
        }
    }

    /**
     * Returns the content pane for alerts.
     *
     * @return the content pane
     */
    protected Component getContentPane() {
        Component component = container;
        while (component != null && !(component instanceof ContentPane)) {
            component = component.getParent();
        }
        if (component == null) {
            component = ApplicationInstance.getActive().getDefaultWindow().getContent();
        }
        return component;
    }

    /**
     * Invoked to cancel an alert.
     *
     * @param id the alert identifier
     */
    protected void cancelAlert(long id) {
        for (WindowPane alert : alerts.toArray(new WindowPane[alerts.size()])) {
            if (System.identityHashCode(alert) == id) {
                alert.userClose();
            }
        }
    }

    private static class InformationMessage extends WindowPane {
        public InformationMessage(String message) {
            setStyleName("InformationMessage");
            setClosable(false);
            setPositionX(new Extent(OFFSET));
            setPositionY(new Extent(OFFSET));
            int fontSize = StyleSheetHelper.getProperty("font.size", 10);
            Extent height = getHeight(message, fontSize);
            setHeight(height);
            setMinimumHeight(height);
            Label label = LabelFactory.create(true, true);
            label.setStyleName("InformationMessage");
            label.setText(message);
            Button button = ButtonFactory.create(null, "Message.close");
            button.addActionListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    userClose();
                }
            });

            GridLayoutData layoutData = new GridLayoutData();
            label.setLayoutData(layoutData);
            Grid grid = GridFactory.create(2, label, button);
            grid.setColumnWidth(0, Styles.FULL_WIDTH);
            grid.setWidth(Styles.FULL_WIDTH);
            grid.setHeight(Styles.FULL_HEIGHT);
            add(grid);
        }

        /**
         * Returns the height of the alert.
         *
         * @param message    the message
         * @param fontSize the font size
         * @return the height
         */
        private Extent getHeight(String message, int fontSize) {
            int lines = StringUtils.countMatches(message, "\n");
            lines += 3; // at least one line + 2 for padding
            return new Extent(fontSize * lines);
        }
    }
}
