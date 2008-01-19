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
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.layout.RowLayoutData;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.doc.DocumentActTableHelper;
import org.openvpms.web.component.im.table.AbstractIMObjectTableModel;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ComponentFactory;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Patient medical history summary table model.
 * NOTE: this should ideally rendered using using TableLayoutDataEx row
 * spanning but for a bug in TableEx that prevents events on buttons
 * when row selection is enabled in Firefox.
 * See http://forum.nextapp.com/forum/index.php?showtopic=4114 for details
 * TODO.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SummaryTableModel extends AbstractIMObjectTableModel<Act> {

    /**
     * A map of jxpath expressions, keyed on archetype short name,
     * used to format the text column.
     */
    private Map<String, String> expressions = new HashMap<String, String>();

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(SummaryTableModel.class);


    /**
     * Creates a new <tt>SummaryTableModel</tt>.
     */
    public SummaryTableModel() {
        TableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(new TableColumn(0));
        setTableColumnModel(model);
    }

    /**
     * A map of jxpath expressions, keyed on archetype short name,
     * used to format the text column.
     *
     * @param expressions the expressions
     */
    public void setExpressions(Map<String, String> expressions) {
        this.expressions = expressions;
    }

    /**
     * Returns a map of jxpath expressions, keyed on archetype short name,
     * used to format the text column.
     *
     * @return the expressions
     */
    public Map<String, String> getExpressions() {
        return expressions;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param act    the object
     * @param column the column
     * @param row    the row
     * @return the value at the given coordinate.
     */
    protected Object getValue(Act act, TableColumn column, int row) {
        Object result = null;
        try {
            if (TypeHelper.isA(act, PatientRecordTypes.CLINICAL_EVENT)) {
                result = formatEvent(act);
            } else {
                result = formatItem(act, row);
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
        String started = null;
        String completed = null;
        String clinician;
        String reason = getValue(bean, "reason");
        String status = ArchetypeServiceFunctions.lookup(act, "status");

        Date startTime = bean.getDate("startTime");
        if (startTime != null) {
            started = DateHelper.formatDate(startTime, false);
        }

        Date endTime = bean.getDate("endTime");
        if (endTime != null) {
            completed = DateHelper.formatDate(endTime, false);
        }

        IMObjectReference clinicianRef
                = bean.getParticipantRef("participation.clinician");

        clinician = IMObjectHelper.getName(clinicianRef);
        clinician = getValue(clinician, bean, "clinician");

        String text;
        if (completed == null || ObjectUtils.equals(started, completed)) {
            text = Messages.get("patient.record.summary.singleDate",
                                started, reason, clinician, status);
        } else {
            text = Messages.get("patient.record.summary.dateRange",
                                started, completed, reason, clinician, status);
        }
        Label summary = LabelFactory.create(null, "bold");
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
    private Component formatItem(Act act, int row) {
        Component date = getDate(act, row);
        Component type = getType(act);
        Component detail;

        RowLayoutData layout = new RowLayoutData();
        layout.setAlignment(new Alignment(Alignment.DEFAULT, Alignment.TOP));

        date.setLayoutData(layout);
        type.setLayoutData(layout);

        if (TypeHelper.isA(act, "act.patientInvestigation*")
                || TypeHelper.isA(act, "act.patientDocument*")) {
            detail = getDocumentDetail((DocumentAct) act);
        } else {
            detail = getDetail(act);
        }
        Row padding = RowFactory.create("Inset", new Label(""));
        return RowFactory.create("CellSpacing", padding, date, type, detail);
    }

    /**
     * Returns a component to represent the act date. If the date is the same
     * as a prior date for the same parent act, an empty label is returned.
     *
     * @param act the act
     * @param row the act row
     * @return a component to represent the date
     */
    private Component getDate(Act act, int row) {
        LabelEx date;
        boolean showDate = true;
        if (row > 0) {
            Act prev = getObject(row - 1);
            if (!TypeHelper.isA(prev, PatientRecordTypes.CLINICAL_EVENT)
                    && ObjectUtils.equals(act.getActivityStartTime(),
                                          prev.getActivityStartTime())) {
                // act belongs to the same parent act as the prior row,
                // and has the same date, so don't display it again
                showDate = false;
            }
        }
        if (showDate) {
            date = new LabelEx(DateHelper.formatDate(
                    act.getActivityStartTime(), false));
        } else {
            date = new LabelEx("");
        }
        ComponentFactory.setDefaultStyle(date);
        date.setWidth(new Extent(150));
        // hack to work around lack of cell spanning facility in Table. todo
        return date;
    }

    /**
     * Returns a component for the act type.
     *
     * @param act the act
     * @return a component representing the act type
     */
    private LabelEx getType(Act act) {
        LabelEx type = new LabelEx(DescriptorHelper.getDisplayName(act));
        type.setWidth(new Extent(150));
        ComponentFactory.setDefaultStyle(type);
        // hack to work around lack of cell spanning facility in Table. todo
        return type;
    }

    /**
     * Returns a component for the detail of an act.patientDocument*. or
     * act.patientInvestigation*.
     *
     * @param act the act
     * @return a new component
     */
    private Component getDocumentDetail(DocumentAct act) {
        Component result;
        Label label = getDetail(act);

        // only display a hyperlink if there is a document to download
        boolean link = act.getDocReference() != null;
        Component viewer = DocumentActTableHelper.getDocumentViewer(act, link);

        if (StringUtils.isEmpty(label.getText())) {
            result = viewer;
        } else {
            result = RowFactory.create("CellSpacing", label, viewer);
        }
        return result;
    }

    /**
     * Returns a label to represent the act detail.
     * If a jxpath expression is registered, this will be evaluated, otherwise
     * the act description will be used.
     *
     * @param act the act
     * @return a new component
     */
    private Label getDetail(Act act) {
        Label result;
        String text = null;
        String shortName = act.getArchetypeId().getShortName();
        String expr = getExpression(shortName);
        if (!StringUtils.isEmpty(expr)) {
            try {
                JXPathContext context = JXPathHelper.newContext(act);
                Object value = context.getValue(expr);
                if (value != null) {
                    text = value.toString();
                }
            } catch (Throwable exception) {
                log.error(exception, exception);
                text = exception.getMessage();
            }
        } else {
            text = act.getDescription();
        }
        if (text != null) {
            LabelEx label = new LabelEx(text);
            label.setIntepretNewlines(true);
            label.setLineWrap(true);
            ComponentFactory.setDefaultStyle(label);
            result = label;
        } else {
            result = new Label();
        }
        return result;
    }

    /**
     * Helper to return the jxpath expression for an archetype short name.
     *
     * @param shortName the archetype short name
     * @return an expression, or <tt>null</tt> if none is found
     */
    private String getExpression(String shortName) {
        String result = expressions.get(shortName);
        if (result == null) {
            // try a wildcard match
            for (Map.Entry<String, String> entry : expressions.entrySet()) {
                if (TypeHelper.matches(shortName, entry.getKey())) {
                    result = entry.getValue();
                    break;
                }
            }
        }
        return result;
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
