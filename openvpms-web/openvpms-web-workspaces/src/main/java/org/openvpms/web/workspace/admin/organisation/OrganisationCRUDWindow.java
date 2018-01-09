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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.organisation;

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.model.party.Party;
import org.openvpms.insurance.service.Changes;
import org.openvpms.insurance.service.InsuranceService;
import org.openvpms.insurance.service.InsuranceServices;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.workspace.ResultSetCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;

import static org.openvpms.archetype.rules.patient.insurance.InsuranceArchetypes.INSURANCE_SERVICES;

/**
 * CRUD window for the Organisation workspace.
 *
 * @author Tim Anderson
 */
public class OrganisationCRUDWindow extends ResultSetCRUDWindow<Entity> {

    /**
     * Synchronise insurers button identifier.
     */
    private static final String SYNCH_INSURERS = "button.syncInsurers";

    /**
     * Constructs a {@link OrganisationCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create instances of
     * @param query      the query. May be {@code null}
     * @param set        the result set. May be {@code null}
     * @param context    the context
     * @param help       the help context
     */
    public OrganisationCRUDWindow(Archetypes<Entity> archetypes, Query<Entity> query, ResultSet<Entity> set,
                                  Context context, HelpContext help) {
        super(archetypes, query, set, context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(SYNCH_INSURERS, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                synchroniseInsurers();
            }
        });
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
        buttons.setEnabled(SYNCH_INSURERS, enable && TypeHelper.isA(getObject(), INSURANCE_SERVICES));
    }

    /**
     * Synchronises insurers associated with an insurance service.
     */
    protected void synchroniseInsurers() {
        Entity object = getObject();
        if (TypeHelper.isA(object, INSURANCE_SERVICES)) {
            InsuranceServices insuranceServices = ServiceHelper.getBean(InsuranceServices.class);
            InsuranceService service = insuranceServices.getServiceForConfiguration(object);
            Changes<Party> changes = service.synchroniseInsurers();
            List<Changes.Change<Party>> list = changes.getChanges();
            if (list.isEmpty()) {
                InformationDialog.show(Messages.get("admin.organisation.insurer.sync.title"),
                                       Messages.format("admin.organisation.insurer.sync.nochanges",
                                                       object.getName()));
            } else {
                InsurerChanges popup = new InsurerChanges(list);
                popup.show();
            }
        }
    }

}
