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

package org.openvpms.web.component.im.edit.estimation;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.act.ActHelper;
import org.openvpms.web.component.im.edit.act.ActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.DateHelper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.customerEstimation</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class EstimationEditor extends ActEditor {

    /**
     * Construct a new <code>EstimationEditor</code>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context
     */
    public EstimationEditor(Act act, IMObject parent,
                            LayoutContext context) {
        super(act, parent, context);
        if (!TypeHelper.isA(act, "act.customerEstimation")) {
            throw new IllegalArgumentException(
                    "Invalid act type:" + act.getArchetypeId().getShortName());
        }
        addStartEndTimeListeners();
    }

    /**
     * Update totals when an act item changes.
     */
    protected void onItemsChanged() {
        Property highTotal = getProperty("highTotal");
        Property lowTotal = getProperty("lowTotal");

        List<Act> acts = getEditor().getCurrentActs();
        BigDecimal low = ActHelper.sum((Act) getObject(), acts, "lowTotal");
        BigDecimal high = ActHelper.sum((Act) getObject(), acts, "highTotal");
        lowTotal.setValue(low);
        highTotal.setValue(high);
    }

    /**
     * Invoked when the start time changes. Sets the value to today if
     * start time < today.
     */
    @Override
    protected void onStartTimeChanged() {
        Date start = getStartTime();
        if (start != null) {
            Date now = new Date();
            if (DateHelper.compareDates(start, now) < 0) {
                // ensure start date isn't before the current date
                setStartTime(now);
            } else {
                Date end = getEndTime();
                if (end != null && end.compareTo(start) < 0) {
                    setEndTime(start);
                }
            }
        }
    }

}
