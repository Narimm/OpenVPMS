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

package org.openvpms.web.component.mail;

import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.web.component.im.layout.LayoutContext;

/**
 * Factory for {@link MailDialog}s.
 *
 * @author Tim Anderson
 */
public class MailDialogFactory {

    /**
     * Constructs a {@link MailDialog}.
     *
     * @param mailContext the mail context
     * @param context     the layout context
     */
    public MailDialog create(MailContext mailContext, LayoutContext context) {
        return create(mailContext, null, context);
    }

    /**
     * Constructs a {@link MailDialog}.
     *
     * @param mailContext the mail context
     * @param preferred   the preferred contact. May be {@code null}
     * @param context     the layout context
     */
    public MailDialog create(MailContext mailContext, Contact preferred, LayoutContext context) {
        return new MailDialog(mailContext, preferred, context);
    }

}
