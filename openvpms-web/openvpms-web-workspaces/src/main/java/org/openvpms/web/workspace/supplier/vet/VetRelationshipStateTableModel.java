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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.supplier.vet;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.RelationshipState;
import org.openvpms.web.component.im.relationship.RelationshipStateTableModel;

import java.util.Date;

import static org.openvpms.component.system.common.query.Constraints.gte;
import static org.openvpms.component.system.common.query.Constraints.isNull;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.lte;
import static org.openvpms.component.system.common.query.Constraints.or;
import static org.openvpms.component.system.common.query.Constraints.sort;

/**
 * An {@link RelationshipStateTableModel} for <em>entityRelationship.referredFrom</em> and
 * <em>entityRelationship.referredTo</em> archetypes that includes the vet's practice.
 *
 * @author Tim Anderson
 */
public class VetRelationshipStateTableModel extends RelationshipStateTableModel {

    /**
     * The practice column index.
     */
    private static final int PRACTICE_INDEX = ACTIVE_INDEX + 1;

    /**
     * Constructs a {@link RelationshipStateTableModel}.
     * <p/>
     * Enables selection if the context is in edit mode.
     *
     * @param context       layout context
     * @param displayTarget if {@code true} display the relationship target, otherwise display the source
     */
    public VetRelationshipStateTableModel(LayoutContext context, boolean displayTarget) {
        super(context, displayTarget);
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the table column
     * @param row    the table row
     */
    @Override
    protected Object getValue(RelationshipState object, TableColumn column, int row) {
        Object result;
        if (column.getModelIndex() == PRACTICE_INDEX) {
            IMObjectReference vet = TypeHelper.isA(object.getTarget(), SupplierArchetypes.SUPPLIER_VET)
                                    ? object.getTarget() : object.getSource();
            result = (vet != null) ? getPractice(vet) : null;
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }

    /**
     * Creates a new column model.
     *
     * @return a new column model
     */
    @Override
    protected TableColumnModel createTableColumnModel() {
        TableColumnModel model = super.createTableColumnModel();
        model.addColumn(createTableColumn(PRACTICE_INDEX, "supplier.vet.practice"));
        int count = model.getColumnCount();
        model.moveColumn(count - 1, count - 2);
        return model;
    }

    /**
     * Returns a component displaying a vet's practice.
     *
     * @param vet the vet reference
     * @return the practice component or {@code null} if the vet doesn't have one
     */
    private Component getPractice(IMObjectReference vet) {
        ArchetypeQuery query = new ArchetypeQuery(vet);
        Date now = new Date();
        query.add(join("practices").add(lte("activeStartTime", now))
                          .add(or(gte("activeEndTime", now), isNull("activeEndTime")))
                          .add(join("source", "practice")));
        query.add(sort("practice", "id"));
        query.add(new ObjectRefSelectConstraint("practice"));
        query.add(new NodeSelectConstraint("practice.name"));
        query.setMaxResults(1);
        ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(query);
        if (iterator.hasNext()) {
            ObjectSet set = iterator.next();
            return getEntityViewer(set.getReference("practice.reference"), set.getString("practice.name"));
        }
        return null;
    }
}
