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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.workspace;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.webcontainer.command.BrowserOpenWindowCommand;
import org.apache.commons.io.FilenameUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.report.DocFormats;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.echo.servlet.ServletHelper;


/**
 * Determines the operations that may be performed on document acts.
 *
 * @author Tim Anderson
 */
public class DocumentActActions extends ActActions<DocumentAct> {

    /**
     * Determines if a document act can be refreshed.
     *
     * @param act the act to check
     * @return {@code true} if the act isn't posted, and has <em>documentTemplate</em> and <em>document</em> nodes.
     */
    public boolean canRefresh(DocumentAct act) {
        boolean refresh = false;
        if (!ActStatus.POSTED.equals(act.getStatus())) {
            ActBean bean = new ActBean(act);
            if (bean.hasNode("documentTemplate") && bean.hasNode("document")) {
                refresh = true;
            }
        }
        return refresh;
    }

    /**
     * Determines if an act is a document that can be edited externally by OpenOffice.
     *
     * @param act the act
     * @return {@code true} if the act is a document can be edited
     */
    public boolean canExternalEdit(Act act) {
        return (act instanceof DocumentAct) && canExternalEdit((DocumentAct) act);
    }

    /**
     * Determines if a document can be edited externally by OpenOffice.
     *
     * @param act the document act
     * @return {@code true} if the document can be edited
     */
    public boolean canExternalEdit(DocumentAct act) {
        if (canEdit(act) && act.getDocument() != null) {
            String ext = FilenameUtils.getExtension(act.getFileName());
            if (DocFormats.ODT_EXT.equalsIgnoreCase(ext)
                || DocFormats.DOC_EXT.equalsIgnoreCase(ext)
                || DocFormats.DOCX_EXT.equalsIgnoreCase(ext)
                || DocFormats.ODT_TYPE.equals(act.getMimeType())
                || DocFormats.DOC_TYPE.equals(act.getMimeType())
                || DocFormats.DOCX_TYPE.equals(act.getMimeType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Launches an external editor to edit a document, if editing of the document is supported.
     *
     * @param act the document act
     */
    public void externalEdit(DocumentAct act) {
        if (canExternalEdit(act)) {
            String documentURL = ServletHelper.getContextURL() + "/document/" + act.getId() + "/" + act.getFileName();
            String url = ServletHelper.getRedirectURI("externaledit?") + documentURL;
            ApplicationInstance.getActive().enqueueCommand(new BrowserOpenWindowCommand(url, "", ""));
        }
    }
}
