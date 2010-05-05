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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.workflow.messaging;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;

import java.util.List;


/**
 * Table model for <em>act.userMessage</em> and <em>act.systemMessage</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MessageTableModel extends AbstractActTableModel {

    /**
     * The node descriptor names to display in the table.
     */
    private static final String[] NODES = {"to", "startTime", "reason", "description", "status", "item"};


    /**
     * Constructs a <tt>MessageTableModel</tt>.
     *
     * @param shortNames the act archetype short names
     * @param context    the layout context. May be <tt>null</tt>
     */
    public MessageTableModel(String[] shortNames, LayoutContext context) {
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
     * Determines if the archetype column should be displayed.
     *
     * @param archetypes the archetypes
     * @return <tt>false</tt>
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
        if ("item".equals(column.getName())) {
            List<IMObject> values = column.getValues(object);
            if (values != null && !values.isEmpty()) {
                result = values.get(0);
                if (result instanceof ActRelationship) {
                    IMObjectReference ref = ((ActRelationship) result).getTarget();
                    String name = DescriptorHelper.getDisplayName(ref.getArchetypeId().getShortName());
                    result = new IMObjectReferenceViewer(ref, name, true).getComponent();
                }
            } else {
                result = null;
            }
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }
}
