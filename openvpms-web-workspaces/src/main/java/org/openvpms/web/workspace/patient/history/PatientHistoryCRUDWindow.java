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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.act.ActHierarchyIterator;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.retry.Retryer;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.patient.PatientMedicalRecordLinker;


/**
 * CRUD Window for patient history.
 *
 * @author Tim Anderson
 */
public class PatientHistoryCRUDWindow extends AbstractPatientHistoryCRUDWindow {

    /**
     * The current query.
     */
    private PatientHistoryQuery query;


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
    }

    /**
     * Sets the current patient clinical event.
     * <p/>
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
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(createPrintButton());
        buttons.add(createAddNoteButton());
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
        buttons.setEnabled(PRINT_ID, enable);
    }

    /**
     * Invoked when the 'new' button is pressed.
     *
     * @param archetypes the archetypes
     */
    @Override
    protected void onCreate(Archetypes<Act> archetypes) {
        if (getEvent() != null) {
            // an event is selected, so display all of the possible event item archetypes
            String[] shortNames = getShortNames(PatientArchetypes.CLINICAL_EVENT_ITEM,
                                                PatientArchetypes.CLINICAL_EVENT);
            archetypes = new Archetypes<Act>(shortNames, archetypes.getType(), PatientArchetypes.CLINICAL_NOTE,
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
        if (!TypeHelper.isA(act, PatientArchetypes.CLINICAL_EVENT)) {
            if (getEvent() == null) {
                createEvent();
            }
            // link the item to its parent event, if required. As there might be multiple user's accessing the event,
            // use a Retryer to retry if the linking fails initially
            PatientMedicalRecordLinker linker = createMedicalRecordLinker(getEvent(), act);
            Retryer.run(linker);
        } else {
            setEvent(act);
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
        if (TypeHelper.isA(object, PatientArchetypes.CLINICAL_EVENT)) {
            setEvent(null);
        }
        super.onDeleted(object);
    }

    /**
     * Invoked when the 'print' button is pressed.
     * This implementation prints the current summary list, rather than
     * the selected item.
     */
    @Override
    protected void onPrint() {
        if (query != null) {
            try {
                Context context = getContext();
                PatientHistoryFilter filter = new PatientHistoryFilter(query.getActItemShortNames());
                // maxDepth = 2 - display the events, and their immediate children
                Iterable<Act> summary = new ActHierarchyIterator<Act>(query, filter, 2);
                DocumentTemplateLocator locator = new ContextDocumentTemplateLocator(PatientArchetypes.CLINICAL_EVENT,
                                                                                     context);
                IMObjectReportPrinter<Act> printer = new IMObjectReportPrinter<Act>(summary, locator, context);
                String title = Messages.get("patient.record.summary.print");
                HelpContext help = getHelpContext().topic(PatientArchetypes.CLINICAL_EVENT + "/print");
                InteractiveIMPrinter<Act> iPrinter = new InteractiveIMPrinter<Act>(title, printer, context, help);
                iPrinter.setMailContext(getMailContext());
                iPrinter.print();
            } catch (OpenVPMSException exception) {
                ErrorHelper.show(exception);
            }
        }
    }

    /**
     * Adds a new <em>act.patientClinicalNote</em>.
     */
    protected void onAddNote() {
        setEvent(null);     // event will be created in onSaved()
        Archetypes<Act> archetypes = new Archetypes<Act>(PatientArchetypes.CLINICAL_NOTE, Act.class);
        onCreate(archetypes);
    }

    /**
     * Creates a new event, making it the current event.
     */
    private void createEvent() {
        Act event = (Act) IMObjectCreator.create(PatientArchetypes.CLINICAL_EVENT);
        if (event == null) {
            throw new IllegalStateException("Failed to create " + PatientArchetypes.CLINICAL_EVENT);
        }
        LayoutContext layoutContext = createLayoutContext(getHelpContext());
        IMObjectEditor editor = IMObjectEditorFactory.create(event, layoutContext);
        editor.getComponent();
        if (editor instanceof AbstractActEditor) {
            ((AbstractActEditor) editor).setStatus(ActStatus.COMPLETED);
        }
        editor.save();
        setEvent(event);
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

}
