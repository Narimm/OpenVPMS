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
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.servlet.DownloadServlet;


/**
 * Helper to download documents.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DownloadHelper {

    /**
     * Download a document.
     *
     * @param doc a reference to the document
     */
    public static void download(IMObjectReference doc) {
        Document document = (Document) IMObjectHelper.getObject(doc);
        if (document != null) {
            download(document);
        }
    }

    /**
     * Download a document. This opens a new browser window on the client
     * to display the document.
     * If the document is unsaved,
     *
     * @param document the document to download
     */
    public static void download(final Document document) {
        DownloadServlet.startDownload(document);
    }

    /**
     * Returns a button enabling a {@link DocumentAct}s associated document
     * to be downloaded.
     *
     * @param act the act
     * @return a download button
     */
    public static Component getButton(final DocumentAct act) {
        Button button = ButtonFactory.create();
        String styleName;
        if (act.getFileName() != null)
            styleName = "download.".concat(
                    FilenameUtils.getExtension(act.getFileName()));
        else
            styleName = "download.default";
        if (ApplicationInstance.getActive().getStyle(Button.class,
                                                     styleName) == null) {
            styleName = "download.default";
        }
        button.setStyleName(styleName);
        button.setText(act.getDescription());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                download(act.getDocReference());
            }
        });
        return button;
    }
}
