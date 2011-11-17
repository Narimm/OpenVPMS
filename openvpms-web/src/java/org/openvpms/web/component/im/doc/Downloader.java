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
import org.apache.commons.io.FilenameUtils;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.servlet.DownloadServlet;


/**
 * Helper to render a component to download a document.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class Downloader {

    /**
     * Default download button style.
     */
    protected static final String DEFAULT_BUTTON_STYLE = "download.default";


    /**
     * Returns a component representing the downloader.
     *
     * @return the component
     */
    public abstract Component getComponent();

    /**
     * Initiates download of the document.
     */
    protected void onDownload() {
        try {
            Document document = getDocument();
            DownloadServlet.startDownload(document);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Returns the document for download.
     *
     * @return the document for download
     * @throws OpenVPMSException for any error
     */
    protected abstract Document getDocument();

    /**
     * Returns a document, given its referemce.
     *
     * @param reference the document reference
     * @return the document
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         if the document can't be found
     */
    protected Document getDocument(IMObjectReference reference) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        Document doc = (Document) service.get(reference);
        if (doc == null) {
            throw new DocumentException(DocumentException.ErrorCode.NotFound);
        }
        return doc;
    }

    protected void setButtonStyle(Button button, String name) {
        String tooltip = null;
        if (name.length() > 18) {
            tooltip = name;
            name = name.substring(0, 8) + "..." + name.substring(name.length() - 7);
        }
        button.setText(name);
        if (tooltip != null) {
            button.setToolTipText(tooltip);
        }
        String styleName = getStyleName(name);
        button.setStyleName(styleName);
    }

    /**
     * Helper to determine the button style name from a file name.
     *
     * @param name the file name. May be <tt>null</tt>
     * @return the button style name, or {@link #DEFAULT_BUTTON_STYLE} if the style is not known
     */
    protected String getStyleName(String name) {
        String styleName;
        if (name != null) {
            String ext = FilenameUtils.getExtension(name).toLowerCase();
            styleName = "download." + ext;
            ApplicationInstance active = ApplicationInstance.getActive();
            if (active.getStyle(Button.class, styleName) == null) {
                styleName = DEFAULT_BUTTON_STYLE;
            }
        } else {
            styleName = DEFAULT_BUTTON_STYLE;
        }
        return styleName;
    }

}
