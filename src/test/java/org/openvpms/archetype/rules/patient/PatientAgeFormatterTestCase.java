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

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.openvpms.archetype.i18n.time.DurationFormatterTestHelper.addFormat;
import static org.openvpms.archetype.i18n.time.DurationFormatterTestHelper.createDurationFormats;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import static org.openvpms.archetype.test.TestHelper.getDate;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Tests the {@link PatientAgeFormatter} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class PatientAgeFormatterTestCase extends ArchetypeServiceTest {

    /**
     * The lookup service.
     */
    @Autowired
    protected ILookupService lookupService;

    /**
     * The bean factory.
     */
    @Autowired
    protected IMObjectBeanFactory factory;

    /**
     * Configures a lookup.durationformat and verifies that LookupDateDurationFormatter formats correctly with it.
     */
    @Test
    public void testFormat() {
        Lookup formats = createDurationFormats();
        addFormat(formats, 7, DateUnits.DAYS, false, false, false, true);  // show days
        addFormat(formats, 90, DateUnits.DAYS, false, false, true, false); // weeks
        addFormat(formats, 1, DateUnits.YEARS, false, true, false, false); // months
        addFormat(formats, 2, DateUnits.YEARS, true, true, false, false);  // years, months

        setPracticeFormat(TestHelper.getPractice(), formats);
        PatientAgeFormatter formatter = createFormatter();

        Date from = getDate("2011-01-01");
        Date to1 = getDate("2011-01-07");
        Date to2 = getDate("2011-01-08");
        Date to3 = getDate("2012-01-01");
        Date to4 = getDate("2013-02-01");
        checkFormat("6 Days", from, to1, formatter);
        checkFormat("7 Days", from, to2, formatter);
        checkFormat("12 Months", from, to3, formatter);
        checkFormat("2 Years 1 Month", from, to4, formatter);
    }

    /**
     * Tests the formatting if the practice has no associated format.
     */
    @Test
    public void testDefaultFormat() {
        setPracticeFormat(TestHelper.getPractice(), null);
        PatientAgeFormatter formatter = createFormatter();

        Date from = getDate("2011-01-01");
        Date to1 = getDate("2011-01-07");
        Date to2 = getDate("2011-01-08");
        Date to3 = getDate("2012-01-01");
        Date to4 = getDate("2013-02-01");
        checkFormat("6 Days", from, to1, formatter);
        checkFormat("7 Days", from, to2, formatter);
        checkFormat("12 Months", from, to3, formatter);
        checkFormat("2 Years", from, to4, formatter);
    }

    /**
     * Sets the format associated with the practice.
     *
     * @param practice the practice
     * @param formats  the <em>lookup.durationformats</em>. May be <tt>null</tt>
     */
    protected void setPracticeFormat(Party practice, Lookup formats) {
        IMObjectBean bean = factory.createBean(practice);

        String code = (formats != null) ? formats.getCode() : null;
        bean.setValue("patientAgeFormat", code);
        bean.save();
    }

    /**
     * Creates a new formatter.
     *
     * @return a new formatter
     */
    protected PatientAgeFormatter createFormatter() {
        PracticeRules rules = new PracticeRules();
        return new PatientAgeFormatter(lookupService, rules, factory);
    }

    /**
     * Verifies a format matches that expected.
     *
     * @param expected  the expected result
     * @param from      the from date
     * @param to        the to date
     * @param formatter the formatter to use
     */
    protected void checkFormat(String expected, Date from, Date to, PatientAgeFormatter formatter) {
        String result = formatter.format(from, to);
        assertEquals(expected, result);
    }
}
