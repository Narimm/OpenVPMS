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
import org.openvpms.component.business.domain.im.document.Document;

/**
 * Supported reporting document formats.
 *
 * @author Tim Anderson
 */
public class DocFormats {

    /**
     * Jasper Reports XML extension.
     */
    public static final String JRXML_EXT = "jrxml";

    /**
     * Portable Document Format mime-type.
     */
    public static final String PDF_TYPE = "application/pdf";

    /**
     * Portable Document Format file extension.
     */
    public static final String PDF_EXT = "pdf";

    /**
     * Excel mime-type.
     */
    public static final String XLS_TYPE = "application/excel";

    /**
     * Excel post 2007 mime-type.
     */
    public static final String XLSX_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * Excel file extension.
     */
    public static final String XLS_EXT = "xls";

    /**
     * Excel Post 2007 file extension.
     */
    public static final String XLSX_EXT = "xlsx";

    /**
     * OpenDocument Text mime-type.
     */
    public static final String ODT_TYPE
            = "application/vnd.oasis.opendocument.text";

    /**
     * MS Word Document mime-type.
     */
    public static final String DOC_TYPE
            = "application/msword";

    /**
     * MS Word Document mime-type
     */
    public static final String DOCX_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    /**
     * OpenDocument Text file extension.
     */
    public static final String ODT_EXT = "odt";

    /**
     * Rich Text Format mime-type.
     */
    public static final String RTF_TYPE = "text/rtf";

    /**
     * Rich Text Format file extension.
     */
    public static final String RTF_EXT = "rtf";

    /**
     * MS Word Document file extension.
     */
    public static final String DOC_EXT = "doc";

    /**
     * MS Word Document file extension.
     */
    public static final String DOCX_EXT = "docx";
    /**
     * CSV Format mime-type.
     */
    public static final String CSV_TYPE = "text/csv";

    /**
     * CSV file extension.
     */
    public static final String CSV_EXT = "csv";

    /**
     * XML Format mime-type.
     */
    public static final String XML_TYPE = "text/xml";

    /**
     * XML file extension.
     */
    public static final String XML_EXT = "xml";

    /**
     * Plain text mime-type.
     */
    public static final String TEXT_TYPE = "text/plain";

    /**
     * Text file extension.
     */
    public static final String TEXT_EXT = "txt";

    /**
     * HTML mime type.
     */
    public static final String HTML_TYPE = "text/html";

    /**
     * HTML file extension.
     */
    public static final String HTML_EXT = "html";

    /**
     * Determines if a document name has one of the specified extensions.
     *
     * @param document   the document
     * @param extensions the file name extensions
     * @return {@code true} if the document name has one of the specified extensions (case insensitive)
     */
    public static boolean hasExtension(Document document, String... extensions) {
        return hasExtension(document.getName(), extensions);
    }

    /**
     * Determines if a file name has one of the specified extensions.
     *
     * @param name       the file name
     * @param extensions the file name extensions
     * @return {@code true} if the name has one of the specified extensions (case insensitive)
     */
    public static boolean hasExtension(String name, String... extensions) {
        String nameExt = FilenameUtils.getExtension(name);
        if (nameExt != null) {
            for (String extension : extensions) {
                if (nameExt.equalsIgnoreCase(extension)) {
                    return true;
                }
            }
        }
        return false;
    }

}
