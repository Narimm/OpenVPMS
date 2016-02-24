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

package org.openvpms.web.component.im.doc;

import org.apache.commons.lang.StringUtils;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.IOException;
import java.io.StringReader;

import static javax.swing.text.html.HTML.Tag.BR;
import static javax.swing.text.html.HTML.Tag.DD;
import static javax.swing.text.html.HTML.Tag.DT;
import static javax.swing.text.html.HTML.Tag.H1;
import static javax.swing.text.html.HTML.Tag.H2;
import static javax.swing.text.html.HTML.Tag.H3;
import static javax.swing.text.html.HTML.Tag.H4;
import static javax.swing.text.html.HTML.Tag.H5;
import static javax.swing.text.html.HTML.Tag.H6;
import static javax.swing.text.html.HTML.Tag.LI;
import static javax.swing.text.html.HTML.Tag.OL;
import static javax.swing.text.html.HTML.Tag.P;
import static javax.swing.text.html.HTML.Tag.TABLE;
import static javax.swing.text.html.HTML.Tag.TD;
import static javax.swing.text.html.HTML.Tag.TH;
import static javax.swing.text.html.HTML.Tag.TR;
import static javax.swing.text.html.HTML.Tag.UL;


/**
 * Converts html to plain text.
 * <p/>
 * Note that this will not format tables with any degree of prettiness.
 *
 * @author Tim Anderson
 */
public class HtmlToTextFormatter {

    /**
     * Formats HTML to plain text.
     *
     * @param html the html
     * @return the formatted plain text
     */
    public String format(String html) {
        HtmlToTextBuilder builder = new HtmlToTextBuilder();
        ParserDelegator delegator = new ParserDelegator();
        try {
            delegator.parse(new StringReader(html), builder, true);
        } catch (IOException exception) {
            // do nothing
        }
        return builder.getText();
    }

    private static class HtmlToTextBuilder extends HTMLEditorKit.ParserCallback {
        private final StringBuilder buffer = new StringBuilder();
        Element element;

        public String getText() {
            return buffer.toString();
        }

        @Override
        public void handleStartTag(HTML.Tag tag, MutableAttributeSet a, int pos) {
            element = new Element(tag, element);
            if (element.isA(P, TABLE, H1, H2, H3, H4, H5, H6)) {
                newLine();
            } else if (tag == LI) {
                newLine();
                indent(element);
                Element parent = element.getParent();
                if (parent != null) {
                    if (parent.isA(OL)) {
                        buffer.append(element.getOffset() + 1);
                        buffer.append(". ");
                    } else if (parent.isA(UL)) {
                        buffer.append("* ");
                    }
                }
            } else if (tag == DD) {
                buffer.append("  ");
            } else if (element.isA(tag, TD, TH)) {
                if (element.getOffset() > 0) {
                    buffer.append(" ");
                }
            }
        }

        @Override
        public void handleText(char[] data, int pos) {
            buffer.append(new String(data));
        }

        @Override
        public void handleEndTag(HTML.Tag tag, int pos) {
            if (element != null && element.isA(P, DD, DT, TR, H1, H2, H3, H4, H5, H6)) {
                newLine();
            }
            if (element != null) {
                element = element.getParent();
            }
        }

        @Override
        public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet a, int pos) {
            if (tag == BR) {
                newLine();
            }
        }

        private void newLine() {
            buffer.append("\n");
        }

        private void indent(Element element) {
            int depth = element.getDepth();
            if (depth > 0) {
                buffer.append(StringUtils.repeat("  ", depth));
            }
        }
    }

    private static class Element {

        /**
         * The element tag.
         */
        private final HTML.Tag tag;

        /**
         * The parent element. May be {@code null}
         */
        private final Element parent;

        /**
         * Tracks the level of nesting of UL, OL and TR elements to aid indenting.
         */
        private final int depth;

        /**
         * The child offset. Only relevant for LI, TH and TD elements.
         */
        private int offset = 0;

        /**
         * Constructs an {@link Element}.
         *
         * @param tag    the tag
         * @param parent the parent element. May be {@code null}
         */
        public Element(HTML.Tag tag, Element parent) {
            this.parent = parent;
            this.tag = tag;
            if (isA(OL, UL, TR)) {
                if (parent != null) {
                    depth = parent.depth + 1;
                } else {
                    depth = -1;
                }
            } else if (parent == null || isA(H1, H2, H3, H4, H5, H6)) {
                depth = -1;
            } else {
                depth = parent.depth;
            }
            if (isA(tag, TH, TD, LI) && parent != null && parent.isA(TR, OL, UL)) {
                offset = parent.nextOffset();
            }
        }

        public Element getParent() {
            return parent;
        }

        public int getDepth() {
            return depth;
        }

        public boolean isA(HTML.Tag... tags) {
            for (HTML.Tag other : tags) {
                if (tag == other) {
                    return true;
                }
            }
            return false;
        }

        public int getOffset() {
            return offset;
        }

        /**
         * Returns the next offset of a child element.
         *
         * @return the next offset
         */
        public int nextOffset() {
            return offset++;
        }

    }

}
