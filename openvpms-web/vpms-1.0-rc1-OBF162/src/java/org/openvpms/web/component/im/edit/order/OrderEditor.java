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

package org.openvpms.web.component.im.edit.order;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.act.ActEditor;
import org.openvpms.web.component.im.edit.act.ActHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;

import java.math.BigDecimal;
import java.util.List;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.supplierOrder</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class OrderEditor extends ActEditor {

    /**
     * Construct a new <code>ActEditor</code>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context
     */
    public OrderEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
        if (!TypeHelper.isA(act, "act.supplierOrder")) {
            throw new IllegalArgumentException(
                    "Invalid act type: " + act.getArchetypeId().getShortName());
        }
    }

    /**
     * Update totals when an act item changes.
     */
    protected void updateTotals() {
        Property total = getProperty("total");
        // @todo - workaround for OVPMS-211

        List<Act> acts = getEditor().getActs();
        BigDecimal value = ActHelper.sum((Act) getObject(), acts, "total");
        total.setValue(value);
    }

}
