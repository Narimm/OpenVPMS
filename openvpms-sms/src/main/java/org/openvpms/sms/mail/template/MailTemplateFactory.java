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
 * Factory for creating {@link MailTemplate} instances from <em>entity.SMSConfigEmail*</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class MailTemplateFactory {

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * Nodes which may not appear as variables.
     */
    private static final String[] RESERVED = {"name", "description", "website", "from", "fromExpression", "to",
                                              "toExpression", "replyTo", "replyToExpression", "subject",
                                              "subjectExpression", "text", "textExpression", "phone", "message"};

    /**
     * Constructs a <tt>MailTemplatePopulator</tt>.
     *
     * @param service the service
     */
    public MailTemplateFactory(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Populates a template from an <em>entity.SMSEmail*</em> configuration.
     *
     * @param config the SMS configuration
     * @return the template
     */
    public MailTemplate getTemplate(Entity config) {
        IMObjectBean configBean = new IMObjectBean(config, service);
        ArchetypeDescriptor archetype = service.getArchetypeDescriptor(config.getArchetypeId());
        MailTemplate result = new MailTemplate();
        result.setCountryPrefix(getString(configBean, "countryPrefix"));
        result.setAreaPrefix(getString(configBean, "areaPrefix"));
        result.setFrom(getString(configBean, "from"));
        result.setFromExpression(getString(configBean, "fromExpression"));
        result.setTo(getString(configBean, "to"));
        result.setToExpression(getString(configBean, "toExpression"));
        result.setReplyTo(getString(configBean, "replyTo"));
        result.setReplyToExpression(getString(configBean, "replyToExpression"));
        result.setSubject(getString(configBean, "subject"));
        result.setSubjectExpression(getString(configBean, "subjectExpression"));
        result.setText(getString(configBean, "text"));
        result.setTextExpression(getString(configBean, "textExpression"));
        for (NodeDescriptor descriptor : archetype.getNodeDescriptorsAsArray()) {
            if (String.class.getName().equals(descriptor.getType())
                && !ArrayUtils.contains(RESERVED, descriptor.getName())) {
                Object value = descriptor.getValue(config);
                result.addVariable(descriptor.getName(), value != null ? value.toString() : null);
            }
        }
        return result;
    }

    /**
     * Helper to returned the named string from the bean, if it exists.
     *
     * @param bean the bean
     * @param name the node name
     * @return the string value. May be <tt>null</tt>
     */
    private String getString(IMObjectBean bean, String name) {
        String result = null;
        if (bean.hasNode(name)) {
            result = StringUtils.trimToNull(bean.getString(name));
        }
        return result;
    }

}
