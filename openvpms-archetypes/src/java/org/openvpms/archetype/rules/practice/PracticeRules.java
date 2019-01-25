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

package org.openvpms.archetype.rules.practice;

import org.apache.commons.lang.StringUtils;
import org.joda.time.Period;
import org.openvpms.archetype.rules.math.Currencies;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.math.CurrencyException;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.util.EntityRelationshipHelper;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;

import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Practice rules.
 *
 * @author Tim Anderson
 */
public class PracticeRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The currencies.
     */
    private final Currencies currencies;

    /**
     * Constructs a {@link PracticeRules}.
     *
     * @param service    the archetype service
     * @param currencies the currencies
     */
    public PracticeRules(IArchetypeService service, Currencies currencies) {
        this.service = service;
        this.currencies = currencies;
    }

    /**
     * Determines if the specified practice is the only active practice.
     *
     * @param practice the practice
     * @throws ArchetypeServiceException for any archetype service error
     */
    public boolean isActivePractice(org.openvpms.component.model.party.Party practice) {
        if (practice.isActive()) {
            ArchetypeQuery query = new ArchetypeQuery(PracticeArchetypes.PRACTICE, true, true);
            IMObjectQueryIterator<Party> iter = new IMObjectQueryIterator<>(service, query);
            Reference practiceRef = practice.getObjectReference();
            while (iter.hasNext()) {
                Party party = iter.next();
                if (!party.getObjectReference().equals(practiceRef)) {
                    return false; // there is another active practice
                }
            }
            return true; // no other active practice
        }
        return false;    // practice is inactive
    }

    /**
     * Returns the practice.
     *
     * @return the practice, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getPractice() {
        return getPractice(service);
    }

    /**
     * Returns the locations associated with a practice.
     *
     * @param practice the practice
     * @return the locations associated with the user
     * @throws ArchetypeServiceException for any archetype service error
     */
    public List<Party> getLocations(org.openvpms.component.model.party.Party practice) {
        IMObjectBean bean = service.getBean(practice);
        return bean.getTargets("locations", Party.class);
    }

    /**
     * Returns the default user to be used by background services.
     *
     * @param practice the practice
     * @return the service user
     */
    public User getServiceUser(org.openvpms.component.model.party.Party practice) {
        IMObjectBean bean = service.getBean(practice);
        return bean.getTarget("serviceUser", User.class);
    }

    /**
     * Returns the currency associated with a practice.
     *
     * @param practice the practice
     * @return the practice currency
     * @throws CurrencyException if the currency code is invalid or no <em>lookup.currency</em> is defined for the
     *                           currency
     */
    public Currency getCurrency(org.openvpms.component.model.party.Party practice) {
        IMObjectBean bean = service.getBean(practice);
        String code = bean.getString("currency");
        return currencies.getCurrency(code);
    }

    /**
     * Returns the default location associated with a practice.
     *
     * @param practice the practice
     * @return the default location, or the first location if there is no
     * default location or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getDefaultLocation(org.openvpms.component.model.party.Party practice) {
        return (Party) EntityRelationshipHelper.getDefaultTarget(practice, "locations", service);
    }

    /**
     * Determines the period after which patient medical records are locked.
     *
     * @param practice the practice
     * @return the period, or {@code null} if no period is defined
     */
    public Period getRecordLockPeriod(org.openvpms.component.model.party.Party practice) {
        return getPeriod(practice, "recordLockPeriod", "recordLockPeriodUnits");
    }

    /**
     * Returns the default estimate expiry date, based on the practice settings for the
     * <em>estimateExpiryPeriod</em> and <em>estimateExpiryUnits</em> nodes.
     *
     * @param startDate the estimate start date
     * @param practice  the practice configuration
     * @return the estimate expiry date, or {@code null} if there are no default expiry settings
     */
    public Date getEstimateExpiryDate(Date startDate, org.openvpms.component.model.party.Party practice) {
        Date result = null;
        IMObjectBean bean = service.getBean(practice);
        int period = bean.getInt("estimateExpiryPeriod");
        String units = bean.getString("estimateExpiryUnits");
        if (period > 0 && !StringUtils.isEmpty(units)) {
            result = DateRules.getDate(startDate, period, DateUnits.valueOf(units));
        }
        return result;
    }

    /**
     * Returns the default prescription expiry date, based on the practice settings for the
     * <em>prescriptionExpiryPeriod</em> and <em>prescriptionExpiryUnits</em> nodes.
     *
     * @param startDate the prescription start date
     * @param practice  the practice configuration
     * @return the prescription expiry date, or {@code startDate} if there are no default expiry settings
     */
    public Date getPrescriptionExpiryDate(Date startDate, org.openvpms.component.model.party.Party practice) {
        IMObjectBean bean = service.getBean(practice);
        int period = bean.getInt("prescriptionExpiryPeriod");
        String units = bean.getString("prescriptionExpiryUnits");
        if (!StringUtils.isEmpty(units)) {
            return DateRules.getDate(startDate, period, DateUnits.valueOf(units));
        }
        return startDate;
    }

    /**
     * Returns the field separator to use when exporting files.
     *
     * @param practice the practice
     * @return the field separator
     */
    public char getExportFileFieldSeparator(org.openvpms.component.model.party.Party practice) {
        char separator = ',';
        IMObjectBean bean = service.getBean(practice);
        if ("TAB".equals(bean.getString("fileExportFormat"))) {
            separator = '\t';
        }
        return separator;
    }

    /**
     * Determines if SMS is configured for the practice.
     *
     * @param practice the practice
     * @return {@code true} if SMS is configured, otherwise {@code false}
     */
    public boolean isSMSEnabled(org.openvpms.component.model.party.Party practice) {
        boolean enabled = false;
        IMObjectBean bean = service.getBean(practice);
        List<Reference> refs = bean.getTargetRefs("sms");
        for (Reference ref : refs) {
            if (isActive(ref)) {
                enabled = true;
                break;
            }
        }
        return enabled;
    }

    /**
     * Determines if products should be filtered by location.
     *
     * @param practice the practice
     * @return {@code true} if products should be filtered by location
     */
    public boolean useLocationProducts(org.openvpms.component.model.party.Party practice) {
        IMObjectBean bean = service.getBean(practice);
        return bean.getBoolean("useLocationProducts");
    }

    /**
     * Determines if finalisation of orders containing restricted medications is limited to clinicians.
     *
     * @param practice the practice
     * @return {@code true} if only clinicians can finalize and send ESCI orders.
     */
    public boolean isOrderingRestricted(org.openvpms.component.model.party.Party practice) {
        IMObjectBean bean = service.getBean(practice);
        return bean.getBoolean("restrictOrdering");
    }

    /**
     * Determines the period after an invoice is finalised that pharmacy orders are discontinued.
     * <p/>
     * If no period is defined, orders are discontinued when invoices are finalised.
     *
     * @param practice the practice
     * @return the period, or {@code null} if no period is defined
     */
    public Period getPharmacyOrderDiscontinuePeriod(org.openvpms.component.model.party.Party practice) {
        return getPeriod(practice, "pharmacyOrderDiscontinuePeriod", "pharmacyOrderDiscontinuePeriodUnits");
    }

    /**
     * Returns the practice.
     *
     * @param service the archetype service
     * @return the practice, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static Party getPractice(IArchetypeService service) {
        ArchetypeQuery query = new ArchetypeQuery(PracticeArchetypes.PRACTICE, true, true);
        query.setMaxResults(1);
        IMObjectQueryIterator<Party> iter = new IMObjectQueryIterator<>(service, query);
        return (iter.hasNext()) ? iter.next() : null;
    }

    /**
     * Determines if an object associated with a reference is active.
     *
     * @param reference the object reference. May be {@code null}
     * @return {@code true} if the object is active, otherwise {@code false}
     */
    private boolean isActive(Reference reference) {
        ObjectRefConstraint constraint = new ObjectRefConstraint("o", reference);
        ArchetypeQuery query = new ArchetypeQuery(constraint);
        query.add(new NodeSelectConstraint("o.active"));
        query.setMaxResults(1);
        Iterator<ObjectSet> iterator = new ObjectSetQueryIterator(service, query);
        if (iterator.hasNext()) {
            ObjectSet set = iterator.next();
            return set.getBoolean("o.active");
        }
        return false;
    }

    /**
     * Returns a configured period.
     *
     * @param practice   the practice configuration
     * @param periodName the period node name
     * @param unitsName  the period units node name
     * @return the period, or {@code null} if none is defined
     */
    private Period getPeriod(org.openvpms.component.model.party.Party practice, String periodName, String unitsName) {
        Period result = null;
        IMObjectBean bean = service.getBean(practice);
        int period = bean.getInt(periodName, -1);
        if (period > 0) {
            DateUnits units = DateUnits.fromString(bean.getString(unitsName), null);
            if (units != null) {
                result = units.toPeriod(period);
            }
        }
        return result;
    }

}
