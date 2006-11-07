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

package org.openvpms.web.app.patient.mr;

import echopointng.LabelEx;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.MutableStyle;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.layout.RowLayoutData;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.archetype.query.NodeSet;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.table.AbstractIMObjectTableModel;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.DateFormatter;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.Date;


/**
 * Patient medical history summary table model.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SummaryTableModel extends AbstractIMObjectTableModel<Act> {

    /**
     * Construct a new <code>SummaryTableModel</code>.
     */
    public SummaryTableModel() {
        TableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(new TableColumn(0));
        setTableColumnModel(model);
        // todo - hack to override EPNG default label style
        ((MutableStyle) LabelEx.DEFAULT_STYLE).setProperty(
                LabelEx.PROPERTY_BACKGROUND, null);
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the column
     * @param row    the row
     * @return the value at the given coordinate.
     */
    protected Object getValue(IMObject object, int column, int row) {
        Object result = null;
        Act act = (Act) object;
        try {
            if (TypeHelper.isA(act, "act.patientClinicalEvent")) {
                result = formatEvent(act);
            } else {
                result = formatItem(act);
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        return result;
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
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        return null;
    }

    /**
     * Returns a component for an <em>act.patientClinicalEvent</em>.
     *
     * @param act the event
     * @return a component representing the act
     * @throws OpenVPMSException for any error
     */
    private Component formatEvent(Act act) {
        ActBean bean = new ActBean(act);
        String completed = null;
        String clinician = null;
        String reason = getValue(bean, "reason");
        String status = ArchetypeServiceFunctions.lookup(act, "status");

        Date date = bean.getDate("completeTime");
        if (date != null) {
            completed = getValue(DateFormatter.formatDate(date, false),
                                 bean, "completedTime");
        }

        IMObjectReference clinicianRef
                = bean.getParticipantRef("participation.clinician");

        NodeSet name = IMObjectHelper.getNodes(clinicianRef, "name");
        if (name != null) {
            clinician = (String) name.get("name");
        }
        clinician = getValue(clinician, bean, "clinician");

        String text;
        if (completed == null
                || ActStatus.IN_PROGRESS.equals(bean.getStatus())) {
            text = Messages.get("patient.record.summary.incomplete",
                                reason, clinician, status);
        } else {
            text = Messages.get("patient.record.summary.complete",
                                reason, clinician, completed);
        }
        Label summary = LabelFactory.create(null, "PatientSummary");
        summary.setText(text);
        return summary;
    }

    /**
     * Returns a component for an act item.
     *
     * @param act the act item
     * @return a component representing the act
     * @throws OpenVPMSException for any error
     */
    private Component formatItem(Act act) {
        Label date = new Label(DateFormatter.formatDate(
                act.getActivityStartTime(), false));
        RowLayoutData layout = new RowLayoutData();
        layout.setAlignment(new Alignment(Alignment.DEFAULT, Alignment.TOP));
        date.setLayoutData(layout);

        LabelEx description = new LabelEx(act.getDescription());
        description.setIntepretNewlines(true);
        description.setLineWrap(true);
        Row padding = RowFactory.create("Inset", new Label(""));
        return RowFactory.create("CellSpacing", padding, date, description);
    }

    /**
     * Helper to return a value from a bean, or a formatted string if the value
     * is null.
     *
     * @param bean the bean
     * @param node the node
     * @return the value, or a formatting string indicating if the value was
     *         null
     */
    private String getValue(ActBean bean, String node) {
        return getValue(bean.getString(node), bean, node);
    }

    /**
     * Helper to return a value, or a formatted string if the value is null
     * or empty.
     *
     * @param value the value
     * @param bean  the bean the value came from
     * @param node  the value node
     * @return the value, or a formatting string indicating if the value was
     *         null
     */
    private String getValue(String value, ActBean bean, String node) {
        if (StringUtils.isEmpty(value)) {
            return Messages.get("patient.record.summary.novalue",
                                bean.getDisplayName(node));
        }
        return value;
    }
}
