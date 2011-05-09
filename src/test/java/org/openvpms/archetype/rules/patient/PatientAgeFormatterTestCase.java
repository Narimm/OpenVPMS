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
import org.openvpms.archetype.i18n.time.LookupDateDurationFormatter;
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
    private ILookupService lookupService;

    /**
     * The bean factory.
     */
    @Autowired
    private IMObjectBeanFactory factory;

    /**
     * Configures a lookup.dateformat and verifies that LookupDateDurationFormatter formats correctly with it.
     */
    @Test
    public void testFormat() {
        String code = "XTESTDATEFORMATS" + System.currentTimeMillis();
        Lookup formats = TestHelper.getLookup(LookupDateDurationFormatter.DATE_FORMATS, code);
        addFormat(formats, 6, DateUnits.DAYS, false, false, false, true);  // show days
        addFormat(formats, 90, DateUnits.DAYS, false, false, true, false); // weeks
        addFormat(formats, 1, DateUnits.YEARS, false, true, false, false); // months
        addFormat(formats, 2, DateUnits.YEARS, true, true, false, false);  // years, months

        Party practice = TestHelper.getPractice();
        IMObjectBean bean = factory.createBean(practice);
        bean.setValue("patientAgeFormat", code);
        bean.save();

        PracticeRules rules = new PracticeRules();
        PatientAgeFormatter formatter = new PatientAgeFormatter(lookupService, rules, factory);

        Date from = getDate("2011-01-01");
        Date to1 = getDate("2011-01-07");
        Date to2 = getDate("2011-01-08");
        Date to3 = getDate("2012-01-01");
        Date to4 = getDate("2013-02-01");
        checkFormat("6 Days", from, to1, formatter);
        checkFormat("1 Week", from, to2, formatter);
        checkFormat("12 Months", from, to3, formatter);
        checkFormat("2 Years 1 Month", from, to4, formatter);
    }

    /**
     * Verifies a format matches that expected.
     *
     * @param expected  the expected result
     * @param from      the from date
     * @param to        the to date
     * @param formatter the formatter to use
     */
    private void checkFormat(String expected, Date from, Date to, PatientAgeFormatter formatter) {
        String result = formatter.format(from, to);
        assertEquals(expected, result);
    }
}
