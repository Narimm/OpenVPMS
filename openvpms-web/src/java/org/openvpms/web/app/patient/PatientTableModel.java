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

package org.openvpms.web.app.patient;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.table.BaseIMObjectTableModel;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientTableModel extends BaseIMObjectTableModel<Party> {

    private final PatientRules rules;

    private boolean showOwner;

    /**
     * The owner model index.
     */
    private static final int OWNER_INDEX = NEXT_INDEX;


    /**
     * Constructs a new <code>PatientTableModel</code>.
     *
     * @param showOwner if <code>true</code> display the owner
     */
    public PatientTableModel(boolean showOwner) {
        super(null);
        this.showOwner = showOwner;
        rules = new PatientRules();
        setTableColumnModel(createTableColumnModel());
    }

    /**
     * Determines if the patient-owner column should be displayed.
     *
     * @param showOwner if <code>true</code> show the patient-owner column
     */
    public void setShowOwner(boolean showOwner) {
        if (showOwner != this.showOwner) {
            this.showOwner = showOwner;
            setTableColumnModel(createTableColumnModel());
        }
    }

    /**
     * Determines if the patient-owner column should be displayed.
     *
     * @return <code>true</code> if the patient-owner column should be displayed
     */
    public boolean isShowOwner() {
        return showOwner;
    }


    /**
     * Creates a new column model.
     *
     * @return a new column model
     */
    @Override
    protected TableColumnModel createTableColumnModel() {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(createTableColumn(NAME_INDEX, "table.imobject.name"));
        if (showOwner) {
            model.addColumn(
                    createTableColumn(OWNER_INDEX, "patienttablemodel.owner"));
        }
        model.addColumn(createTableColumn(DESCRIPTION_INDEX,
                                          "table.imobject.description"));
        return model;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the column
     * @param row    the row
     * @return the value at the given coordinate
     */
    @Override
    protected Object getValue(Party object, int column, int row) {
        TableColumn col = getColumn(column);
        if (col.getModelIndex() == OWNER_INDEX) {
            Party owner = rules.getOwner(object);
            IMObjectReference ref = (owner != null) ? owner.getObjectReference() : null;
            IMObjectReferenceViewer viewer
                    = new IMObjectReferenceViewer(ref, false);
            return viewer.getComponent();
        } else {
            return super.getValue(object, column, row);
        }
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if <code>true</code> sort in ascending order; otherwise
     *                  sort in <code>descending</code> order
     * @return the sort criteria, or <code>null</code> if the column isn't
     *         sortable
     */
    @Override
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        TableColumn col = getColumn(column);
        if (col.getModelIndex() == OWNER_INDEX) {
            return null;
        }
        return super.getSortConstraints(column, ascending);
    }
}
