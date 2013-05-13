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
 */

package org.openvpms.web.workspace.customer.info;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextHelper;
import org.openvpms.web.component.im.customer.CustomerBrowser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.workspace.customer.CustomerMailContext;
import org.openvpms.web.workspace.customer.CustomerSummary;
import org.openvpms.web.workspace.subsystem.BasicCRUDWorkspace;


/**
 * Customer information workspace.
 *
 * @author Tim Anderson
 */
public class InformationWorkspace extends BasicCRUDWorkspace<Party> {

    /**
     * Constructs an {@code InformationWorkspace}.
     */
    public InformationWorkspace(Context context) {
        super("customer", "info", context);
        setArchetypes(Party.class, "party.customer*");
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
        ContextHelper.setCustomer(getContext(), object);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Renders the workspace summary.
     *
     * @return the component representing the workspace summary, or {@code null} if there is no summary
     */
    @Override
    public Component getSummary() {
        CustomerSummary summarizer = new CustomerSummary(getContext(), getHelpContext());
        return summarizer.getSummary(getObject());
    }

    /**
     * Returns the latest version of the current customer context object.
     *
     * @return the latest version of the context object, or {@link #getObject()} if they are the same
     */
    @Override
    protected Party getLatest() {
        return getLatest(getContext().getCustomer());
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
     * Invoked when the selection browser is closed.
     *
     * @param dialog the browser dialog
     */
    @Override
    protected void onSelectClosed(BrowserDialog<Party> dialog) {
        if (dialog.createNew()) {
            getCRUDWindow().create();
        } else {
            Party customer = dialog.getSelected();
            if (customer != null) {
                onSelected(customer);
                if (dialog.getBrowser() instanceof CustomerBrowser) {
                    CustomerBrowser browser = (CustomerBrowser) dialog.getBrowser();
                    Party patient = browser.getPatient();
                    if (patient != null) {
                        ContextHelper.setPatient(getContext(), patient);
                    }
                }
            }
        }
    }

}
