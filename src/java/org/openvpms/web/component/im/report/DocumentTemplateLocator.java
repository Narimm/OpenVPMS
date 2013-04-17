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
 */

package org.openvpms.web.component.im.report;

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;


/**
 * Locates an <em>entity.documentTemplate</em>.
 *
 * @author Tim Anderson
 */
public interface DocumentTemplateLocator {

    /**
     * Returns the document template.
     *
     * @return the document template, or {@code null} if the template cannot be located
     * @throws ArchetypeServiceException for any archetype service error
     */
    DocumentTemplate getTemplate();

    /**
     * Returns the archetype short name that the template applies to.
     *
     * @return the archetype short name
     */
    String getShortName();
}