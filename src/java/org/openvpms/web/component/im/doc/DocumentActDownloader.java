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
import nextapp.echo2.app.event.ActionListener;
import org.apache.commons.io.FilenameUtils;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.report.openoffice.Converter;
import org.openvpms.report.openoffice.OOConnection;
import org.openvpms.report.openoffice.OpenOfficeHelper;
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
    private final DocumentAct _act;

    /**
     * Creates a new <code>DocumentActDownloader</code>.
     *
     * @param act the act
     */
    public DocumentActDownloader(DocumentAct act) {
        _act = act;
    }

    /**
     * Returns a component representing the downloader.
     *
     * @return the component
     */
    public Component getComponent() {
        Component component;
        Button button = ButtonFactory.create(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onDownload();
            }
        });
        String styleName;
        String fileName = _act.getFileName();
        boolean convert = Converter.canConvert(
                fileName, _act.getMimeType(), DocFormats.PDF_TYPE);
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
                public void actionPerformed(ActionEvent event) {
                    onDownloadAsPDF();
                }
            });
            asPDF.setStyleName("download.pdf");
            asPDF.setProperty(Button.PROPERTY_TOOL_TIP_TEXT,
                              Messages.get("file.download.asPDF.tooltip"));
            component = RowFactory.create("CellSpacing", button, asPDF);
        } else {
            component = button;
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
        IMObjectReference ref = _act.getDocument();
        if (ref == null) {
            throw new DocumentException(DocumentException.ErrorCode.NotFound);
        }
        return getDocument(ref);
    }

    /**
     * Initiates download of the document as a PDF file.
     */
    protected void onDownloadAsPDF() {
        OOConnection connection = null;
        try {
            Document source = getDocument();
            DocumentHandlers handlers = ServiceHelper.getDocumentHandlers();
            connection = OpenOfficeHelper.getConnectionPool().getConnection();
            Converter converter = new Converter(connection, handlers);
            Document target = converter.convert(source, DocFormats.PDF_TYPE);
            DownloadServlet.startDownload(target);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        } finally {
            OpenOfficeHelper.close(connection);
        }
    }
}