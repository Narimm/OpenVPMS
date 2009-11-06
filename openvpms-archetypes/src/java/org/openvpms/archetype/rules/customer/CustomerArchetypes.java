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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.customer;


/**
 * Customer archetypes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerArchetypes {

    /**
     * Customer short name.
     */
    public static final String PERSON = "party.customerperson";

    /**
     * Organisation customer short name.
     * @deprecated use {@link #PERSON} instead.
     */
    @Deprecated
    public static final String ORGANISATION = PERSON;

    /**
     * 'Over the counter' short name.
     */
    public static final String OTC = "party.organisationOTC";

    /**
     * Customer participation short name.
     */
    public static final String CUSTOMER_PARTICIPATION
            = "participation.customer";

    /**
     * Customer document attachment act short name.
     */
    public static final String DOCUMENT_ATTACHMENT = "act.customerDocumentAttachment";

    /**
     * Customer document attachment version act short name.
     */
    public static final String DOCUMENT_ATTACHMENT_VERSION = "act.customerDocumentAttachmentVersion";

    /**
     * Customer document form act short name.
     */
    public static final String DOCUMENT_FORM = "act.customerDocumentForm";

    /**
     * Customer document letter act short name.
     */
    public static final String DOCUMENT_LETTER = "act.customerDocumentLetter";

    /**
     * Customer document letter version act short name.
     */
    public static final String DOCUMENT_LETTER_VERSION = "act.customerDocumentLetterVersion";
}
