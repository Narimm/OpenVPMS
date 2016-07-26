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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.workspace;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.webcontainer.command.BrowserOpenWindowCommand;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.report.DocFormats;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.servlet.ServletHelper;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.webdav.milton.DocumentSessionManager;
import org.openvpms.web.webdav.resource.EditableDocuments;
import org.openvpms.web.webdav.session.Session;


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
     * @return {@code true} if the act isn't locked, and has <em>documentTemplate</em> and <em>document</em> nodes.
     */
    public boolean canRefresh(DocumentAct act) {
        boolean refresh = false;
        if (!isLocked(act)) {
            ActBean bean = new ActBean(act);
            if (bean.hasNode("documentTemplate") && bean.hasNode("document")) {
                refresh = true;
            }
        }
        return refresh;
    }

    /**
     * Determines if an act is a document that can be edited externally by OpenOffice.
     * <p/>
     * Note that if an act is supported for editing, but:
     * <ul>
     * <li>doesn't have a document attached; and</li>
     * <li>does have a template</li>
     * </ul>
     * this will assume it can be edited. This may not actually be the case, but it is an expensive operation to
     * determine if a template will produce a supported document
     *
     * @param act the act
     * @return {@code true} if the act is a document can be edited
     */
    public boolean canExternalEdit(Act act) {
        return (act instanceof DocumentAct) && canExternalEdit((DocumentAct) act);
    }

    /**
     * Determines if a document can be edited externally by OpenOffice.
     * <p/>
     * Note that if an act is supported for editing, but:
     * <ul>
     * <li>doesn't have a document attached; and</li>
     * <li>does have a template</li>
     * </ul>
     * this will assume it can be edited. This may not actually be the case, but it is an expensive operation to
     * determine if a template will produce a supported document
     *
     * @param act the document act
     * @return {@code true} if the document can be edited
     */
    public boolean canExternalEdit(DocumentAct act) {
        if (canEdit(act)) {
            EditableDocuments documents = ServiceHelper.getBean(EditableDocuments.class);
            return documents.canEdit(act);
        }
        return false;
    }

    /**
     * Determines if a document associated with a template can be edited externally by OpenOffice.
     *
     * @param template the document template
     * @return {@code true} if the associated document can be edited
     */
    public boolean canExternalEdit(Entity template) {
        DocumentAct act = getDocumentAct(template);
        return act != null && canExternalEdit(act);
    }

    /**
     * Launches an external editor to edit a document, if editing of the document is supported.
     * <p/>
     * If the document is a Word document, a confirmation will be displayed, as merge fields are lost when editing
     * Word documents in OpenOffice.
     *
     * @param act the document act
     */
    public void externalEdit(final DocumentAct act) {
        if (canExternalEdit(act) && act.getDocument() != null) {
            if (DocFormats.hasExtension(act.getFileName(), DocFormats.DOC_EXT)) {
                String title = Messages.get("document.edit.wordinopenoffice.title");
                String message = Messages.get("document.edit.wordinopenoffice.message");
                ConfirmationDialog.show(title, message, ConfirmationDialog.YES_NO, new PopupDialogListener() {
                    @Override
                    public void onYes() {
                        onExternalEdit(act);
                    }
                });
            } else {
                onExternalEdit(act);
            }
        }
    }

    /**
     * Launches an external editor to edit a document associated with a template, if editing of the document is
     * supported.
     * <p/>
     * If the document is a Word document, a confirmation will be displayed, as merge fields are lost when editing
     * Word documents in OpenOffice.
     *
     * @param template the document template
     */
    public void externalEdit(Entity template) {
        DocumentAct act = getDocumentAct(template);
        if (act != null) {
            externalEdit(act);
        }
    }

    /**
     * Launches an external editor for a document.
     *
     * @param act the document act
     */
    protected void onExternalEdit(DocumentAct act) {
        DocumentSessionManager sessions = ServiceHelper.getBean(DocumentSessionManager.class);
        Session session = sessions.create(act);
        String documentURL = ServletHelper.getContextURL() + "/document" + session.getPath();
        String url = ServletHelper.getRedirectURI("externaledit?") + documentURL;
        ApplicationInstance.getActive().enqueueCommand(new BrowserOpenWindowCommand(url, "", ""));
    }

    /**
     * Returns the document act associated the template
     *
     * @return the document act, or {@code null} if none exists
     */
    private DocumentAct getDocumentAct(Entity template) {
        DocumentAct result = null;
        if (TypeHelper.isA(template, DocumentArchetypes.DOCUMENT_TEMPLATE, DocumentArchetypes.SYSTEM_EMAIL_TEMPLATE,
                           DocumentArchetypes.USER_EMAIL_TEMPLATE)) {
            DocumentTemplate documentTemplate = new DocumentTemplate(template, ServiceHelper.getArchetypeService());
            result = documentTemplate.getDocumentAct();
        }
        return result;
    }
}
