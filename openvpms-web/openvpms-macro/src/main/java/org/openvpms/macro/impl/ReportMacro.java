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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.macro.impl;

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.macro.MacroException;

/**
 * Definition of a macro that launches a report.
 *
 * @author Tim Anderson
 */
public class ReportMacro extends AbstractExpressionMacro {

    /**
     * The report document template.
     */
    private final DocumentTemplate template;

    /**
     * Constructs a {@link ReportMacro}.
     *
     * @param lookup  the report macro lookup
     * @param service the archetype service
     * @throws MacroException if the document template cannot be found
     */
    public ReportMacro(Lookup lookup, IArchetypeService service) {
        this(new IMObjectBean(lookup, service), service);
    }

    /**
     * Constructs an {@link ReportMacro}.
     *
     * @param bean    the macro definition
     * @param service the archetype service
     * @throws MacroException if the document template cannot be found
     */
    protected ReportMacro(IMObjectBean bean, IArchetypeService service) {
        super(bean);
        IMObjectReference reference = bean.getReference("report");
        Entity entity = null;
        if (reference != null) {
            entity = (Entity) service.get(reference);
        }
        template = (entity != null) ? new DocumentTemplate(entity, service) : null;
    }

    /**
     * Returns the report document.
     *
     * @return the report document, or {@code null} if it cannot be found
     */
    public Document getDocument() {
        return template != null ? template.getDocument() : null;
    }

}
