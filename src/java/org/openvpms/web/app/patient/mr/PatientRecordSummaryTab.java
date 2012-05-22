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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.patient.mr;

import echopointng.TabbedPane;
import echopointng.tabbedpane.DefaultTabModel;
import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.resource.util.Messages;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * Helper to add a tab to a tab pane that contains a patient medical record summary.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientRecordSummaryTab {

    /**
     * A listener for tab events.
     */
    private PropertyChangeListener listener;


    /**
     * Adds a medical record summary tab for a patient referred to by the supplied act.
     * <p/>
     * If no patient is found, the pane is not added
     *
     * @param act      the act to get the patient from
     * @param pane     the pane to add the tab to
     * @param shortcut if <tt>true</tt>, add a shortcut to the tab
     */
    public void addTab(Act act, final TabbedPane pane, boolean shortcut) {
        Party patient = getPatient(act);
        if (patient != null) {
            addTab(patient, pane, shortcut);
        }
    }

    /**
     * Adds a medical record summary tab for the specified patient.
     *
     * @param patient  the patient
     * @param pane     the pane to add the tab to
     * @param shortcut if <tt>true</tt>, add a shortcut to the tab
     */
    public void addTab(Party patient, final TabbedPane pane, boolean shortcut) {
        String title = Messages.get("button.summary");
        addTab(patient, pane, title, shortcut);
    }

    /**
     * Adds a medical record summary tab for the specified patient.
     *
     * @param patient  the patient
     * @param pane     the pane to add the tab to
     * @param title    the tab title
     * @param shortcut if <tt>true</tt>, add a shortcut to the tab
     */
    public void addTab(Party patient, final TabbedPane pane, String title, boolean shortcut) {
        if (listener != null) {
            throw new IllegalStateException("This component can only by used once");
        }
        PatientSummaryQuery query = new PatientSummaryQuery(patient);
        final Browser browser = new SummaryTableBrowser(query);
        Component inset = ColumnFactory.create("Inset", browser.getComponent());
        DefaultTabModel model = (DefaultTabModel) pane.getModel();
        final int index = model.size();
        if (shortcut) {
            title = "&" + (index + 1) + " " + title;
        }
        model.addTab(title, inset);

        // register the listener to perform an initial query
        listener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (pane.getSelectedIndex() == index) {
                    browser.query();
                    // don't need the listener any longer
                    pane.removePropertyChangeListener(listener);
                }
            }
        };
        pane.addPropertyChangeListener(listener);
    }

    /**
     * Helper to get the patient for an act.
     *
     * @param act the act
     * @return the patient, or <tt>null</tt> if none is found
     */
    private Party getPatient(Act act) {
        ActBean bean = new ActBean(act);
        IMObjectReference patientRef = bean.getNodeParticipantRef("patient");
        return (Party) IMObjectHelper.getObject(patientRef);
    }
}