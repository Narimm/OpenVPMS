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

package org.openvpms.web.workspace.admin.template;

import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.doc.AbstractDocumentTemplateQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryBrowser;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.component.workspace.ResultSetCRUDWorkspace;


/**
 * Document template workspace.
 *
 * @author Tim Anderson
 */
public class DocumentTemplateWorkspace extends ResultSetCRUDWorkspace<Entity> {

    /**
     * Constructs a {@link DocumentTemplateWorkspace}.
     */
    public DocumentTemplateWorkspace(Context context) {
        super("admin.documentTemplate", context);
        setArchetypes(Entity.class, DocumentArchetypes.DOCUMENT_TEMPLATE, DocumentArchetypes.SYSTEM_EMAIL_TEMPLATE,
                      DocumentArchetypes.USER_EMAIL_TEMPLATE, "entity.documentTemplateSMS*",
                      DocumentArchetypes.LETTERHEAD);
    }

    /**
     * Creates a new query to populate the browser.
     *
     * @return a new query
     */
    @Override
    protected Query<Entity> createQuery() {
        return new TemplateQuery(getArchetypes().getShortNames());
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    @Override
    protected CRUDWindow<Entity> createCRUDWindow() {
        QueryBrowser<Entity> browser = getBrowser();
        return new DocumentTemplateCRUDWindow(getArchetypes(), browser.getQuery(), browser.getResultSet(),
                                              getContext(), getHelpContext());
    }

    private static class TemplateQuery extends AbstractDocumentTemplateQuery {

        public TemplateQuery(String[] shortNames) {
            super(shortNames);
            setContains(true);
        }
    }

}
