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

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Editor for <em>entity.documentTemplate</em>s entities.
 * <p/>
 * This supports associating a single mandatory <em>act.documentTemplate</em> with the template, representing the
 * template content.
 *
 * @author Tim Anderson
 */
public class DocumentTemplateEditor extends AbstractDocumentTemplateEditor {

    /**
     * Constructs a {@link DocumentTemplateEditor}.
     *
     * @param template the object to edit
     * @param parent   the parent object. May be {@code null}
     * @param context  the layout context. May be {@code null}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public DocumentTemplateEditor(Entity template, IMObject parent, LayoutContext context) {
        super(template, parent, false, null, context);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new DocumentTemplateLayoutStrategy(getSelector());
    }
}