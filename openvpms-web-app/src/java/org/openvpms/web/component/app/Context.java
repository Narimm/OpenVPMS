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
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface Context {

    /**
     * Practice short name.
     */
    public static final String PRACTICE_SHORTNAME
        = "party.organisationPractice";

    /**
     * Location short name.
     */
    public static final String LOCATION_SHORTNAME
        = "party.organisationLocation";

    /**
     * Stock location short name.
     */
    public static final String STOCK_LOCATION_SHORTNAME
        = "party.organisationStockLocation";

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
    public static final String SCHEDULE_VIEW_SHORTNAME
        = "entity.organisationScheduleView";

    /**
     * Schedule short name.
     */
    public static final String SCHEDULE_SHORTNAME
        = "party.organisationSchedule";

    /**
     * Work list view short name.
     */
    public static final String WORKLIST_VIEW_SHORTNAME
        = "entity.organisationWorkListView";

    /**
     * Work list short name.
     */
    public static final String WORKLIST_SHORTNAME
        = "party.organisationWorkList";


    /**
     * Sets the current object being viewed/edited.
     *
     * @param object the current object being viewed/edited. May be
     *               <tt>null</tt>
     */
    void setCurrent(IMObject object);

    /**
     * Returns the current object being viewed/edited.
     *
     * @return the object being viewed/edited, or <tt>null</tt> if there is
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
     * Sets the current stock location.
     *
     * @param location the current location
     */
    void setStockLocation(Party location);

    /**
     * Returns the current stock location.
     *
     * @return the current stock location, or <tt>null</tt> if there is no
     *         current location
     */
    Party getStockLocation();

    /**
     * Sets the current customer.
     *
     * @param customer the current customer. May be <tt>null</tt>
     */
    void setCustomer(Party customer);

    /**
     * Returns the current customer.
     *
     * @return the current customer, or <tt>null</tt> if there is no current
     *         customer
     */
    Party getCustomer();

    /**
     * Sets the current patient.
     *
     * @param patient the current patient. May be <tt>null</tt>
     */
    void setPatient(Party patient);

    /**
     * Returns the current patient.
     *
     * @return the current patient, or <tt>null</tt> if there is no current
     *         patient
     */
    Party getPatient();

    /**
     * Sets the current supplier.
     *
     * @param supplier the current supplier. May be <tt>null</tt>
     */
    void setSupplier(Party supplier);

    /**
     * Returns the current suppller.
     *
     * @return the current supplier, or <tt>null</tt> if there is no current
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
     * @return the current product, or <tt>null</tt> if there is no current
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
     * @return the current deposit, or <tt>null</tt> if there is no current
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
     * @return the current till, or <tt>null</tt> if there is no current
     *         till
     */
    Party getTill();

    /**
     * Sets the current clinician.
     *
     * @param clinician the current clinician. May be <tt>null</tt>
     */
    void setClinician(User clinician);

    /**
     * Returns the current clinician.
     *
     * @return the current clinician, or <tt>null</tt> if there is no current
     *         clinician
     */
    User getClinician();

    /**
     * Sets the current schedule view.
     *
     * @param view the current schedule view. May be <tt>null</tt>
     */
    void setScheduleView(Entity view);

    /**
     * Returns the current schedule view.
     *
     * @return the current schedule view. May be <tt>null</tt>
     */
    Entity getScheduleView();

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
     * Sets the current work list view.
     *
     * @param view the current work list view. May be <tt>null</tt>
     */
    void setWorkListView(Entity view);

    /**
     * Returns the current work list view.
     *
     * @return the current work list view. May be <tt>null</tt>
     */
    Entity getWorkListView();

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
     * Returns the current work list date.
     *
     * @return the current work list date
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
     * @return the object corresponding to <tt>key</tt> or
     *         <tt>null</tt> if none is found
     */
    IMObject getObject(String key);

    /**
     * Returns a context object that matches the specified archetype range.
     *
     * @param range the archetype range
     * @return a context object whose short name is in <tt>range</tt> or
     *         <tt>null</tt> if none exists
     */
    IMObject getObject(String[] range);

    /**
     * Returns a context object that matches the specified reference.
     *
     * @param reference the object reference
     * @return the context object whose reference matches <tt>reference</tt>,
     *         or <tt>null</tt> if there is no matches
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
