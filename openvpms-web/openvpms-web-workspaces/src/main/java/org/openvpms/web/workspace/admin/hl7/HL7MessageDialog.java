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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.hl7;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.hl7.io.MessageDispatcher;
import org.openvpms.hl7.util.HL7Archetypes;
import org.openvpms.hl7.util.HL7MessageStatuses;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.BrowserListener;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;

/**
 * Dialog to browse and display HL7 messages for a connector.
 *
 * @author Tim Anderson
 */
public class HL7MessageDialog extends PopupDialog {

    /**
     * The context.
     */
    private final Context context;

    /**
     * The ,message browser.
     */
    private final Browser<Act> browser;

    /**
     * The container for the message.
     */
    private Column messageContainer;

    /**
     * Determines if messages can be resubmitted to the connector.
     */
    private final boolean isSender;

    /**
     * Resubmit button identifier.
     */
    private static final String RESUBMIT_ID = "button.resubmit";

    /**
     * Dequeue message button identifier.
     */
    private static final String DEQUEUE_ID = "button.dequeue";

    /**
     * The message statuses.
     */
    private static final ActStatuses STATUSES = new ActStatuses(HL7Archetypes.MESSAGE);

    static {
        STATUSES.setDefault((String) null);
    }

    /**
     * Constructs a {@link HL7MessageDialog}.
     *
     * @param connector the connector
     * @param context   the context
     * @param help      the help context. May be {@code null}
     */
    public HL7MessageDialog(Entity connector, Context context, HelpContext help) {
        super(Messages.format("admin.hl7.messages.title", connector.getName()), OK, help);
        setModal(true);
        setStyleName("BrowserDialog");
        this.context = context;
        DefaultActQuery<Act> query = new DefaultActQuery<Act>(connector, "connector", "participation.HL7Connector",
                HL7Archetypes.MESSAGE, STATUSES);
        query.setStatus(null);
        query.setAuto(true);
        isSender = isSender(connector);
        if (isSender) {
            addButton(RESUBMIT_ID, new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onResubmit();
                }
            });
            addButton(DEQUEUE_ID, new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onDequeue();
                }
            });
        }
        DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, help);
        TableComponentFactory factory = new TableComponentFactory(layoutContext) {
            @Override
            protected String getDateValue(Property property) {
                Date value = (Date) property.getValue();
                return (value != null) ? DateFormatter.formatDateTimeAbbrev(value) : null;
            }

            @Override
            protected Component createString(Property property) {
                // display labels with new-lines
                return super.createLabel(property, true);
            }
        };
        layoutContext.setComponentFactory(factory);
        browser = BrowserFactory.create(query, layoutContext);
        browser.addBrowserListener(new BrowserListener<Act>() {
            @Override
            public void selected(Act object) {
                onSelected(object);
            }

            @Override
            public void browsed(Act object) {
            }

            @Override
            public void query() {
            }
        });
    }

    /**
     * Invoked to resubmit a message that was rejected.
     */
    private void onResubmit() {
        if (isSender) {
            DocumentAct act = (DocumentAct) IMObjectHelper.reload(browser.getSelected());
            if (act != null && HL7MessageStatuses.ERROR.equals(act.getStatus())) {
                ServiceHelper.getBean(MessageDispatcher.class).resubmit(act);
                act.setStatus(HL7MessageStatuses.PENDING);
                SaveHelper.save(act);
            }
            refresh();
        }
    }

    /**
     * Invoked to dequeue a message.
     */
    private void onDequeue() {
        if (isSender) {
            final DocumentAct act = (DocumentAct) IMObjectHelper.reload(browser.getSelected());
            if (act != null && HL7MessageStatuses.PENDING.equals(act.getStatus())) {
                String title = Messages.get("admin.hl7.dequeue.title");
                String message = Messages.get("admin.hl7.dequeue.message");
                ConfirmationDialog dialog = new ConfirmationDialog(title, message, ConfirmationDialog.YES_NO);
                dialog.addWindowPaneListener(new PopupDialogListener() {
                    @Override
                    public void onYes() {
                        act.setStatus(HL7MessageStatuses.CANCELLED);
                        SaveHelper.save(act);
                        refresh();
                    }
                });
                dialog.show();
            } else {
                // need a refresh.
                refresh();
            }
        }
    }

    /**
     * Lays out the component prior to display.
     * This implementation is a no-op.
     */
    @Override
    protected void doLayout() {
        messageContainer = ColumnFactory.create(Styles.INSET);
        SplitPane container = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL, "BrowserCRUDWorkspace.Layout",
                browser.getComponent(), messageContainer);
        getLayout().add(container);
        super.doLayout();
    }

    /**
     * Refreshes the message display.
     */
    private void refresh() {
        browser.query();
        onSelected(browser.getSelected());
    }

    /**
     * Invoked to display a message, or remove any existing message if no message is selected.
     *
     * @param object the message. May be {@code null}
     */
    private void onSelected(Act object) {
        messageContainer.removeAll();
        ButtonSet buttons = getButtons();
        if (object != null) {
            IMObjectViewer viewer = new IMObjectViewer(object, new DefaultLayoutContext(context, getHelpContext()));
            messageContainer.add(viewer.getComponent());
            if (isSender) {
                String status = object.getStatus();
                buttons.setEnabled(RESUBMIT_ID, HL7MessageStatuses.ERROR.equals(status));
                buttons.setEnabled(DEQUEUE_ID, HL7MessageStatuses.PENDING.equals(status));
            }
        } else if (isSender) {
            buttons.setEnabled(RESUBMIT_ID, false);
            buttons.setEnabled(DEQUEUE_ID, false);
        }
    }

    /**
     * Determines if a connector is a sender.
     *
     * @param connector the connector
     * @return {@code true} if the connector is a sender
     */
    private boolean isSender(Entity connector) {
        return TypeHelper.isA(connector, HL7Archetypes.SENDERS);
    }

}
