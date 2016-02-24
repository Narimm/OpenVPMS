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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.communication;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.util.Date;

import static org.openvpms.web.workspace.customer.communication.CommunicationLayoutStrategy.ADDRESS;
import static org.openvpms.web.workspace.customer.communication.CommunicationLayoutStrategy.START_TIME;


/**
 * Table model for <em>act.customerCommunication*</em> acts.
 *
 * @author Tim Anderson
 */
public class CommunicationTableModel extends AbstractActTableModel {

    /**
     * The node descriptor names to display in the table.
     */
    private static final String[] NODES = {START_TIME,
                                           CommunicationLayoutStrategy.DESCRIPTION,
                                           CommunicationLayoutStrategy.AUTHOR,
                                           ADDRESS,
                                           CommunicationLayoutStrategy.REASON,
                                           CommunicationLayoutStrategy.LOCATION};


    /**
     * Constructs a {@link CommunicationTableModel}.
     *
     * @param shortNames the act archetype short names
     * @param context    the layout context. May be {@code null}
     */
    public CommunicationTableModel(String[] shortNames, LayoutContext context) {
        super(shortNames, context);
    }

    /**
     * Returns a list of node descriptor names to include in the table.
     *
     * @return the list of node descriptor names to include in the table
     */
    @Override
    protected String[] getNodeNames() {
        return NODES;
    }

    /**
     * Returns a value for a given column.
     *
     * @param object the object to operate on
     * @param column the column
     * @param row    the row
     * @return the value for the column
     */
    @Override
    protected Object getValue(Act object, DescriptorTableColumn column, int row) {
        Object result;
        if (START_TIME.equals(column.getName())) {
            result = getStartTime(object);
        } else if (ADDRESS.equals(column.getName())) {
            result = getAddress(object);
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }

    /**
     * Returns the first line of the address, if present.
     *
     * @param act the communication act
     * @return the address. May be {@code null}
     */
    private String getAddress(Act act) {
        IMObjectBean bean = new IMObjectBean(act);
        String result = null;
        if (bean.hasNode(ADDRESS)) {
            result = bean.getString(ADDRESS);
            if (!StringUtils.isEmpty(result)) {
                String abbr = StringUtils.substringBefore(result, "\n");
                if (result.length() != abbr.length()) {
                    result = abbr + "...";
                }
            }
        }
        return result;
    }

    /**
     * Formats the start time.
     *
     * @param act the communication act
     * @return the formatted start time
     */
    private String getStartTime(Act act) {
        Date startTime = act.getActivityStartTime();
        return startTime != null ? DateFormatter.formatDateTimeAbbrev(startTime) : null;
    }

}
