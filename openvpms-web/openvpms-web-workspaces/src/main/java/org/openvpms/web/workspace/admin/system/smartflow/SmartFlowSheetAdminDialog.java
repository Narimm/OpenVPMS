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

package org.openvpms.web.workspace.admin.system.smartflow;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.event.SmartFlowSheetEventService;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.AbstractBrowserListener;
import org.openvpms.web.component.im.query.AbstractQueryBrowser;
import org.openvpms.web.component.im.query.ListQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.ReadOnlyComponentFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ModalDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextArea;
import org.openvpms.web.echo.text.TextComponent;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Smart Flow Sheet administration dialog.
 * <p>
 * Provides support to display Smart Flow Sheet status per location.
 *
 * @author Tim Anderson
 */
public class SmartFlowSheetAdminDialog extends ModalDialog {

    /**
     * The event service.
     */
    private final SmartFlowSheetEventService service;

    /**
     * The status browser.
     */
    private final StatusBrowser browser;

    /**
     * The statuses.
     */
    private final List<Status> statuses;

    /**
     * The container for displaying the selected status.
     */
    private final Column container;

    /**
     * The component factory.
     */
    private final ReadOnlyComponentFactory componentFactory;

    /**
     * Constructs a {@link SmartFlowSheetAdminDialog}.
     *
     * @param context the context
     * @param help    the help context
     */
    public SmartFlowSheetAdminDialog(Context context, HelpContext help) {
        super(Messages.get("admin.system.smartflow.title"), "BrowserDialog", OK);
        Party practice = context.getPractice();
        PracticeRules rules = ServiceHelper.getBean(PracticeRules.class);
        service = ServiceHelper.getBean(SmartFlowSheetEventService.class);
        FlowSheetServiceFactory factory = ServiceHelper.getBean(FlowSheetServiceFactory.class);
        PracticeService practiceService = ServiceHelper.getBean(PracticeService.class);

        List<Party> locations = new ArrayList<>();
        locations.addAll(rules.getLocations(practice));
        locations.sort((o1, o2) -> ObjectUtils.compare(o1.getName(), o2.getName()));

        statuses = new ArrayList<>();
        for (Party location : locations) {
            statuses.add(new Status(location, service, factory, practiceService));
        }

        ListQuery<Status> query = new ListQuery<>(statuses, PracticeArchetypes.LOCATION, Status.class);

        DefaultLayoutContext layout = new DefaultLayoutContext(context, help);
        componentFactory = new ReadOnlyComponentFactory(new DefaultLayoutContext(layout));
        browser = new StatusBrowser(query, layout);
        browser.addBrowserListener(new AbstractBrowserListener<Status>() {
            @Override
            public void selected(Status object) {
                onSelected(object);
            }

        });
        container = ColumnFactory.create(Styles.LARGE_INSET);
        SplitPane pane = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_TOP_BOTTOM,
                                                 "BrowserCRUDWorkspace.Layout",
                                                 browser.getComponent(),
                                                 container);
        getLayout().add(pane);

        ButtonSet buttons = getButtons();
        buttons.add("button.refresh", new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                refresh();
            }
        });
        buttons.add("button.restartsfs", new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onRestart();
            }
        });
    }

    /**
     * Displays the selected status.
     *
     * @param object the status
     */
    private void onSelected(Status object) {
        container.removeAll();
        Property id = createProperty("id", object.getId(), Long.class, "table.imobject.id");
        Property name = createProperty("name", object.getName(), String.class, "table.imobject.name");
        Property key = createProperty("key", object.getDisplayKey(), String.class, "admin.system.smartflow.key");
        Property apiStatus = createProperty("apistatus", object.getAPIStatusName(), String.class,
                                            "admin.system.smartflow.apistatus");
        Property queueStatus = createProperty("queuestatus", object.getQueueStatusName(), String.class,
                                              "admin.system.smartflow.queuestatus");
        String receivedTime = object.getEventReceived() != null
                              ? DateFormatter.formatDateTimeAbbrev(object.getEventReceived()) : null;
        Property received = createProperty("received", receivedTime, String.class, "admin.system.smartflow.received");
        ComponentGrid grid = new ComponentGrid();
        grid.add(createComponent(id));
        grid.add(createComponent(name));
        grid.add(createComponent(key, 10));
        ComponentState apiComponent = createComponent(apiStatus, 20);
        if (object.getAPIStatus() == Status.ComponentStatus.ERROR) {
            grid.add(apiComponent.getLabel(), createError(object.getAPIError()));
        } else {
            grid.add(apiComponent);
        }
        if (object.getQueueStatus() == Status.ComponentStatus.CONNECTED ||
            object.getQueueStatus() == Status.ComponentStatus.ERROR) {
            ComponentState queueComponent = createComponent(queueStatus, 20);
            if (object.getQueueStatus() == Status.ComponentStatus.ERROR) {
                grid.add(queueComponent.getLabel(), createError(object.getQueueError()));
            } else {
                grid.add(queueComponent);
            }
            grid.add(createComponent(received, 10));
        }
        container.add(grid.createGrid());
    }

    /**
     * Helper to create a component to display an error message.
     *
     * @param message the message
     * @return a new component
     */
    private Component createError(String message) {
        TextArea area = TextComponentFactory.createTextArea(80, 4);
        area.setText(message);
        area.setEnabled(false);
        return area;
    }

    /**
     * Helper to create a component for a property.
     *
     * @param property the property
     * @param length   the display length
     * @return a new component
     */
    private ComponentState createComponent(Property property, int length) {
        ComponentState state = new ComponentState(componentFactory.create(property), property);
        if (state.getComponent() instanceof TextComponent) {
            ((TextComponent) state.getComponent()).setWidth(new Extent(length, Extent.EM));
        }
        return state;
    }

    /**
     * Helper to create a component for a property.
     *
     * @param property the property
     * @return a new component
     */
    private ComponentState createComponent(Property property) {
        return new ComponentState(componentFactory.create(property), property);
    }

    /**
     * Helper to create a new read-only property.
     *
     * @param name  the property name
     * @param value the property value
     * @param type  the property type
     * @param key   the localisation key
     * @return a new property
     */
    private Property createProperty(String name, Object value, Class<?> type, String key) {
        return new SimpleProperty(name, value, type, Messages.get(key), true);
    }

    /**
     * Confirms to restart Smart Flow Sheet.
     */
    private void onRestart() {
        ConfirmationDialog.show(Messages.get("admin.system.smartflow.restartqueue.title"),
                                Messages.get("admin.system.smartflow.restartqueue.message"),
                                ConfirmationDialog.YES_NO, new PopupDialogListener() {
                    @Override
                    public void onOK() {
                        service.restart();
                        refresh();
                    }
                });
    }

    /**
     * Refreshes statuses.
     */
    private void refresh() {
        for (Status status : statuses) {
            status.refresh();
        }
        browser.query();
    }

    private static class StatusBrowser extends AbstractQueryBrowser<Status> {

        /**
         * Constructs an {@link StatusBrowser}.
         *
         * @param query   the query
         * @param context the layout context
         */
        public StatusBrowser(Query<Status> query, LayoutContext context) {
            super(query, null, new StatusTableModel(), context);
        }
    }
}
