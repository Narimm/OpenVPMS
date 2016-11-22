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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.DoseManager;

/**
 * Context information shared between charge item editors.
 *
 * @author Tim Anderson
 */
public class ChargeEditContext extends PriceActEditContext {

    /**
     * The editor queue, used to manage dialogs.
     */
    private EditorQueue editorQueue;

    /**
     * Constructs a {@link ChargeEditContext}.
     *
     * @param customer the customer
     * @param location the practice location. May be {@code null}
     * @param context  the layout context
     */
    public ChargeEditContext(Party customer, Party location, LayoutContext context) {
        super(customer, location, context);
        ProductRules rules = new ProductRules(getCachingArchetypeService(), ServiceHelper.getLookupService());
        setDoseManager(new DoseManager(ServiceHelper.getBean(PatientRules.class), rules));
        editorQueue = new DefaultEditorQueue(context.getContext());
    }

    /**
     * Registers the editor queue.
     *
     * @param editorQueue the editor queue. May be {@code null}
     */
    public void setEditorQueue(EditorQueue editorQueue) {
        this.editorQueue = editorQueue;
    }

    /**
     * Returns the editor queue.
     *
     * @return the editor queue. May be {@code null}
     */
    public EditorQueue getEditorQueue() {
        return editorQueue;
    }
}
