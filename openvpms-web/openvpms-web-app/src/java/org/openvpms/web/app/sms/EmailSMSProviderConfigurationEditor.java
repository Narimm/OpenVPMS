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

package org.openvpms.web.app.sms;

import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.system.ServiceHelper;


/**
 * Editor for <em>entity.SMSEmail*</em> provider configurations.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class EmailSMSProviderConfigurationEditor extends AbstractIMObjectEditor {

    /**
     * The sample text message viewer.
     */
    private EmailSMSSampler sampler;


    /**
     * Constructs an <tt>EmailSMSProviderConfigurationEditor</tt>.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be <tt>null</tt>
     * @param layoutContext the layout context. May be <tt>null</tt>.
     */
    public EmailSMSProviderConfigurationEditor(Entity object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);

        sampler = new EmailSMSSampler(object);

        if (object.isNew()) {
            // default the from address to that of the practice, if it has one
            Property from = getProperty("from");
            if (from != null && from.getValue() == null) {
                Party practice = layoutContext.getContext().getPractice();
                if (practice != null) {
                    PartyRules rules = new PartyRules(ServiceHelper.getArchetypeService());
                    Contact email = rules.getContact(practice, ContactArchetypes.EMAIL, null);
                    if (email != null) {
                        IMObjectBean bean = new IMObjectBean(email);
                        from.setValue(bean.getString("emailAddress"));
                    }
                }
            }
        }
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        IMObjectLayoutStrategy strategy = super.createLayoutStrategy();
        if (strategy instanceof SMSConfigEmailLayoutStrategy) {
            ((SMSConfigEmailLayoutStrategy) strategy).setSampler(sampler);
        }
        return strategy;
    }

    /**
     * Invoked when an object is modified.
     * <p/>
     * This implementation refreshes the sampler
     *
     * @param modifiable the modified object
     */
    @Override
    protected void onModified(Modifiable modifiable) {
        super.onModified(modifiable);
        sampler.refresh();
    }

}
