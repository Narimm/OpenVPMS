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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.patient.visit;

/**
 * A listener for {@link VisitEditor} events.
 *
 * @author Tim Anderson
 */
public interface VisitEditorListener {

    /**
     * Invoked when the patient history tab is selected.
     */
    void historySelected();

    /**
     * Invoked when the invoice tab is selected.
     */
    void invoiceSelected();

    /**
     * Invoked when the reminders/alert tab is selected.
     */
    void remindersSelected();

    /**
     * Invoked when the documents tab is selected.
     */
    void documentsSelected();

}
