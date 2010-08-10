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

package org.openvpms.web.component.im.lookup;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.BaseIMObjectTableModel;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;


/**
 * Table model for {@link LookupRelationship}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-04-11 04:09:07Z $
 */
public class LookupRelationshipTableModel
        extends BaseIMObjectTableModel<LookupRelationship> {

    /**
     * The layout context.
     */
    private final LayoutContext layoutContext;


    /**
     * Construct a new <tt>LookupRelationshipTableModel</tt>.
     * <p/>
     * Enables selection if the context is in edit mode.
     *
     * @param context layout context
     */
    public LookupRelationshipTableModel(LayoutContext context) {
        layoutContext = context;
        setEnableSelection(layoutContext.isEdit());
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the table column
     * @param row    the table row
     * @return the value at the specified coordinate
     */
    @Override
    protected Object getValue(LookupRelationship object, TableColumn column,
                              int row) {
        Object result;
        if (column.getModelIndex() == NAME_INDEX) {
            result = getEntity(object);
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }

    /**
     * Returns the name of the entity in a relationship. This returns the
     * "non-current" or target side of the relationship. "Non-current" refers
     * the object that is NOT currently being viewed/edited. If the source and
     * target entities don't refer to the current object being viewed/edited,
     * then the target entity of the relationship is used.
     *
     * @param relationship the relationship
     * @return a viewer of the "non-current" entity of the relationship
     */
    protected Component getEntity(LookupRelationship relationship) {
        IMObjectReference entity;
        IMObject current = layoutContext.getContext().getCurrent();
        if (current == null) {
            entity = relationship.getTarget();
        } else {
            IMObjectReference ref = new IMObjectReference(current);

            if (relationship.getSource() != null
                && ref.equals(relationship.getSource())) {
                entity = relationship.getTarget();
            } else if (relationship.getTarget() != null
                       && ref.equals(relationship.getTarget())) {
                entity = relationship.getSource();
            } else {
                entity = relationship.getTarget();
            }
        }

        return new IMObjectReferenceViewer(entity, layoutContext.getContextSwitchListener()).getComponent();
    }

}
