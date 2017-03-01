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

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.scheduler.JobScheduler;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.workspace.ResultSetCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;

/**
 * CRUD window for the job workspace.
 *
 * @author Tim Anderson
 */
public class JobCRUDWindow extends ResultSetCRUDWindow<Entity> {

    /**
     * Run button identifier.
     */
    private static final String RUN_ID = "button.run";

    /**
     * Constructs a {@link JobCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create instances of
     * @param query      the query. May be {@code null}
     * @param set        the result set. May be {@code null}
     * @param context    the context
     * @param help       the help context
     */
    public JobCRUDWindow(Archetypes<Entity> archetypes, Query<Entity> query,
                         ResultSet<Entity> set, Context context, HelpContext help) {
        super(archetypes, query, set, context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(RUN_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onRun();
            }
        });
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        super.enableButtons(buttons, enable);
        buttons.setEnabled(RUN_ID, enable);
    }

    /**
     * Invoked to run a job.
     */
    protected void onRun() {
        Entity object = getObject();
        final Entity config = IMObjectHelper.reload(object);
        if (config != null) {
            final JobScheduler scheduler = ServiceHelper.getBean(JobScheduler.class);
            String title = Messages.get("admin.job.run.title");
            if (!config.isActive()) {
                InformationDialog.show(title, Messages.format("admin.job.run.inactive", config.getName()));
            } else {
                Date run = scheduler.getNextRunTime(config);
                String scheduled = (run != null) ? DateFormatter.formatDateTimeAbbrev(run) :
                                   Messages.get("admin.job.run.never");
                String message = Messages.format("admin.job.run.message", config.getName(), scheduled);
                ConfirmationDialog.show(title, message, ConfirmationDialog.YES_NO, new PopupDialogListener() {
                    @Override
                    public void onYes() {
                        scheduler.run(config);
                    }
                });
            }
        } else {
            onRefresh(object);
        }
    }
}
