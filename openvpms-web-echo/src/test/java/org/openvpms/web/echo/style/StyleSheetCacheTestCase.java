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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.style;

import java.io.IOException;


/**
 * Tests {@link StyleSheetCache}.
 *
 * @author Tim Anderson
 */
public class StyleSheetCacheTestCase extends AbstractStyleSheetsTest {

    /**
     * Creates the style sheets.
     *
     * @return the style sheets
     * @throws IOException for any I/O error
     */
    protected StyleSheets createStyleSheets() throws IOException {
        return new StyleSheetCache("org/openvpms/web/echo/style/valid");
    }
}