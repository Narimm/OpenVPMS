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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.dialog;

import echopointng.LabelEx;
import echopointng.xhtml.XhtmlFragment;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.ResourceImageReference;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.RowLayoutData;
import nextapp.echo2.webcontainer.command.BrowserOpenWindowCommand;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.web.app.admin.organisation.SubscriptionHelper;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.Version;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.openvpms.web.resource.util.Styles.BOLD;
import static org.openvpms.web.resource.util.Styles.INSET;


/**
 * Help dialog.
 *
 * @author Tim Anderson
 */
public class HelpDialog extends PopupDialog {

    /**
     * The project logo.
     */
    private static final String PATH = "/org/openvpms/web/resource/image/openvpms.gif";

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(HelpDialog.class);

    /**
     * Constructs a {@code HelpDialog}.
     */
    public HelpDialog() {
        this(null, null);
    }

    protected HelpDialog(String topic, final String topicURL) {
        super(Messages.get("helpdialog.title"), "HelpDialog", OK);

        Component component = null;

        if (topic != null) {
            if (topicURL == null) {
                Label label = LabelFactory.create(true, true);
                label.setStyleName(BOLD);
                label.setText(Messages.get("helpdialog.nohelp", topic));
                component = label;
            } else {
                String content = "<p xmlns='http://www.w3.org/1999/xhtml'>"
                        + Messages.get("helpdialog.nohelp.create", topicURL)
                        + "</p>";
                LabelEx label = new LabelEx(new XhtmlFragment(content));
                label.setLineWrap(true);
                component = label;
            }
        }

        Component topics = getTopics();

        Component content;
        if (component != null) {
            Grid hack = new Grid();
            hack.setStyleName("HelpDialog.content.size");
            Row container = RowFactory.create(component, hack);
            content = SplitPaneFactory.create(SplitPane.ORIENTATION_HORIZONTAL, "HelpDialog.content", topics,
                                              container);
        } else {
            content = topics;
        }
        SplitPane footer = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP, "HelpDialog.footer",
                                                   ColumnFactory.create(INSET, getSubscription()), content);
        SplitPane header = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_TOP_BOTTOM, "HelpDialog.header",
                                                   getHeader(), footer);
        getLayout().add(header);
    }

    /**
     * Displays a help dialog for the specified help context.
     *
     * @param context the help context. May be {@code null}
     */
    public static void show(HelpContext context) {
        if (context == null) {
            new HelpDialog().show();
        } else {
            show(context.getTopic());
        }
    }

    /**
     * Displays a help dialog for the specified topic.
     *
     * @param topic the topic identifier
     */
    public static void show(String topic) {
        String url = getTopicURL(topic);
        if (url != null) {
            if (exists(url)) {
                ApplicationInstance.getActive().enqueueCommand(new BrowserOpenWindowCommand(url, null, null));
            } else {
                HelpDialog dialog = new HelpDialog(topic, url);
                dialog.show();
            }
        } else {
            HelpDialog dialog = new HelpDialog(topic, null);
            dialog.show();
        }
    }

    private Component getTopics() {
        Column topics = ColumnFactory.create();
        Button helpLink = ButtonFactory.create("helpdialog.topics", "hyperlink");
        helpLink.setBackground(null); // want to inherit style of parent
        helpLink.addActionListener(new ActionListener() {
            public void onAction(ActionEvent e) {
                String link = Messages.get("helpdialog.topics.link");
                ApplicationInstance.getActive().enqueueCommand(new BrowserOpenWindowCommand(link, null, null));
                close();
            }
        });
        topics.add(RowFactory.create(helpLink)); // force to minimum width
        return ColumnFactory.create("Inset.Large", topics);
    }

    private Component getHeader() {
        Label logo = LabelFactory.create(new ResourceImageReference(PATH));
        RowLayoutData centre = new RowLayoutData();
        centre.setAlignment(new Alignment(Alignment.DEFAULT, Alignment.CENTER));
        logo.setLayoutData(centre);

        Label label = LabelFactory.create(null, "small");
        label.setText(Messages.get("helpdialog.version", Version.VERSION, Version.REVISION));

        Row labelRow = RowFactory.create("InsetX", label);
        RowLayoutData right = new RowLayoutData();
        right.setAlignment(new Alignment(Alignment.RIGHT, Alignment.BOTTOM));
        right.setWidth(new Extent(100, Extent.PERCENT));
        labelRow.setLayoutData(right);

        return RowFactory.create(INSET, logo, labelRow);
    }

    private LabelEx getSubscription() {
        String content = "<p xmlns='http://www.w3.org/1999/xhtml'>" + SubscriptionHelper.formatSubscription() + "</p>";
        LabelEx subscription = new LabelEx(new XhtmlFragment(content));
        subscription.setLineWrap(true);
        subscription.setTextAlignment(Alignment.ALIGN_CENTER);
        return subscription;
    }


    private static String getTopicURL(String topic) {
        String result = null;
        String baseURL = Messages.get("help.url", Messages.HELP, true);
        String fragment = Messages.get(topic, Messages.HELP, true);
        if (baseURL != null && fragment != null) {
            result = baseURL + "/" + fragment;
        }
        return result;
    }

    private static boolean exists(String topicURL) {
        boolean exists = false;
        try {
            URL url = new URL(topicURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode <= 399) {
                exists = true;
            }
        } catch (IOException exception) {
            log.warn(exception.getMessage(), exception);
        }
        return exists;
    }

}
