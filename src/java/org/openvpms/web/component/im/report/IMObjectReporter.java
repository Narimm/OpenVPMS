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

import org.openvpms.archetype.rules.doc.DocumentException;
import static org.openvpms.archetype.rules.doc.DocumentException.ErrorCode.NotFound;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.IMReport;
import org.openvpms.report.ReportException;
import static org.openvpms.report.ReportException.ErrorCode.NoTemplateForArchetype;
import org.openvpms.report.ReportFactory;
import org.openvpms.web.system.ServiceHelper;


/**
 * Generates {@link Document}s from one or more {@link IMObject}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectReporter<T extends IMObject> extends TemplatedReporter<T> {

    /**
     * Constructs an <tt>IMObjectReporter</tt> for a single object.
     *
     * @param object   the object
     * @param template the document template to use
     */
    public IMObjectReporter(T object, DocumentTemplate template) {
        super(object, template);
    }

    /**
     * Constructs an <tt>IMObjectReporter</tt> for a single object.
     *
     * @param object  the object
     * @param locator the document template locator
     * @throws OpenVPMSException for any error
     */
    public IMObjectReporter(T object, DocumentTemplateLocator locator) {
        super(object, locator);
    }

    /**
     * Constructs an <tt>IMObjectReporter</tt> for a collection of objects.
     *
     * @param objects  the objects to print
     * @param template the document template to use
     */
    public IMObjectReporter(Iterable<T> objects, DocumentTemplate template) {
        super(objects, template);
    }

    /**
     * Constructs an <tt>IMObjectReporter</tt> for a collection of objects.
     *
     * @param objects the objects to print
     * @param locator the document template locator
     */
    public IMObjectReporter(Iterable<T> objects, DocumentTemplateLocator locator) {
        super(objects, locator);
    }

    /**
     * Returns the report.
     *
     * @return the report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         if the document can't be found
     */
    @SuppressWarnings("unchecked")
    public IMReport<T> getReport() {
        DocumentTemplate template = getTemplate();
        if (template == null) {
            String displayName = DescriptorHelper.getDisplayName(getShortName());
            throw new ReportException(NoTemplateForArchetype, displayName);
        }
        IArchetypeService service = ServiceHelper.getArchetypeService();
        DocumentHandlers handlers = ServiceHelper.getDocumentHandlers();
        Document doc = getTemplateDocument();
        if (doc == null) {
            throw new DocumentException(NotFound);
        }
        IMReport<IMObject> report = ReportFactory.createIMObjectReport(doc, service, handlers);
        return (IMReport<T>) report;
    }

}
