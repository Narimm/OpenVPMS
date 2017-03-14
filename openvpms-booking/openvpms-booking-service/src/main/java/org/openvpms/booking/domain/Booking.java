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

package org.openvpms.booking.domain;

import java.util.Date;

/**
 * Appointment booking request.
 *
 * @author Tim Anderson
 */
public class Booking {

    /**
     * The practice location identifier.
     */
    private long location;

    /**
     * The schedule identifier.
     */
    private long schedule;

    /**
     * The appointment type identifier.
     */
    private long appointmentType;

    /**
     * The appointment start time.
     */
    private Date start;

    /**
     * The appointment end time.
     */
    private Date end;

    /**
     * The customer title.
     */
    private String title;

    /**
     * The customer first name.
     */
    private String firstName;

    /**
     * The customer last name.
     */
    private String lastName;

    /**
     * The customer email.
     */
    private String email;

    /**
     * The customer phone.
     */
    private String phone;

    /**
     * The customer mobile.
     */
    private String mobile;

    /**
     * The patient name.
     */
    private String patientName;

    /**
     * The notes.
     */
    private String notes;

    /**
     * Default constructor.
     */
    public Booking() {
    }

    /**
     * Returns the practice location identifier.
     *
     * @return the practice location identifier
     */
    public long getLocation() {
        return location;
    }

    /**
     * Sets the practice location identifier.
     *
     * @param location the practice location identifier
     */
    public void setLocation(long location) {
        this.location = location;
    }

    /**
     * Returns the schedule identifier.
     *
     * @return the schedule identifier
     */
    public long getSchedule() {
        return schedule;
    }

    /**
     * Sets the schedule identifier.
     *
     * @param schedule the schedule identifier
     */
    public void setSchedule(long schedule) {
        this.schedule = schedule;
    }

    /**
     * Returns the appointment type.
     *
     * @return the appointment type
     */
    public long getAppointmentType() {
        return appointmentType;
    }

    /**
     * Sets the appointment type.
     *
     * @param appointmentType the appointment type
     */
    public void setAppointmentType(long appointmentType) {
        this.appointmentType = appointmentType;
    }

    /**
     * Returns the appointment start time.
     *
     * @return the appointment start time
     */
    public Date getStart() {
        return start;
    }

    /**
     * Sets the appointment start time.
     *
     * @param start the appointment start time
     */
    public void setStart(Date start) {
        this.start = start;
    }

    /**
     * Returns the appointment end time.
     *
     * @return the appointment end time
     */
    public Date getEnd() {
        return end;
    }

    /**
     * Sets the appointment end time.
     *
     * @param end the appointment end time
     */
    public void setEnd(Date end) {
        this.end = end;
    }

    /**
     * Returns the customer title.
     *
     * @return the customer title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the customer title.
     *
     * @param title the customer title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the customer first name.
     *
     * @return the customer first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the customer first name.
     *
     * @param firstName the customer first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the customer last name.
     *
     * @return the customer last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the customer last name.
     *
     * @param lastName the customer last name.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the customer email.
     *
     * @return the customer email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the customer email.
     *
     * @param email the customer email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the customer phone number.
     *
     * @return the customer phone number. May be {@code null}
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the customer phone number.
     *
     * @param phone the customer phone number. May be {@code null}
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns the customer mobile number.
     *
     * @return the customer mobile number. May be {@code null}
     */
    public String getMobile() {
        return mobile;
    }

    /**
     * Sets the customer mobile number.
     *
     * @param mobile the customer mobile number. May be {@code null}
     */
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    /**
     * Returns the patient name.
     *
     * @return the patient name
     */
    public String getPatientName() {
        return patientName;
    }

    /**
     * Sets the patient name.
     *
     * @param patientName the patient name
     */
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    /**
     * Returns the notes.
     *
     * @return the notes. May be {@code null}
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the notes.
     *
     * @param notes the notes. May be {@code null}
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
