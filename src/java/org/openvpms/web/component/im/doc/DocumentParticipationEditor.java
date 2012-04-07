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

import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Editor for <em>participation.document</em> participation relationships.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentParticipationEditor extends AbstractDocumentParticipationEditor {

    /**
     * Constructs a <tt>DocumentParticipationEditor</tt>.
     *
     * @param participation the participation to edit
     * @param parent        the parent entity
     * @param context       the layout context. May be <tt>null</tt>.
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
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        return (DocumentAct) service.create("act.documentTemplate");
    }
}
