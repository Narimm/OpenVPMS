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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.supplier;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextMailContext;
import org.openvpms.web.component.im.contact.ContactHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.MultiSelectBrowser;
import org.openvpms.web.component.im.query.MultiSelectTableBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.supplier.document.SupplierDocumentQuery;

import java.util.List;


/**
 * An {@link MailContext} that uses an {@link Context} to returns 'from' addresses from the practice location and
 * practice, and 'to' addresses from the current supplier.
 *
 * @author Tim Anderson
 */
public class SupplierMailContext extends ContextMailContext {

    /**
     * Constructs a {@code SupplierMailContext}
     *
     * @param context the context
     * @param help    the help context
     */
    public SupplierMailContext(Context context, final HelpContext help) {
        super(context);
        setAttachmentBrowserFactory(mailContext -> {
            MultiSelectBrowser<Act> browser = null;
            Party supplier = getContext().getSupplier();
            if (supplier != null) {
                Query<Act> query = new SupplierDocumentQuery<>(supplier);
                DefaultLayoutContext layout = new DefaultLayoutContext(getContext(), help);
                browser = new MultiSelectTableBrowser<>(query, layout);
            }
            return browser;
        });
    }

    /**
     * Returns the available ''to' email addresses.
     *
     * @return the 'to' email addresses
     */
    public List<Contact> getToAddresses() {
        return ContactHelper.getEmailContacts(getContext().getSupplier());
    }

}