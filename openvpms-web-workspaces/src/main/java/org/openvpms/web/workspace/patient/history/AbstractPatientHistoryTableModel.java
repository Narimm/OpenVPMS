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

package org.openvpms.web.workspace.patient.history;

import echopointng.LabelEx;
import echopointng.xhtml.XhtmlFragment;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.HttpImageReference;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.Style;
import nextapp.echo2.app.layout.RowLayoutData;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.doc.DocumentViewer;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.AbstractIMObjectTableModel;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.factory.ComponentFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.style.UserStyleSheets;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for tables displaying summaries of patient medical history.
 * <p/>
 * NOTE: this should ideally rendered using using TableLayoutDataEx row spanning but for a bug in TableEx that prevents
 * events on buttons when row selection is enabled in Firefox.
 * See http://forum.nextapp.com/forum/index.php?showtopic=4114 for details
 * TODO.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPatientHistoryTableModel extends AbstractIMObjectTableModel<Act> {

    /**
     * The parent act short name.
     */
    private final String parentShortName;

    /**
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * The selected parent act row.
     */
    private int selectedParent;

    /**
     * A map of jxpath expressions, keyed on archetype short name, used to format the text column.
     */
    private Map<String, String> expressions = new HashMap<String, String>();

    /**
     * The patient rules.
     */
    private final PatientRules patientRules;

    /**
     * Determines if the clinician is shown in history items.
     */
    private boolean showClinician;

    /**
     * Cache of clinician names. This is refreshed each time the table is rendered to ensure the data doesn't
     * become stale.
     */
    private Map<Long, String> clinicianNames = new HashMap<Long, String>();

    /**
     * The last processed row, used to determine if the clinician cache needs to be refreshed.
     */
    private int lastRow;

    /**
     * Column indicating the selected parent record.
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
     * Path of the selected parent act icon.
     */
    private static final String SELECTED_ICON = "../images/navigation/next.png";

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(AbstractPatientHistoryTableModel.class);

    private int padding = -1;

    /**
     * Constructs an {@link AbstractPatientHistoryTableModel}.
     *
     * @param parentShortName the parent act short name
     * @param context         the layout context
     */
    public AbstractPatientHistoryTableModel(String parentShortName, LayoutContext context) {
        this.parentShortName = parentShortName;
        this.context = context;
        patientRules = ServiceHelper.getBean(PatientRules.class);
        TableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(new TableColumn(SELECTION_COLUMN, new Extent(16))); // 16px for the icon
        model.addColumn(new TableColumn(SUMMARY_COLUMN));
        model.addColumn(new TableColumn(SPACER_COLUMN));
        setTableColumnModel(model);
        Party practice = context.getContext().getPractice();
        if (practice != null) {
            IMObjectBean bean = new IMObjectBean(practice);
            showClinician = bean.getBoolean("showClinicianInHistoryItems");
        }
    }

    /**
     * Returns the parent act short name.
     *
     * @return the parent act short name
     */
    public String getParentShortName() {
        return parentShortName;
    }

    /**
     * Sets the selected parent act row.
     *
     * @param row the row, or {@code -1} if no parent act is selected
     */
    public void setSelectedParent(int row) {
        if (selectedParent != row) {
            if (selectedParent != -1) {
                fireTableCellUpdated(0, selectedParent);
            }
            selectedParent = row;
            fireTableCellUpdated(0, row);
        }
    }

    /**
     * Returns the selected parent act row.
     *
     * @return the row or {@code -1} if no parent act is selected
     */
    public int getSelectedParent() {
        return selectedParent;
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
     * Returns the layout context.
     *
     * @return the layout context
     */
    protected LayoutContext getContext() {
        return context;
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
                if (row == selectedParent) {
                    return new Label(new HttpImageReference(SELECTED_ICON));
                }
                break;
            case SUMMARY_COLUMN:
                ActBean bean = new ActBean(act);
                try {
                    if (TypeHelper.isA(act, parentShortName)) {
                        result = formatParent(bean, row);
                    } else {
                        result = formatItem(bean, row, showClinician);
                    }
                } catch (OpenVPMSException exception) {
                    ErrorHelper.show(exception);
                }
                break;
        }
        return result;
    }

    /**
     * Returns a component for a parent act.
     *
     * @param bean the parent act
     * @param row  the current row
     * @return a component representing the act
     * @throws OpenVPMSException for any error
     */
    protected Component formatParent(ActBean bean, int row) {
        Act act = bean.getAct();
        String started = null;
        String completed = null;
        String clinician;
        String reason = getReason(bean.getAct());
        if (StringUtils.isEmpty(reason)) {
            reason = Messages.get("patient.record.summary.reason.none");
        }
        String status = ArchetypeServiceFunctions.lookup(act, "status");

        Date startTime = bean.getDate("startTime");
        if (startTime != null) {
            started = DateFormatter.formatDate(startTime, false);
        }

        Date endTime = bean.getDate("endTime");
        if (endTime != null) {
            completed = DateFormatter.formatDate(endTime, false);
        }

        clinician = getClinician(bean, row);
        String age = getAge(bean);

        String text;
        if (completed == null || ObjectUtils.equals(started, completed)) {
            text = Messages.format("patient.record.summary.singleDate", started, reason, clinician, status, age);
        } else {
            text = Messages.format("patient.record.summary.dateRange", started, completed, reason, clinician, status,
                                   age);
        }
        Label summary = LabelFactory.create(null, Styles.BOLD);
        summary.setText(text);
        return summary;
    }

    /**
     * Returns the reason for the parent act.
     *
     * @param act the act
     * @return the reason. May be {@code null}
     */
    protected String getReason(Act act) {
        return act.getReason();
    }

    /**
     * Returns a component for an act item.
     *
     * @param bean the act item
     * @param row  the current row
     * @return a component representing the act
     * @throws OpenVPMSException for any error
     */
    protected Component formatItem(ActBean bean, int row, boolean showClinician) {
        Act act = bean.getAct();
        Component date = getDate(act, row);
        Component type = getType(bean);
        Component clinician = (showClinician) ? getClinicianLabel(bean, row) : null;
        Component detail;

        RowLayoutData layout = new RowLayoutData();
        layout.setAlignment(new Alignment(Alignment.DEFAULT, Alignment.TOP));

        date.setLayoutData(layout);
        type.setLayoutData(layout);
        if (clinician != null) {
            clinician.setLayoutData(layout);
        }

        detail = formatItem(bean, row);
        Row padding = RowFactory.create(Styles.INSET, new Label(""));
        Row item = RowFactory.create(Styles.CELL_SPACING, padding, date, type);
        if (clinician != null) {
            item.add(clinician);
        }
        item.add(detail);
        return item;
    }

    /**
     * Formats an act item.
     *
     * @param bean the item bean
     * @param row  the current row
     * @return a component representing the item
     */
    protected Component formatItem(ActBean bean, int row) {
        if (bean.isA("act.patientInvestigation*") || bean.isA("act.patientDocument*")) {
            return getDocumentDetail((DocumentAct) bean.getAct());
        }
        return getTextDetail(bean.getAct());
    }

    /**
     * Returns a component for the act type.
     *
     * @param bean the act
     * @return a component representing the act type
     */
    protected Component getType(ActBean bean) {
        Component result;
        String text = getTypeName(bean);
        LabelEx label = new LabelEx(text);
        label.setStyleName("MedicalRecordSummary.type");
        int depth = getDepth(bean);
        result = label;
        if (depth > 0) {
            Row row = RowFactory.create(label);
            row.setInsets(new Insets(depth * getPadding(), 0, 0, 0));
            result = row;
        }
        return result;
    }

    /**
     * Returns the name of an act to display in the Type column.
     *
     * @param bean the act
     * @return the name
     */
    protected String getTypeName(ActBean bean) {
        return bean.getDisplayName();
    }

    /**
     * Returns the depth of an act relative to an event or problem.
     * <p/>
     * This is used to inset child acts.
     *
     * @param bean the act
     * @return the minimum number of steps to a parent event/problem
     */
    protected int getDepth(ActBean bean) {
        int depth = 0;
        if (bean.isA("act.patientDocument*Version")) {
            ++depth;
        }
        if (bean.hasNode("problem") && !bean.getNodeSourceObjectRefs("problem").isEmpty()) {
            ++depth;
        }
        return depth;
    }

    /**
     * Returns the age of the patient at the time of an act.
     *
     * @param bean the act bean
     * @return the patient age
     */
    protected String getAge(ActBean bean) {
        Act act = bean.getAct();
        Party patient = (Party) IMObjectHelper.getObject(bean.getNodeParticipantRef("patient"), context.getContext());
        return (patient != null) ? patientRules.getPatientAge(patient, act.getActivityStartTime()) : "";
    }

    /**
     * Returns a label indicating the clinician associated with an act.
     *
     * @param bean the act bean
     * @param row  the row being processed
     * @return a new label
     */
    protected Component getClinicianLabel(ActBean bean, int row) {
        String clinician = getClinician(bean, row);
        // Need to jump through some hoops to restrict long clinician names from exceeding the column width.
        Object width = null;
        Style style = ApplicationInstance.getActive().getStyle(LabelEx.class, "MedicalRecordSummary.clinician");
        if (style != null) {
            width = style.getProperty("width");
        }
        if (width == null) {
            width = "150px";
        }
        String content = "<div xmlns='http://www.w3.org/1999/xhtml' style='width:" + width + "; overflow:hidden'>"
                         + clinician + "</div>";
        return new LabelEx(new XhtmlFragment(content));
    }

    /**
     * Returns the clinician name associated with an act.
     * <p/>
     * This caches clinician names for the duration of a single table render, to improve performance.
     *
     * @param bean the act
     * @param row  the row being processed
     * @return the clinician name or a formatted string indicating the act has no clinician
     */
    protected String getClinician(ActBean bean, int row) {
        String clinician = null;
        if (row < lastRow) {
            clinicianNames.clear();
        }
        lastRow = row;
        IMObjectReference clinicianRef = bean.getParticipantRef(UserArchetypes.CLINICIAN_PARTICIPATION);
        if (clinicianRef != null) {
            clinician = clinicianNames.get(clinicianRef.getId());
            if (clinician == null) {
                clinician = IMObjectHelper.getName(clinicianRef);
                if (clinician != null) {
                    clinicianNames.put(clinicianRef.getId(), clinician);
                }
            }
        }

        if (StringUtils.isEmpty(clinician)) {
            clinician = Messages.get("patient.record.summary.clinician.none");
        }
        return clinician;
    }

    /**
     * Returns a component for the detail of an act.patientDocument*. or act.patientInvestigation*.
     *
     * @param act the act
     * @return a new component
     */
    protected Component getDocumentDetail(DocumentAct act) {
        Component result;
        Label label = getTextDetail(act);

        DocumentViewer viewer = new DocumentViewer(act, true, getContext());
        viewer.setShowNoDocument(false);

        if (StringUtils.isEmpty(label.getText())) {
            result = viewer.getComponent();
        } else {
            result = RowFactory.create(Styles.CELL_SPACING, label, viewer.getComponent());
        }
        return result;
    }

    /**
     * Returns a label to represent the act detail.
     * If a jxpath expression is registered, this will be evaluated, otherwise the act description will be used.
     *
     * @param act the act
     * @return a new component
     */
    protected Label getTextDetail(Act act) {
        String text = getText(act);
        return getTextDetail(text);
    }

    /**
     * Returns label for text.
     *
     * @param text the text. May be {@code null} or contain new lines
     * @return a new label
     */
    protected Label getTextDetail(String text) {
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
    protected String getText(Act act) {
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
     * Helper to return a value from a bean, or a formatted string if the value is empty.
     *
     * @param bean    the bean
     * @param node    the node
     * @param message the message key, if the value is empty
     * @return the value, or a message indicating if the value was empty
     */
    protected String getValue(ActBean bean, String node, String message) {
        String value = bean.getString(node);
        return (!StringUtils.isEmpty(value)) ? value : Messages.get(message);
    }

    /**
     * Helper to return the jxpath expression for an archetype short name.
     *
     * @param shortName the archetype short name
     * @return an expression, or {@code null} if none is found
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
            if (!TypeHelper.isA(prev, parentShortName)
                && ObjectUtils.equals(DateRules.getDate(act.getActivityStartTime()),
                                      DateRules.getDate(prev.getActivityStartTime()))) {
                // act belongs to the same parent act as the prior row,
                // and has the same date, so don't display it again
                showDate = false;
            }
        }
        if (showDate) {
            date = new LabelEx(DateFormatter.formatDate(act.getActivityStartTime(), false));
        } else {
            date = new LabelEx("");
        }
        date.setStyleName("MedicalRecordSummary.date");
        return date;
    }


    /**
     * Helper to determine the padding, in pixels to indent child acts.
     * <p/>
     * This uses the "padding.large" property of the active style.
     *
     * @return the padding
     */
    private int getPadding() {
        if (padding == -1) {
            UserStyleSheets styleSheets = ServiceHelper.getBean(UserStyleSheets.class);
            org.openvpms.web.echo.style.Style style = styleSheets.getStyle();
            if (style != null) {
                String value = style.getProperty("padding.large");
                if (value != null) {
                    try {
                        padding = Integer.valueOf(value);
                    } catch (NumberFormatException ignore) {
                        // do nothing
                    }
                }
            }
            if (padding < 0) {
                padding = 10;
            }
        }
        return padding;
    }

}
