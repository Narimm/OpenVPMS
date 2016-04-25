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

package org.openvpms.web.component.im.doc;

import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.component.system.common.query.ArchetypeQueryException;


/**
 * Query for <em>entity.documentTemplate</em>s.
 *
 * @author Tim Anderson
 */
public class DocumentTemplateQuery extends AbstractDocumentTemplateQuery {

    /**
     * Constructs a {@link DocumentTemplateQuery}.
     *
     * @throws ArchetypeQueryException if the short name don't match any archetypes
     */
    public DocumentTemplateQuery() {
        super(new String[]{DocumentArchetypes.DOCUMENT_TEMPLATE});
    }

}
