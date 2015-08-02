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
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Editor for <em>participation.document</em> participation relationships.
 *
 * @author Tim Anderson
 */
public class DocumentParticipationEditor extends AbstractDocumentParticipationEditor {

    /**
     * Constructs a {@link DocumentParticipationEditor}.
     *
     * @param participation the participation to edit
     * @param parent        the parent entity
     * @param context       the layout context. May be {@code null}.
     */
    public DocumentParticipationEditor(Participation participation, Entity parent, LayoutContext context) {
        super(participation, parent, context);
    }

    /**
     * Creates a new document act.
     *
     * @return a new document act
     */
    @Override
    protected DocumentAct createDocumentAct() {
        return createDocumentAct(DocumentArchetypes.DOCUMENT_TEMPLATE_ACT);
    }

}
