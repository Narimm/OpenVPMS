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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.app;

import org.apache.commons.beanutils.MethodUtils;
import org.openvpms.component.business.domain.im.common.Entity;
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
 * A {@link Context} implementation that delegates to another context.
 * <p/>
 * A context heirarchy can be established by specifying a fallback context. If a getter on the delegate returns null,
 * the same method will be invoked on the fallback.
 * <p/>
 * Note that the delegate and fallback context may be one and the same. Subclasses can override individual methods
 * to avoid propagating specific context changes.
 *
 * @author Tim Anderson
 */
public abstract class DelegatingContext implements Context {

    /**
     * The context to delegate to.
     */
    private final Context context;

    /**
     * The fallback context. May be {@code null}
     */
    private final Context fallback;


    /**
     * Constructs a {@code DelegatingContext}.
     *
     * @param context the context to delegate to
     */
    public DelegatingContext(Context context) {
        this(context, null);
    }

    /**
     * Constructs a {@code DelegatingContext} with an optional fallback context.
     *
     * @param context  the context
     * @param fallback the fallback context. May be {@code null}
     */
    public DelegatingContext(Context context, Context fallback) {
        this.context = context;
        this.fallback = fallback;
    }

    /**
     * Sets the current object being viewed/edited.
     *
     * @param object the current object being viewed/edited. May be {@code null}
     */
    public void setCurrent(IMObject object) {
        context.setCurrent(object);
    }

    /**
     * Returns the current object being viewed/edited.
     *
     * @return the object being viewed/edited, or {@code null} if there is no current object
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
        context.setUser(user);
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
        context.setPractice(practice);
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
        context.setLocation(location);
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
        context.setStockLocation(location);
    }

    /**
     * Returns the current stock location.
     *
     * @return the current stock location, or {@code null} if there is no current location
     */
    public Party getStockLocation() {
        return get("getStockLocation");
    }

    /**
     * Sets the current customer.
     *
     * @param customer the current customer. May be {@code null}
     */
    public void setCustomer(Party customer) {
        context.setCustomer(customer);
    }

    /**
     * Returns the current customer.
     *
     * @return the current customer, or {@code null} if there is no current customer
     */
    public Party getCustomer() {
        return get("getCustomer");
    }

    /**
     * Sets the current patient.
     *
     * @param patient the current patient. May be {@code null}
     */
    public void setPatient(Party patient) {
        context.setPatient(patient);
    }

    /**
     * Returns the current patient.
     *
     * @return the current patient, or {@code null} if there is no current patient
     */
    public Party getPatient() {
        return get("getPatient");
    }

    /**
     * Sets the current supplier.
     *
     * @param supplier the current supplier. May be {@code null}
     */
    public void setSupplier(Party supplier) {
        context.setSupplier(supplier);
    }

    /**
     * Returns the current supplier.
     *
     * @return the current supplier, or {@code null} if there is no current supplier
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
        context.setProduct(product);
    }

    /**
     * Returns the current product.
     *
     * @return the current product, or {@code null} if there is no current product
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
        context.setDeposit(deposit);
    }

    /**
     * Returns the current deposit.
     *
     * @return the current deposit, or {@code null} if there is no current deposit
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
        context.setTill(till);
    }

    /**
     * Returns the current till.
     *
     * @return the current till, or {@code null} if there is no current till
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
        context.setClinician(clinician);
    }

    /**
     * Returns the current clinician.
     *
     * @return the current clinician, or {@code null} if there is no current clinician
     */
    public User getClinician() {
        return get("getClinician");
    }

    /**
     * Sets the current schedule view.
     *
     * @param view the current schedule view. May be {@code null}
     */
    public void setScheduleView(Entity view) {
        context.setScheduleView(view);
    }

    /**
     * Returns the current schedule view.
     *
     * @return the current schedule view. May be {@code null}
     */
    public Entity getScheduleView() {
        return get("getScheduleView");
    }

    /**
     * Sets the current schedule.
     *
     * @param schedule the current schedule
     */
    public void setSchedule(Party schedule) {
        context.setSchedule(schedule);
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
        context.setScheduleDate(date);
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
     * Sets the current work list view.
     *
     * @param view the current work list view. May be {@code null}
     */
    public void setWorkListView(Entity view) {
        context.setWorkListView(view);
    }

    /**
     * Returns the current work list view.
     *
     * @return the current work list view. May be {@code null}
     */
    public Entity getWorkListView() {
        return get("getWorkListtView");
    }

    /**
     * Sets the current work list.
     *
     * @param workList the current work list
     */
    public void setWorkList(Party workList) {
        context.setWorkList(workList);
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
        context.setWorkListDate(date);
    }

    /**
     * Returns the current work list date.
     *
     * @return the current work list date
     */
    public Date getWorkListDate() {
        return get("getWorkListDate");
    }

    /**
     * Adds an object to the context.
     *
     * @param object the object to add.
     */
    public void addObject(IMObject object) {
        context.addObject(object);
    }

    /**
     * Removes an object from the context.
     * Note that this <em>does not</em> remove it from the parent context.
     *
     * @param object the object to remove
     */
    public void removeObject(IMObject object) {
        context.removeObject(object);
    }

    /**
     * Returns a context object that matches the specified archetype range.
     *
     * @param range the archetype range
     * @return a context object whose short name is in {@code range} or
     *         {@code null} if none exists
     */
    public IMObject getObject(String[] range) {
        IMObject object = context.getObject(range);
        if (object == null && fallback != null && fallback != context) {
            object = fallback.getObject(range);
        }
        return object;
    }

    /**
     * Returns a context object that matches the specified reference.
     *
     * @param reference the object reference
     * @return the context object whose reference matches {@code reference},
     *         or {@code null} if there is no matches
     */
    public IMObject getObject(IMObjectReference reference) {
        IMObject object = context.getObject(reference);
        if (object == null && fallback != null && fallback != context) {
            object = fallback.getObject(reference);
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
        context.setObject(key, object);
    }

    /**
     * Returns an object for the specified key.
     *
     * @param key the context key
     * @return the object corresponding to {@code key} or {@code null} if none is found
     */
    public IMObject getObject(String key) {
        IMObject result = context.getObject(key);
        if (result == null && fallback != null && fallback != context) {
            result = fallback.getObject(key);
        }
        return result;
    }

    /**
     * Returns the fallback context.
     * <p/>
     * If available
     *
     * @return the fallback context, or {@code null} if there is none
     */
    public Context getFallback() {
        return fallback;
    }

    /**
     * Helper to return the context objects in an array.
     *
     * @return the a list of the context objects
     */
    public IMObject[] getObjects() {
        Set<IMObject> objects = new HashSet<IMObject>();
        if (fallback != null) {
            objects.addAll(Arrays.asList(fallback.getObjects()));
        }
        if (context != fallback) {
            objects.addAll(Arrays.asList(context.getObjects()));
        }
        return objects.toArray(new IMObject[objects.size()]);
    }

    /**
     * Helper to invoke a get method and return the result.
     * <p/>
     * This first invokes the method on the context. If that returns {@code null} and the parent context
     * is non-null and not the same context, invokes it on the parent.
     *
     * @param methodName the name of the method to invoke
     * @return the method return value. May be {@code null}
     * @throws RuntimeException if the method cannot be invoked
     */
    @SuppressWarnings("unchecked")
    protected <T> T get(String methodName) {
        Object result;
        try {
            result = MethodUtils.invokeMethod(context, methodName, null);
            if (result == null && fallback != null && fallback != context) {
                result = MethodUtils.invokeMethod(fallback, methodName, null);
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to invoke " + methodName, exception);
        }
        return (T) result;
    }

}
