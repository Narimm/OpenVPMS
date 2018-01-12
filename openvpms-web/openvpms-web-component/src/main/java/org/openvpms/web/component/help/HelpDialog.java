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

package org.openvpms.web.component.help;

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
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.util.StringUtilities;
import org.openvpms.version.Version;
import org.openvpms.web.component.subscription.SubscriptionHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.dialog.ModalDialog;
import org.openvpms.web.echo.error.ErrorHandler;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.TimeZone;

import static org.openvpms.web.echo.style.Styles.BOLD;
import static org.openvpms.web.echo.style.Styles.INSET;


/**
 * Help dialog.
 *
 * @author Tim Anderson
 */
public class HelpDialog extends ModalDialog {

    /**
     * The help topics.
     */
    private final HelpTopics topics;

    /**
     * Features to pass to window.open().
     */
    private final String features;

    /**
     * The project logo.
     */
    private static final String PATH = "/org/openvpms/web/resource/image/openvpms.png";

    /**
     * The home page.
     */
    private static final String HOME = "http://www.openvpms.org";

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(HelpDialog.class);

    /**
     * Server timeout in milliseconds.
     */
    private static final int TIMEOUT = 10 * 1000;

    /**
     * Constructs a {@link HelpDialog}.
     *
     * @param topics   the help topics
     * @param service  the archetype service
     * @param features browser feature string. May be {@code null}
     */
    public HelpDialog(HelpTopics topics, IArchetypeService service, String features) {
        this(null, null, null, topics, service, features);
    }

    /**
     * Constructs a {@link HelpDialog}.
     *
     * @param topic     the topic. May be {@code null}
     * @param topicURL  the topic URL. May be {@code null}
     * @param parentURL the parent URL. May be {@code null}
     * @param topics    the help topics
     * @param service   the archetype service
     * @param features  browser feature string. May be {@code null}
     */
    protected HelpDialog(String topic, final String topicURL, String parentURL, HelpTopics topics,
                         IArchetypeService service, String features) {
        super(Messages.get("helpdialog.title"), "HelpDialog", OK);
        this.topics = topics;
        this.features = features;

        Component component = null;

        if (topic != null) {
            if (topicURL == null) {
                Label label = LabelFactory.create(true, true);
                label.setStyleName(BOLD);
                label.setText(Messages.format("helpdialog.nohelp.topic", topic));
                component = label;
            } else {
                StringBuilder content = new StringBuilder();
                content.append("<div xmlns='http://www.w3.org/1999/xhtml'>");
                content.append("<p>");
                content.append(Messages.format("helpdialog.nohelp.create", topicURL));
                content.append("</p>");
                if (parentURL != null) {
                    content.append("<p>");
                    content.append(Messages.format("helpdialog.nohelp.parent", parentURL));
                    content.append("</p>");
                }
                content.append("</div>");
                LabelEx label = new LabelEx(new XhtmlFragment(content.toString()));
                label.setLineWrap(true);
                component = label;
            }
        }

        Component topicComponent = getTopics();

        Component content;
        if (component != null) {
            Grid hack = new Grid();
            hack.setStyleName("HelpDialog.content.size");
            Row container = RowFactory.create(component, hack);
            content = SplitPaneFactory.create(SplitPane.ORIENTATION_HORIZONTAL, "HelpDialog.content", topicComponent,
                                              container);
        } else {
            content = topicComponent;
        }
        SplitPane footer = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP, "HelpDialog.footer",
                                                   ColumnFactory.create(INSET, getSubscription(service)), content);
        SplitPane header = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_TOP_BOTTOM, "HelpDialog.header",
                                                   getHeader(), footer);
        getLayout().add(header);
    }

    /**
     * Displays a help dialog for the specified help context.
     *
     * @param help     the help context. May be {@code null}
     * @param topics   the help topics
     * @param service  the archetype service
     * @param features the browser features. May be {@code null}
     */
    public static void show(HelpContext help, HelpTopics topics, IArchetypeService service, String features) {
        if (help == null) {
            new HelpDialog(topics, service, features).show();
        } else {
            show(help.getTopic(), topics, service, features);
        }
    }

    /**
     * Displays a help dialog for the specified topic.
     *
     * @param topic    the topic identifier
     * @param topics   the help topics
     * @param service  the archetype service
     * @param features the browser features. May be {@code null}
     */
    public static void show(String topic, HelpTopics topics, IArchetypeService service, String features) {
        String url = getTopicURL(topic, topics);
        if (url != null) {
            try {
                Topic resource = new Topic(url);
                if (resource.exists()) {
                    openWindow(url, features);
                } else if (resource.notFound()) {
                    Topic parentResource = resource.findParent();
                    if (parentResource == null || parentResource.exists()) {
                        String parent = (parentResource != null) ? parentResource.getURL() : null;
                        HelpDialog dialog = new HelpDialog(topic, url, parent, topics, service, features);
                        dialog.show();
                    } else {
                        showError(resource);
                    }
                } else if (resource.unavailable()) {
                    ErrorHelper.show(Messages.get("helpdialog.title"), Messages.get("helpdialog.unavailable"));
                } else {
                    showError(resource);
                }
            } catch (IOException exception) {
                ErrorHelper.show(Messages.get("helpdialog.title"),
                                 Messages.format("helpdialog.error", exception.getMessage()));
            }
        } else {
            HelpDialog dialog = new HelpDialog(topic, null, null, topics, service, features);
            dialog.show();
        }
    }

    /**
     * Shows an error for a topic.
     *
     * @param resource the topic resource
     */
    private static void showError(Topic resource) {
        if (log.isErrorEnabled()) {
            log.error("Failed to open help for URL=" + resource.getURL() + ", response=" + resource.getResponseCode());
        }
        String status = Messages.format("helpdialog.errorstatus", resource.getResponseCode());
        String message = Messages.format("helpdialog.error", status);
        ErrorHandler.getInstance().error(Messages.get("helpdialog.title"), message, null, null);
    }

    /**
     * Returns the topics hyperlink.
     * <p/>
     * This launches a new browser window.
     *
     * @return the topics hyperlink.
     */
    private Component getTopics() {
        Column container = ColumnFactory.create(Styles.WIDE_CELL_SPACING);

        for (int i = 0; ; ++i) {
            String topic = topics.get("help.topic." + i + ".title", true);
            if (topic != null) {
                final String url = topics.get("help.topic." + i + ".url", true);
                if (url != null) {
                    Button helpLink = createHelpURL(topic, url);
                    container.add(RowFactory.create(helpLink)); // force to minimum width
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return ColumnFactory.create(Styles.LARGE_INSET, container);
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

        Label version = LabelFactory.create(null, "small");
        version.setText(Messages.format("helpdialog.version", Version.VERSION, Version.REVISION));

        Label locale = LabelFactory.create(null, "small");
        locale.setText(Messages.format("helpdialog.locale", Locale.getDefault().toLanguageTag(),
                                       TimeZone.getDefault().getID()));

        Row labelRow = RowFactory.create("InsetX", RowFactory.create(Styles.CELL_SPACING, version, locale));
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
        openWindow(url, features);
        close();
    }

    /**
     * Opens a new window for the specified url.
     *
     * @param url the url
     */
    private static void openWindow(String url, String features) {
        ApplicationInstance.getActive().enqueueCommand(new BrowserOpenWindowCommand(url, null, features));
    }

    /**
     * Returns the subscription details.
     *
     * @param service the archetype service
     * @return the subscription details
     */
    private LabelEx getSubscription(IArchetypeService service) {
        String subscription = SubscriptionHelper.formatSubscription(service);
        String content = "<p xmlns='http://www.w3.org/1999/xhtml'>" + subscription + "</p>";
        LabelEx label = new LabelEx(new XhtmlFragment(content));
        label.setLineWrap(true);
        label.setTextAlignment(Alignment.ALIGN_CENTER);
        return label;
    }


    /**
     * Returns the topic URL for a given topic identifier.
     *
     * @param topic  the topic identifier
     * @param topics the help topics
     * @return the topic URL or {@code null} if none is found
     */
    private static String getTopicURL(String topic, HelpTopics topics) {
        String result = null;
        String baseURL = topics.get("help.url", true);
        if (baseURL != null) {
            String fragment = topics.get(topic, true);
            if (fragment == null) {
                for (String key : topics.getKeys()) {
                    if (StringUtilities.matches(topic, key)) {
                        fragment = topics.get(key, true);
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
     * Helper to determine if a topic URL exists.
     */
    private static class Topic {

        /**
         * The topic URL.
         */
        private final String topicURL;

        /**
         * The response code when accessing the topic URL.
         */
        private int responseCode;

        /**
         * Constructs a {@link Topic}.
         *
         * @param topicURL the topic URL
         * @throws IOException for any I/O error
         */
        public Topic(String topicURL) throws IOException {
            this.topicURL = topicURL;
            URL url = new URL(topicURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            responseCode = connection.getResponseCode();
            connection.disconnect();
        }

        /**
         * Returns the topic URL.
         *
         * @return the topic URL
         */
        public String getURL() {
            return topicURL;
        }

        /**
         * Returns the response code from accessing the URL.
         *
         * @return the response code
         */
        public int getResponseCode() {
            return responseCode;
        }

        /**
         * Determines if the topic URL exists.
         *
         * @return {@code true} if the topic URL exists
         */
        public boolean exists() {
            return responseCode >= 200 && responseCode < 400;
        }

        /**
         * Determines if the topic URL was not found.
         *
         * @return {@code true} if the topic URL was not found
         */
        public boolean notFound() {
            return responseCode == 404;
        }

        /**
         * Determines if the topic URL is unavailable.
         *
         * @return {@code true} if the topic URL is unavailable
         */
        public boolean unavailable() {
            return responseCode == 503;
        }

        /**
         * Finds a parent of the topic URL.
         *
         * @return a parent topic, or {@code null} if there is no parent. If non-null, check the response code to
         * determine if the parent can be accessed
         * @throws IOException for any I/O error
         */
        public Topic findParent() throws IOException {
            Topic result = null;
            try {
                URI uri = new URI(topicURL);
                String path = uri.getPath();
                while (!StringUtils.isEmpty(path)) {
                    if (!path.endsWith("/")) {
                        uri = uri.resolve(".");
                    } else {
                        uri = uri.resolve("..");
                    }

                    String parentURL = uri.toURL().toString();
                    Topic parent = new Topic(parentURL);
                    if (parent.notFound()) {
                        path = uri.getPath();
                    } else {
                        result = parent;
                        break;
                    }
                }
            } catch (URISyntaxException | MalformedURLException exception) {
                log.debug(exception, exception);
            }
            return result;
        }

    }

}
