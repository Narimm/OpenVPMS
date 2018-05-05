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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.sms.mail.template;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.sms.SMSException;
import org.openvpms.sms.i18n.SMSMessages;
import org.openvpms.sms.mail.SMSArchetypes;

import javax.annotation.PreDestroy;


/**
 * An {@link MailTemplateConfig} that obtains the template from the <em>party.organisationPractice</em>.
 * <p>
 * The practice must have an <em>entity.SMSemail*</em> associated with it.
 *
 * @author Tim Anderson
 */
public class PracticeMailTemplateConfig implements MailTemplateConfig {

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * The practice.
     */
    private Party practice;

    /**
     * The version of the practice.
     */
    private long version;

    /**
     * The mail template.
     */
    private MailTemplate template;

    /**
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * Listener for archetype service events.
     */
    private final IArchetypeServiceListener listener;


    /**
     * Constructs a {@link PracticeMailTemplateConfig}.
     *
     * @param service         the archetype service
     * @param practiceService the practice service
     */
    public PracticeMailTemplateConfig(IArchetypeService service, PracticeService practiceService) {
        this.service = service;
        this.practiceService = practiceService;
        listener = new AbstractArchetypeServiceListener() {

            public void saved(IMObject object) {
                clear();
            }

            public void removed(IMObject object) {
                clear();
            }
        };

        service.addListener(SMSArchetypes.EMAIL_CONFIGURATIONS, listener);
    }

    /**
     * Returns the email template.
     *
     * @return the email template
     */
    public synchronized MailTemplate getTemplate() {
        if (practiceUpdated() || template == null) {
            Entity config = practiceService.getSMS();
            if (TypeHelper.isA(config, SMSArchetypes.EMAIL_CONFIGURATIONS)) {
                template = new MailTemplateFactory(service).getTemplate(config);
            } else {
                throw new SMSException(SMSMessages.SMSNotConfigured(practice));
            }
        }
        return template;
    }

    /**
     * Disposes of this config.
     */
    @PreDestroy
    public void dispose() {
        service.removeListener(SMSArchetypes.EMAIL_CONFIGURATIONS, listener);
    }

    /**
     * Resets the template, to force it to be reloaded.
     */
    private synchronized void clear() {
        template = null;
    }

    /**
     * Determines if the practice has been updated.
     *
     * @return {@code true} if the practice has been updated
     * @throws SMSException if the practice is not configured
     */
    private boolean practiceUpdated() {
        boolean updated = false;
        Party current = practiceService.getPractice();
        if (current == null) {
            throw new SMSException(SMSMessages.practiceNotFound());
        }
        if (practice == null || !ObjectUtils.equals(current, practice)
            || current.getVersion() > version) {
            practice = current;
            version = practice.getVersion();
            updated = true;
        }
        return updated;
    }
}
