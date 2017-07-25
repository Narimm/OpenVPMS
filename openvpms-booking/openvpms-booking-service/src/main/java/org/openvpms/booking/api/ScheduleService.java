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

package org.openvpms.booking.api;

import org.openvpms.booking.domain.AppointmentType;
import org.openvpms.booking.domain.FreeBusy;
import org.openvpms.booking.domain.Range;
import org.openvpms.booking.domain.Schedule;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.List;

/**
 * Service for querying appointment schedules.
 *
 * @author Tim Anderson
 */
@Path("schedules")
public interface ScheduleService {

    /**
     * Returns a schedule given its identifier.
     *
     * @param scheduleId the schedule identifier
     * @return the schedule
     */
    @GET
    @Produces({"application/json"})
    @Path("/{id}")
    Schedule getSchedule(@PathParam("id") long scheduleId);

    /**
     * Returns free time ranges for a schedule between two dates.
     *
     * @param scheduleId the schedule identifier
     * @param from       the start of the date range, in ISO date/time format
     * @param to         the end of the date range, in ISO date/time format
     * @param slots      if {@code true}, split ranges into slots
     * @return the free time ranges
     */
    @GET
    @Produces({"application/json"})
    @Path("/{id}/free")
    List<Range> getFree(@PathParam("id") long scheduleId, @QueryParam("from") String from,
                        @QueryParam("to") String to, @QueryParam("slots") boolean slots);

    /**
     * Returns busy time ranges for a schedule between two dates.
     *
     * @param scheduleId the schedule identifier
     * @param from       the start of the date range, in ISO date/time format
     * @param to         the end of the date range, in ISO date/time format
     * @param slots      if {@code true}, split ranges into slots
     * @return the busy time ranges
     */
    @GET
    @Produces({"application/json"})
    @Path("/{id}/busy")
    List<Range> getBusy(@PathParam("id") long scheduleId, @QueryParam("from") String from,
                        @QueryParam("to") String to, @QueryParam("slots") boolean slots);

    /**
     * Returns free and busy time ranges for a schedule between two dates.
     *
     * @param scheduleId the schedule identifier
     * @param from       the start of the date range, in ISO date/time format
     * @param to         the end of the date range, in ISO date/time format
     * @param slots      if {@code true}, split ranges into slots
     * @return the free and busy time ranges
     */
    @GET
    @Produces({"application/json"})
    @Path("/{id}/freebusy")
    FreeBusy getFreeBusy(@PathParam("id") long scheduleId, @QueryParam("from") String from,
                         @QueryParam("to") String to, @QueryParam("slots") boolean slots);

    /**
     * Returns the appointment types associated with a schedule.
     *
     * @param scheduleId the schedule identifier
     * @return the appointment types
     */
    @GET
    @Produces({"application/json"})
    @Path("/{id}/appointmentTypes")
    List<AppointmentType> getAppointmentTypes(@PathParam("id") long scheduleId);
}
