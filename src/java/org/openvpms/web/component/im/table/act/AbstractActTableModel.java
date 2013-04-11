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
 */

package org.openvpms.web.component.im.table.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableModel;


/**
 * Table model for displaying {@link Act}s. Any "items" nodes are filtered..
 *
 * @author Tim Anderson
 */
public abstract class AbstractActTableModel extends DescriptorTableModel<Act> {

    /**
     * Constructs a {@code AbstractActTableModel}.
     * The column model must be set using {@link #setTableColumnModel}.
     *
     * @param context the layout context
     */
    public AbstractActTableModel(LayoutContext context) {
        super(context);
    }

    /**
     * Constructs a {@code AbstractActTableModel}.
     *
     * @param shortName the act archetype short names
     * @param context   the layout context
     */
    public AbstractActTableModel(String shortName, LayoutContext context) {
        this(new String[]{shortName}, context);
    }

    /**
     * Constructs a {@code AbstractActTableModel}.
     *
     * @param shortNames the act archetype short names
     * @param context    the layout context
     */
    public AbstractActTableModel(String[] shortNames, LayoutContext context) {
        super(shortNames, context);
    }

}
