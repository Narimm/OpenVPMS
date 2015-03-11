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

package org.openvpms.web.component.app;

import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;

import java.util.Date;


/**
 * Application context information.
 *
 * @author Tim Anderson
 */
public interface Context {

    /**
     * Practice short name.
     */
    public static final String PRACTICE_SHORTNAME = "party.organisationPractice";

    /**
     * Location short name.
     */
    public static final String LOCATION_SHORTNAME = "party.organisationLocation";

    /**
     * Stock location short name.
     */
    public static final String STOCK_LOCATION_SHORTNAME = "party.organisationStockLocation";

    /**
     * Customer short name.
     */
    public static final String CUSTOMER_SHORTNAME = "party.customer*";

    /**
     * Patient short name.
     */
    public static final String PATIENT_SHORTNAME = "party.patient*";

    /**
     * Supplier short name.
     */
    public static final String SUPPLIER_SHORTNAME = "party.supplier*";

    /**
     * Product short name.
     */
    public static final String PRODUCT_SHORTNAME = "product.*";

    /**
     * Till short name.
     */
    public static final String TILL_SHORTNAME = "party.organisationTill";

    /**
     * Bank Deposit short name.
     */
    public static final String DEPOSIT_SHORTNAME = "party.organisationDeposit";

    /**
     * Clinician short name.
     */
    public static final String CLINICIAN_SHORTNAME = "security.user";

    /**
     * Schedule view short name.
     */
    public static final String SCHEDULE_VIEW_SHORTNAME = "entity.organisationScheduleView";

    /**
     * Schedule short name.
     */
    public static final String SCHEDULE_SHORTNAME = "party.organisationSchedule";

    /**
     * Appointment short name.
     */
    public static final String APPOINTMENT_SHORTNAME = ScheduleArchetypes.APPOINTMENT;

    /**
     * Work list view short name.
     */
    public static final String WORKLIST_VIEW_SHORTNAME = "entity.organisationWorkListView";

    /**
     * Work list short name.
     */
    public static final String WORKLIST_SHORTNAME = "party.organisationWorkList";

    /**
     * Task short name.
     */
    public static final String TASK_SHORTNAME = ScheduleArchetypes.TASK;

    /**
     * Sets the current object being viewed/edited.
     *
     * @param object the current object being viewed/edited. May be {@code null}
     */
    void setCurrent(IMObject object);

    /**
     * Returns the current object being viewed/edited.
     *
     * @return the object being viewed/edited, or {@code null} if there is no current object
     */
    IMObject getCurrent();

    /**
     * Sets the current user.
     *
     * @param user the current user. May be {@code null}
     */
    void setUser(User user);

    /**
     * Returns the current user.
     *
     * @return the current user. May be {@code null}
     */
    User getUser();

    /**
     * Sets the current practice.
     *
     * @param practice the current practice. May be {@code null}
     */
    void setPractice(Party practice);

    /**
     * Returns the current practice.
     *
     * @return the current practice. May be {@code null}
     */
    Party getPractice();

    /**
     * Sets the current location.
     *
     * @param location the current location. May be {@code null}
     */
    void setLocation(Party location);

    /**
     * Returns the current location.
     *
     * @return the current location. May be {@code null}
     */
    Party getLocation();

    /**
     * Sets the current stock location.
     *
     * @param location the current location. May be {@code null}
     */
    void setStockLocation(Party location);

    /**
     * Returns the current stock location.
     *
     * @return the current stock location, or {@code null} if there is no current location
     */
    Party getStockLocation();

    /**
     * Sets the current customer.
     *
     * @param customer the current customer. May be {@code null}
     */
    void setCustomer(Party customer);

    /**
     * Returns the current customer.
     *
     * @return the current customer, or {@code null} if there is no current customer
     */
    Party getCustomer();

    /**
     * Sets the current patient.
     *
     * @param patient the current patient. May be {@code null}
     */
    void setPatient(Party patient);

    /**
     * Returns the current patient.
     *
     * @return the current patient, or {@code null} if there is no current patient
     */
    Party getPatient();

    /**
     * Sets the current supplier.
     *
     * @param supplier the current supplier. May be {@code null}
     */
    void setSupplier(Party supplier);

    /**
     * Returns the current suppller.
     *
     * @return the current supplier, or {@code null} if there is no current supplier
     */
    Party getSupplier();

    /**
     * Sets the current product.
     *
     * @param product the current product. May be {@code null}
     */
    void setProduct(Product product);

    /**
     * Returns the current product.
     *
     * @return the current product, or {@code null} if there is no current product
     */
    Product getProduct();

    /**
     * Sets the current deposit.
     *
     * @param deposit the current deposit. May be {@code null}
     */
    void setDeposit(Party deposit);

    /**
     * Returns the current deposit.
     *
     * @return the current deposit, or {@code null} if there is no current deposit
     */
    Party getDeposit();

    /**
     * Sets the current till.
     *
     * @param till the current till. May be {@code null}
     */
    void setTill(Party till);

    /**
     * Returns the current till.
     *
     * @return the current till, or {@code null} if there is no current till
     */
    Party getTill();

    /**
     * Sets the current clinician.
     *
     * @param clinician the current clinician. May be {@code null}
     */
    void setClinician(User clinician);

    /**
     * Returns the current clinician.
     *
     * @return the current clinician, or {@code null} if there is no current clinician
     */
    User getClinician();

    /**
     * Sets the current schedule view.
     *
     * @param view the current schedule view. May be {@code null}
     */
    void setScheduleView(Entity view);

    /**
     * Returns the current schedule view.
     *
     * @return the current schedule view. May be {@code null}
     */
    Entity getScheduleView();

    /**
     * Sets the current schedule.
     *
     * @param schedule the current schedule. May be {@code null}
     */
    void setSchedule(Party schedule);

    /**
     * Returns the current schedule.
     *
     * @return the current schedule. May be {@code null}
     */
    Party getSchedule();

    /**
     * The current schedule date.
     *
     * @return the current schedule date
     */
    Date getScheduleDate();

    /**
     * Sets the current schedule date.
     *
     * @param date the current schedule date
     */
    void setScheduleDate(Date date);

    /**
     * Sets the current appointment.
     *
     * @param appointment the current appointment. May be {@code null}
     */
    void setAppointment(Act appointment);

    /**
     * Returns the current appointment.
     *
     * @return the current appointment. May be {@code null}
     */
    Act getAppointment();

    /**
     * Sets the current work list view.
     *
     * @param view the current work list view. May be {@code null}
     */
    void setWorkListView(Entity view);

    /**
     * Returns the current work list view.
     *
     * @return the current work list view. May be {@code null}
     */
    Entity getWorkListView();

    /**
     * Sets the current work list.
     *
     * @param workList the current work list. May be {@code null}
     */
    void setWorkList(Party workList);

    /**
     * Returns the current work list.
     *
     * @return the current work list. May be {@code null}
     */
    Party getWorkList();

    /**
     * Sets the current work list date.
     *
     * @param date the current work list date
     */
    void setWorkListDate(Date date);

    /**
     * Returns the current work list date.
     *
     * @return the current work list date
     */
    Date getWorkListDate();

    /**
     * Sets the current task.
     *
     * @param task the current task
     */
    void setTask(Act task);

    /**
     * Returns the current task.
     *
     * @return the current task. May be {@code null}
     */
    Act getTask();

    /**
     * Adds an object to the context.
     *
     * @param object the object to add.
     */
    void addObject(IMObject object);

    /**
     * Removes an object from the context.
     *
     * @param object the object to remove
     */
    void removeObject(IMObject object);

    /**
     * Returns an object for the specified key.
     *
     * @param key the context key
     * @return the object corresponding to {@code key} or {@code null} if none is found
     */
    IMObject getObject(String key);

    /**
     * Returns a context object that matches the specified archetype range.
     *
     * @param range the archetype range
     * @return a context object whose short name is in {@code range} or {@code null} if none exists
     */
    IMObject getObject(String[] range);

    /**
     * Returns a context object that matches the specified reference.
     *
     * @param reference the object reference
     * @return the context object whose reference matches {@code reference}, or {@code null} if there are no matches
     */
    IMObject getObject(IMObjectReference reference);

    /**
     * Sets a context object.
     *
     * @param key    the context key
     * @param object the object
     */
    void setObject(String key, IMObject object);

    /**
     * Helper to return the context objects in an array.
     *
     * @return the a list of the context objects
     */
    IMObject[] getObjects();

}
