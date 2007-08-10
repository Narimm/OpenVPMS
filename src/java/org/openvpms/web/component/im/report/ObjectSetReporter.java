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
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.report.IMReport;
import org.openvpms.report.ReportException;
import org.openvpms.report.ReportFactory;
import org.openvpms.web.system.ServiceHelper;


/**
 * Generates {@link Document}s from one or more {@link ObjectSet}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ObjectSetReporter extends TemplatedReporter<ObjectSet> {

    /**
     * Constructs a new <tt>ObjectSetReporter</tt>.
     *
     * @param set       the set to print
     * @param shortName the archetype short name
     * @throws DocumentException         if the document template can't be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public ObjectSetReporter(Iterable<ObjectSet> set, String shortName) {
        super(set, shortName);
        if (getTemplate() == null) {
            throw new DocumentException(DocumentException.ErrorCode.NotFound);
        }
    }

    /**
     * Returns the report.
     *
     * @return the report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         if the template document can't be found
     */
    public IMReport<ObjectSet> getReport() {
        Document document = getTemplateDocument();
        if (document == null) {
            throw new DocumentException(NotFound);
        }
        return ReportFactory.createObjectSetReport(
                document, ArchetypeServiceHelper.getArchetypeService(),
                ServiceHelper.getDocumentHandlers());
    }

}
