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

package org.openvpms.web.app.patient.history;

import echopointng.LabelEx;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.HttpImageReference;
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
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.doc.DocumentViewer;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.AbstractIMObjectTableModel;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ComponentFactory;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.NumberFormatter;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;

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
 * @author Tim Anderson
 */
public class PatientHistoryTableModel extends AbstractIMObjectTableModel<Act> {

    /**
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * A map of jxpath expressions, keyed on archetype short name,
     * used to format the text column.
     */
    private Map<String, String> expressions = new HashMap<String, String>();

    /**
     * The selected visit row.
     */
    private int selectedVisit;

    /**
     * The patient rules.
     */
    private PatientRules patientRules;

    /**
     * Column indicating the selected visit.
     */
    private static final int SELECTION_COLUMN = 0;

    /**
     * Column with the act summary.
     */
    private static final int SUMMARY_COLUMN = 1;

    /**
     * Column used to add a spacer to differentiate the selected row and the coloured visit items.
     */
    private static final int SPACER_COLUMN = 2;

    /**
     * Path of the selected visit icon.
     */
    private static final String SELECTED_VISIT_IMAGE = "../images/navigation/next.png";

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(PatientHistoryTableModel.class);


    /**
     * Constructs a {@code PatientHistoryTableModel}.
     *
     * @param context the layout context
     */
    public PatientHistoryTableModel(LayoutContext context) {
        this.context = context;
        TableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(new TableColumn(SELECTION_COLUMN, new Extent(16))); // 16px for the icon
        model.addColumn(new TableColumn(SUMMARY_COLUMN));
        model.addColumn(new TableColumn(SPACER_COLUMN));
        setTableColumnModel(model);
        patientRules = (PatientRules) ServiceHelper.getContext().getBean("patientRules");
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
     * Sets the selected visit row.
     *
     * @param row the row, or <tt>-1</tt> if no visit is selected
     */
    public void setSelectedVisit(int row) {
        if (selectedVisit != row) {
            selectedVisit = row;
            refresh();
        }
    }

    /**
     * Returns the selected visit row.
     *
     * @return the row or <tt>-1</tt> if no visit is selected
     */
    public int getSelectedVisit() {
        return selectedVisit;
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
        switch (column.getModelIndex()) {
            case SELECTION_COLUMN:
                if (row == selectedVisit) {
                    return new Label(new HttpImageReference(SELECTED_VISIT_IMAGE));
                }
                break;
            case SUMMARY_COLUMN:
                try {
                    if (TypeHelper.isA(act, PatientArchetypes.CLINICAL_EVENT)) {
                        result = formatEvent(act);
                    } else {
                        result = formatItem(act, row);
                    }
                } catch (OpenVPMSException exception) {
                    ErrorHelper.show(exception);
                }
                break;
        }
        return result;
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if {@code true}{ sort in ascending order; otherwise sort in {@code descending}{ order
     * @return the sort criteria, or {@code null}{ if the column isn't sortable
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

        Party patient = (Party) IMObjectHelper.getObject(bean.getNodeParticipantRef("patient"), context.getContext());
        String age = (patient != null) ? patientRules.getPatientAge(patient, act.getActivityStartTime()) : "";

        String text;
        if (completed == null || ObjectUtils.equals(started, completed)) {
            text = Messages.get("patient.record.summary.singleDate", started, reason, clinician, status, age);
        } else {
            text = Messages.get("patient.record.summary.dateRange", started, completed, reason, clinician, status, age);
        }
        Label summary = LabelFactory.create(null, "bold");
        summary.setText(text);
        return summary;
    }

    /**
     * Returns a component for an act item.
     *
     * @param act the act item
     * @param row the current row
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
        } else if (TypeHelper.isA(act, PatientArchetypes.PATIENT_MEDICATION)) {
            detail = getMedicationDetail(act);
        } else if (TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE_ITEM)) {
            detail = getInvoiceItemDetail((FinancialAct) act);
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
            if (!TypeHelper.isA(prev, PatientArchetypes.CLINICAL_EVENT)
                && ObjectUtils.equals(DateRules.getDate(act.getActivityStartTime()),
                                      DateRules.getDate(prev.getActivityStartTime()))) {
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
     * <p/>
     * This indents document version acts.
     *
     * @param act the act
     * @return a component representing the act type
     */
    private Component getType(Act act) {
        Component result;
        String text = DescriptorHelper.getDisplayName(act);
        LabelEx type = new LabelEx(text);
        type.setWidth(new Extent(150));
        // hack to work around lack of cell spanning facility in Table. todo
        ComponentFactory.setDefaultStyle(type);
        if (TypeHelper.isA(act, "act.patientDocument*Version")) {
            result = RowFactory.create("InsetX", type);
        } else {
            result = type;
        }

        return result;
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

        DocumentViewer viewer = new DocumentViewer(act, true, context);
        viewer.setShowNoDocument(false);

        if (StringUtils.isEmpty(label.getText())) {
            result = viewer.getComponent();
        } else {
            result = RowFactory.create("CellSpacing", label, viewer.getComponent());
        }
        return result;
    }

    /**
     * Returns a component for the detail of an act.patientMedication.
     * <p/>
     * This includes the invoice item amount, if one is available.
     *
     * @param act the act
     * @return a new component
     */
    private Component getInvoiceItemDetail(FinancialAct act) {
        ActBean bean = new ActBean(act);
        IMObjectReference product = bean.getNodeParticipantRef("product");
        String name = IMObjectHelper.getName(product);
        String text = Messages.get("patient.record.summary.invoiceitem", name, act.getQuantity(),
                                   NumberFormatter.formatCurrency(act.getTotal()));
        return getDetail(text);
    }

    /**
     * Returns a component for the detail of an act.patientMedication.
     *
     * @param act the act
     * @return a new component
     */
    private Component getMedicationDetail(Act act) {
        String text = getText(act);
        ActBean bean = new ActBean(act);
        FinancialAct item = (FinancialAct) bean.getNodeSourceObject("invoiceItem");
        if (item != null) {
            text = Messages.get("patient.record.summary.medication", text,
                                NumberFormatter.formatCurrency(item.getTotal()));
        }
        return getDetail(text);
    }

    /**
     * Returns a label to represent the act detail.
     * If a jxpath expression is registered, this will be evaluated, otherwise the act description will be used.
     *
     * @param act the act
     * @return a new component
     */
    private Label getDetail(Act act) {
        String text = getText(act);
        return getDetail(text);
    }

    private Label getDetail(String text) {
        Label result;
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
     * Returns the act detail as a string.
     * If a jxpath expression is registered, this will be evaluated, otherwise the act description will be used.
     *
     * @param act the act
     * @return the text. May be {@code null}
     */
    private String getText(Act act) {
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
        return text;
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
            return Messages.get("patient.record.summary.novalue", bean.getDisplayName(node));
        }
        return value;
    }
}
