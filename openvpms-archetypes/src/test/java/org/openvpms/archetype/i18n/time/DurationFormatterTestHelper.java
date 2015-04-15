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

package org.openvpms.archetype.i18n.time;

import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.Arrays;
import java.util.UUID;


/**
 * Test helper for {@link DurationFormatter} test cases.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class DurationFormatterTestHelper {

    /**
     * Adds a date duration format to an <em>lookup.durationformats</em>.
     *
     * @param lookup     the lookup to add to
     * @param interval   the interval
     * @param units      the interval units
     * @param showYears  determines if years are shown
     * @param showMonths determines if months are shown
     * @param showWeeks  determines if weeks are shown
     * @param showDays   determines if days are shown
     * @return the format
     */
    public static Lookup addFormat(Lookup lookup, int interval, DateUnits units, boolean showYears, boolean showMonths,
                                   boolean showWeeks, boolean showDays) {
        Lookup format = createDurationFormat(interval, units, showYears, showMonths, showWeeks, showDays);
        LookupRelationship rel = (LookupRelationship) TestHelper.create(
                LookupDateDurationFormatter.DURATION_FORMATS_RELATIONSHIP);
        rel.setSource(lookup.getObjectReference());
        rel.setTarget(format.getObjectReference());
        lookup.addLookupRelationship(rel);
        format.addLookupRelationship(rel);
        TestHelper.save(Arrays.asList(lookup, format));
        return format;
    }

    /**
     * Creates and saves a <em>lookup.durationformats</em>.
     *
     * @return a new lookup
     */
    public static Lookup createDurationFormats() {
        String code = "XTESTDURATIONFORMATS_" + UUID.randomUUID().toString().toUpperCase().replace("-", "_");
        return TestHelper.getLookup(LookupDateDurationFormatter.DURATION_FORMATS, code);
    }

    /**
     * Creates a <em>lookup.durationformat</em>.
     *
     * @param interval   the interval
     * @param units      the interval units
     * @param showYears  determines if years are shown
     * @param showMonths determines if months are shown
     * @param showWeeks  determines if weeks are shown
     * @param showDays   determines if days are shown
     * @return a new lookup
     */
    public static Lookup createDurationFormat(int interval, DateUnits units, boolean showYears, boolean showMonths, boolean showWeeks, boolean showDays) {
        String code = "XTESTDURATIONFORMAT-" + UUID.randomUUID().toString();
        Lookup format = (Lookup) TestHelper.create(LookupDateDurationFormatter.DURATION_FORMAT);
        IMObjectBean bean = new IMObjectBean(format);
        bean.setValue("code", code);
        bean.setValue("interval", interval);
        bean.setValue("units", units.toString());
        bean.setValue("showYears", showYears);
        bean.setValue("showMonths", showMonths);
        bean.setValue("showWeeks", showWeeks);
        bean.setValue("showDays", showDays);
        return format;
    }

}
