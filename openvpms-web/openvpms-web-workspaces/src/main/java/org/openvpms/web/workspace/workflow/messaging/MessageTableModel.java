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

package org.openvpms.web.workspace.workflow.messaging;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.MessageArchetypes;
import org.openvpms.archetype.rules.workflow.MessageStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextSwitchListener;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Table model for <em>act.userMessage</em> and <em>act.systemMessage</em> acts.
 *
 * @author Tim Anderson
 */
public class MessageTableModel extends AbstractActTableModel {

    /**
     * The node descriptor names to display in the table.
     */
    private static final String[] NODES = {"startTime", "description", "from", "reason", "status", "item"};

    /**
     * Cache of status names, keyed on code.
     */
    private final Map<String, String> statuses;


    /**
     * Constructs a {@code MessageTableModel}.
     *
     * @param shortNames the act archetype short names
     * @param context    the layout context. May be {@code null}
     */
    public MessageTableModel(String[] shortNames, LayoutContext context) {
        super(shortNames, context);
        statuses = LookupNameHelper.getLookupNames(MessageArchetypes.USER, "status");
    }

    /**
     * Helper to format an act start time.
     * This excludes the date if the act was done today.
     *
     * @param act the act
     * @return the formatted start time
     */
    public static String formatStartTime(Act act) {
        String result;
        DateFormat format;
        Date startTime = act.getActivityStartTime();
        if (startTime != null) {
            if (DateRules.compareDates(startTime, new Date()) == 0) {
                format = DateFormatter.getTimeFormat(DateFormat.SHORT);
            } else {
                format = DateFormatter.getDateTimeFormat(false);
            }
            result = format.format(startTime);
        } else {
            result = "";
        }
        return result;
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
     * Determines if the archetype column should be displayed.
     *
     * @param archetypes the archetypes
     * @return {@code false}
     */
    @Override
    protected boolean showArchetypeColumn(List<ArchetypeDescriptor> archetypes) {
        return false;
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
        String columnName = column.getName();
        if ("startTime".equals(columnName)) {
            result = formatStartTime(object);
        } else if ("from".equals(columnName)) {
            result = getFrom(object);
        } else if ("status".equalsIgnoreCase(columnName)) {
            result = getStatus(object);
        } else if ("item".equals(columnName)) {
            List<IMObject> values = column.getValues(object);
            if (values != null && !values.isEmpty()) {
                result = values.get(0);
                if (result instanceof ActRelationship) {
                    IMObjectReference ref = ((ActRelationship) result).getTarget();
                    String name = DescriptorHelper.getDisplayName(ref.getArchetypeId().getShortName());
                    LayoutContext layout = getLayoutContext();
                    Context context = layout.getContext();
                    ContextSwitchListener listener = layout.getContextSwitchListener();
                    result = new IMObjectReferenceViewer(ref, name, listener, context).getComponent();
                }
            } else {
                result = null;
            }
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }

    /**
     * Returns the name of the user that the message is from.
     *
     * @param object the message
     * @return the user name
     */
    private String getFrom(Act object) {
        ActBean bean = new ActBean(object);
        return IMObjectHelper.getName(bean.getNodeParticipantRef("from"));
    }

    /**
     * Returns the message status. This treats {@link MessageStatus#READ} as the same as {@link MessageStatus#PENDING}.
     *
     * @param object the message
     * @return the status
     */
    private String getStatus(Act object) {
        String status = object.getStatus();
        if (MessageStatus.READ.equals(status)) {
            status = MessageStatus.PENDING;
        }
        return statuses.get(status);
    }
}
