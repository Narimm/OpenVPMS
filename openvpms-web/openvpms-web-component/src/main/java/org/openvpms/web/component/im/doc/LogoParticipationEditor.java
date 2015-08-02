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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.doc;

import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.ImageDocumentHandler;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

/**
 * An editor for <em>participation.logo</em> participation relationships.
 *
 * @author Tim Anderson
 */
public class LogoParticipationEditor extends AbstractDocumentParticipationEditor {

    /**
     * Constructs a {@link LogoParticipationEditor}.
     *
     * @param participation the participation to edit
     * @param parent        the parent entity
     * @param context       the layout context
     */
    public LogoParticipationEditor(Participation participation, Entity parent, LayoutContext context) {
        super(participation, parent, context);
        setDocumentHandler(ServiceHelper.getBean(ImageDocumentHandler.class));
    }

    /**
     * Creates a new document act.
     *
     * @return a new document act
     */
    @Override
    protected DocumentAct createDocumentAct() {
        return createDocumentAct(DocumentArchetypes.LOGO_ACT);
    }

    /**
     * Populates a document act with details from a document.
     *
     * @param act      the act to populate
     * @param document the document
     */
    @Override
    protected void populateDocumentAct(DocumentAct act, Document document) {
        super.populateDocumentAct(act, document);
        IMObjectBean bean = new IMObjectBean(document);
        int width = bean.getInt("width");
        int height = bean.getInt("height");
        String description;
        if (width != -1 && height != -1) {
            description = Messages.format("admin.pratice.logo.size", width, height);
        } else {
            description = Messages.get("admin.practice.logo.nosize");
        }
        act.setDescription(description);
    }
}
