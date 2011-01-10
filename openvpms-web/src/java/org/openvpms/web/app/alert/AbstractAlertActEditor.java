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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.app.alert;

import nextapp.echo2.app.TextField;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.util.TextComponentFactory;
import org.openvpms.web.system.ServiceHelper;


/**
 * An editor for <em>act.customerAlert</em> and <em>act.patientAlert</em> acts.
 * <p/>
 * This includes a field to display the selected alert type's priority and colour.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public abstract class AbstractAlertActEditor extends AbstractActEditor {

    /**
     * The alert type lookup archetype.
     */
    private final String alertTypeShortName;

    /**
     * The field to display the alert type priority.
     */
    private final TextField priority;


    /**
     * Constructs an <tt>AbstractAlertActEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context. May be <tt>null</tt>
     * @param alertType the alert type archetype short name
     */
    public AbstractAlertActEditor(Act act, IMObject parent, LayoutContext context,
                                  String alertType) {
        super(act, parent, context);
        alertTypeShortName = alertType;
        priority = TextComponentFactory.create();
        getProperty("alertType").addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onAlertTypeChanged();
            }
        });
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new AlertLayoutStrategy(priority);
    }

    /**
     * Invoked when the alert type changes. Updates the priority field and colour.
     */
    private void onAlertTypeChanged() {
        Object value = getProperty("alertType").getValue();
        String code = (value != null) ? value.toString() : null;
        ILookupService service = ServiceHelper.getLookupService();
        Lookup alertType = service.getLookup(alertTypeShortName, code);
        AlertHelper.setPriority(priority, alertType);
    }
    
}
