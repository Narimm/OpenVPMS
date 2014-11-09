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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.hl7;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.hl7.io.MessageDispatcher;
import org.openvpms.hl7.io.Statistics;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.IMObjectTableBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.table.BaseIMObjectTableModel;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;


/**
 * Displays the HL7 connectors.
 *
 * @author Tim Anderson
 */
public class HL7ConnectorBrowser extends IMObjectTableBrowser<Entity> {

    /**
     * Constructs an {@link HL7ConnectorBrowser} that queries IMObjects using the specified query.
     *
     * @param query   the query
     * @param context the layout context
     */
    public HL7ConnectorBrowser(Query<Entity> query, LayoutContext context) {
        super(query, context);
    }

    /**
     * Creates a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    protected IMTableModel<Entity> createTableModel(LayoutContext context) {
        return new Model(getQuery());
    }

    private static class Model extends BaseIMObjectTableModel<Entity> {

        private int lastRow = -1;

        private Statistics stats = null;

        private MessageDispatcher dispatcher;

        private static final int QUEUED = NEXT_INDEX;

        private static final int ERRORS = QUEUED + 1;

        private static final int LAST_PROCESSED = ERRORS + 1;

        private static final int LAST_ERROR = LAST_PROCESSED + 1;

        private static final int LAST_ERROR_MSG = LAST_ERROR + 1;

        /**
         * Constructs a {@link Model}.
         */
        public Model(Query<Entity> query) {
            super(null);
            boolean showArchetype = query.getShortNames().length > 1;
            boolean showActive = query.getActive() == BaseArchetypeConstraint.State.BOTH;
            DefaultTableColumnModel model
                    = (DefaultTableColumnModel) createTableColumnModel(true, showArchetype, showActive);
            model.addColumn(createTableColumn(QUEUED, "admin.hl7.queued"));
            model.addColumn(createTableColumn(LAST_PROCESSED, "admin.hl7.lastprocessed"));
            model.addColumn(createTableColumn(ERRORS, "admin.hl7.errors"));
            model.addColumn(createTableColumn(LAST_ERROR, "admin.hl7.lasterror"));
            model.addColumn(createTableColumn(LAST_ERROR_MSG, "admin.hl7.lasterrormessage"));
            setTableColumnModel(model);
            dispatcher = ServiceHelper.getBean(MessageDispatcher.class);
        }

        /**
         * Returns the value found at the given coordinate within the table.
         *
         * @param object the object
         * @param column the column
         * @param row    the row
         * @return the value at the given coordinate
         */
        @Override
        protected Object getValue(Entity object, TableColumn column, int row) {
            switch (column.getModelIndex()) {
                case QUEUED:
                    return getQueued(object, row);
                case ERRORS:
                    return getErrors(object, row);
                case LAST_PROCESSED:
                    return getLastProcessed(object, row);
                case LAST_ERROR:
                    return getLastError(object, row);
                case LAST_ERROR_MSG:
                    return getLastErrorMessage(object, row);
            }
            return super.getValue(object, column, row);
        }

        private Component getQueued(Entity object, int row) {
            Statistics stats = getStats(object, row);
            int queued = (stats != null) ? stats.getQueued() : 0;
            return LabelFactory.create(queued, new TableLayoutData());
        }

        private Component getErrors(Entity object, int row) {
            Statistics stats = getStats(object, row);
            int errors = (stats != null) ? stats.getErrors() : 0;
            return LabelFactory.create(errors, new TableLayoutData());
        }

        private String getLastProcessed(Entity object, int row) {
            String result = null;
            Statistics stats = getStats(object, row);
            if (stats != null) {
                Date date = stats.getProcessedTimestamp();
                if (date != null) {
                    result = DateFormatter.formatDateTimeAbbrev(date);
                }
            }
            return result;
        }

        private String getLastError(Entity object, int row) {
            String result = null;
            Statistics stats = getStats(object, row);
            if (stats != null) {
                Date date = stats.getErrorTimestamp();
                if (date != null) {
                    result = DateFormatter.formatDateTimeAbbrev(date);
                }
            }
            return result;
        }

        private String getLastErrorMessage(Entity object, int row) {
            Statistics stats = getStats(object, row);
            return (stats != null) ? stats.getErrorMessage() : null;
        }

        private Statistics getStats(Entity object, int row) {
            if (row == lastRow) {
                return stats;
            } else {
                stats = dispatcher.getStatistics(object.getObjectReference());
                lastRow = row;
            }
            return stats;
        }
    }
}
