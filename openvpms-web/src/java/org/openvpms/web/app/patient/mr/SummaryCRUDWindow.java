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

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.subsystem.AbstractCRUDWindow;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.im.act.ActHierarchyIterator;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.relationship.RelationshipHelper;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.Retryer;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.resource.util.Messages;

import java.util.Arrays;


/**
 * CRUD Window for patient summary.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SummaryCRUDWindow extends AbstractCRUDWindow<Act>
        implements PatientRecordCRUDWindow {

    /**
     * The current act.patientClinicalEvent.
     */
    private Act event;

    /**
     * The current query.
     */
    private PatientSummaryQuery query;


    /**
     * Constructs a <tt>SummaryCRUDWindow</tt>.
     */
    public SummaryCRUDWindow() {
        this(Archetypes.create(PatientArchetypes.CLINICAL_EVENT, Act.class, Messages.get("patient.record.createtype")));
    }

    /**
     * Constructs a <tt>SummaryCRUDWindow</tt>.
     *
     * @param archetypes the archetypes
     */
    public SummaryCRUDWindow(Archetypes<Act> archetypes) {
        super(archetypes);
    }

    /**
     * Sets the current patient clinical event.
     *
     * @param event the current event
     */
    public void setEvent(Act event) {
        this.event = event;
    }

    /**
     * Returns the current patient clinical event.
     *
     * @return the current event. May be <tt>null</tt>
     */
    public Act getEvent() {
        return event;
    }

    /**
     * Sets the current query, for printing.
     *
     * @param query the query
     */
    public void setQuery(PatientSummaryQuery query) {
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
    protected void onSaved(final Act act, final boolean isNew) {
        if (!TypeHelper.isA(act, PatientArchetypes.CLINICAL_EVENT)) {
            // link the item to its parent event, if required. As there might be multiple user's accessing the event,
            // use a Retryer to retry if the linking fails initially
            PatientMedicalRecordLinker recordAction = new PatientMedicalRecordLinker(getEvent(), act);
            Runnable done = new Runnable() {
                public void run() {
                    SummaryCRUDWindow.super.onSaved(act, isNew);
                }
            };
            Retryer retryer = new Retryer(recordAction, done, done);
            retryer.start();
        } else {
            setEvent(act);
            SummaryCRUDWindow.super.onSaved(act, isNew);
        }
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
                Iterable<Act> summary = new ActHierarchyIterator<Act>(query, query.getActItemShortNames());
                DocumentTemplateLocator locator = new ContextDocumentTemplateLocator(PatientArchetypes.CLINICAL_EVENT,
                                                                                     GlobalContext.getInstance());
                IMObjectReportPrinter<Act> printer = new IMObjectReportPrinter<Act>(summary, locator);
                String title = Messages.get("patient.record.summary.print");
                InteractiveIMPrinter<Act> iPrinter
                        = new InteractiveIMPrinter<Act>(title, printer);
                iPrinter.setMailContext(getMailContext());
                iPrinter.print();
            } catch (OpenVPMSException exception) {
                ErrorHelper.show(exception);
            }
        }
    }

    /**
     * Helper to concatenate the short names for the target of a relationship with those supplied.
     *
     * @param relationship the relationship archetype short name
     * @param shortNames   the short names to add
     * @return the archetype shortnames
     */
    private String[] getShortNames(String relationship, String... shortNames) {
        String[] targets = RelationshipHelper.getTargetShortNames(relationship);
        String[] result = Arrays.copyOf(targets, targets.length + shortNames.length);
        System.arraycopy(shortNames, 0, result, targets.length, shortNames.length);
        return result;
    }

}
