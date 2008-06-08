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

package org.openvpms.report;


/**
 * Supported reporting document formats.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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

}
