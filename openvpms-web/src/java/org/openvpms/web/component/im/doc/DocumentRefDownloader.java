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

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.report.openoffice.OpenOfficeException;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.util.ButtonFactory;


/**
 * Downloads a document given its a {@link IMObjectReference}.
 * *
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentRefDownloader extends Downloader {

    /**
     * The document reference.
     */
    private final IMObjectReference reference;

    /**
     * The document name. May be <tt>null</tt>
     */
    private final String name;

    /**
     * Constructs a <tt>DocumentRefDownloader</tt>.
     *
     * @param reference the document reference
     */
    public DocumentRefDownloader(IMObjectReference reference) {
        this(reference, null);
    }

    /**
     * Constructs a <tt>DocumentRefDownloader</tt>.
     *
     * @param reference the document reference
     * @param name      the document name. May be <tt>null</tt>
     */
    public DocumentRefDownloader(IMObjectReference reference, String name) {
        this.reference = reference;
        this.name = name;
    }

    /**
     * Returns a component representing the downloader.
     *
     * @return the component
     */
    public Component getComponent() {
        Button button = ButtonFactory.create(new ActionListener() {
            public void onAction(ActionEvent event) {
                selected(null);
            }
        });
        setButtonStyle(button, name);
        button.setAlignment(Alignment.ALIGN_LEFT);
        button.setTextAlignment(Alignment.ALIGN_LEFT);
        return button;
    }

    /**
     * Returns the document for download.
     *
     * @param mimeType the expected mime type. If <tt>null</tt>, then no conversion is required.
     * @return the document for download
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         if the document can't be found
     * @throws OpenOfficeException       if the document cannot be converted
     */
    protected Document getDocument(String mimeType) {
        return getDocumentByRef(reference, mimeType);
    }
}
