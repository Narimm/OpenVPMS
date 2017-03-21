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

package org.openvpms.web.workspace.admin.job;

import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.scheduler.JobScheduler;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.table.NameDescTableModel;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;

/**
 * Table for <em>entity.job*</em> entities.
 * <p>
 * Includes a column when they will be next run.
 *
 * @author Tim Anderson
 */
public class JobTableModel extends NameDescTableModel<Entity> {

    /**
     * The next run column index.
     */
    private int nextRunIndex;

    /**
     * The scheduler.
     */
    private final JobScheduler scheduler;

    /**
     * Constructs a {@link JobTableModel}.
     */
    public JobTableModel() {
        super();
        this.scheduler = ServiceHelper.getBean(JobScheduler.class);
    }

    /**
     * Constructs a {@link JobTableModel}.
     *
     * @param query the query. If both active and inactive results are being queried, an Active column will be displayed
     */
    public JobTableModel(Query<Entity> query) {
        super(query);
        this.scheduler = ServiceHelper.getBean(JobScheduler.class);
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the table column
     * @param row    the table row
     */
    @Override
    protected Object getValue(Entity object, TableColumn column, int row) {
        Object result = null;
        if (column.getModelIndex() == nextRunIndex) {
            Date time = scheduler.getNextRunTime(object);
            if (time != null) {
                result = DateFormatter.formatDateTimeAbbrev(time);
            }
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }


    /**
     * Creates the column model.
     *
     * @param showId        if {@code true}, show the ID
     * @param showArchetype if {@code true} show the archetype
     * @param showActive    if {@code true} show the active status
     * @return a new column model
     */
    protected TableColumnModel createTableColumnModel(boolean showId, boolean showArchetype, boolean showActive) {
        TableColumnModel model = super.createTableColumnModel(showId, showArchetype, showActive);
        nextRunIndex = getNextModelIndex(model);
        TableColumn column = new TableColumn(nextRunIndex);
        column.setHeaderValue(Messages.get("admin.job.nextrun"));
        model.addColumn(column);
        return model;
    }

}
