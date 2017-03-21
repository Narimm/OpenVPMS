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

package org.openvpms.web.workspace.reporting.reminder;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.scheduler.JobScheduler;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;
import java.util.List;

/**
 * A browser for <em>act.patientReminderItem*</em> acts with {@code PENDING} status.
 *
 * @author Tim Anderson
 */
class PendingReminderItemBrowser extends ReminderItemBrowser {

    /**
     * Constructs a {@link PendingReminderItemBrowser}.
     *
     * @param query   the query
     * @param context the context
     */
    public PendingReminderItemBrowser(ReminderItemQuery query, LayoutContext context) {
        super(query, context);
    }

    /**
     * Creates a table browser that changes the model depending on what columns have been queried on.
     *
     * @param query   the query
     * @param context the layout context
     * @return a new browser
     */
    @Override
    protected Browser<ObjectSet> createBrowser(ReminderItemQuery query, LayoutContext context) {
        return new PendingReminderItemObjectSetBrowser(query, context);
    }

    private static class PendingReminderItemObjectSetBrowser extends ReminderItemObjectSetBrowser {
        private Label label;

        public PendingReminderItemObjectSetBrowser(ReminderItemQuery query, LayoutContext context) {
            super(query.getQuery(), context);
            label = LabelFactory.create(null, true);
            label.setStyleName(Styles.BOLD);
            label.setLayoutData(ColumnFactory.layout(Alignment.ALIGN_CENTER));
        }

        /**
         * Adds the table to the browser container.
         *
         * @param container  the browser container
         * @param hasResults determines if there are results to display
         */
        @Override
        protected void doLayout(Component container, boolean hasResults) {
            super.doLayout(container, hasResults);
            if (!hasResults) {
                JobScheduler scheduler = ServiceHelper.getBean(JobScheduler.class);
                List<IMObject> jobs = scheduler.getJobs("entity.jobPatientReminderQueue");
                if (jobs.isEmpty()) {
                    label.setText(Messages.get("reporting.reminder.send.nojob"));
                } else {
                    IMObject configuration = jobs.get(0);
                    Date date = scheduler.getNextRunTime(configuration);
                    if (date == null) {
                        label.setText(Messages.format("reporting.reminder.send.notscheduled",
                                                      configuration.getName()));
                    } else {
                        label.setText(Messages.format("reporting.reminder.send.scheduled",
                                                      DateFormatter.formatDateTimeAbbrev(date)));

                    }
                }
            }
        }

        /**
         * Lays out the container when there are no results to display.
         *
         * @param container the container
         */
        @Override
        protected void doLayoutForNoResults(Component container) {
            Column wrapper = ColumnFactory.create(Styles.LARGE_INSET, label);
            wrapper.setLayoutData(SplitPaneFactory.layout(Alignment.ALIGN_CENTER));
            container.add(wrapper);
        }
    }
}
