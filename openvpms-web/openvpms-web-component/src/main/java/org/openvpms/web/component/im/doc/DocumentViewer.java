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
 *  Copyright 2006-2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.doc;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.webcontainer.command.BrowserOpenWindowCommand;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.echo.i18n.Messages;


/**
 * Viewer for {@link IMObjectReference}s of type <em>document.*</em>.
 *
 * @author Tim Anderson
 */
public class DocumentViewer {

    /**
     * The reference to view.
     */
    private final IMObjectReference reference;

    /**
     * The parent object. May be {@code null}
     */
    private final IMObject parent;

    /**
     * The document name. May be {@code null}
     */
    private final String name;

    /**
     * Determines if a hyperlink should be created, to enable downloads of
     * the document.
     */
    private final boolean link;

    /**
     * Determines if the document should be downloaded as a template.
     */
    private final boolean template;

    /**
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * The downloader.
     */
    private Downloader downloader;

    /**
     * Listener for downloader events.
     */
    private DownloaderListener listener;

    /**
     * Determines if a message should be displayed if no document is present.
     */
    private boolean showNoDocument = true;


    /**
     * Constructs a {@code DocumentViewer}.
     *
     * @param act     the document act
     * @param link    if {@code true} enable an hyperlink to the object
     * @param context the layout context
     */
    public DocumentViewer(DocumentAct act, boolean link, LayoutContext context) {
        this(act, link, false, context);
    }

    /**
     * Constructs a {@code DocumentViewer}.
     *
     * @param act      the document act
     * @param link     if {@code true} enable an hyperlink to the object
     * @param template if {@code true}, display as a template, otherwise generate the document if required
     * @param context  the layout context
     */
    public DocumentViewer(DocumentAct act, boolean link, boolean template, LayoutContext context) {
        this(act.getDocument(), act, act.getFileName(), link, template, context);
    }

    /**
     * Constructs a {@code DocumentViewer}.
     *
     * @param reference the reference to view
     * @param parent    the parent. May be {@code null}
     * @param link      if {@code true} enable an hyperlink to the object
     * @param template  if {@code true}, display as a template, otherwise generate the document if required
     * @param context   the layout context
     */
    public DocumentViewer(IMObjectReference reference, IMObject parent, boolean link, boolean template,
                          LayoutContext context) {
        this(reference, parent, null, link, template, context);
    }

    /**
     * Constructs a {@code DocumentViewer}.
     *
     * @param reference the reference to view. May be {@code null}
     * @param parent    the parent. May be {@code null}
     * @param name      the document file name. May be {@code null}
     * @param link      if {@code true} enable an hyperlink to the object
     * @param template  if {@code true}, display as a template, otherwise generate the document if required
     * @param context   the layout context
     */
    public DocumentViewer(IMObjectReference reference, IMObject parent, String name, boolean link, boolean template,
                          LayoutContext context) {
        this.reference = reference;
        this.parent = parent;
        if (name != null) {
            this.name = name;
        } else if (parent instanceof DocumentAct) {
            this.name = ((DocumentAct) parent).getFileName();
        } else if (reference != null) {
            this.name = DescriptorHelper.getDisplayName(reference.getArchetypeId().getShortName());
        } else {
            this.name = null;
        }
        this.link = link;
        this.template = template;
        this.context = context;
    }

    /**
     * Registers a listener for download events.
     * <p/>
     * This enables download events to be intercepted. Only applicable if {@code link} was specified at construction.
     *
     * @param listener the listener. May be {@code null}
     */
    public void setDownloadListener(DownloaderListener listener) {
        if (downloader != null) {
            downloader.setListener(listener);
        } else {
            this.listener = listener;
        }
    }

    /**
     * Determines if a message should be displayed if no document is present.
     *
     * @param show if {@code true} show a message, otherwise leave the component blank. Defaults to {@code true}
     */
    public void setShowNoDocument(boolean show) {
        showNoDocument = show;
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        Component result = null;
        boolean hasDoc = false;
        if (reference != null) {
            hasDoc = true;
        } else if (parent instanceof DocumentAct) {
            ActBean bean = new ActBean((DocumentAct) parent);
            if (parent.getId() != -1 && bean.hasNode("investigationType")) {
                // can't link if the act hasn't been saved
                result = getInvestigation(bean);
            }
            if (result == null) {
                hasDoc = bean.hasNode("documentTemplate");
            }
        }
        if (result == null) {
            if (hasDoc) {
                if (link) {
                    if (parent instanceof DocumentAct) {
                        downloader = new DocumentActDownloader((DocumentAct) parent, template);
                    } else {
                        downloader = new DocumentRefDownloader(reference, name);
                    }
                    downloader.setListener(listener);
                    result = downloader.getComponent();
                } else {
                    Label label = LabelFactory.create();
                    label.setText(name);
                    result = label;
                }
            } else {
                Label label = LabelFactory.create();
                if (showNoDocument) {
                    label.setText(Messages.get("document.none"));
                }
                result = label;
            }
        }
        return result;
    }

    /**
     * Returns a component to view an external investigation, if available.
     *
     * @param bean the act bean
     * @return a component to view the external investigation, or {@code null} if the investigation type doesn't support
     *         it or the act has no investigation type
     */
    private Component getInvestigation(ActBean bean) {
        Component result = null;
        Entity investigationType = (Entity) context.getCache().get(bean.getNodeParticipantRef("investigationType"));
        if (investigationType != null) {
            IMObjectBean typeBean = new IMObjectBean(investigationType);
            if (typeBean.hasNode("url")) {
                String url = typeBean.getString("url");
                if (!StringUtils.isEmpty(url)) {
                    final String accessionURL = url + parent.getId();
                    Button button = ButtonFactory.create(null, "hyperlink", new ActionListener() {
                        @Override
                        public void onAction(ActionEvent event) {
                            ApplicationInstance.getActive().enqueueCommand(
                                new BrowserOpenWindowCommand(accessionURL, "", ""));
                        }
                    });
                    button.setText(Messages.get("document.link", investigationType.getName()));
                    button.setToolTipText(accessionURL);
                    result = button;
                }
            }
        }
        return result;
    }

}
