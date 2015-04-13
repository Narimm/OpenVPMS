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

package org.openvpms.web.workspace.customer.note;

import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.property.Property;

import java.util.List;


/**
 * Layout strategy for <em>act.customerNote</em> acts.
 *
 * @author Tim Anderson
 */
public class NoteLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Determines the no. of columns to display.
     *
     * @param properties the properties
     * @return {@code 1}
     */
    @Override
    protected int getColumns(List<Property> properties) {
        return 1;
    }

}
