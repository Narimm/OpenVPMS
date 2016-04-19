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

package org.openvpms.web.component.mail;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;

/**
 * An HTML filter for the {@link MailEditor}.
 * <p/>
 * This strips out everything but the inner html of the body element.
 *
 * @author Tim Anderson
 */
class HtmlFilter {

    public static String filter(String html) {
        String result;
        ParserDelegator delegator = new ParserDelegator();
        try {
            final StringBuilder buffer = new StringBuilder();
            delegator.parse(new StringReader(html), new Filter(buffer), true);
            result = buffer.toString();
        } catch (IOException exception) {
            // do nothing
            result = null;
        }
        return result;
    }

    private static class Filter extends HTMLEditorKit.ParserCallback {

        /**
         * The buffer.
         */
        private final StringBuilder buffer;

        /**
         * If non-zero, indicates that elements and content should be skipped.
         */
        private int skip;

        /**
         * Constructs a {@link Filter}.
         *
         * @param buffer the buffer to output to
         */
        public Filter(StringBuilder buffer) {
            this.buffer = buffer;
        }

        /**
         * Handles a start tag.
         *
         * @param tag the tag
         * @param set the tag's attribute set
         * @param pos the position
         */
        @Override
        public void handleStartTag(HTML.Tag tag, MutableAttributeSet set, int pos) {
            if (skip == 0 && !exclude(tag)) {
                if (emit(tag)) {
                    buffer.append('<').append(tag.toString());
                    append(set);
                    buffer.append('>');
                }
            } else {
                skip++; // skip this element and its children
            }
        }

        /**
         * Handles element text.
         *
         * @param data the element text
         * @param pos  the position
         */
        @Override
        public void handleText(char[] data, int pos) {
            if (skip == 0) {
                buffer.append(new String(data));
            }
        }

        /**
         * Handles an end tag.
         *
         * @param tag the tag
         * @param pos the position
         */
        @Override
        public void handleEndTag(HTML.Tag tag, int pos) {
            if (!exclude(tag)) {
                if (emit(tag)) {
                    buffer.append("</").append(tag.toString()).append(">");
                }
            } else {
                skip--;
            }
        }

        /**
         * Handles a simple tag.
         *
         * @param tag the tag
         * @param set the tag's attribute set
         * @param pos the position
         */
        @Override
        public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet set, int pos) {
            if (skip == 0 && !exclude(tag)) {
                if (emit(tag)) {
                    buffer.append("<").append(tag.toString());
                    append(set);
                    buffer.append("/>");
                }
            }
        }

        /**
         * Determines if an element and all of its children should be excluded.
         *
         * @param tag the element tag
         * @return {@code true} if the element and all of its children should be excluded
         */
        protected boolean exclude(HTML.Tag tag) {
            return tag == HTML.Tag.HEAD || tag == HTML.Tag.META || tag == HTML.Tag.STYLE
                   || tag == HTML.Tag.TITLE || tag == HTML.Tag.SCRIPT;
        }

        /**
         * Determines if an element should be emitted.
         *
         * @param tag the element tag
         * @return {@code true} if the element should be emitted
         */
        protected boolean emit(HTML.Tag tag) {
            return tag != HTML.Tag.HTML && tag != HTML.Tag.BODY;
        }

        /**
         * Appends attributes to the buffer.
         *
         * @param set the attribute set
         */
        private void append(MutableAttributeSet set) {
            Enumeration<?> names = set.getAttributeNames();
            if (names.hasMoreElements()) {
                buffer.append(' ');
                boolean first = true;
                while (names.hasMoreElements()) {
                    if (!first) {
                        buffer.append(' ');
                    } else {
                        first = false;
                    }
                    Object element = names.nextElement();
                    buffer.append(element).append("=\"").append(set.getAttribute(element)).append('\"');
                }
            }
        }
    }
}
