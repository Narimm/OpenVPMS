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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.report;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.report.ReportException;
import org.openvpms.web.component.im.doc.FileNameFormatter;

import java.util.Iterator;
import java.util.Map;

import static org.openvpms.report.ReportException.ErrorCode.NoTemplateForArchetype;


/**
 * Base class for implementations that generate {@link Document}s using a
 * template.
 *
 * @author Tim Anderson
 */
public abstract class TemplatedReporter<T> extends Reporter<T> {

    /**
     * The document template entity to use. May be {@code null}
     */
    private DocumentTemplate template;

    /**
     * The document template locator.
     */
    private DocumentTemplateLocator locator;

    /**
     * The file name formatter.
     */
    private final FileNameFormatter formatter;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * Cache of template names, keyed on template archetype short name.
     */
    private Map<String, String> templateNames;


    /**
     * Constructs a {@link TemplatedReporter} for a single object.
     *
     * @param object    the object
     * @param template  the document template to use
     * @param formatter the file name formatter
     * @param service   the archetype service
     * @param lookups   the lookup service
     */
    public TemplatedReporter(T object, DocumentTemplate template, FileNameFormatter formatter,
                             IArchetypeService service, ILookupService lookups) {
        super(object);
        this.template = template;
        this.formatter = formatter;
        this.service = service;
        this.lookups = lookups;
    }

    /**
     * Constructs a {@link TemplatedReporter} for a single object.
     *
     * @param object    the object
     * @param locator   the document template locator
     * @param formatter the file name formatter
     * @param service   the archetype service
     * @param lookups   the lookup service
     */
    public TemplatedReporter(T object, DocumentTemplateLocator locator, FileNameFormatter formatter,
                             IArchetypeService service, ILookupService lookups) {
        super(object);
        this.locator = locator;
        this.formatter = formatter;
        this.service = service;
        this.lookups = lookups;
    }

    /**
     * Constructs a {@link TemplatedReporter} for a collection of objects.
     *
     * @param objects   the objects
     * @param template  the document template to use
     * @param formatter the file name formatter
     * @param service   the archetype service
     * @param lookups   the lookup service
     */
    public TemplatedReporter(Iterable<T> objects, DocumentTemplate template, FileNameFormatter formatter,
                             IArchetypeService service, ILookupService lookups) {
        super(objects);
        this.template = template;
        this.formatter = formatter;
        this.service = service;
        this.lookups = lookups;
    }

    /**
     * Constructs a {@link TemplatedReporter} for a collection of objects.
     *
     * @param objects   the objects
     * @param locator   the document template locator
     * @param formatter the file name formatter
     * @param service   the archetype service
     * @param lookups   the lookup service
     */
    public TemplatedReporter(Iterable<T> objects, DocumentTemplateLocator locator, FileNameFormatter formatter,
                             IArchetypeService service, ILookupService lookups) {
        super(objects);
        this.locator = locator;
        this.formatter = formatter;
        this.service = service;
        this.lookups = lookups;
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
     * Returns a display name for the objects being reported on.
     *
     * @return a display name for the objects being printed
     */
    public String getDisplayName() {
        String result = null;
        DocumentTemplate template = getTemplate();
        if (template != null) {
            result = template.getName();
        }
        if (StringUtils.isEmpty(result)) {
            if (templateNames == null) {
                templateNames = LookupHelper.getNames(service, lookups, DocumentArchetypes.DOCUMENT_TEMPLATE,
                                                      "archetype");
            }
            String shortName = getShortName();
            result = templateNames.get(shortName);
            if (result == null) {
                result = DescriptorHelper.getDisplayName(shortName, service);
            }
        }
        return result;
    }

    /**
     * Returns the document template.
     *
     * @return the document template, or {@code null} if none can be found
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
     * @return the document
     * @throws ReportException   if the template cannot be found
     * @throws DocumentException if the template doesn't have a document
     */
    public Document getTemplateDocument() {
        DocumentTemplate template = getTemplate();
        if (template == null) {
            String shortName = getShortName();
            String displayName = DescriptorHelper.getDisplayName(shortName, service);
            if (displayName == null) {
                displayName = shortName;
            }
            throw new ReportException(NoTemplateForArchetype, displayName);
        }
        Document doc = template.getDocument();
        if (doc == null) {
            throw new DocumentException(DocumentException.ErrorCode.TemplateHasNoDocument, template.getName());
        }
        return doc;
    }

    /**
     * Registers the document template.
     *
     * @param template the template to use. May be {@code null}
     */
    protected void setTemplate(DocumentTemplate template) {
        this.template = template;
    }

    /**
     * Updates the document name.
     *
     * @param document the document to update
     */
    @Override
    protected void setName(Document document) {
        DocumentTemplate template = getTemplate();
        if (template != null) {
            Object value = getObject();
            if (value == null) {
                Iterator<T> iterator = getObjects().iterator();
                if (iterator.hasNext()) {
                    // use the first object in the list to format the file name
                    value = iterator.next();
                }
            }
            if (value instanceof IMObject) {
                IMObject object = (IMObject) value;
                String fileName = formatter.format(template.getName(), object, template);
                String extension = FilenameUtils.getExtension(document.getName());
                document.setName(fileName + "." + extension);
            }
            super.setName(document);
        }
    }
}
