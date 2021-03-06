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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.app;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.model.object.Reference;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Abstract implementation of the {@link Context} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractContext implements Context {

    /**
     * The context objects.
     */
    private final Map<String, IMObject> objects = new HashMap<>();

    /**
     * The object being viewed/edited.
     */
    private IMObject current;

    /**
     * The current user.
     */
    private User user;

    /**
     * The current schedule date.
     */
    private Date scheduleDate;

    /**
     * The current work list date.
     */
    private Date workListDate;

    /**
     * Set of recognised short names.
     */
    private static final String[] SHORT_NAMES = {
            APPOINTMENT_SHORTNAME, CLINICIAN_SHORTNAME, CUSTOMER_SHORTNAME, DEPOSIT_SHORTNAME, LOCATION_SHORTNAME,
            PATIENT_SHORTNAME, PRACTICE_SHORTNAME, PRODUCT_SHORTNAME, SCHEDULE_SHORTNAME, SCHEDULE_VIEW_SHORTNAME,
            STOCK_LOCATION_SHORTNAME, SUPPLIER_SHORTNAME, TASK_SHORTNAME, TILL_SHORTNAME, WORKLIST_SHORTNAME,
            WORKLIST_VIEW_SHORTNAME};

    /**
     * Sets the current object being viewed/edited.
     *
     * @param object the current object being viewed/edited. May be
     *               {@code null}
     */
    public void setCurrent(IMObject object) {
        current = object;
    }

    /**
     * Returns the current object being viewed/edited.
     *
     * @return the object being viewed/edited, or {@code null} if there is
     * no current object
     */
    public IMObject getCurrent() {
        return current;
    }

    /**
     * Sets the current user.
     *
     * @param user the current user
     */
    public void setUser(User user) {
        // don't add the current user to 'objects' as it would clash with
        // clinician.
        this.user = user;
    }

    /**
     * Returns the current user.
     *
     * @return the current user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the current practice.
     *
     * @param practice the current practice
     */
    public void setPractice(Party practice) {
        setObject(PRACTICE_SHORTNAME, practice);
    }

    /**
     * Returns the current practice.
     *
     * @return the current practice
     */
    public Party getPractice() {
        return (Party) getObject(PRACTICE_SHORTNAME);
    }

    /**
     * Sets the current practice location.
     *
     * @param location the current practice location
     */
    public void setLocation(Party location) {
        setObject(LOCATION_SHORTNAME, location);
    }

    /**
     * Returns the current practice location.
     *
     * @return the current location
     */
    public Party getLocation() {
        return (Party) getObject(LOCATION_SHORTNAME);
    }

    /**
     * Sets the current stock location.
     *
     * @param location the current location
     */
    public void setStockLocation(Party location) {
        setObject(STOCK_LOCATION_SHORTNAME, location);
    }

    /**
     * Returns the current stock location.
     *
     * @return the current stock location, or {@code null} if there is no current location
     */
    public Party getStockLocation() {
        return (Party) getObject(STOCK_LOCATION_SHORTNAME);
    }

    /**
     * Sets the current customer.
     *
     * @param customer the current customer. May be {@code null}
     */
    public void setCustomer(Party customer) {
        setObject(CUSTOMER_SHORTNAME, customer);
    }

    /**
     * Returns the current customer.
     *
     * @return the current customer, or {@code null} if there is no current
     * customer
     */
    public Party getCustomer() {
        return (Party) getObject(CUSTOMER_SHORTNAME);
    }

    /**
     * Sets the current patient.
     *
     * @param patient the current patient. May be {@code null}
     */
    public void setPatient(Party patient) {
        setObject(PATIENT_SHORTNAME, patient);
    }

    /**
     * Returns the current patient.
     *
     * @return the current patient, or {@code null} if there is no current
     * patient
     */
    public Party getPatient() {
        return (Party) getObject(PATIENT_SHORTNAME);
    }

    /**
     * Sets the current supplier.
     *
     * @param supplier the current supplier. May be {@code null}
     */
    public void setSupplier(Party supplier) {
        setObject(SUPPLIER_SHORTNAME, supplier);
    }

    /**
     * Returns the current suppller.
     *
     * @return the current supplier, or {@code null} if there is no current
     * supplier
     */
    public Party getSupplier() {
        return (Party) getObject(SUPPLIER_SHORTNAME);
    }

    /**
     * Sets the current product.
     *
     * @param product the current product.
     */
    public void setProduct(Product product) {
        setObject(PRODUCT_SHORTNAME, product);
    }

    /**
     * Returns the current product.
     *
     * @return the current product, or {@code null} if there is no current
     * product
     */
    public Product getProduct() {
        return (Product) getObject(PRODUCT_SHORTNAME);
    }

    /**
     * Sets the current till.
     *
     * @param till the current till.
     */
    public void setTill(Party till) {
        setObject(TILL_SHORTNAME, till);
    }

    /**
     * Returns the current till.
     *
     * @return the current till, or {@code null} if there is no current
     * till
     */
    public Party getTill() {
        return (Party) getObject(TILL_SHORTNAME);
    }

    /**
     * Sets the current deposit account.
     *
     * @param deposit the current deposit account.
     */
    public void setDeposit(Party deposit) {
        setObject(DEPOSIT_SHORTNAME, deposit);
    }

    /**
     * Returns the current deposit account.
     *
     * @return the current depsoit, or {@code null} if there is no current
     * deposit
     */
    public Party getDeposit() {
        return (Party) getObject(DEPOSIT_SHORTNAME);
    }

    /**
     * Sets the current clinician.
     *
     * @param clinician the current clinician.
     */
    public void setClinician(User clinician) {
        setObject(CLINICIAN_SHORTNAME, clinician);
    }

    /**
     * Returns the current clinician.
     *
     * @return the current clinician, or {@code null} if there is no current
     * clinician
     */
    public User getClinician() {
        return (User) getObject(CLINICIAN_SHORTNAME);
    }

    /**
     * Sets the current schedule view.
     *
     * @param view the current schedule view. May be {@code null}
     */
    public void setScheduleView(Entity view) {
        setObject(SCHEDULE_VIEW_SHORTNAME, view);
    }

    /**
     * Returns the current schedule view.
     *
     * @return the current schedule view. May be {@code null}
     */
    public Entity getScheduleView() {
        return (Entity) getObject(SCHEDULE_VIEW_SHORTNAME);
    }

    /**
     * Sets the current schedule.
     *
     * @param schedule the current schedule
     */
    public void setSchedule(Entity schedule) {
        setObject(SCHEDULE_SHORTNAME, schedule);
    }

    /**
     * Returns the current schedule.
     *
     * @return the current schedule
     */
    public Party getSchedule() {
        return (Party) getObject(SCHEDULE_SHORTNAME);
    }

    /**
     * The current schedule date.
     *
     * @return the current schedule date
     */
    public Date getScheduleDate() {
        return scheduleDate;
    }

    /**
     * Sets the current schedule date.
     *
     * @param date the current schedule date
     */
    public void setScheduleDate(Date date) {
        scheduleDate = date;
    }

    /**
     * Sets the current appointment.
     *
     * @param appointment the current appointment
     */
    @Override
    public void setAppointment(Act appointment) {
        setObject(ScheduleArchetypes.APPOINTMENT, appointment);
    }

    /**
     * Returns the current appointment.
     *
     * @return the current appointment
     */
    @Override
    public Act getAppointment() {
        return (Act) getObject(ScheduleArchetypes.APPOINTMENT);
    }

    /**
     * Sets the current work list view.
     *
     * @param view the current work list view. May be {@code null}
     */
    public void setWorkListView(Entity view) {
        setObject(WORKLIST_VIEW_SHORTNAME, view);
    }

    /**
     * Returns the current work list view.
     *
     * @return the current work list view. May be {@code null}
     */
    public Entity getWorkListView() {
        return (Entity) getObject(WORKLIST_VIEW_SHORTNAME);
    }

    /**
     * Sets the current work list.
     *
     * @param workList the current work list
     */
    public void setWorkList(Party workList) {
        setObject(WORKLIST_SHORTNAME, workList);
    }

    /**
     * Returns the current work list.
     *
     * @return the current work list
     */
    public Party getWorkList() {
        return (Party) getObject(WORKLIST_SHORTNAME);
    }

    /**
     * Sets the current work list date.
     *
     * @param date the current schedule date
     */
    public void setWorkListDate(Date date) {
        workListDate = date;
    }

    /**
     * Returns the current work list date.
     *
     * @return the current work list date
     */
    public Date getWorkListDate() {
        return workListDate;
    }

    /**
     * Sets the current task.
     *
     * @param task the current task
     */
    @Override
    public void setTask(Act task) {
        setObject(ScheduleArchetypes.TASK, task);
    }

    /**
     * Returns the current task.
     *
     * @return the current task
     */
    @Override
    public Act getTask() {
        return (Act) getObject(ScheduleArchetypes.TASK);
    }

    /**
     * Adds an object to the context.
     *
     * @param object the object to add.
     */
    public void addObject(IMObject object) {
        ArchetypeId id = object.getArchetypeId();
        String match = null;
        for (String shortName : SHORT_NAMES) {
            if (TypeHelper.matches(id, shortName)) {
                match = shortName;
                break;
            }
        }
        if (match == null) {
            match = id.getShortName();
        }
        setObject(match, object);
    }

    /**
     * Removes an object from the context.
     *
     * @param object the object to remove
     */
    public void removeObject(IMObject object) {
        if (object != null) {
            objects.values().remove(object);
            if (ObjectUtils.equals(object, user)) {
                user = null;
            }
            if (ObjectUtils.equals(object, current)) {
                current = null;
            }
        }
    }

    /**
     * Returns an object for the specified key.
     *
     * @param key the context key
     * @return the object corresponding to {@code key} or {@code null} if none is found
     */
    public IMObject getObject(String key) {
        for (String shortName : SHORT_NAMES) {
            if (TypeHelper.matches(key, shortName)) {
                key = shortName;
                break;
            }
        }
        return objects.get(key);
    }

    /**
     * Returns a context object that matches the specified archetype range.
     *
     * @param range the archetype range
     * @return a context object whose short name is in {@code range} or {@code null} if none exists
     */
    public IMObject getObject(String[] range) {
        IMObject result = null;
        for (IMObject object : getObjects()) {
            if (object != null) {
                for (String shortName : range) {
                    ArchetypeId id = object.getArchetypeId();
                    if (TypeHelper.matches(id, shortName)) {
                        result = object;
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns a context object that matches the specified reference.
     *
     * @param reference the object reference
     * @return the context object whose reference matches {@code reference},
     * or {@code null} if there is no matches
     */
    public IMObject getObject(Reference reference) {
        IMObject result = null;
        for (IMObject object : getObjects()) {
            if (object != null) {
                if (reference.equals(object.getArchetype(), object.getLinkId())) {
                    result = object;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Sets a context object.
     *
     * @param key    the context key
     * @param object the object
     */
    public void setObject(String key, IMObject object) {
        if (object == null) {
            objects.remove(key);
        } else {
            objects.put(key, object);
        }
    }

    /**
     * Helper to return the context objects in an array.
     *
     * @return the a list of the context objects
     */
    public IMObject[] getObjects() {
        Set<IMObject> result = new HashSet<>();
        result.addAll(objects.values());
        if (current != null) {
            result.add(current);
        }
        if (user != null) {
            result.add(user);
        }
        return result.toArray(new IMObject[result.size()]);
    }

}
