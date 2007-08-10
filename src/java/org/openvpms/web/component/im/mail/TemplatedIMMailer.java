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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.mail;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.report.TemplatedReporter;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class TemplatedIMMailer<T> extends AbstractIMMailer<T> {

    /**
     * Constructs a new <tt>TemplatedIMMailer</tt>.
     *
     * @param reporter the reporter
     */
    public TemplatedIMMailer(TemplatedReporter<T> reporter) {
        super(reporter);
        Entity template = getTemplate();
        if (template != null) {
            IMObjectBean bean = new IMObjectBean(template);
            String subject = bean.getString("emailSubject");
            if (StringUtils.isEmpty(subject)) {
                subject = template.getName();
            }
            String body = bean.getString("emailText");
            setSubject(subject);
            setBody(body);
        }
    }

    /**
     * Returns the reporter.
     *
     * @return the reporter
     */
    @Override
    protected TemplatedReporter<T> getReporter() {
        return (TemplatedReporter<T>) super.getReporter();
    }

    /**
     * Returns the document template entity.
     *
     * @return the document template, or <tt>null</tt> if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected Entity getTemplate() {
        return getReporter().getTemplate();
    }
}
