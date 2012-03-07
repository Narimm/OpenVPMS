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

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;


/**
 * Base class for implementations that generate {@link Document}s using a
 * template.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class TemplatedReporter<T> extends Reporter<T> {

    /**
     * The document template entity to use. May be <tt>null</tt>
     */
    private DocumentTemplate template;

    /**
     * The document template locator.
     */
    private DocumentTemplateLocator locator;


    /**
     * Constructs a <tt>TemplatedReporter</tt> for a single object.
     *
     * @param object   the object
     * @param template the document template to use
     */
    public TemplatedReporter(T object, DocumentTemplate template) {
        super(object);
        this.template = template;
    }

    /**
     * Constructs a <tt>TemplatedReporter</tt> for a single object.
     *
     * @param object  the object
     * @param locator the document template locator
     */
    public TemplatedReporter(T object, DocumentTemplateLocator locator) {
        super(object);
        this.locator = locator;
    }

    /**
     * Constructs a <tt>TemplatedReporter</tt> to print a collection of objects.
     *
     * @param objects  the objects to print
     * @param template the document template to use
     */
    public TemplatedReporter(Iterable<T> objects, DocumentTemplate template) {
        super(objects);
        this.template = template;
    }

    /**
     * Constructs a <tt>TemplatedReporter</tt> to print a collection of objects.
     *
     * @param objects the objects to print
     * @param locator the document template locator
     */
    public TemplatedReporter(Iterable<T> objects, DocumentTemplateLocator locator) {
        super(objects);
        this.locator = locator;
    }

    /**
     * Returns the archetype short name that the template applies to.
     *
     * @return the archetype short name
     */
    public String getShortName() {
        return locator.getShortName();
    }

    /**
     * Returns the document template.
     *
     * @return the document template, or <tt>null</tt> if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public DocumentTemplate getTemplate() {
        if (template == null && locator != null) {
            template = locator.getTemplate();
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
        DocumentTemplate template = getTemplate();
        return (template != null) ? template.getDocument() : null;
    }

    /**
     * Registers the document template.
     *
     * @param template the template to use. May be <tt>null</tt>
     */
    protected void setTemplate(DocumentTemplate template) {
        this.template = template;
    }
}
