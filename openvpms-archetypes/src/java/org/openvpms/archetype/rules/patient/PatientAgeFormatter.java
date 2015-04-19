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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.archetype.rules.patient;

import org.openvpms.archetype.i18n.time.CompositeDurationFormatter;
import org.openvpms.archetype.i18n.time.DateDurationFormatter;
import org.openvpms.archetype.i18n.time.DurationFormatter;
import org.openvpms.archetype.i18n.time.LookupDateDurationFormatter;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.business.service.lookup.ILookupService;

import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Formats the age of a patient.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class PatientAgeFormatter implements DurationFormatter {

    /**
     * The practice rules.
     */
    private final PracticeRules rules;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * Bean factory.
     */
    private final IMObjectBeanFactory factory;

    /**
     * The duration formatter.
     */
    private DurationFormatter formatter;

    /**
     * Message when no birthdate is known.
     */
    private static final String NO_BIRTHDATE;

    /**
     * Message when the birthdate is in the future.
     */
    private static final String FUTURE_BIRTHDATE;

    static {
        ResourceBundle bundle = ResourceBundle.getBundle(PatientAgeFormatter.class.getName(), Locale.getDefault());
        NO_BIRTHDATE = bundle.getString("noBirthdate");
        FUTURE_BIRTHDATE = bundle.getString("futureBirthdate");
    }

    /**
     * Constructs a <tt>PatientAgeFormatter</tt>.
     *
     * @param lookups the lookup service
     * @param rules   the practice rules, used to determine the practice
     * @param factory the bean factory
     */
    public PatientAgeFormatter(ILookupService lookups, PracticeRules rules, IMObjectBeanFactory factory) {
        this.lookups = lookups;
        this.rules = rules;
        this.factory = factory;
        init();
    }

    /**
     * Formats a patient's age determined by its birthdate and the current time.
     *
     * @param birthDate the birth date. May be <tt>null</tt>
     * @return the formatted birth date
     */
    public String format(Date birthDate) {
        return format(birthDate, new Date());
    }

    /**
     * Formats a patient's age relative to a particular date.
     *
     * @param from the patient's birth date. May be <tt>null</tt>
     * @param to   the ending time. May be <tt>null</tt>
     * @return the formatted duration
     */
    public String format(Date from, Date to) {
        String result;
        if (from != null && to != null) {
            if (to.getTime() >= from.getTime()) {
                result = getFormatter().format(from, to);
            } else {
                result = FUTURE_BIRTHDATE;
            }
        } else {
            result = NO_BIRTHDATE;
        }
        return result;
    }

    /**
     * Returns the formatter, creating it if necessary.
     *
     * @return the formatter
     */
    protected synchronized DurationFormatter getFormatter() {
        if (formatter == null) {
            init();
        }
        return formatter;
    }

    /**
     * Creates a default formatter to use if the practice has no format configured.
     *
     * @return a new duration formatter
     */
    protected DurationFormatter createDefaultFormatter() {
        CompositeDurationFormatter formatter = new CompositeDurationFormatter();
        formatter.add(7, DateUnits.DAYS, DateDurationFormatter.DAY);
        formatter.add(90, DateUnits.DAYS, DateDurationFormatter.WEEK);
        formatter.add(23, DateUnits.MONTHS, DateDurationFormatter.MONTH);
        formatter.add(2, DateUnits.YEARS, DateDurationFormatter.YEAR);
        return formatter;
    }

    /**
     * Forces recreation of the formatter on next use.
     */
    protected synchronized void refresh() {
        formatter = null;
    }

    /**
     * Initialises the formatter.
     */
    private synchronized void init() {
        Party practice = rules.getPractice();
        if (practice != null) {
            IMObjectBean bean = factory.createBean(practice);
            String code = bean.getString("patientAgeFormat");
            if (code != null) {
                Lookup lookup = lookups.getLookup(LookupDateDurationFormatter.DURATION_FORMATS, code);
                if (lookup != null) {
                    formatter = new LookupDateDurationFormatter(lookup, lookups, factory);
                }
            }
        }
        if (formatter == null) {
            formatter = createDefaultFormatter();
        }
    }

}
