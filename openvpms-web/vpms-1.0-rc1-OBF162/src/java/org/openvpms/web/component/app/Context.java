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

import java.util.Date;


/**
 * Application context information.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface Context {

    /**
     * Sets the current object being viewed/edited.
     *
     * @param object the current object being viewed/edited. May be
     *               <code>null</code>
     */
    void setCurrent(IMObject object);

    /**
     * Returns the current object being viewed/edited.
     *
     * @return the object being viewed/edited, or <code>null</code> if there is
     *         no current object
     */
    IMObject getCurrent();

    /**
     * Sets the current user.
     *
     * @param user the current user
     */
    void setUser(User user);

    /**
     * Returns the current user.
     *
     * @return the current user
     */
    User getUser();

    /**
     * Sets the current practice.
     *
     * @param practice the current practice
     */
    void setPractice(Party practice);

    /**
     * Returns the current practice.
     *
     * @return the current practice
     */
    Party getPractice();

    /**
     * Sets the current location.
     *
     * @param location the current location
     */
    void setLocation(Party location);

    /**
     * Returns the current location.
     *
     * @return the current location
     */
    Party getLocation();

    /**
     * Sets the current customer.
     *
     * @param customer the current customer. May be <code>null</code>
     */
    void setCustomer(Party customer);

    /**
     * Returns the current customer.
     *
     * @return the current customer, or <code>null</code> if there is no current
     *         customer
     */
    Party getCustomer();

    /**
     * Sets the current patient.
     *
     * @param patient the current patient. May be <code>null</code>
     */
    void setPatient(Party patient);

    /**
     * Returns the current patient.
     *
     * @return the current patient, or <code>null</code> if there is no current
     *         patient
     */
    Party getPatient();

    /**
     * Sets the current supplier.
     *
     * @param supplier the current supplier. May be <code>null</code>
     */
    void setSupplier(Party supplier);

    /**
     * Returns the current suppller.
     *
     * @return the current supplier, or <code>null</code> if there is no current
     *         supplier
     */
    Party getSupplier();

    /**
     * Sets the current product.
     *
     * @param product the current product.
     */
    void setProduct(Product product);

    /**
     * Returns the current product.
     *
     * @return the current product, or <code>null</code> if there is no current
     *         product
     */
    Product getProduct();

    /**
     * Sets the current deposit.
     *
     * @param deposit the current deposit.
     */
    void setDeposit(Party deposit);

    /**
     * Returns the current deposit.
     *
     * @return the current deposit, or <code>null</code> if there is no current
     *         deposit
     */
    Party getDeposit();

    /**
     * Sets the current till.
     *
     * @param till the current till.
     */
    void setTill(Party till);

    /**
     * Returns the current till.
     *
     * @return the current till, or <code>null</code> if there is no current
     *         till
     */
    Party getTill();

    /**
     * Sets the current clinician.
     *
     * @param clinician the current clinician.
     */
    void setClinician(User clinician);

    /**
     * Returns the current clinician.
     *
     * @return the current clinician, or <code>null</code> if there is no current
     *         clinician
     */
    User getClinician();

    /**
     * Sets the current schedule.
     *
     * @param schedule the current schedule
     */
    void setSchedule(Party schedule);

    /**
     * Returns the current schedule.
     *
     * @return the current schedule
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
     * Sets the current work list.
     *
     * @param workList the current work list
     */
    void setWorkList(Party workList);

    /**
     * Returns the current work list.
     *
     * @return the current work list
     */
    Party getWorkList();

    /**
     * Sets the current work list date.
     *
     * @param date the current schedule date
     */
    void setWorkListDate(Date date);

    /**
     * Returns the current work lsit date.
     *
     * @return the current work lsit date
     */
    Date getWorkListDate();

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
     * @return the object corresponding to <code>key</code> or
     *         <code>null</code> if none is found
     */
    IMObject getObject(String key);

    /**
     * Returns a context object that matches the specified archetype range.
     *
     * @param range the archetype range
     * @return a context object whose short name is in <code>range</code> or
     *         <code>null</code> if none exists
     */
    IMObject getObject(String[] range);

    /**
     * Returns a context object that matches the specified reference.
     *
     * @param reference the object reference
     * @return the context object whose reference matches <code>reference</code>,
     *         or <code>null</code> if there is no matches
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
