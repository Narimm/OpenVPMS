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

package org.openvpms.web.component.app;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * Local context.
 * Note that this doesn't extend AbstractContext
 * to avoid bugs where extensions to the {@link Context} interface aren't
 * propagated to this.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LocalContext implements Context {

    /**
     * The local context.
     */
    private final Context local;

    /**
     * The parent context. May be <tt>null</tt>.
     */
    private final Context parent;


    /**
     * Constructs a new <code>LocalContext</code>, with the
     * {@link GlobalContext} as the immediate parent.
     */
    public LocalContext() {
        this(ContextApplicationInstance.getInstance().getContext());
    }

    /**
     * Constructs a new <code>LocalContext</code>, with the specified parent
     * context.
     *
     * @param parent the parent context. May be <tt>null</tt>
     */
    public LocalContext(Context parent) {
        local = new DefaultContext();
        this.parent = parent;
    }

    /**
     * Sets the current object being viewed/edited.
     *
     * @param object the current object being viewed/edited. May be
     *               <code>null</code>
     */
    public void setCurrent(IMObject object) {
        local.setCurrent(object);
    }

    /**
     * Returns the current object being viewed/edited.
     *
     * @return the object being viewed/edited, or <code>null</code> if there is
     *         no current object
     */
    public IMObject getCurrent() {
        IMObject current = local.getCurrent();
        if (current == null && parent != null) {
            current = parent.getCurrent();
        }
        return current;
    }

    /**
     * Sets the current user.
     *
     * @param user the current user
     */
    public void setUser(User user) {
        local.setUser(user);
    }

    /**
     * Returns the current user.
     *
     * @return the current user
     */
    public User getUser() {
        User user = local.getUser();
        if (user == null && parent != null) {
            user = parent.getUser();
        }
        return user;
    }

    /**
     * Sets the current practice.
     *
     * @param practice the current practice
     */
    public void setPractice(Party practice) {
        local.setPractice(practice);
    }

    /**
     * Returns the current practice.
     *
     * @return the current practice
     */
    public Party getPractice() {
        Party practice = local.getPractice();
        if (practice == null && parent != null) {
            practice = parent.getPractice();
        }
        return practice;
    }

    /**
     * Sets the current location.
     *
     * @param location the current location
     */
    public void setLocation(Party location) {
        local.setLocation(location);
    }

    /**
     * Returns the current location.
     *
     * @return the current location
     */
    public Party getLocation() {
        Party location = local.getLocation();
        if (location == null && parent != null) {
            location = parent.getLocation();
        }
        return location;
    }

    /**
     * Sets the current customer.
     *
     * @param customer the current customer. May be <code>null</code>
     */
    public void setCustomer(Party customer) {
        local.setCustomer(customer);
    }

    /**
     * Returns the current customer.
     *
     * @return the current customer, or <code>null</code> if there is no current
     *         customer
     */
    public Party getCustomer() {
        Party customer = local.getCustomer();
        if (customer == null && parent != null) {
            customer = parent.getCustomer();
        }
        return customer;
    }

    /**
     * Sets the current patient.
     *
     * @param patient the current patient. May be <code>null</code>
     */
    public void setPatient(Party patient) {
        local.setPatient(patient);
    }

    /**
     * Returns the current patient.
     *
     * @return the current patient, or <code>null</code> if there is no current
     *         patient
     */
    public Party getPatient() {
        Party patient = local.getPatient();
        if (patient == null && parent != null) {
            patient = parent.getPatient();
        }
        return patient;
    }

    /**
     * Sets the current supplier.
     *
     * @param supplier the current supplier. May be <code>null</code>
     */
    public void setSupplier(Party supplier) {
        local.setSupplier(supplier);
    }

    /**
     * Returns the current suppller.
     *
     * @return the current supplier, or <code>null</code> if there is no current
     *         supplier
     */
    public Party getSupplier() {
        Party supplier = local.getSupplier();
        if (supplier == null && parent != null) {
            supplier = parent.getSupplier();
        }
        return supplier;
    }

    /**
     * Sets the current product.
     *
     * @param product the current product.
     */
    public void setProduct(Product product) {
        local.setProduct(product);
    }

    /**
     * Returns the current product.
     *
     * @return the current product, or <code>null</code> if there is no current
     *         product
     */
    public Product getProduct() {
        Product product = local.getProduct();
        if (product == null && parent != null) {
            product = parent.getProduct();
        }
        return product;
    }

    /**
     * Sets the current deposit.
     *
     * @param deposit the current deposit.
     */
    public void setDeposit(Party deposit) {
        local.setDeposit(deposit);
    }

    /**
     * Returns the current deposit.
     *
     * @return the current deposit, or <code>null</code> if there is no current
     *         deposit
     */
    public Party getDeposit() {
        Party deposit = local.getDeposit();
        if (deposit == null && parent != null) {
            deposit = parent.getDeposit();
        }
        return deposit;
    }

    /**
     * Sets the current till.
     *
     * @param till the current till.
     */
    public void setTill(Party till) {
        local.setTill(till);
    }

    /**
     * Returns the current till.
     *
     * @return the current till, or <code>null</code> if there is no current
     *         till
     */
    public Party getTill() {
        Party till = local.getTill();
        if (till == null && parent != null) {
            till = parent.getTill();
        }
        return till;
    }

    /**
     * Sets the current clinician.
     *
     * @param clinician the current clinician.
     */
    public void setClinician(User clinician) {
        local.setClinician(clinician);
    }

    /**
     * Returns the current clinician.
     *
     * @return the current clinician, or <tt>null</tt> if there is no current
     *         clinician
     */
    public User getClinician() {
        User clinician = local.getClinician();
        if (clinician == null && parent != null) {
            clinician = parent.getClinician();
        }
        return clinician;
    }

    /**
     * Sets the current schedule.
     *
     * @param schedule the current schedule
     */
    public void setSchedule(Party schedule) {
        local.setSchedule(schedule);
    }

    /**
     * Returns the current schedule.
     *
     * @return the current schedule
     */
    public Party getSchedule() {
        Party schedule = local.getSchedule();
        if (schedule == null && parent != null) {
            schedule = parent.getSchedule();
        }
        return schedule;
    }

    /**
     * Sets the current schedule date.
     *
     * @param date the current schedule date
     */
    public void setScheduleDate(Date date) {
        local.setScheduleDate(date);
    }

    /**
     * The current schedule date.
     *
     * @return the current schedule date
     */
    public Date getScheduleDate() {
        Date date = local.getScheduleDate();
        if (date == null && parent != null) {
            date = parent.getScheduleDate();
        }
        return date;
    }

    /**
     * Sets the current work list.
     *
     * @param workList the current work list
     */
    public void setWorkList(Party workList) {
        local.setWorkList(workList);
    }

    /**
     * Returns the current work list.
     *
     * @return the current work list
     */
    public Party getWorkList() {
        Party workList = local.getWorkList();
        if (workList == null && parent != null) {
            workList = parent.getWorkList();
        }
        return workList;
    }

    /**
     * Sets the current work list date.
     *
     * @param date the current schedule date
     */
    public void setWorkListDate(Date date) {
        local.setWorkListDate(date);
    }

    /**
     * Returns the current work list date.
     *
     * @return the current work list date
     */
    public Date getWorkListDate() {
        Date date = local.getWorkListDate();
        if (date == null && parent != null) {
            date = parent.getWorkListDate();
        }
        return date;
    }

    /**
     * Adds an object to the context.
     *
     * @param object the object to add.
     */
    public void addObject(IMObject object) {
        local.addObject(object);
    }

    /**
     * Removes an object from the context.
     * Note that this <em>does not</em> remove it from the parent context.
     *
     * @param object the object to remove
     */
    public void removeObject(IMObject object) {
        local.removeObject(object);
    }

    /**
     * Returns a context object that matches the specified archetype range.
     *
     * @param range the archetype range
     * @return a context object whose short name is in <code>range</code> or
     *         <code>null</code> if none exists
     */
    public IMObject getObject(String[] range) {
        IMObject object = local.getObject(range);
        if (object == null && parent != null) {
            object = parent.getObject(range);
        }
        return object;
    }

    /**
     * Returns a context object that matches the specified reference.
     *
     * @param reference the object reference
     * @return the context object whose reference matches <code>reference</code>,
     *         or <code>null</code> if there is no matches
     */
    public IMObject getObject(IMObjectReference reference) {
        IMObject object = local.getObject(reference);
        if (object == null && parent != null) {
            object = parent.getObject(reference);
        }
        return object;
    }

    /**
     * Sets a context object.
     *
     * @param key    the context key
     * @param object the object
     */
    public void setObject(String key, IMObject object) {
        local.setObject(key, object);
    }

    /**
     * Returns an object for the specified key.
     *
     * @param key the context key
     * @return the object corresponding to <code>key</code> or
     *         <code>null</code> if none is found
     */
    public IMObject getObject(String key) {
        IMObject result = local.getObject(key);
        if (result == null && parent != null) {
            result = parent.getObject(key);
        }
        return result;
    }

    /**
     * Helper to return the context objects in an array.
     *
     * @return the a list of the context objects
     */
    public IMObject[] getObjects() {
        Set<IMObject> objects = new HashSet<IMObject>();
        if (parent != null) {
            objects.addAll(Arrays.asList(parent.getObjects()));
        }
        objects.addAll(Arrays.asList(local.getObjects()));
        return objects.toArray(new IMObject[0]);
    }

    private static final class DefaultContext extends AbstractContext {
    }
}
