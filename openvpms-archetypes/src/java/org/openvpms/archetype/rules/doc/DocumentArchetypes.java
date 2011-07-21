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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.archetype.rules.doc;


/**
 * Document archetypes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
     * The default document archetype.
     */
    public static final String DEFAULT_DOCUMENT = "document.other";

}
