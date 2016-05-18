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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.workflow.AppointmentService;
import org.openvpms.archetype.rules.workflow.CalendarBlock;
import org.openvpms.archetype.rules.workflow.CalendarBlocks;
import org.openvpms.archetype.rules.workflow.OverlappingEvents;
import org.openvpms.archetype.rules.workflow.Times;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Edit dialog for appointments.
 *
 * @author Tim Anderson
 */
public class AppointmentEditDialog extends CalendarEventEditDialog {

    /**
     * The customer.
     */
    private Party customer;

    private static final int MAX_OVERLAPS = 25;

    /**
     * Constructs a {@link AppointmentEditDialog}.
     *
     * @param editor  the editor
     * @param context the context
     */
    public AppointmentEditDialog(CalendarEventEditor editor, Context context) {
        super(editor, context);
    }

    /**
     * Caches the event start and end times, customer and repeat details.
     */
    protected void getState() {
        super.getState();
        this.customer = getEditor().getCustomer();
    }

    /**
     * Determines if the event can be saved without checking the event times.
     *
     * @return {@code true} if the event can be saved
     */
    @Override
    protected boolean noTimeCheckRequired() {
        boolean result = super.noTimeCheckRequired();
        if (result) {
            Party newCustomer = getEditor().getCustomer();
            result = ObjectUtils.equals(newCustomer, customer);
        }
        return result;
    }

    /**
     * Verifies that the event or events in the series don't overlap existing events.
     * <p/>
     * If they do, and double scheduling is allowed, a confirmation dialog is shown prompting to save or continue
     * editing.
     * <br/>
     * If double scheduling is not allowed, an error dialog is shown and no save is performed.
     *
     * @param times the event and series times
     * @param close determines if the dialog should close if the user OKs overlapping appointments
     * @return {@code true} if the event and series is valid, otherwise {@code false}
     */
    @Override
    protected boolean checkEventTimes(List<Times> times, final boolean close) {
        boolean result = true;
        CalendarEventEditor editor = getEditor();
        Entity schedule = editor.getSchedule();
        Party customer = editor.getCustomer();
        if (schedule != null && customer != null) {
            AppointmentService service = getService();
            OverlappingEvents overlaps = service.getOverlappingEvents(times, schedule, MAX_OVERLAPS + 1);
            if (overlaps != null) {
                if (!overlaps.allowDoubleBooking() && overlaps.getFirstAppointment() != null) {
                    // double booking in a schedule where double booking is not permitted
                    displayOverlapError(overlaps.getFirstAppointment());
                    result = false;
                } else {
                    CalendarBlocks blocks = overlaps.getCalendarBlocks(customer);
                    if (blocks != null) {
                        if (blocks.getReserved() != null) {
                            // the event overlaps a reserved calendar block
                            displayReservedCalendarBlockError(blocks.getReserved());
                            result = false;
                        }
                    }
                    if (result) {
                        result = false;
                        // have a double booked appointment or unreserved calendar block overlap
                        List<Times> appointments = overlaps.getAppointments();
                        List<CalendarBlock> unreservedBlocks
                                = (blocks != null) ? blocks.getUnreserved() : Collections.<CalendarBlock>emptyList();
                        if (overlaps.getEvents().size() > MAX_OVERLAPS) {
                            // there were too many overlaps, and it bailed out early
                            String title = Messages.get("workflow.scheduling.toomanyoverlaps.title");
                            String message = Messages.get("workflow.scheduling.toomanyoverlaps.message");
                            ErrorDialog.show(title, message);
                        } else {
                            String title = Messages.get("workflow.scheduling.overlap.title");
                            String message = Messages.get("workflow.scheduling.overlap.message");
                            OverlappingCalendarEventDialog dialog = new OverlappingCalendarEventDialog(
                                    title, message, appointments, unreservedBlocks);
                            dialog.addWindowPaneListener(new PopupDialogListener() {
                                @Override
                                public void onOK() {
                                    if (save()) {
                                        if (close) {
                                            close(OK_ID);
                                        } else {
                                            getState();
                                        }
                                    }
                                }
                            });
                            dialog.show();
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Displays an error when an appointment overlaps a reserved calendar block.
     *
     * @param block the calendar block
     */
    private void displayReservedCalendarBlockError(CalendarBlock block) {
        String displayName = DescriptorHelper.getDisplayName(getEvent());
        String title = Messages.format("workflow.scheduling.reserved.title", displayName);
        StringBuilder buffer = new StringBuilder();
        List<Lookup> types = new ArrayList<>();
        types.addAll(block.getCustomerAccountTypes());
        types.addAll(block.getCustomerTypes());
        Collections.sort(types, IMObjectSorter.getNameComparator(true));
        for (Lookup type : types) {
            if (buffer.length() != 0) {
                buffer.append(", ");
            }
            buffer.append(type.getName());
        }

        String message = Messages.format("workflow.scheduling.reserved.message", displayName, block.getName(),
                                         DateFormatter.formatDate(block.getStartTime(), false),
                                         DateFormatter.formatTime(block.getStartTime(), false),
                                         buffer);
        ErrorHelper.show(title, message);
    }

}
