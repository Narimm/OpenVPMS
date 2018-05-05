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

package org.openvpms.report;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.jxpath.Functions;
import org.openvpms.archetype.function.factory.ArchetypeFunctionsFactory;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ReadOnlyArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.report.jasper.TemplatedJasperIMObjectReport;
import org.openvpms.report.jasper.TemplatedJasperObjectSetReport;
import org.openvpms.report.msword.MsWordIMReport;
import org.openvpms.report.openoffice.OpenOfficeIMReport;

import static org.openvpms.report.ReportException.ErrorCode.NoTemplateForArchetype;
import static org.openvpms.report.ReportException.ErrorCode.UnsupportedTemplate;


/**
 * A factory for {@link Report} instances.
 *
 * @author Tim Anderson
 */
public class ReportFactory {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The JXPath extension function factory.
     */
    private final ArchetypeFunctionsFactory factory;

    /**
     * Constructs an {@link ReportFactory}.
     *
     * @param service  the archetype service
     * @param lookups  the lookup service
     * @param handlers the document handlers
     * @param factory  the factory for JXPath extension functions
     */
    public ReportFactory(IArchetypeService service, ILookupService lookups, DocumentHandlers handlers,
                         ArchetypeFunctionsFactory factory) {
        this.service = new ReadOnlyArchetypeService(service);
        this.lookups = lookups;
        this.handlers = handlers;
        this.factory = factory;
    }

    /**
     * Creates a new report.
     *
     * @param template the document template
     * @return a new report
     * @throws ReportException for any report error
     */
    public Report createReport(Document template) {
        String name = template.getName();
        Report report;
        if (DocFormats.hasExtension(template, DocFormats.JRXML_EXT)) {
            IArchetypeService serviceProxy = proxy(service);
            Functions functions = factory.create(serviceProxy, true);
            report = new TemplatedJasperIMObjectReport(template, serviceProxy, lookups, handlers, functions);
        } else {
            throw new ReportException(UnsupportedTemplate, name);
        }
        return report;
    }

    /**
     * Determines if a template can be used to create a report via {@link #createIMObjectReport(Document)}.
     *
     * @param template the template
     * @return {@code true} if the template can be used to create a report
     */
    public boolean isIMObjectReport(Document template) {
        return DocFormats.hasExtension(template, DocFormats.JRXML_EXT, DocFormats.ODT_EXT, DocFormats.DOC_EXT);
    }

    /**
     * Creates a new report for a collection of {@link IMObject}s.
     *
     * @param template the document template
     * @return a new report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMReport<IMObject> createIMObjectReport(Document template) {
        IMReport<IMObject> report;
        IArchetypeService serviceProxy = proxy(service);
        Functions functions = factory.create(serviceProxy, true);

        String ext = FilenameUtils.getExtension(template.getName());
        if (ext != null) {
            if (isJRXML(ext)) {
                report = new TemplatedJasperIMObjectReport(template, serviceProxy, lookups, handlers, functions);
            } else if (isODT(ext)) {
                report = new OpenOfficeIMReport<>(template, serviceProxy, lookups, handlers, functions);
            } else if (isDOC(ext)) {
                report = new MsWordIMReport<>(template, serviceProxy, lookups, handlers, functions);
            } else {
                throw new ReportException(UnsupportedTemplate, template.getName());
            }
        } else {
            throw new ReportException(UnsupportedTemplate, template.getName());
        }
        return report;
    }

    /**
     * Creates a new report for a collection of {@link IMObject}s.
     *
     * @param shortName the archetype short name
     * @return a new report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMReport<IMObject> createIMObjectReport(String shortName) {
        TemplateHelper helper = new TemplateHelper(service);
        Document doc = helper.getDocumentForArchetype(shortName);
        if (doc == null) {
            String displayName = DescriptorHelper.getDisplayName(shortName, service);
            throw new ReportException(NoTemplateForArchetype, displayName);
        }
        return createIMObjectReport(doc);
    }

    /**
     * Determines if a template can be used to create a report via {@link #createObjectSetReport(Document)}.
     *
     * @param template    the template
     * @param cardinality the no. of objects being reported on. OpenOffice/Word templates only support a single object
     * @return {@code true} if the template can be used to create a report
     */
    public boolean isObjectSetReport(Document template, int cardinality) {
        boolean result = false;
        String ext = FilenameUtils.getExtension(template.getName());
        if (ext != null) {
            if (isJRXML(ext) || (cardinality == 1 && (isODT(ext) || isDOC(ext)))) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Creates a new report for a collection of {@link ObjectSet}s.
     *
     * @param template the document template
     * @return a new report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMReport<ObjectSet> createObjectSetReport(Document template) {
        IMReport<ObjectSet> report;
        String ext = FilenameUtils.getExtension(template.getName());
        if (ext != null) {
            IArchetypeService serviceProxy = proxy(service);
            Functions functions = factory.create(serviceProxy, true);
            if (isJRXML(ext)) {
                report = new TemplatedJasperObjectSetReport(template, serviceProxy, lookups, handlers, functions);
            } else if (isODT(ext)) {
                report = new OpenOfficeIMReport<>(template, serviceProxy, lookups, handlers, functions);
            } else {
                throw new ReportException(UnsupportedTemplate, template.getName());
            }
        } else {
            throw new ReportException(UnsupportedTemplate, template.getName());
        }
        return report;
    }

    /**
     * Creates a proxy for the archetype service.
     *
     * @param service service
     * @return the proxied service
     */
    protected IArchetypeService proxy(IArchetypeService service) {
        return service;
    }

    /**
     * Determines if an extension is a JasperReports .jrxml.
     *
     * @param ext the extension
     * @return {@code true} if the extension is a .jrxml
     */
    private boolean isJRXML(String ext) {
        return ext.equalsIgnoreCase(DocFormats.JRXML_EXT);
    }

    /**
     * Determines if an extension is an OpenOffice .odt.
     *
     * @param ext the extension
     * @return {@code true} if the extension is a .odt
     */
    private boolean isODT(String ext) {
        return ext.equalsIgnoreCase(DocFormats.ODT_EXT);
    }

    /**
     * Determines if an extension is a Microsoft Word .doc.
     *
     * @param ext the extension
     * @return {@code true} if the extension is a .jrxml
     */
    private boolean isDOC(String ext) {
        return ext.equalsIgnoreCase(DocFormats.DOC_EXT);
    }

}
