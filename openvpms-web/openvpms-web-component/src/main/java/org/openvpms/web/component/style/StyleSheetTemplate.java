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
package org.openvpms.web.component.style;

import nextapp.echo2.app.StyleSheet;
import nextapp.echo2.app.componentxml.ComponentXmlException;
import nextapp.echo2.app.componentxml.StyleSheetLoader;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;


/**
 * A template for a {@code StyleSheet}.
 * <p/>
 * This supports replacements of tokens defined using the format: ${name}
 *
 * @author Tim Anderson
 */
public class StyleSheetTemplate {

    /**
     * The style sheet template.
     */
    private final List<String> template;

    /**
     * Constructs a {@code StyleSheetTemplate}.
     *
     * @param stream a stream to the template
     * @throws IOException if the stream cannot be read
     */
    @SuppressWarnings("unchecked")
    public StyleSheetTemplate(InputStream stream) throws IOException {
        List lines = IOUtils.readLines(stream);
        template = (List<String>) lines;
    }

    /**
     * Creates a {@code StyleSheet} from the template, after performing token replacement using the supplied
     * properties.
     *
     * @param properties the properties to perform token replacement with
     * @return a new style sheet
     * @throws StyleSheetException if the style sheet cannot be created
     */
    public StyleSheet getStyleSheet(Map<String, String> properties) {
        InputStream expanded = replaceTokens(properties);
        try {
            return StyleSheetLoader.load(expanded, StyleSheetTemplate.class.getClassLoader());
        } catch (ComponentXmlException exception) {
            throw new StyleSheetException(StyleSheetException.ErrorCode.InvalidStyleSheet, exception);
        }
    }

    /**
     * Performs token replacement on the template, returning a stream to the style sheet.
     *
     * @param properties the properties to perform token replacement with
     * @return a stream of the style sheet
     * @throws StyleSheetException if the token replacement fails
     */
    private InputStream replaceTokens(Map<String, String> properties) {
        StringBuilder buffer = new StringBuilder();
        for (int count = 0; count < template.size(); ++count) {
            String line = template.get(count);
            int pos;
            int prev = 0;
            while ((pos = line.indexOf("${", prev)) >= 0) {
                if (pos > 0) {
                    buffer.append(line.substring(prev, pos));
                }
                int index = line.indexOf('}', pos);
                if (index < 0) {
                    // invalid format
                    throw new StyleSheetException(StyleSheetException.ErrorCode.UnterminatedProperty, count + 1,
                                                  pos + 1);
                } else {
                    String name = line.substring(pos + 2, index);
                    String property = getProperty(name, properties);
                    buffer.append(property);
                    try {
                        // assume all numeric properties are pixel extents
                        Integer.valueOf(property);
                        buffer.append("px");
                    } catch (NumberFormatException ignore) {
                        // do nothing
                    }
                    prev = index + 1;
                }
            }
            if (prev < line.length()) {
                buffer.append(line.substring(prev));
            }
        }
        return new ByteArrayInputStream(buffer.toString().getBytes());
    }

    /**
     * Returns a property value, given its name.
     *
     * @param name       the property name
     * @param properties the properties to query
     * @return the property value
     * @throws StyleSheetException if the property is undefined
     */
    private String getProperty(String name, Map<String, String> properties) {
        String result = properties.get(name);
        if (result == null) {
            // attribute cannot be expanded as the property is
            // not defined
            throw new StyleSheetException(StyleSheetException.ErrorCode.UndefinedProperty, name);
        }
        return result;
    }
}
