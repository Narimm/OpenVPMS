/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow;

import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.AndConstraint;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.resource.util.Messages;

import java.util.Date;
import java.util.List;


/**
 * Edit dialog for appointment acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentEditDialog extends EditDialog {

    /**
     * Construct a new <code>AppointmentEditDialog</code>.
     *
     * @param editor  the editor
     * @param context the layout context
     */
    public AppointmentEditDialog(IMObjectEditor editor, LayoutContext context) {
        super(editor, context);
    }

    /**
     * Save the current object.
     */
    @Override
    protected void onApply() {
        if (!checkForOverlappingAppointment(false)) {
            super.onApply();
        }
    }

    /**
     * Save the current object, and close the editor.
     */
    @Override
    protected void onOK() {
        if (!checkForOverlappingAppointment(true)) {
            super.onOK();
        }
    }

    /**
     * Determines if the appointment overlaps an existing appointment.
     * If so, and double scheduling is allowed, a confirmation dialog is shown
     * prompting to save or continue editing. If double scheduling is not
     * allowed, an error dialog is shown and no save is performed.
     *
     * @param close determines if the dialog should close if the user OKs
     *              overlapping appointments
     * @return <code>true</code> if there are overlapping appointments, otherwise
     *         <code>false</code>
     */
    private boolean checkForOverlappingAppointment(final boolean close) {
        final IMObjectEditor editor = getEditor();
        IMObject object = editor.getObject();
        ActBean appointment = new ActBean((Act) object);
        Date startTime = appointment.getDate("startTime");
        Date endTime = appointment.getDate("endTime");
        boolean overlap = false;
        if (startTime != null && endTime != null) {
            overlap = hasOverlappingAppointments(object.getUid(), startTime,
                                                 endTime);
            if (overlap) {
                if (!allowDoubleBooking(appointment)) {
                    String title = Messages.get(
                            "workflow.scheduling.nodoubleschedule.title");
                    String message = Messages.get(
                            "workflow.scheduling.nodoubleschedule.message");
                    ErrorDialog.show(title, message);
                } else {
                    String title = Messages.get(
                            "workflow.scheduling.doubleschedule.title");
                    String message = Messages.get(
                            "workflow.scheduling.doubleschedule.message");
                    final ConfirmationDialog dialog = new ConfirmationDialog(
                            title, message);
                    dialog.addWindowPaneListener(new WindowPaneListener() {
                        public void windowPaneClosing(WindowPaneEvent e) {
                            if (ConfirmationDialog.OK_ID.equals(
                                    dialog.getAction())) {
                                if (editor.save() && close) {
                                    close();
                                }
                            }
                        }
                    });
                    dialog.show();
                }
            }
        }
        return overlap;
    }

    /**
     * Determines if double booking is allowed.
     *
     * @param appointment the appointment
     * @return <code>true</code> if double booking is allowed, otherwise
     *         <code>false</code>
     */
    private boolean allowDoubleBooking(ActBean appointment) {
        boolean result;
        IMObject schedule = appointment.getParticipant(
                "participation.schedule");
        if (schedule != null) {
            IMObjectBean bean = new IMObjectBean(schedule);
            result = bean.getBoolean("allowDoubleBooking");
        } else {
            result = false;
        }
        return result;
    }

    /**
     * Determines if there are acts that overlap with the appointment.
     *
     * @param uid       the object identifier
     * @param startTime the appointment start time
     * @param endTime   the appointment end time
     * @return a list of acts that overlap with the appointment
     */
    private boolean hasOverlappingAppointments(long uid, Date startTime,
                                               Date endTime) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        ArchetypeQuery query = new ArchetypeQuery(
                null, "act", "customerAppointment", false, true);
        query.setFirstRow(0);
        query.setNumOfRows(1);
        query.add(new NodeConstraint("uid", RelationalOp.NE, uid));
        OrConstraint or = new OrConstraint();
        or.add(createTimeConstraint("startTime", startTime, endTime));
        or.add(createTimeConstraint("endTime", startTime, endTime));
        query.add(or);
        List<IMObject> overlaps = service.get(query).getRows();
        return !overlaps.isEmpty();
    }

    /**
     * Helper to create a time range constraint of the form:
     * <code>node > from and node < to</code>.
     *
     * @param node the node name
     * @param from the from time
     * @param to   the to time
     * @return a new constraint
     */
    private IConstraint createTimeConstraint(String node, Date from, Date to) {
        AndConstraint and = new AndConstraint();
        and.add(new NodeConstraint(node, RelationalOp.GT, from));
        and.add(new NodeConstraint(node, RelationalOp.LT, to));
        return and;
    }
}
