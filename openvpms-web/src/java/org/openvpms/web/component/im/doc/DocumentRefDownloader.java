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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
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
    private final IMObjectReference _reference;

    /**
     * Constructs a new <code>DocumentRefDownloader</code>.
     *
     * @param reference the document reference
     */
    public DocumentRefDownloader(IMObjectReference reference) {
        _reference = reference;
    }

    /**
     * Returns a component representing the downloader.
     *
     * @return the component
     */
    public Component getComponent() {
        Button button = ButtonFactory.create(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onDownload();
            }
        });
        button.setStyleName(DEFAULT_BUTTON_STYLE);
        String text = DescriptorHelper.getDisplayName(
                _reference.getArchetypeId().getShortName());
        button.setText(text);
        return button;
    }

    /**
     * Returns the document for download.
     *
     * @return the document for download
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         if the document can't be found
     */
    protected Document getDocument() {
        return getDocument(_reference);
    }
}
