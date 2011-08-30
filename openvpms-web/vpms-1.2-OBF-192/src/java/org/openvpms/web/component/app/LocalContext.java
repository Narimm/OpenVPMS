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

import org.apache.commons.beanutils.MethodUtils;
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
     * Constructs a new <tt>LocalContext</tt>, with the
     * {@link GlobalContext} as the immediate parent.
     */
    public LocalContext() {
        this(ContextApplicationInstance.getInstance().getContext());
    }

    /**
     * Constructs a new <tt>LocalContext</tt>, with the specified parent
     * context.
     *
     * @param parent the parent context. May be <tt>null</tt>
     */
    public LocalContext(Context parent) {
        local = new DefaultContext();
        this.parent = parent;
    }

    /**
     * Returns the parent context.
     *
     * @return the parent context, or <tt>null</tt> if there is none
     */
    public Context getParent() {
        return parent;
    }

    /**
     * Sets the current object being viewed/edited.
     *
     * @param object the current object being viewed/edited. May be
     *               <tt>null</tt>
     */
    public void setCurrent(IMObject object) {
        local.setCurrent(object);
    }

    /**
     * Returns the current object being viewed/edited.
     *
     * @return the object being viewed/edited, or <tt>null</tt> if there is
     *         no current object
     */
    public IMObject getCurrent() {
        return get("getCurrent");
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
        return get("getUser");
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
        return get("getPractice");
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
        return get("getLocation");
    }

    /**
     * Sets the current stock location.
     *
     * @param location the current location
     */
    public void setStockLocation(Party location) {
        local.setStockLocation(location);
    }

    /**
     * Returns the current stock location.
     *
     * @return the current stock location, or <tt>null</tt> if there is no
     *         current location
     */
    public Party getStockLocation() {
        return get("getStockLocation");
    }

    /**
     * Sets the current customer.
     *
     * @param customer the current customer. May be <tt>null</tt>
     */
    public void setCustomer(Party customer) {
        local.setCustomer(customer);
    }

    /**
     * Returns the current customer.
     *
     * @return the current customer, or <tt>null</tt> if there is no current
     *         customer
     */
    public Party getCustomer() {
        return get("getCustomer");
    }

    /**
     * Sets the current patient.
     *
     * @param patient the current patient. May be <tt>null</tt>
     */
    public void setPatient(Party patient) {
        local.setPatient(patient);
    }

    /**
     * Returns the current patient.
     *
     * @return the current patient, or <tt>null</tt> if there is no current
     *         patient
     */
    public Party getPatient() {
        return get("getPatient");
    }

    /**
     * Sets the current supplier.
     *
     * @param supplier the current supplier. May be <tt>null</tt>
     */
    public void setSupplier(Party supplier) {
        local.setSupplier(supplier);
    }

    /**
     * Returns the current suppller.
     *
     * @return the current supplier, or <tt>null</tt> if there is no current
     *         supplier
     */
    public Party getSupplier() {
        return get("getSupplier");
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
     * @return the current product, or <tt>null</tt> if there is no current
     *         product
     */
    public Product getProduct() {
        return get("getProduct");
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
     * @return the current deposit, or <tt>null</tt> if there is no current
     *         deposit
     */
    public Party getDeposit() {
        return get("getDeposit");
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
     * @return the current till, or <tt>null</tt> if there is no current
     *         till
     */
    public Party getTill() {
        return get("getTill");
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
        return get("getClinician");
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
        return get("getSchedule");
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
        return get("getScheduleDate");
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
        return get("getWorkList");
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
     * @return a context object whose short name is in <tt>range</tt> or
     *         <tt>null</tt> if none exists
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
     * @return the context object whose reference matches <tt>reference</tt>,
     *         or <tt>null</tt> if there is no matches
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
     * @return the object corresponding to <tt>key</tt> or
     *         <tt>null</tt> if none is found
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

    @SuppressWarnings("unchecked")
    private <T> T get(String methodName) {
        Object result;
        try {
            result = MethodUtils.invokeMethod(local, methodName, null);
            if (result == null && parent != null) {
                result = MethodUtils.invokeMethod(parent, methodName, null);
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to invoke " + methodName,
                                       exception);
        }
        return (T) result;
    }

    private static final class DefaultContext extends AbstractContext {
    }

}
