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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.business.service.lookup.ILookupService;

import java.util.Collection;
import java.util.Date;


/**
 * An {@link DurationFormatter} for dates that configures itself from a <em>lookup.durationformats</em> lookup.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
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
     * Constructs a <tt>LookupDurationFormatter</tt>.
     *
     * @param formats an <em>lookup.durationformats</em>
     * @param service the lookup service
     * @param factory the bean factory
     */
    public LookupDateDurationFormatter(Lookup formats, ILookupService service,
                                       IMObjectBeanFactory factory) {
        formatter = new CompositeDurationFormatter();
        Collection<Lookup> lookups = service.getTargetLookups(formats, DURATION_FORMATS_RELATIONSHIP);
        for (Lookup lookup : lookups) {
            IMObjectBean bean = factory.createBean(lookup);
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
