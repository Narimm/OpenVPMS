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

/**
 * Definition of a macro that launches a report.
 *
 * @author Tim Anderson
 */
public class ReportMacro extends Macro {

    /**
     * The report document template.
     */
    private final DocumentTemplate template;

    /**
     * The report page width, in characters. This only applies to Jasperreports.
     */
    private final int width;

    /**
     * The report page height, in characters. This only applies to Jasperreports.
     */
    private final int height;


    /**
     * Constructs a {@link ReportMacro}.
     *
     * @param lookup  the report macro lookup
     * @param service the archetype service
     */
    public ReportMacro(Lookup lookup, IArchetypeService service) {
        super(lookup);
        IMObjectBean bean = new IMObjectBean(lookup, service);
        IMObjectReference reference = bean.getReference("report");
        Entity entity = null;
        if (reference != null) {
            entity = (Entity) service.get(reference);
        }
        template = (entity != null) ? new DocumentTemplate(entity, service) : null;
        this.width = bean.getInt("width");
        this.height = bean.getInt("height");
    }

    /**
     * Returns the report document.
     *
     * @return the report document, or {@code null} if it cannot be found
     */
    public Document getDocument() {
        return template != null ? template.getDocument() : null;
    }

    /**
     * Returns the report page width.
     * <p/>
     * This attribute only applies to Jasperreports
     *
     * @return the report page width, in characters
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the report page height.
     * <p/>
     * This attribute only applies to Jasperreports
     *
     * @return the report page height, in characters
     */
    public int getHeight() {
        return height;
    }
}
