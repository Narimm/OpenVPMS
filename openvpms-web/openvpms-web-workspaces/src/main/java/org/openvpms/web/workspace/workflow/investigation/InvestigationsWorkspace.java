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

package org.openvpms.web.workspace.workflow.investigation;

import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.DefaultContextSwitchListener;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryBrowser;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.component.workspace.ResultSetCRUDWorkspace;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.CustomerPatientSummary;
import org.openvpms.web.workspace.patient.summary.CustomerPatientSummaryFactory;


/**
 * Workspace to display list of investigation results.
 *
 * @author Tim Anderson
 */
public class InvestigationsWorkspace extends ResultSetCRUDWorkspace<Act> {

    /**
     * Constructs an {@link InvestigationsWorkspace}.
     *
     * @param context     the context
     * @param mailContext the mail context
     */
    public InvestigationsWorkspace(Context context, MailContext mailContext) {
        super("workflow", "investigation", context);
        setArchetypes(Archetypes.create(InvestigationArchetypes.PATIENT_INVESTIGATION, Act.class));
        setMailContext(mailContext);
    }

    /**
     * Renders the workspace summary.
     *
     * @return the component representing the workspace summary, or {@code null} if there is no summary
     */
    @Override
    public Component getSummary() {
        CRUDWindow<Act> window = getCRUDWindow();
        if (window != null) {
            CustomerPatientSummaryFactory factory = ServiceHelper.getBean(CustomerPatientSummaryFactory.class);
            CustomerPatientSummary summary = factory.createCustomerPatientSummary(getContext(), getHelpContext());
            return summary.getSummary(window.getObject());
        }
        return null;
    }

    /**
     * Creates a new query to populate the browser.
     *
     * @return a new query
     */
    @Override
    protected Query<Act> createQuery() {
        return new InvestigationsQuery(new DefaultLayoutContext(getContext(), getHelpContext()));
    }

    /**
     * Creates a new browser.
     *
     * @param query the query
     * @return a new browser
     */
    @Override
    protected Browser<Act> createBrowser(Query<Act> query) {
        DefaultLayoutContext layoutContext = new DefaultLayoutContext(getContext(), getHelpContext());
        layoutContext.setContextSwitchListener(DefaultContextSwitchListener.INSTANCE);
        IMObjectTableModel<Act> model = new InvestigationsTableModel(layoutContext);
        return BrowserFactory.create(query, null, model, layoutContext);
    }

    /**
     * Determines if the workspace should be refreshed.
     *
     * @return {@code true}
     */
    @Override
    protected boolean refreshWorkspace() {
        return true;
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    @Override
    protected CRUDWindow<Act> createCRUDWindow() {
        QueryBrowser<Act> browser = getBrowser();
        return new InvestigationCRUDWindow(getArchetypes(), browser.getQuery(), browser.getResultSet(),
                                           getContext(), getHelpContext());
    }

    /**
     * Invoked when a browser object is selected.
     * <p/>
     * This implementation sets the object in the CRUD window and if it has been double clicked:
     * <ul>
     * <li>pops up an editor, if editing is supported; otherwise
     * <li>pops up a viewer
     * </li>
     *
     * @param object the selected object
     */
    @Override
    protected void onBrowserSelected(Act object) {
        super.onBrowserSelected(object);
        if (updateSummaryOnChildUpdate()) {
            firePropertyChange(SUMMARY_PROPERTY, null, null);
        }
    }
}
