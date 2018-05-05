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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.doc;

import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;


/**
 * Editor for <em>participation.documentTemplate</em> participation relationships.
 *
 * @author Tim Anderson
 */
public class DocumentTemplateParticipationEditor extends ParticipationEditor<Entity> {

    /**
     * Constructs a {@code DocumentTemplateParticipationEditor}.
     *
     * @param participation the participation
     * @param parent        the parent act
     * @param context       the layout context. May be {@code null}
     */
    public DocumentTemplateParticipationEditor(Participation participation, DocumentAct parent, LayoutContext context) {
        super(participation, parent, context);
    }

    /**
     * Creates a new object reference editor.
     *
     * @param property the reference property
     * @return a new object reference editor
     */
    @Override
    protected IMObjectReferenceEditor<Entity> createEntityEditor(Property property) {
        LayoutContext context = getLayoutContext();
        LayoutContext subContext = new DefaultLayoutContext(context, context.getHelpContext().topic("template"));
        DocumentTemplateReferenceEditor editor = new DocumentTemplateReferenceEditor(property, getObject(), subContext);
        if (getParent() != null) {
            String shortname = getParent().getArchetypeId().getShortName();
            editor.setTypes(shortname);
        }
        return editor;
    }
}
