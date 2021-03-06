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

package org.openvpms.web.workspace.summary;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.alert.Alert;
import org.openvpms.web.component.alert.AlertManager;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;


/**
 * Creates summary components for a given party.
 *
 * @author Tim Anderson
 */
public abstract class PartySummary {

    /**
     * The context.
     */
    private final Context context;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * The user preferences.
     */
    private final Preferences preferences;

    /**
     * Constructs a {@code PartySummary}.
     *
     * @param context     the context
     * @param help        the help context
     * @param preferences user preferences
     */
    public PartySummary(Context context, HelpContext help, Preferences preferences) {
        this.context = context;
        this.help = help;
        this.preferences = preferences;
    }

    /**
     * Returns summary information for a party.
     * <p>
     * The summary includes any alerts.
     *
     * @param party the party. May be {@code null}
     * @return a summary component, or {@code null} if there is no summary
     */
    public Component getSummary(Party party) {
        Component result = null;
        if (party != null) {
            List<Alert> alerts = ServiceHelper.getBean(AlertManager.class).getAlerts(party);
            result = createSummary(party, alerts);
        }
        return result;
    }

    /**
     * Returns the context.
     *
     * @return the context
     */
    protected Context getContext() {
        return context;
    }

    /**
     * Returns the help context.
     *
     * @return the context
     */
    protected HelpContext getHelpContext() {
        return help;
    }

    /**
     * Returns the user preferences.
     *
     * @return the preferences
     */
    protected Preferences getPreferences() {
        return preferences;
    }

    /**
     * Returns summary information for a party.
     * <p>
     * The summary includes any alerts.
     *
     * @param party  the party
     * @param alerts the alerts
     * @return a summary component
     */
    protected abstract Component createSummary(Party party, List<Alert> alerts);

    /**
     * Helper to create a formatted label.
     *
     * @param key       the key of the message to be returned
     * @param arguments an array of arguments to be inserted into the message
     * @return the label
     */
    protected Label createLabel(String key, Object... arguments) {
        String text = Messages.format(key, arguments);
        Label label = LabelFactory.create();
        label.setText(text);
        return label;
    }
}
