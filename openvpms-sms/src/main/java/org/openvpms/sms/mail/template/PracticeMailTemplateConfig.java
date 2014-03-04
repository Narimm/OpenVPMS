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

package org.openvpms.sms.mail.template;

import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.sms.SMSException;
import org.openvpms.sms.i18n.SMSMessages;
import org.openvpms.sms.mail.SMSArchetypes;

import javax.annotation.PreDestroy;


/**
 * An {@link MailTemplateConfig} that obtains the template from the <em>party.organisationPractice</em>.
 * <p/>
 * The practice must have an <em>entity.SMSemail*</em> associated with it.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class PracticeMailTemplateConfig implements MailTemplateConfig {

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * The mail template.
     */
    private MailTemplate template;

    /**
     * The practice rules.
     */
    private final PracticeRules rules;

    /**
     * Listener for archetype service events.
     */
    private final IArchetypeServiceListener listener;


    /**
     * Constructs a <tt>PracticeMailTemplateConfig</tt>.
     *
     * @param service the archetype service
     * @param rules   the practice rules
     */
    public PracticeMailTemplateConfig(IArchetypeService service, PracticeRules rules) {
        this.service = service;
        this.rules = rules;
        listener = new AbstractArchetypeServiceListener() {
            public void saved(IMObject object) {
                clear();
            }

            public void removed(IMObject object) {
                clear();
            }
        };

        service.addListener(PracticeArchetypes.PRACTICE, listener);
        service.addListener(SMSArchetypes.EMAIL_CONFIGURATIONS, listener);
    }

    /**
     * Returns the email template.
     *
     * @return the email template
     */
    public synchronized MailTemplate getTemplate() {
        if (template == null) {
            Party practice = rules.getPractice();
            if (practice != null) {
                EntityBean bean = new EntityBean(practice, service);
                Entity config = bean.getTargetEntity(SMSArchetypes.PRACTICE_SMS_CONFIGURATION);
                if (TypeHelper.isA(config, SMSArchetypes.EMAIL_CONFIGURATIONS)) {
                    template = new MailTemplateFactory(service).getTemplate(config);
                } else {
                    throw new SMSException(SMSMessages.SMSNotConfigured(practice));
                }
            } else {
                throw new SMSException(SMSMessages.practiceNotFound());
            }
        }
        return template;
    }

    /**
     * Disposes of this config.
     */
    @PreDestroy
    public void dispose() {
        service.removeListener(PracticeArchetypes.PRACTICE, listener);
        service.removeListener(SMSArchetypes.EMAIL_CONFIGURATIONS, listener);
    }

    /**
     * Resets the template, to force it to be reloaded.
     */
    private synchronized void clear() {
        template = null;
    }

}
