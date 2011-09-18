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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

/**
 * Abstract implementation of {@link MailTemplateConfig} that generates the template from an <em>entity.SMSEmail*</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public abstract class AbstractMailTemplateConfig implements MailTemplateConfig {

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * Nodes which may not appear as variables.
     */
    private static final String[] RESERVED = {"name", "description", "website", "from", "to", "subject", "text",
                                              "phone", "message"};

    /**
     * Constructs a <tt>AbstractMailTemplateConfig</tt>.
     *
     * @param service the service
     */
    public AbstractMailTemplateConfig(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Populates a template from an <em>entity.SMSEmail*</em> configuration.
     *
     * @param config the SMS configuration
     * @return the template
     */
    protected MailTemplate getTemplate(Entity config) {
        IMObjectBean configBean = new IMObjectBean(config, service);
        ArchetypeDescriptor archetype = service.getArchetypeDescriptor(config.getArchetypeId());
        String country = StringUtils.trimToNull(configBean.getString("country"));
        String trunkPrefix = StringUtils.trimToNull(configBean.getString("trunkPrefix"));
        String from = StringUtils.trimToNull(configBean.getString("from"));
        String to = StringUtils.trimToNull(configBean.getString("to"));
        String subject = StringUtils.trimToNull(configBean.getString("subject"));
        String text = StringUtils.trimToNull(configBean.getString("text"));
        MailTemplate result = new MailTemplate(country, trunkPrefix, from, to, subject, text);
        for (NodeDescriptor descriptor : archetype.getNodeDescriptorsAsArray()) {
            if (!descriptor.isDerived() && String.class.getName().equals(descriptor.getType())
                && !ArrayUtils.contains(RESERVED, descriptor.getName())) {
                Object value = descriptor.getValue(config);
                result.addVariable(descriptor.getName(), value != null ? value.toString() : null);
            }
        }
        return result;
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }
}
