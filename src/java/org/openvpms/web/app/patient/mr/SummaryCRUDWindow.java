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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.subsystem.AbstractCRUDWindow;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.act.ActHierarchyIterator;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * CRUD Window for patient summary. Only supports the display of the acts.
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
     * Creates a new <tt>SummaryCRUDWindow</tt>.
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
     * Invoked when the edit button is pressed. This edits the current
     * <em>act.patientClinicalEvent</em>.
     */
    @Override
    public void edit() {
        Act event = getEvent();
        if (event != null) {
            // make sure the latest instance is being used.
            Act current = IMObjectHelper.reload(event);
            if (current == null) {
                ErrorDialog.show(Messages.get("imobject.noexist"),
                                 DescriptorHelper.getDisplayName(event));
            } else {
                edit(current);
            }
        }
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        buttons.add(getEditButton());
        buttons.add(getCreateButton());
        buttons.add(getDeleteButton());
        buttons.add(getPrintButton());
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        buttons.removeAll();
        if (enable) {
            buttons.add(getCreateButton());
            buttons.add(getEditButton());
            buttons.add(getDeleteButton());
            buttons.add(getPrintButton());
        } else {
            buttons.add(getCreateButton());
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
            try {
                Iterable<Act> summary = new ActHierarchyIterator<Act>(
                        query, query.getActItemShortNames());
                IMObjectReportPrinter<Act> printer
                        = new IMObjectReportPrinter<Act>(
                        summary, PatientRecordTypes.CLINICAL_EVENT);
                String title = Messages.get("patient.record.summary.print");
                InteractiveIMPrinter<Act> iPrinter
                        = new InteractiveIMPrinter<Act>(title, printer);
                iPrinter.print();
            } catch (OpenVPMSException exception) {
                ErrorHelper.show(exception);
            }
        }
    }

}
