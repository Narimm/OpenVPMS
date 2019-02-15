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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.prefs.PreferenceArchetypes;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.patient.PatientContextFactory;
import org.openvpms.hl7.patient.PatientInformationService;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.client.HospitalizationService;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.report.ReporterFactory;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.retry.Retryer;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.servlet.DownloadServlet;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.PatientMedicalRecordLinker;
import org.openvpms.web.workspace.patient.info.PatientContextHelper;
import org.openvpms.web.workspace.patient.mr.PatientVisitNoteEditDialog;
import org.openvpms.web.workspace.patient.mr.PatientVisitNoteEditor;

import java.util.Date;


/**
 * CRUD Window for patient history.
 *
 * @author Tim Anderson
 */
public class PatientHistoryCRUDWindow extends AbstractPatientHistoryCRUDWindow {

    /**
     * The Smart Flow Sheet service factory.
     */
    private final FlowSheetServiceFactory flowSheetServiceFactory;

    /**
     * The current query.
     */
    private PatientHistoryQuery query;

    /**
     * Import flow sheet documents button identifier.
     */
    private static final String IMPORT_FLOWSHEET_ID = "button.importFlowSheet";

    /**
     * Constructs a {@link PatientHistoryCRUDWindow}.
     *
     * @param context the context
     * @param help    the help context
     */
    public PatientHistoryCRUDWindow(Context context, HelpContext help) {
        this(Archetypes.create(PatientArchetypes.CLINICAL_EVENT, Act.class, Messages.get("patient.record.createtype")),
             context, help);
    }

    /**
     * Constructs a {@link PatientHistoryCRUDWindow}.
     *
     * @param archetypes the archetypes
     * @param context    the context
     * @param help       the help context
     */
    public PatientHistoryCRUDWindow(Archetypes<Act> archetypes, Context context, HelpContext help) {
        super(archetypes, PatientHistoryActions.INSTANCE, context, help);
        this.flowSheetServiceFactory = ServiceHelper.getBean(FlowSheetServiceFactory.class);
    }

    /**
     * Constructs a {@link PatientHistoryCRUDWindow}.
     *
     * @param context the context
     * @param actions the history actions
     * @param help    the help context
     */
    protected PatientHistoryCRUDWindow(Context context, PatientHistoryActions actions, HelpContext help) {
        super(Archetypes.create(PatientArchetypes.CLINICAL_EVENT, Act.class, Messages.get("patient.record.createtype")),
              actions, context, help);
        this.flowSheetServiceFactory = ServiceHelper.getBean(FlowSheetServiceFactory.class);
    }

    /**
     * Sets the current patient clinical event.
     * <p>
     * This updates the context.
     *
     * @param event the current event
     */
    @Override
    public void setEvent(Act event) {
        super.setEvent(event);
        getContext().setObject(PatientArchetypes.CLINICAL_EVENT, event);
    }

    /**
     * Sets the current query, for printing.
     *
     * @param query the query
     */
    public void setQuery(PatientHistoryQuery query) {
        this.query = query;
    }

    /**
     * Determines the actions that may be performed on the selected object.
     *
     * @return the actions
     */
    @Override
    protected PatientHistoryActions getActions() {
        return (PatientHistoryActions) super.getActions();
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(createPrintButton());
        buttons.add(createMailButton());
        buttons.add(createAddNoteButton());
        buttons.add(createExternalEditButton());
        buttons.add(createImportFlowSheetButton());
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        super.enableButtons(buttons, enable);
        enablePrintPreview(buttons, enable);

        Act event = getEvent();
        Party location = getContext().getLocation();
        boolean importFlowSheet = enable && getActions().canImportFlowSheet(event, location, flowSheetServiceFactory);
        buttons.setEnabled(IMPORT_FLOWSHEET_ID, importFlowSheet);
    }

    /**
     * Invoked when the 'new' button is pressed.
     *
     * @param archetypes the archetypes
     */
    @Override
    protected void onCreate(Archetypes<Act> archetypes) {
        if (getEvent() != null) {
            boolean includeAddendum = false;
            String defaultShortName = PatientArchetypes.CLINICAL_NOTE;
            Act selected = getObject();
            if (TypeHelper.isA(selected, PatientArchetypes.CLINICAL_NOTE, PatientArchetypes.PATIENT_MEDICATION)) {
                includeAddendum = true;
                if (getActions().isLocked(selected)) {
                    defaultShortName = PatientArchetypes.CLINICAL_ADDENDUM;
                }
            }
            // an event is selected, so display all of the possible event item archetypes
            String[] shortNames = getShortNames(PatientArchetypes.CLINICAL_EVENT_ITEM,
                                                includeAddendum, PatientArchetypes.CLINICAL_EVENT);
            archetypes = new Archetypes<>(shortNames, archetypes.getType(), defaultShortName,
                                          archetypes.getDisplayName());
        }
        super.onCreate(archetypes);
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param act   the object
     * @param isNew determines if the object is a new instance
     */
    @Override
    protected void onSaved(Act act, boolean isNew) {
        if (act.isA(PatientArchetypes.CLINICAL_EVENT)) {
            setEvent(act);
        } else if (act.isA(PatientArchetypes.PATIENT_WEIGHT)) {
            onWeightChanged(act);
        }
        super.onSaved(act, isNew);
    }


    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    @Override
    protected void onDeleted(Act object) {
        if (object.isA(PatientArchetypes.CLINICAL_EVENT)) {
            setEvent(null);
        }
        super.onDeleted(object);
        if (object.isA(PatientArchetypes.PATIENT_WEIGHT)) {
            onWeightChanged(object);
        }
    }

    /**
     * Invoked when the 'print' button is pressed.
     * This implementation prints the current summary list, rather than
     * the selected item.
     */
    @Override
    protected void onPrint() {
        if (query != null) {
            Context context = getContext();
            IMObjectReportPrinter<Act> printer = createPrinter(context);
            String title = Messages.get("patient.record.summary.print");
            HelpContext help = getHelpContext().topic(PatientArchetypes.CLINICAL_EVENT + "/print");
            InteractiveIMPrinter<Act> iPrinter = new InteractiveIMPrinter<>(title, printer, context, help);
            iPrinter.setMailContext(getMailContext());
            iPrinter.print();
        }
    }

    /**
     * Invoked to preview the patient history.
     */
    protected void onPreview() {
        if (query != null) {
            Context context = getContext();
            IMObjectReportPrinter<Act> printer = createPrinter(context);
            Document document = printer.getDocument();
            DownloadServlet.startDownload(document);
        }
    }

    /**
     * Invoked when the 'mail' button is pressed.
     */
    @Override
    protected void onMail() {
        if (query != null) {
            Context context = getContext();
            IMObjectReportPrinter<Act> printer = createPrinter(context);
            mail(printer);
        }
    }

    /**
     * Adds a new <em>act.patientClinicalVisit</em> and <em>act.patientClinicalNote</em>.
     */
    protected void onAddNote() {
        Act event = (Act) IMObjectCreator.create(PatientArchetypes.CLINICAL_EVENT);
        event.setStatus(ActStatus.COMPLETED);
        event.setActivityEndTime(new Date());
        HelpContext help = getHelpContext().subtopic("visitnote");
        LayoutContext layoutContext = createLayoutContext(help);
        PatientVisitNoteEditor editor = new PatientVisitNoteEditor(event, layoutContext);
        edit(editor, null);
    }

    /**
     * Creates a new edit dialog.
     * <p>
     * This implementation registers a post save callback to link the act into the patient history on save.<br/>
     * This is needed for dialogs that show patient history, in order for new records to be displayed when Apply is
     * pressed.
     *
     * @param editor the editor
     * @return a new edit dialog
     */
    @Override
    protected EditDialog createEditDialog(IMObjectEditor editor) {
        EditDialog dialog;
        if (editor instanceof PatientVisitNoteEditor) {
            dialog = new PatientVisitNoteEditDialog((PatientVisitNoteEditor) editor, getContext());
        } else {
            dialog = super.createEditDialog(editor);
        }
        dialog.setPostSaveCallback(e -> linkRecords((Act) e.getObject()));
        return dialog;
    }

    /**
     * Links patient records, once they have been saved.
     *
     * @param act the record act
     */
    protected void linkRecords(Act act) {
        if (!act.isA(PatientArchetypes.CLINICAL_EVENT)) {
            Act event = getEvent();
            if (event == null) {
                event = createEvent();
            }
            // link the item to its parent event, if required. As there might be multiple user's accessing the event,
            // use a Retryer to retry if the linking fails initially
            PatientMedicalRecordLinker linker;
            Act selected = getObject();
            if (act.isA(PatientArchetypes.CLINICAL_ADDENDUM)) {
                if (selected != null && !selected.isA(PatientArchetypes.CLINICAL_ADDENDUM)) {
                    linker = createMedicalRecordLinker(event, null, selected, act);
                } else {
                    linker = createMedicalRecordLinker(event, null, null, act);
                }
            } else {
                linker = createMedicalRecordLinker(event, act);
            }
            Retryer.run(linker);
        }
    }

    /**
     * Invoked when the patient weight changes or a weight record is deleted.
     * <p>
     * If the act is for the current visit, registered listeners will be notified via
     * the {@link PatientInformationService}.
     *
     * @param act the weight act
     */
    protected void onWeightChanged(Act act) {
        Act event = getEvent();
        PatientContext context = PatientContextHelper.getPatientContext(act, getContext());
        if (context != null && ObjectUtils.equals(event, context.getVisit())) {
            PatientInformationService service = ServiceHelper.getBean(PatientInformationService.class);
            service.updated(context, getContext().getUser());
        }
    }

    /**
     * Creates a button to add a new <em>act.patientClinicalNote</em>.
     *
     * @return a new button
     */
    private Button createAddNoteButton() {
        return ButtonFactory.create("addNote", new ActionListener() {
            public void onAction(ActionEvent event) {
                onAddNote();
            }
        });
    }

    /**
     * Creates a button to import flow sheet documents.
     *
     * @return a new button
     */
    private Button createImportFlowSheetButton() {
        return ButtonFactory.create(IMPORT_FLOWSHEET_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onImportFlowSheet();
            }
        });
    }

    /**
     * Creates a printer to print/preview the patient history.
     *
     * @param context the context
     * @return a new printer
     */
    private IMObjectReportPrinter<Act> createPrinter(Context context) {
        String value = query.getValue();
        TextSearch search = null;
        if (!StringUtils.isEmpty(value)) {
            Preferences preferences = ServiceHelper.getPreferences();
            boolean showClinician = preferences.getBoolean(PreferenceArchetypes.HISTORY, "showClinician", false);
            boolean showBatches = preferences.getBoolean(PreferenceArchetypes.HISTORY, "showBatches", false);
            search = new TextSearch(value, showClinician, showBatches, ServiceHelper.getArchetypeService());
        }
        PatientHistoryFilter filter = new PatientHistoryFilter(query.getSelectedItemShortNames(), search, true);
        // need to use maxDepth=3 so that addendum records appear after the records they link to.
        PatientHistoryIterator summary = new PatientHistoryIterator(query, filter, 3);
        DocumentTemplateLocator locator = new ContextDocumentTemplateLocator(PatientArchetypes.CLINICAL_EVENT,
                                                                             context);
        ReporterFactory factory = ServiceHelper.getBean(ReporterFactory.class);
        return new IMObjectReportPrinter<>(summary, locator, context, factory);
    }

    /**
     * Imports flow sheet documents.
     */
    private void onImportFlowSheet() {
        final Act visit = IMObjectHelper.reload(getEvent());
        if (visit != null) {
            Party location = getContext().getLocation();
            PatientContext context = ServiceHelper.getBean(PatientContextFactory.class).createContext(visit, location);
            if (context != null) {
                FlowSheetServiceFactory factory = ServiceHelper.getBean(FlowSheetServiceFactory.class);
                HospitalizationService service = factory.getHospitalizationService(location);
                if (service.exists(context)) {
                    FlowSheetReportsDialog dialog = new FlowSheetReportsDialog(context);
                    dialog.addWindowPaneListener(new WindowPaneListener() {
                        @Override
                        public void onClose(WindowPaneEvent event) {
                            onRefresh(visit);
                        }
                    });
                    dialog.show();
                } else {
                    InformationDialog.show(Messages.format("patient.record.flowsheet.import.nohospitalisation",
                                                           context.getPatient().getName()));
                }
            }
        }
    }
}
