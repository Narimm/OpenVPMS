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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.doc;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.component.event.ActionListener;
import org.apache.commons.io.FilenameUtils;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.report.openoffice.Converter;
import org.openvpms.report.openoffice.OOConnection;
import org.openvpms.report.openoffice.OpenOfficeHelper;
import org.openvpms.web.component.im.report.DocumentActReporter;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.servlet.DownloadServlet;
import org.openvpms.web.system.ServiceHelper;


/**
 * Downloads a document from a {@link DocumentAct}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentActDownloader extends Downloader {

    /**
     * The document act.
     */
    private final DocumentAct act;

    /**
     * The template, when there is no document present.
     */
    private Entity template;

    /**
     * Creates a new <tt>DocumentActDownloader</tt>.
     *
     * @param act the act
     */
    public DocumentActDownloader(DocumentAct act) {
        this.act = act;
    }

    /**
     * Returns a component representing the downloader.
     *
     * @return the component
     */
    public Component getComponent() {
        Component component;
        Button button = ButtonFactory.create(new ActionListener() {
            public void onAction(ActionEvent event) {
                onDownload();
            }
        });
        String styleName;
        String fileName = act.getFileName();
        if (fileName == null) {
            Entity template = getTemplate();
            if (template != null) {
                fileName = template.getName();
            }
        }
        boolean convert = Converter.canConvert(
                fileName, act.getMimeType(), DocFormats.PDF_TYPE);
        if (fileName != null) {
            String ext = FilenameUtils.getExtension(fileName).toLowerCase();
            styleName = "download." + ext;
            button.setText(fileName);
        } else {
            styleName = DEFAULT_BUTTON_STYLE;
        }
        ApplicationInstance active = ApplicationInstance.getActive();
        if (active.getStyle(Button.class, styleName) == null) {
            styleName = DEFAULT_BUTTON_STYLE;
        }
        button.setStyleName(styleName);

        if (convert) {
            Button asPDF = ButtonFactory.create(new ActionListener() {
                public void onAction(ActionEvent event) {
                    onDownloadAsPDF();
                }
            });
            asPDF.setStyleName("download.pdf");
            asPDF.setProperty(Button.PROPERTY_TOOL_TIP_TEXT,
                              Messages.get("file.download.asPDF.tooltip"));
            component = RowFactory.create("CellSpacing", button, asPDF);
        } else {
            component = RowFactory.create("CellSpacing", button);
        }
        return component;
    }

    /**
     * Returns the document for download.
     *
     * @return the document for download
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         if the document can't be found
     */
    protected Document getDocument() {
        IMObjectReference ref = act.getDocument();
        Document document = null;
        if (ref != null) {
            document = getDocument(ref);
        } else {
            Entity template = getTemplate();
            if (template != null) {
                DocumentActReporter reporter
                        = new DocumentActReporter(act, template);
                document = reporter.getDocument();
            }
        }
        if (document == null) {
            throw new DocumentException(DocumentException.ErrorCode.NotFound);
        }
        return document;
    }

    /**
     * Initiates download of the document as a PDF file.
     */
    protected void onDownloadAsPDF() {
        OOConnection connection = null;
        try {
            Document source = getDocument();
            if (DocFormats.PDF_TYPE.equals(source.getMimeType())) {
                DownloadServlet.startDownload(source);
            } else {
                DocumentHandlers handlers = ServiceHelper.getDocumentHandlers();
                connection
                        = OpenOfficeHelper.getConnectionPool().getConnection();
                Converter converter = new Converter(connection, handlers);
                Document target = converter.convert(source,
                                                    DocFormats.PDF_TYPE);
                DownloadServlet.startDownload(target);
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        } finally {
            OpenOfficeHelper.close(connection);
        }
    }

    /**
     * Returns the document template.
     *
     * @return the document template. May be <tt>null</tt>
     */
    private Entity getTemplate() {
        if (template == null) {
            ActBean bean = new ActBean(act);
            if (bean.hasNode("documentTemplate")) {
                template = bean.getNodeParticipant("documentTemplate");
            }
        }
        return template;
    }
}