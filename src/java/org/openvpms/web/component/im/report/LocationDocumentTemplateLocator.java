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

package org.openvpms.web.component.im.report;

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;


/**
 * An {@link DocumentTemplateLocator} that resolves document templates associated with an organisation location
 * or practice.
 * <p/>
 * The algorithm for template selection is:
 * <ol>
 * <li>if a location is specified, and it has a template for the given archetype, use it; otherwise
 * <li>if a practice is specified, and it has a template for the given archetype, use it; otherwise
 * <li>use the first available template for the archetype, if any
 * </ol>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class LocationDocumentTemplateLocator implements DocumentTemplateLocator {

    /**
     * The archetype short name that the document template applies to.
     */
    private final String shortName;

    /**
     * An <em>party.organisationLocation</em>. May be <tt>null</tt>
     */
    private final Party location;

    /**
     * An <em>party.organisationPractice</em>. May be <tt>null</tt>
     */
    private final Party practice;


    /**
     * Constructs an <em>LocationDocumentTemplateLocator</tt>.
     *
     * @param shortName the archetype short name that the document template applies to
     * @param location  the practice location. May be <tt>null</tt>
     * @param practice  the practice. May be <tt>null</tt>
     */
    public LocationDocumentTemplateLocator(String shortName, Party location, Party practice) {
        this.shortName = shortName;
        this.location = location;
        this.practice = practice;
    }

    /**
     * Returns the document template.
     * <p/>
     * This implementation looks first at the practice location for a match. If there is no practice location,
     * or no match, it then looks at the practice. If there is no practice or no match that it returns the first
     * template for the archetype short name.
     *
     * @return the document template, or <tt>null</tt> if the template cannot be located
     * @throws ArchetypeServiceException for any archetype service error
     */
    public DocumentTemplate getTemplate() {
        TemplateHelper helper = new TemplateHelper();
        DocumentTemplate result = null;
        if (location != null) {
            result = helper.getDocumentTemplate(shortName, location);
        }
        if (result == null && practice != null) {
            result = helper.getDocumentTemplate(shortName, practice);
        }
        if (result == null) {
            result = helper.getDocumentTemplate(shortName);
        }
        return result;
    }

    /**
     * Returns the archetype short name that the template applies to.
     *
     * @return the archetype short name
     */
    public String getShortName() {
        return shortName;
    }
}
