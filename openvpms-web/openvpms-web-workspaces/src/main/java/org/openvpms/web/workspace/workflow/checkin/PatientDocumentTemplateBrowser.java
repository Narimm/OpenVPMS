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

package org.openvpms.web.workspace.workflow.checkin;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.model.object.Relationship;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.IMObjectTableBrowser;
import org.openvpms.web.component.im.query.MultiSelectTableBrowser;
import org.openvpms.web.component.im.table.DefaultIMObjectTableModel;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.resource.i18n.Messages;

/**
 * An {@link IMObjectTableBrowser} that enables multiple document templates to be selected.
 *
 * @author Tim Anderson
 */
class PatientDocumentTemplateBrowser extends MultiSelectTableBrowser<Entity> {

    /**
     * Constructs a {@link PatientDocumentTemplateBrowser}.
     *
     * @param query   the document template query
     * @param context the context
     */
    public PatientDocumentTemplateBrowser(ScheduleDocumentTemplateQuery query, LayoutContext context) {
        super(query, context);
        preselect(query.getSchedule());
        preselect(query.getWorkList());
    }

    /**
     * Creates a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    protected MultiSelectTableModel createTableModel(LayoutContext context) {
        MultiSelectTableModel model = super.createTableModel(context);
        model.getSelectionColumn().setHeaderValue(Messages.get("batchprintdialog.print"));
        return model;
    }

    /**
     * Creates a new table model the displays the content of the objects.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    protected IMTableModel<Entity> createChildTableModel(LayoutContext context) {
        return new DefaultIMObjectTableModel<>();
    }

    /**
     * Prselects documents flagged for printing by a schedule or work list.
     *
     * @param entity the schedule/work list. May be {@code null}
     */
    private void preselect(Entity entity) {
        if (entity != null) {
            IMObjectBean bean = new IMObjectBean(entity);
            for (Relationship relationship : bean.getValues("templates", Relationship.class)) {
                IMObjectBean relBean = bean.getBean(relationship);
                if (relBean.getBoolean("print")) {
                    Entity object = relBean.getObject("target", Entity.class);
                    if (object != null) {
                        getSelectionTracker().setSelected(object, true);
                    }
                }
            }
        }
    }

}
