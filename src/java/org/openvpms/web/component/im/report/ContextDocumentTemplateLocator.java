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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.component.app.Context;


/**
 * A {@link DocumentTemplateLocator} that locates templates based on a {@link Context}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class ContextDocumentTemplateLocator extends LocationDocumentTemplateLocator {

    /**
     * The document template. May be <tt>null</tt>
     */
    private final DocumentTemplate template;


    /**
     * Constructs a <tt>DefaultDocumentTemplateLocator</tt>.
     *
     * @param object  the object to locate a template for
     * @param context the context to use
     */
    public ContextDocumentTemplateLocator(IMObject object, Context context) {
        this(object.getArchetypeId().getShortName(), context);
    }

    /**
     * Constructs a <tt>DefaultDocumentTemplateLocator</tt>.
     *
     * @param shortName the archetype short name that the document template applies to
     * @param context   the context to use
     */
    public ContextDocumentTemplateLocator(String shortName, Context context) {
        this(null, shortName, context);
    }

    /**
     * Constructs a <tt>DefaultDocumentTemplateLocator</tt>.
     * <p/>
     * This will use the supplied template if non-null, falling back to that associated with the context if it is.
     *
     * @param template  the template. May be <tt>null</tt>
     * @param object the object
     * @param context   the context to use
     */
    public ContextDocumentTemplateLocator(DocumentTemplate template, IMObject object, Context context) {
        this(template, object.getArchetypeId().getShortName(), context);
    }

    /**
     * Constructs a <tt>DefaultDocumentTemplateLocator</tt>.
     * <p/>
     * This will use the supplied template if non-null, falling back to that associated with the context if it is.
     *
     * @param template  the template. May be <tt>null</tt>
     * @param shortName the archetype short name that the document template applies to
     * @param context   the context to use
     */
    public ContextDocumentTemplateLocator(DocumentTemplate template, String shortName, Context context) {
        super(shortName, context.getLocation(), context.getPractice());
        this.template = template;
    }

    /**
     * Returns the document template.
     *
     * @return the document template, or <tt>null</tt> if the template cannot be located
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public DocumentTemplate getTemplate() {
        if (template != null) {
            return template;
        }
        return super.getTemplate();
    }
}
