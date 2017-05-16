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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;

/**
 * An editor for <em>actRelationship.invoiceItemAlert</em> collections.
 * <p/>
 * This is used to monitor alerts associated with an invoice.
 *
 * @author Tim Anderson
 */
public class AlertActRelationshipCollectionEditor extends ActRelationshipCollectionEditor {

    /**
     * The alerts.
     */
    private Alerts alerts;

    /**
     * Constructs a {@link AlertActRelationshipCollectionEditor}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public AlertActRelationshipCollectionEditor(CollectionProperty property, Act act, LayoutContext context) {
        super(property, act, context);
        setExcludeDefaultValueObject(false);
    }

    /**
     * Registers the alerts.
     *
     * @param alerts the alerts. May be {@code null}
     */
    public void setAlerts(Alerts alerts) {
        this.alerts = alerts;
        if (alerts != null) {
            for (Act alert : getCurrentActs()) {
                alerts.add(alert);
            }
        }
    }

    /**
     * Adds an object to the collection, if it doesn't exist.
     *
     * @param object the object to add
     * @return {@code true} if the object was added, otherwise {@code false}
     */
    @Override
    public boolean add(IMObject object) {
        boolean result = super.add(object);
        if (alerts != null) {
            alerts.add((Act) object);
        }
        return result;
    }

    /**
     * Removes an object from the collection.
     *
     * @param object the object to remove
     */
    @Override
    public void remove(IMObject object) {
        super.remove(object);
        if (alerts != null) {
            alerts.remove((Act) object);
        }
    }

}
