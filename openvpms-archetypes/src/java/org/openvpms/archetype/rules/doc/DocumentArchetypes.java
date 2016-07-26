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

package org.openvpms.archetype.rules.doc;


/**
 * Document archetypes.
 *
 * @author Tim Anderson
 */
public class DocumentArchetypes {

    /**
     * The document template archetype short name.
     */
    public static final String DOCUMENT_TEMPLATE = "entity.documentTemplate";

    /**
     * The document template printer short name.
     */
    public static final String DOCUMENT_TEMPLATE_PRINTER = "entityRelationship.documentTemplatePrinter";

    /**
     * Document template act to link a template to a document.
     */
    public static final String DOCUMENT_TEMPLATE_ACT = "act.documentTemplate";

    /**
     * Document template participation.
     */
    public static final String DOCUMENT_TEMPLATE_PARTICIPATION = "participation.documentTemplate";

    /**
     * Document participation.
     */
    public static final String DOCUMENT_PARTICIPATION = "participation.document";

    /**
     * The default document archetype.
     */
    public static final String DEFAULT_DOCUMENT = "document.other";

    /**
     * The file name format lookup archetype.
     */
    public static final String FILE_NAME_FORMAT = "lookup.fileNameFormat";

    /**
     * Image document.
     */
    public static final String IMAGE_DOCUMENT = "document.image";

    /**
     * Text document.
     */
    public static final String TEXT_DOCUMENT = "document.text";

    /**
     * Logo document act.
     */
    public static final String LOGO_ACT = "act.documentLogo";

    /**
     * Logo participation.
     */
    public static final String LOGO_PARTICIPATION = "participation.logo";

    /**
     * System email template.
     */
    public static final String SYSTEM_EMAIL_TEMPLATE = "entity.documentTemplateEmailSystem";

    /**
     * User email template.
     */
    public static final String USER_EMAIL_TEMPLATE = "entity.documentTemplateEmailUser";

    /**
     * Appointment reminder SMS template.
     */
    public static final String APPOINTMENT_SMS_TEMPLATE = "entity.documentTemplateSMSAppointment";

    /**
     * Patient reminder SMS template.
     */
    public static final String REMINDER_SMS_TEMPLATE = "entity.documentTemplateSMSReminder";

    /**
     * Letterhead template.
     */
    public static final String LETTERHEAD = "entity.letterhead";
}
