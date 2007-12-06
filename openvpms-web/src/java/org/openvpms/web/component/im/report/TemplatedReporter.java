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

package org.openvpms.web.component.im.report;

import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.exception.OpenVPMSException;


/**
 * Base class for implementations that generate {@link Document}s using a
 * template.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class TemplatedReporter<T> extends Reporter<T> {

    /**
     * The archetype short name to determine which template to use.
     */
    private final String shortName;

    /**
     * The document template entity to use. May be <tt>null</tt>
     */
    private Entity template;


    /**
     * Constructs a new <tt>TemplatedReporter</tt> for a single object.
     *
     * @param object    the object
     * @param shortName the archetype short name to determine the template to
     *                  use
     */
    public TemplatedReporter(T object, String shortName) {
        this(object, shortName, null);
    }

    /**
     * Constructs a new <tt>TemplatedReporter</tt> for a single object.
     *
     * @param object    the object
     * @param shortName the archetype short name to determine the template to
     *                  use
     * @param template  the document template to use. May be <tt>null</tt>
     */
    public TemplatedReporter(T object, String shortName, Entity template) {
        super(object);
        this.shortName = shortName;
        this.template = template;
    }

    /**
     * Constructs a new <tt>TemplatedReporter</tt> to print a collection of
     * objects.
     *
     * @param objects   the objects to print
     * @param shortName the archetype short name to determine the template to
     *                  use
     * @throws OpenVPMSException for any error
     */
    public TemplatedReporter(Iterable<T> objects, String shortName) {
        this(objects, shortName, null);
    }

    /**
     * Constructs a new <tt>TemplatedReporter</tt> to print a collection of
     * objects.
     *
     * @param objects   the objects to print
     * @param shortName the archetype short name to determine the template to
     *                  use
     * @param template  the document template to use. May be <tt>null</tt>
     */
    public TemplatedReporter(Iterable<T> objects, String shortName,
                             Entity template) {
        super(objects);
        this.shortName = shortName;
        this.template = template;
    }

    /**
     * Returns archetype short name to determine which template to use.
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Returns the document template entity.
     *
     * @return the document template, or <tt>null</tt> if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTemplate() {
        if (template == null) {
            TemplateHelper helper = new TemplateHelper();
            template = helper.getTemplateForArchetype(shortName);
        }
        return template;
    }

    /**
     * Returns the document template associated with the template entity.
     *
     * @return the document, or <tt>null</tt> if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Document getTemplateDocument() {
        Entity template = getTemplate();
        if (template != null) {
            TemplateHelper helper = new TemplateHelper();
            return helper.getDocumentFromTemplate(template);
        }
        return null;
    }

}
