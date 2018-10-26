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

package org.openvpms.web.component.alert;

import nextapp.echo2.app.event.WindowPaneEvent;
import org.apache.commons.lang.mutable.MutableInt;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;

/**
 * Helper to display unacknowledged mandatory alerts for a customer or patient.
 *
 * @author Tim Anderson
 */
public class MandatoryAlerts {

    /**
     * The alert manager.
     */
    private final AlertManager manager;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The help context.
     */
    private final HelpContext help;


    /**
     * Constructs a {@link MandatoryAlerts}.
     *
     * @param context the context
     * @param help    the help context
     */
    public MandatoryAlerts(Context context, HelpContext help) {
        manager = ServiceHelper.getBean(AlertManager.class);
        this.context = context;
        this.help = help;
    }

    /**
     * Show mandatory alerts for a party.
     *
     * @param party the party. A customer or patient. May be {@code null}
     */
    public void show(Party party) {
        show(party, null);
    }

    /**
     * Show mandatory alerts for a party.
     *
     * @param party    the party. A customer or patient. May be {@code null}
     * @param callback a callback to invoke when all of the alerts have been acknowledged. May be {@code null}
     */
    public void show(Party party, Runnable callback) {
        if (party != null && !party.isNew() && party.isA(CustomerArchetypes.PERSON, PatientArchetypes.PATIENT)) {
            List<Alert> alerts = manager.getMandatoryAlerts(party);
            if (alerts.isEmpty()) {
                if (callback != null) {
                    callback.run();
                }
            } else {
                MutableInt count = new MutableInt(0);
                for (Alert alert : alerts) {
                    AlertDialog dialog = new AlertDialog(alert, context, help);
                    dialog.addWindowPaneListener(new WindowPaneListener() {
                        @Override
                        public void onClose(WindowPaneEvent event) {
                            manager.acknowledge(alert);
                            count.increment();
                            if (callback != null && count.intValue() == alerts.size()) {
                                callback.run();
                            }
                        }
                    });
                    dialog.show();
                }
            }
        } else if (callback != null) {
            callback.run();
        }
    }
}
