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

package org.openvpms.archetype.i18n.time;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.lookup.Lookup;

import java.util.Collection;
import java.util.Date;


/**
 * An {@link DurationFormatter} for dates that configures itself from a <em>lookup.durationformats</em> lookup.
 *
 * @author Tim Anderson
 */
public class LookupDateDurationFormatter implements DurationFormatter {

    /**
     * Duration formats lookup archetype short name.
     */
    public static final String DURATION_FORMATS = "lookup.durationformats";

    /**
     * Duration format lookup archetype short name.
     */
    public static final String DURATION_FORMAT = "lookup.durationformat";

    /**
     * Duration formats lookup relationship archetype short name.
     */
    public static final String DURATION_FORMATS_RELATIONSHIP = "lookupRelationship.durationformats";

    /**
     * The formatter.
     */
    private CompositeDurationFormatter formatter;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(LookupDateDurationFormatter.class);


    /**
     * Constructs a {@link LookupDateDurationFormatter}.
     *
     * @param formats       an <em>lookup.durationformats</em>
     * @param lookupService the lookup service
     * @param service       the archetype service
     */
    public LookupDateDurationFormatter(Lookup formats, ILookupService lookupService, IArchetypeService service) {
        formatter = new CompositeDurationFormatter();
        Collection<Lookup> lookups = lookupService.getTargetLookups(formats, DURATION_FORMATS_RELATIONSHIP);
        for (Lookup lookup : lookups) {
            IMObjectBean bean = service.getBean(lookup);
            int interval = bean.getInt("interval");
            DateUnits units = DateUnits.valueOf(bean.getString("units"));
            boolean showYears = bean.getBoolean("showYears");
            boolean showMonths = bean.getBoolean("showMonths");
            boolean showWeeks = bean.getBoolean("showWeeks");
            boolean showDays = bean.getBoolean("showDays");
            if (showYears || showMonths || showWeeks || showDays) {
                formatter.add(interval, units, DateDurationFormatter.create(showYears, showMonths, showWeeks,
                                                                            showDays));
            } else {
                log.warn("Skipping duration format " + lookup.getName() + " (" + lookup.getId()
                         + "): no fields displayed");
            }
        }
    }

    /**
     * Formats the duration between two timestamps.
     *
     * @param from the starting time
     * @param to   the ending time
     * @return the formatted duration
     */
    public String format(Date from, Date to) {
        return formatter.format(from, to);
    }

}
