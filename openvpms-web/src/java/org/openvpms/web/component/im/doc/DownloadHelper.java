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

import nextapp.echo2.app.filetransfer.Download;
import nextapp.echo2.app.filetransfer.DownloadProvider;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.web.app.OpenVPMSApp;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.io.IOException;
import java.io.OutputStream;


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
        final Document document = (Document) IMObjectHelper.getObject(doc);
        if (document != null) {
            Download download = new Download();
            download.setProvider(new DownloadProvider() {

                public String getContentType() {
                    return document.getMimeType();
                }

                public String getFileName() {
                    return document.getName();
                }

                public int getSize() {
                    return (int) document.getDocSize();
                }

                public void writeFile(OutputStream stream) throws IOException {
                    stream.write(document.getContents());
                }
            });
            download.setActive(true);
            OpenVPMSApp.getInstance().enqueueCommand(download);
        }

    }
}
