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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.factory;

import echopointng.BalloonHelp;
import echopointng.TemplatePanel;
import echopointng.template.StringTemplateDataSource;
import org.apache.commons.lang.StringEscapeUtils;
import org.openvpms.web.echo.style.Styles;


/**
 * Factory for {@code BalloonHelp} instances.
 *
 * @author Tim Anderson
 */
public class BalloonHelpFactory {

    /**
     * Creates a new {@code BalloonHelp}.
     *
     * @param text the help text
     * @return a new {@code BalloonHelp}
     */
    public static BalloonHelp create(String text) {
        String xml = "<bdo>" + StringEscapeUtils.escapeXml(text) + "</bdo>";
        BalloonHelp result = new BalloonHelp();
        StringTemplateDataSource dataSource = new StringTemplateDataSource(xml);
        dataSource.setCachingHints(null); // no caching
        result.setPopUp(new TemplatePanel(dataSource));
        result.setStyleName(Styles.DEFAULT);
        return result;
    }
}
