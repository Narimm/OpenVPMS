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

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.report.IMObjectReporter;


/**
 * IMObject report mailer.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectReportMailer<T extends IMObject>
        extends TemplatedIMMailer<T> {

    /**
     * Constructs an <tt>IMObjectReportMailer</tt>.
     *
     * @param object   the object to mail
     * @param template the document template to use. May be <tt>null</tt>
     * @throws OpenVPMSException for any error
     */
    public IMObjectReportMailer(T object, DocumentTemplate template) {
        super(new IMObjectReporter<T>(object, template));
    }


}
