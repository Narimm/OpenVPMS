/*
 * Version: 1.0
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
 *  Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.admin.organisation;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.webcontainer.command.BrowserOpenWindowCommand;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.subscription.core.Subscription;
import org.openvpms.subscription.core.SubscriptionFactory;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.util.DateHelper;
import org.openvpms.web.system.ServiceHelper;

import java.io.InputStream;
import java.util.Date;

/**
 * Layout strategy for <em>act.subscription</em>.
 *
 * @author Tim Anderson
 */
public class SubscriptionViewer {

    /**
     * The root component.
     */
    private Component root;

    /**
     * The <em>act.subscription</em>.
     */
    private DocumentAct act;

    /**
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(SubscriptionViewer.class);

    /**
     * Constructs a <tt>SubscriptionViewer</tt>.
     *
     * @param context the layout context
     */
    public SubscriptionViewer(LayoutContext context) {
        this.context = context;
        root = RowFactory.create();
    }

    /**
     * Sets the subscription.
     *
     * @param subscription the subscription. May be <tt>null</tt>
     */
    public void setSubscription(DocumentAct subscription) {
        act = subscription;
    }

    /**
     * Returns the subscription view.
     *
     * @return the subscription view
     */
    public Component getComponent() {
        refresh();
        return root;
    }

    /**
     * Refreshes the subscription display.
     */
    public void refresh() {
        Component component;
        Document document = (act != null) ? (Document) context.getCache().get(act.getDocument()) : null;
        if (document != null) {
            component = getSubscription(document);
        } else {
            component = getNoSubscription();
        }
        root.removeAll();
        root.add(component);
    }

    /**
     * Returns a component that displays that there is no subscription.
     *
     * @return a new component
     */
    private Component getNoSubscription() {
        Label noSubscription = LabelFactory.create("subscription.nosubscription");
        Component prompt = getSubscribePrompt("subscription.subscribe");
        return ColumnFactory.create("WideCellSpacing", noSubscription, prompt);
    }

    /**
     * Returns a component that prompts to subscribe.
     *
     * @return a new component
     */
    private Component getSubscribePrompt(String messageId) {
        Label subscribe = LabelFactory.create(messageId);
        Button url = ButtonFactory.create("subscription.url", "hyperlink");
        url.addActionListener(new ActionListener() {
            public void onAction(ActionEvent e) {
                String link = Messages.get("subscription.url");
                ApplicationInstance.getActive().enqueueCommand(new BrowserOpenWindowCommand(link, null, null));
            }
        });
        return RowFactory.create("CellSpacing", subscribe, url);
    }

    /**
     * Returns a component displaying the subscription.
     *
     * @param document the subscription document
     * @return a new component
     */
    private Component getSubscription(Document document) {
        Component result;
        try {
            DocumentHandler documentHandler = ServiceHelper.getDocumentHandlers().get(document);
            InputStream content = documentHandler.getContent(document);
            Subscription subscription = SubscriptionFactory.create(content);

            Label organisation = LabelFactory.create();
            Label subscriber = LabelFactory.create();
            Label email = LabelFactory.create();
            Label expiration = LabelFactory.create();
            organisation.setText(subscription.getOrganisationName());
            subscriber.setText(subscription.getSubscriberName());
            email.setText(subscription.getSubscriberEmail());
            Date expiryDate = subscription.getExpiryDate();
            boolean expired = true;
            if (expiryDate != null && DateRules.getDate(expiryDate).compareTo(DateRules.getDate(new Date())) > 0) {
                expired = false;
                expiration.setText(DateHelper.getFullDateFormat().format(expiryDate));
            }

            Grid grid = GridFactory.create(
                2, LabelFactory.create("subscription.organisationName", "bold"), organisation,
                LabelFactory.create("subscription.subscriberName", "bold"), subscriber,
                LabelFactory.create("subscription.subscriberEmail", "bold"), email,
                LabelFactory.create("subscription.expiryDate", "bold"), expiration);
            if (expired) {
                result = ColumnFactory.create("WideCellSpacing", grid,
                                              LabelFactory.create("subscription.expired", "bold"),
                                              getSubscribePrompt("subscription.renew"));
            } else {
                result = grid;
            }
        } catch (Throwable exception) {
            log.error(exception);
            result = LabelFactory.create("subscription.invalid");
        }
        return result;
    }
}
