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

package org.openvpms.web.workspace.alert;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.alert.Alert;
import org.openvpms.web.component.alert.AlertManager;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.system.ServiceHelper;


/**
 * An editor for <em>act.customerAlert</em> and <em>act.patientAlert</em> acts.
 *
 * @author Tim Anderson
 */
public abstract class AbstractAlertActEditor extends AbstractActEditor {

    /**
     * Constructs an {@link AbstractAlertActEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context. May be {@code null}
     */
    public AbstractAlertActEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
    }

    /**
     * Returns the alert type.
     *
     * @return the alert type. May be {@code null}
     */
    public abstract IMObject getAlertType();

    /**
     * Acknowledges the alert if it is mandatory.
     * <p>
     * This prevents the user from having to deal with a mandatory alert popup after editing the alert
     */
    protected void acknowledgeAlert() {
        IMObject alertType = getAlertType();
        if (alertType != null) {
            Alert alert = new Alert(alertType, getObject());
            if (alert.isMandatory()) {
                AlertManager manager = ServiceHelper.getBean(AlertManager.class);
                manager.acknowledge(alert);
            }
        }
    }
}
