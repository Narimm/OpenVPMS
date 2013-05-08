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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.dialog;

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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.system.common.util.StringUtilities;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.component.im.subscription.SubscriptionHelper;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.i18n.Messages;
import org.openvpms.web.system.Version;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;

import static org.openvpms.web.echo.style.Styles.BOLD;
import static org.openvpms.web.echo.style.Styles.INSET;


/**
 * Help dialog.
 *
 * @author Tim Anderson
 */
public class HelpDialog extends PopupDialog {

    /**
     * The context.
     */
    private final Context context;

    /**
     * The project logo.
     */
    private static final String PATH = "/org/openvpms/web/resource/image/openvpms.gif";

    /**
     * The home page.
     */
    private static final String HOME = "http://www.openvpms.org";

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(HelpDialog.class);

    /**
     * Constructs a {@code HelpDialog}.
     *
     * @param context the context
     */
    public HelpDialog(Context context) {
        this(null, null, context);
    }

    /**
     * Constructs a {@code HelpDialog}.
     *
     * @param topic    the topic. May be {@code null}
     * @param topicURL the topic URL. May be {@code null}
     * @param context  the context
     */
    protected HelpDialog(String topic, final String topicURL, Context context) {
        super(Messages.get("helpdialog.title"), "HelpDialog", OK);
        setModal(true);
        this.context = context;

        Component component = null;

        if (topic != null) {
            if (topicURL == null) {
                Label label = LabelFactory.create(true, true);
                label.setStyleName(BOLD);
                label.setText(Messages.get("helpdialog.nohelp.topic", topic));
                component = label;
            } else {
                String parent = getExistingParent(topicURL);
                StringBuilder content = new StringBuilder();
                content.append("<div xmlns='http://www.w3.org/1999/xhtml'>");
                content.append("<p>");
                content.append(Messages.get("helpdialog.nohelp.create", topicURL));
                content.append("</p>");
                if (parent != null) {
                    content.append("<p>");
                    content.append(Messages.get("helpdialog.nohelp.parent", parent));
                    content.append("</p>");
                }
                content.append("</div>");
                LabelEx label = new LabelEx(new XhtmlFragment(content.toString()));
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
     * @param help    the help context. May be {@code null}
     * @param context the context
     */
    public static void show(HelpContext help, Context context) {
        if (help == null) {
            new HelpDialog(context).show();
        } else {
            show(help.getTopic(), context);
        }
    }

    /**
     * Displays a help dialog for the specified topic.
     *
     * @param topic   the topic identifier
     * @param context the context
     */
    public static void show(String topic, Context context) {
        String url = getTopicURL(topic);
        if (url != null) {
            if (exists(url)) {
                ApplicationInstance.getActive().enqueueCommand(new BrowserOpenWindowCommand(url, null, null));
            } else {
                HelpDialog dialog = new HelpDialog(topic, url, context);
                dialog.show();
            }
        } else {
            HelpDialog dialog = new HelpDialog(topic, null, context);
            dialog.show();
        }
    }

    /**
     * Returns the topics hyperlink.
     * <p/>
     * This launches a new browser window.
     *
     * @return the topics hyperlink.
     */
    private Component getTopics() {
        Column topics = ColumnFactory.create("WideCellSpacing");

        for (int i = 0; ; ++i) {
            String topic = Messages.get("help.topic." + i + ".title", Messages.HELP, true);
            if (topic != null) {
                final String url = Messages.get("help.topic." + i + ".url", Messages.HELP, true);
                if (url != null) {
                    Button helpLink = createHelpURL(topic, url);
                    topics.add(RowFactory.create(helpLink)); // force to minimum width
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return ColumnFactory.create("Inset.Large", topics);
    }

    /**
     * Creates a help URL button.
     * <p/>
     * When clicked, this opens a new browser window/tab.
     *
     * @param topic the topic name
     * @param url   the topic url
     * @return a new help URL button
     */
    private Button createHelpURL(String topic, final String url) {
        Button helpLink = ButtonFactory.create(null, "hyperlink");
        helpLink.setText(topic);
        helpLink.setBackground(null); // want to inherit style of parent
        helpLink.setToolTipText(url);
        helpLink.addActionListener(new ActionListener() {
            public void onAction(ActionEvent e) {
                launch(url);
            }
        });
        return helpLink;
    }

    /**
     * Returns the header component.
     *
     * @return the header component
     */
    private Component getHeader() {
        Button logo = new Button(new ResourceImageReference(PATH));
        logo.setToolTipText(HOME);
        logo.addActionListener(new ActionListener() {
            public void onAction(ActionEvent e) {
                launch(HOME);
            }
        });
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

    /**
     * Launches a URL in a new window, closing the help dialog.
     *
     * @param url the URL
     */
    private void launch(String url) {
        ApplicationInstance.getActive().enqueueCommand(new BrowserOpenWindowCommand(url, null, null));
        close();
    }

    /**
     * Returns the subscription details.
     *
     * @return the subscription details
     */
    private LabelEx getSubscription() {
        String content =
            "<p xmlns='http://www.w3.org/1999/xhtml'>" + SubscriptionHelper.formatSubscription(context) + "</p>";
        LabelEx subscription = new LabelEx(new XhtmlFragment(content));
        subscription.setLineWrap(true);
        subscription.setTextAlignment(Alignment.ALIGN_CENTER);
        return subscription;
    }


    /**
     * Returns the topic URL for a given topic identifier.
     *
     * @param topic the topic identifier
     * @return the topic URL or {@code null} if none is found
     */
    private static String getTopicURL(String topic) {
        String result = null;
        String baseURL = Messages.get("help.url", Messages.HELP, true);
        if (baseURL != null) {
            String fragment = Messages.get(topic, Messages.HELP, true);
            if (fragment == null) {
                Enumeration<String> iter = Messages.getKeys(Messages.HELP);
                while (iter.hasMoreElements()) {
                    String key = iter.nextElement();
                    if (StringUtilities.matches(topic, key)) {
                        fragment = Messages.get(key, Messages.HELP, true);
                        break;
                    }
                }
            }
            if (fragment != null) {
                result = baseURL + "/" + fragment;
            }
        }
        return result;
    }

    /**
     * Tries to locate an existing URL for the given topic URL.
     *
     * @param topicURL the topic URL
     * @return the closest parent URL to the topic that exists, or {@code null} if none is found
     */
    private String getExistingParent(String topicURL) {
        String result = null;
        try {
            URI uri = new URI(topicURL);
            String path = uri.getPath();
            while (!StringUtils.isEmpty(path)) {
                if (!path.endsWith("/")) {
                    uri = uri.resolve(".");
                } else {
                    uri = uri.resolve("..");
                }
                String parent = uri.toURL().toString();
                if (exists(parent)) {
                    result = parent;
                    break;
                }
                path = uri.getPath();
            }
        } catch (URISyntaxException exception) {
            log.debug(exception, exception);
        } catch (MalformedURLException exception) {
            log.debug(exception, exception);
        }
        return result;
    }

    /**
     * Determines if a topic URL exists.
     *
     * @param topicURL the topic URL
     * @return {@code true} if the topic URL exists, otherwise {@code false}
     */
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
