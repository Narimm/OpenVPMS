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

package org.openvpms.report.openoffice;

import org.apache.commons.io.FilenameUtils;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.report.DocFormats;


/**
 * Converts a {@link Document} from one format to another.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Converter {

    /**
     * The OpenOffice service.
     */
    private final OpenOfficeService _service;

    /**
     * The archetype service.
     */
    private final IArchetypeService _archetypeService;

    /**
     * Constructs a new <code>Converter</code>.
     *
     * @param service          the OpenOffice service
     * @param archetypeService the archetype service
     */
    public Converter(OpenOfficeService service,
                     IArchetypeService archetypeService) {
        _service = service;
        _archetypeService = archetypeService;
    }

    /**
     * Determines if a document can be converted.
     *
     * @param document the document to convert
     * @param mimeType the target mime type
     * @return <code>true</code> if the document can be converted, otherwise
     *         <code>false</code>
     */
    public static boolean canConvert(Document document, String mimeType) {
        return canConvert(document.getName(), document.getMimeType(), mimeType);
    }

    /**
     * Determines if a document can be converted.
     *
     * @param fileName       the document file name
     * @param sourceMimeType the document mime type
     * @param targetMimeType the target mime type
     * @return <code>true</code> if the document can be converted, otherwise
     *         <code>false</code>
     */
    public static boolean canConvert(String fileName, String sourceMimeType,
                                     String targetMimeType) {
        String ext = null;
        if (fileName != null) {
            ext = FilenameUtils.getExtension(fileName);
        }
        return (DocFormats.ODT_EXT.equals(ext)
                || DocFormats.ODT_TYPE.equals(sourceMimeType))
                && DocFormats.PDF_TYPE.equals(targetMimeType);
    }

    /**
     * Converts a document.
     *
     * @param document the document to convert
     * @param mimeType the target mime type
     * @return the converted document
     * @throws OpenOfficeException       if the document cannot be converted
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Document convert(Document document, String mimeType) {
        OpenOfficeDocument doc = new OpenOfficeDocument(document, _service);
        try {
            Thread.sleep(1000);   // todo - hack to avoid document corruption.
        } catch (InterruptedException ignore) {
            // no-op
        }
        try {
            return doc.export(mimeType, document.getName(), _archetypeService);
        } finally {
            doc.close();
        }
    }

}
