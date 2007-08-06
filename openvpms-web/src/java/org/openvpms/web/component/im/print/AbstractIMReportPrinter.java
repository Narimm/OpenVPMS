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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.print;

import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMReport;
import org.openvpms.report.ReportException;


/**
 * Prints reports for objects generated by {@link IMReport}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractIMReportPrinter<T>
        extends AbstractIMPrinter<T> {

    /**
     * Constructs a new <code>AbstractIMReportPrinter</code> to print a single
     * object.
     *
     * @param object the object to print
     */
    public AbstractIMReportPrinter(T object) {
        super(object);
    }

    /**
     * Constructs a new <code>AbstractIMReportPrinter</code> to print a
     * collection of objects.
     *
     * @param objects the objects to print
     */
    public AbstractIMReportPrinter(Iterable<T> objects) {
        super(objects);
    }

    /**
     * Returns a document for an object.
     *
     * @return a document
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Document getDocument() {
        IMReport<T> report = createReport();
        String[] mimeTypes = {DocFormats.PDF_TYPE};
        return report.generate(getObjects().iterator(), getParameters(),
                               mimeTypes);
    }

}
