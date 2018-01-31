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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.job.scheduledreport;

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.doc.DocumentTemplateReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.relationship.EntityLinkEditor;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.reporting.report.ReportQuery;

/**
 * An editor for <em>entityLink.scheduledReportTemplate</em> archetypes, that constrains templates to SQL reports.
 *
 * @author Tim Anderson
 */
public class ScheduledReportTemplateEditor extends EntityLinkEditor {

    /**
     * Constructs a {@link ScheduledReportTemplateEditor}.
     *
     * @param relationship the link
     * @param parent       the parent object
     * @param context      the layout context
     */
    public ScheduledReportTemplateEditor(EntityLink relationship, IMObject parent, LayoutContext context) {
        super(relationship, parent, context);
    }

    /**
     * Returns the document template.
     *
     * @return document template. May be {@code null}
     */
    public DocumentTemplate getTemplate() {
        Entity template = (Entity) getObject(getTarget().getReference());
        if (template != null) {
            return new DocumentTemplate(template, ServiceHelper.getArchetypeService());
        }
        return null;
    }

    /**
     * Creates a new reference editor.
     *
     * @param property the reference property
     * @param context  the layout context
     * @return a new reference editor
     */
    @Override
    protected IMObjectReferenceEditor<Entity> createReferenceEditor(Property property, LayoutContext context) {
        return new DocumentTemplateReferenceEditor(property, getParent(), context, false) {
            @Override
            protected Query<Entity> createQuery(String name) {
                ReportQuery query = new ReportQuery(context.getContext().getUser());
                query.setValue(name);
                return query;
            }
        };
    }

}
