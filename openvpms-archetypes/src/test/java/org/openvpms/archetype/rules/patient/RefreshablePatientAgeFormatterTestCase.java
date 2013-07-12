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

package org.openvpms.archetype.rules.patient;

import org.junit.Test;
import org.openvpms.archetype.i18n.time.DurationFormatterTestHelper;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.Date;

import static org.openvpms.archetype.i18n.time.DurationFormatterTestHelper.addFormat;
import static org.openvpms.archetype.test.TestHelper.getDate;

/**
 * Tests the {@link RefreshablePatientAgeFormatter} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class RefreshablePatientAgeFormatterTestCase extends PatientAgeFormatterTestCase {

    /**
     * Verifies that the formatter picks up changes to the practice, lookup.durationformats and lookup.durationformat.
     */
    @Test
    public void testRefresh() {
        // ensure the practice has no format
        Party practice = TestHelper.getPractice();
        setPracticeFormat(practice, null);

        PatientAgeFormatter formatter = createFormatter();

        // verify the formatter uses the defaults
        Date from = getDate("2011-01-01");
        Date to = getDate("2013-02-02");
        checkFormat("2 Years", from, to, formatter);

        // create and save a new lookup.durationformats, and verify the formatter still picks up defaults
        Lookup formats = DurationFormatterTestHelper.createDurationFormats();
        Lookup format = addFormat(formats, 2, DateUnits.YEARS, true, true, false, false);  // years, months
        checkFormat("2 Years", from, to, formatter);

        // now associate the formats with the practice, and verify they are picked up
        setPracticeFormat(practice, formats);
        checkFormat("2 Years 1 Month", from, to, formatter);

        // now update the format and verify the formatter picks it up
        IMObjectBean bean = new IMObjectBean(format);
        bean.setValue("showDays", true);
        bean.save();
        checkFormat("2 Years 1 Month 1 Day", from, to, formatter);

        // now delete the format and verify the fomatter picks up the deletion.
        remove(format);
        checkFormat("2 Years", from, to, formatter);
    }

    /**
     * Creates a new formatter.
     *
     * @return a new formatter
     */
    @Override
    protected PatientAgeFormatter createFormatter() {
        IArchetypeService service = getArchetypeService();
        return new RefreshablePatientAgeFormatter(lookupService, new PracticeRules(service), service, factory);
    }
}
