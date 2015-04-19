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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.info;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextHelper;
import org.openvpms.web.component.im.query.PatientQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.workspace.BasicCRUDWorkspace;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.CustomerMailContext;
import org.openvpms.web.workspace.patient.CustomerPatientSummary;
import org.openvpms.web.workspace.patient.summary.CustomerPatientSummaryFactory;


/**
 * Patient information workspace.
 *
 * @author Tim Anderson
 */
public class InformationWorkspace extends BasicCRUDWorkspace<Party> {

    /**
     * Constructs an {@link InformationWorkspace}.
     *
     * @param context the context
     */
    public InformationWorkspace(Context context) {
        super("patient", "info", context);
        setArchetypes(Party.class, "party.patient*");
        setMailContext(new CustomerMailContext(context, getHelpContext()));
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(Party object) {
        super.setObject(object);
        ContextHelper.setPatient(getContext(), object);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Renders the workspace summary.
     *
     * @return the component representing the workspace summary, or {@code null} if there is no summary
     */
    @Override
    public Component getSummary() {
        CustomerPatientSummaryFactory factory = ServiceHelper.getBean(CustomerPatientSummaryFactory.class);
        CustomerPatientSummary summary = factory.createCustomerPatientSummary(getContext(), getHelpContext());
        return summary.getSummary(getObject());
    }

    /**
     * Returns the latest version of the current patient context object.
     *
     * @return the latest version of the context object, or {@link #getObject()} if they are the same
     */
    @Override
    protected Party getLatest() {
        return getLatest(getContext().getPatient());
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        super.doLayout(container);
        Party latest = getLatest();
        if (latest != getObject()) {
            setObject(latest);
        }
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    @Override
    protected CRUDWindow<Party> createCRUDWindow() {
        return new InformationCRUDWindow(getArchetypes(), getContext(), getHelpContext());
    }

    /**
     * Create a new query.
     *
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    @Override
    protected Query<Party> createSelectQuery() {
        Query<Party> query = super.createSelectQuery();
        if (query instanceof PatientQuery) {
            ((PatientQuery) query).setShowAllPatients(true);
        }
        return query;
    }

}
