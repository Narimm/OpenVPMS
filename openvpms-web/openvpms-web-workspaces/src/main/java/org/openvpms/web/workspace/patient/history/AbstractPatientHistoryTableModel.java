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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.history;

import echopointng.LabelEx;
import echopointng.xhtml.XhtmlFragment;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.HttpImageReference;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.layout.RowLayoutData;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.function.factory.ArchetypeFunctionsFactory;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.prefs.PreferenceArchetypes;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.CachingReadOnlyArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.system.common.cache.IMObjectCache;
import org.openvpms.component.system.common.cache.LRUIMObjectCache;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.ContextSwitchListener;
import org.openvpms.web.component.im.doc.DocumentViewer;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.AbstractIMObjectTableModel;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.im.view.IMObjectViewerDialog;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.ComponentFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.util.StyleSheetHelper;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for tables displaying summaries of patient medical history.
 * <p>
 * NOTE: this should ideally rendered using using TableLayoutDataEx row spanning but for a bug in TableEx that prevents
 * events on buttons when row selection is enabled in Firefox.
 * See http://forum.nextapp.com/forum/index.php?showtopic=4114 for details
 * TODO.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPatientHistoryTableModel extends AbstractIMObjectTableModel<Act> {

    /**
     * The default render cache size.
     */
    protected static final int DEFAULT_CACHE_SIZE = 100;

    /**
     * Path of the selected parent act icon.
     */
    protected static final String SELECTED_ICON = "../images/navigation/next.png";

    /**
     * The parent act short name.
     */
    private final String parentShortName;

    /**
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * The patient rules.
     */
    private final PatientRules patientRules;

    /**
     * Determines if the clinician is shown in history items.
     */
    private final boolean showClinician;

    /**
     * Determines if product batches are shown in history items.
     */
    private final boolean showBatches;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The jxpath function library.
     */
    private final FunctionLibrary functions;

    /**
     * The object cache, used during rendering.
     */
    private final IMObjectCache cache;

    /**
     * The selected parent act row.
     */
    private int selectedParent;

    /**
     * A map of jxpath expressions, keyed on archetype short name, used to format the text column.
     */
    private Map<String, String> expressions = new HashMap<>();

    /**
     * Cache of clinician names. This is refreshed each time the table is rendered to ensure the data doesn't
     * become stale.
     */
    private Map<Long, String> clinicianNames = new HashMap<>();

    /**
     * The padding, in pixels, used to indent the Type.
     */
    private int typePadding = -1;

    /**
     * The width of the Type column, in pixels.
     */
    private int typeWidth = -1;

    /**
     * The width of the clinician column, in pixels.
     */
    private int clinicianWidth = -1;

    /**
     * Listener to view a batch.
     */
    private ContextSwitchListener batchViewer;

    /**
     * Default fixed column width, in pixels.
     */
    private static final int DEFAULT_WIDTH = 150;

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
     * The logger.
     */
    private static final Log log = LogFactory.getLog(AbstractPatientHistoryTableModel.class);

    /**
     * Constructs an {@link AbstractPatientHistoryTableModel}.
     *
     * @param parentShortName the parent act short name
     * @param context         the layout context
     * @param cacheSize       the render cache size
     */
    public AbstractPatientHistoryTableModel(String parentShortName, LayoutContext context, int cacheSize) {
        this.parentShortName = parentShortName;
        this.context = context;
        patientRules = ServiceHelper.getBean(PatientRules.class);
        TableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(new TableColumn(SELECTION_COLUMN, new Extent(16))); // 16px for the icon
        model.addColumn(new TableColumn(SUMMARY_COLUMN));
        model.addColumn(new TableColumn(SPACER_COLUMN));
        setTableColumnModel(model);
        Preferences preferences = context.getPreferences();
        showClinician = preferences.getBoolean(PreferenceArchetypes.HISTORY, "showClinician", false);
        showBatches = preferences.getBoolean(PreferenceArchetypes.HISTORY, "showBatches", false);
        ArchetypeFunctionsFactory factory = ServiceHelper.getBean(ArchetypeFunctionsFactory.class);
        IArchetypeService archetypeService = ServiceHelper.getArchetypeService();
        cache = new LRUIMObjectCache(cacheSize, archetypeService);
        service = new CachingReadOnlyArchetypeService(cache, archetypeService);
        functions = factory.create(service, true);
        initStyles();
    }

    /**
     * Sets the objects to display.
     *
     * @param objects the objects to display
     */
    @Override
    public void setObjects(List<Act> objects) {
        selectedParent = -1;
        super.setObjects(objects);
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
     * Returns the parent of the supplied act.
     *
     * @param act the act. May be {@code null}
     * @return the parent, or {@code null} if none is found
     */
    public Act getParent(Act act) {
        return (act != null) ? getParent(act, parentShortName) : null;
    }

    /**
     * Returns the parent of the supplied act.
     *
     * @param act       the act. May be {@code null}
     * @param shortName the parent act short name
     * @return the parent, or {@code null} if none is found
     */
    public Act getParent(Act act, String shortName) {
        boolean found;
        List<Act> acts = getObjects();
        int index = acts.indexOf(act);
        while (!(found = TypeHelper.isA(act, shortName)) && index > 0) {
            act = acts.get(--index);
        }
        return (found) ? act : null;
    }

    /**
     * Determines if the clinician is being displayed.
     *
     * @return {@code true} if the clinician is being displayed
     */
    public boolean showClinician() {
        return showClinician;
    }

    /**
     * Determines if product batches are being displayed.
     *
     * @return {@code true} if the batches are being displayed
     */
    public boolean showBatches() {
        return showBatches;
    }

    /**
     * Invoked after the table has been rendered.
     */
    @Override
    public void postRender() {
        super.postRender();
        cache.clear();
        clinicianNames.clear();
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
        ActBean bean = new ActBean(act, service);
        switch (column.getModelIndex()) {
            case SELECTION_COLUMN:
                if (row == selectedParent) {
                    result = getSelectionIndicator();
                }
                break;
            case SUMMARY_COLUMN:
                try {
                    if (TypeHelper.isA(act, parentShortName)) {
                        result = formatParent(bean);
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
     * @return a component representing the act
     * @throws OpenVPMSException for any error
     */
    protected abstract Component formatParent(ActBean bean);

    /**
     * Formats an act date range.
     *
     * @param bean the act
     * @return the date range
     */
    protected String formatDateRange(ActBean bean) {
        String started = null;
        String completed = null;
        Date startTime = bean.getDate("startTime");
        if (startTime != null) {
            started = DateFormatter.formatDate(startTime, false);
        }

        Date endTime = bean.getDate("endTime");
        if (endTime != null) {
            completed = DateFormatter.formatDate(endTime, false);
        }

        String text;
        if (completed == null || ObjectUtils.equals(started, completed)) {
            text = Messages.format("patient.record.summary.singleDate", started);
        } else {
            text = Messages.format("patient.record.summary.dateRange", started, completed);
        }
        return text;
    }

    /**
     * Formats the text for a parent act.
     *
     * @param bean   the act
     * @param reason the reason. May be {@code null}
     * @return the formatted text
     */
    protected String formatParentText(ActBean bean, String reason) {
        Act act = bean.getAct();
        String clinician;
        if (StringUtils.isEmpty(reason)) {
            reason = Messages.get("patient.record.summary.reason.none");
        }
        String status = LookupNameHelper.getName(act, "status");
        clinician = getClinician(bean);
        String age = getAge(bean);
        return Messages.format("patient.record.summary.title", reason, clinician, status, age);
    }

    /**
     * Formats the text for a clinical event.
     *
     * @param bean the act
     * @return the formatted text
     */
    protected String formatEventText(ActBean bean) {
        Act act = bean.getAct();
        String reason = getReason(act);
        String title = act.getTitle();
        if (!StringUtils.isEmpty(reason) && !StringUtils.isEmpty(title)) {
            String text = reason + " - " + title;
            return formatParentText(bean, text);
        } else if (!StringUtils.isEmpty(reason)) {
            return formatParentText(bean, reason);
        } else if (!StringUtils.isEmpty(title)) {
            return formatParentText(bean, title);
        }
        return formatParentText(bean, null);
    }

    /**
     * Returns the reason for the parent act.
     *
     * @param act the act
     * @return the reason. May be {@code null}
     */
    protected String getReason(Act act) {
        return LookupNameHelper.getName(act, "reason");
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

        detail = formatItem(bean);
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
     * @return a component representing the item
     */
    protected Component formatItem(ActBean bean) {
        if (bean.isA(PatientArchetypes.PATIENT_MEDICATION)) {
            return getMedicationDetail(bean, showBatches);
        } else if (bean.isA("act.patientInvestigation*") || bean.isA("act.patientDocument*")) {
            return getDocumentDetail((DocumentAct) bean.getAct());
        }
        return getTextDetail(bean.getAct());
    }

    /**
     * Returns a component for the act type.
     * <p>
     * This indents the type depending on the act's depth in the act hierarchy.
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
            int inset = depth * typePadding;
            label.setInsets(new Insets(inset, 0, 0, 0));
            label.setWidth(new Extent(typeWidth - inset));
        }
        return result;
    }

    /**
     * Returns a hyperlinked type component.
     *
     * @param bean the act
     * @return a component representing the act type
     */
    protected Component getHyperlinkedType(ActBean bean) {
        LayoutContext context = getContext();
        IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(bean.getObject().getObjectReference(),
                                                                     getTypeName(bean),
                                                                     context.getContextSwitchListener(),
                                                                     context.getContext());
        viewer.setWidth(typeWidth);
        return viewer.getComponent();
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
     * <p>
     * This is used to inset child acts.
     *
     * @param bean the act
     * @return the minimum number of steps to a parent event/problem
     */
    protected int getDepth(ActBean bean) {
        int depth = 0;
        if (bean.isA("act.patientDocument*Version")
            || bean.isA(InvestigationArchetypes.PATIENT_INVESTIGATION_VERSION)) {
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
        String clinician = getClinician(bean);
        // Need to jump through some hoops to restrict long clinician names from exceeding the column width.
        String content = "<div xmlns='http://www.w3.org/1999/xhtml' style='width:" + clinicianWidth
                         + "px; overflow:hidden'>" + clinician + "</div>";
        LabelEx label = new LabelEx(new XhtmlFragment(content));
        label.setStyleName("MedicalRecordSummary.clinician");
        return label;
    }

    /**
     * Returns the clinician name associated with an act.
     * <p>
     * This caches clinician names for the duration of a single table render, to improve performance.
     *
     * @param bean the act
     * @return the clinician name or a formatted string indicating the act has no clinician
     */
    protected String getClinician(ActBean bean) {
        String clinician = null;
        IMObjectReference clinicianRef = bean.getParticipantRef(UserArchetypes.CLINICIAN_PARTICIPATION);
        if (clinicianRef != null) {
            clinician = clinicianNames.get(clinicianRef.getId());
            if (clinician == null) {
                clinician = IMObjectHelper.getName(clinicianRef, service);
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
     * Returns a component for the detail of an <em>act.patientMedication</em>.
     *
     * @param bean        the act bean
     * @param showBatches if (@code true}, include any batch number
     */
    protected Component getMedicationDetail(ActBean bean, boolean showBatches) {
        Component component = getTextDetail(bean.getAct());
        if (showBatches) {
            component = addBatch(bean, component);
        }
        return component;
    }

    /**
     * Adds a batch to the medication display, if one is present.
     *
     * @param bean       the act bean
     * @param medication the medication component
     * @return the medication and batch, if one is present, otherwise just the medication
     */
    protected Component addBatch(ActBean bean, Component medication) {
        Component result;
        if (showBatches) {
            Component batch = getBatch(bean);
            if (batch != null) {
                result = ColumnFactory.create(Styles.CELL_SPACING, medication, batch);
            } else {
                result = medication;
            }
        } else {
            result = medication;
        }
        return result;
    }

    /**
     * Returns a component representing a medication batch, if it has one.
     *
     * @param bean the medication act bean
     * @return the batch component, or {@code null} if the medication has no batch
     */
    protected Component getBatch(ActBean bean) {
        Component result = null;
        IMObjectReference reference = bean.getNodeParticipantRef("batch");
        if (reference != null) {
            if (batchViewer == null) {
                batchViewer = new ContextSwitchListener() {
                    @Override
                    public void switchTo(IMObject object) {
                        onShowBatch(object);
                    }

                    @Override
                    public void switchTo(String shortName) {

                    }
                };
            }
            IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(reference, batchViewer,
                                                                         getContext().getContext());
            result = RowFactory.create(Styles.CELL_SPACING, LabelFactory.create("patient.record.summary.batch"),
                                       viewer.getComponent());
        }
        return result;
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
        String text = getText(act, true);
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
     * <p>
     * If a jxpath expression is registered, this will be evaluated. If not, and {@code useDescription} is {@code true}
     * the act description will be used.
     *
     * @param act            the act
     * @param useDescription if {@code true}, fall back to the act description if there is no expression registered
     * @return the text. May be {@code null}
     */
    protected String getText(Act act, boolean useDescription) {
        String text = null;
        String shortName = act.getArchetypeId().getShortName();
        String expr = getExpression(shortName);
        if (!StringUtils.isEmpty(expr)) {
            try {
                JXPathContext context = JXPathHelper.newContext(act, functions);
                Object value = context.getValue(expr);
                if (value != null) {
                    text = value.toString();
                }
            } catch (Throwable exception) {
                log.error(exception, exception);
                text = exception.getMessage();
            }
        } else if (useDescription) {
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
     * Returns a component indicating that a row has been selected.
     *
     * @return the component
     */
    protected Component getSelectionIndicator() {
        Label label = new Label(new HttpImageReference(SELECTED_ICON));
        TableLayoutData layout = new TableLayoutData();
        layout.setAlignment(Alignment.ALIGN_TOP);
        label.setLayoutData(layout);
        return label;
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }

    /**
     * Returns an empty component styled to the same width as a date.
     *
     * @return a new component
     */
    protected Component getDateSpacer() {
        LabelEx result = new LabelEx("");
        result.setStyleName("MedicalRecordSummary.date");
        return result;
    }

    /**
     * Displays a batch in a popup.
     *
     * @param object the batch
     */
    private void onShowBatch(IMObject object) {
        HelpContext help = context.getHelpContext().topic(object, "view");
        IMObjectViewerDialog dialog = new IMObjectViewerDialog(object, context.getContext(), help);
        dialog.show();
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
        Component date;
        boolean showDate = true;
        Date startTime = act.getActivityStartTime();
        if (row > 0) {
            Act prev = getObject(row - 1);
            if (!TypeHelper.isA(prev, parentShortName)
                && ObjectUtils.equals(DateRules.getDate(startTime), DateRules.getDate(prev.getActivityStartTime()))) {
                // act belongs to the same parent act as the prior row, and has the same date, so don't display it again
                showDate = false;
            }
        }
        if (showDate) {
            date = new LabelEx(DateFormatter.formatDate(act.getActivityStartTime(), false));
            date.setStyleName("MedicalRecordSummary.date");
        } else {
            date = getDateSpacer();
        }
        return date;
    }

    /**
     * Initialises the typePadding, typeWidth, and clinicianWidth style properties.
     */
    private void initStyles() {
        typePadding = StyleSheetHelper.getProperty("padding.large", 10);
        typeWidth = StyleSheetHelper.getProperty("history.type.width", DEFAULT_WIDTH);
        clinicianWidth = StyleSheetHelper.getProperty("history.clinician.width", DEFAULT_WIDTH);
        if (typePadding <= 0) {
            typePadding = 10;
        }
        if (typeWidth <= 0) {
            typeWidth = DEFAULT_WIDTH;
        }
        if (clinicianWidth <= 0) {
            clinicianWidth = DEFAULT_WIDTH;
        }
    }

}
