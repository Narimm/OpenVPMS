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

package org.openvpms.web.component.button;

import echopointng.ButtonEx;
import echopointng.ui.syncpeer.PushButtonPeer;
import echopointng.ui.util.ImageManager;
import echopointng.ui.util.RenderingContext;
import echopointng.ui.util.TriCellTable;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.Style;
import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webcontainer.propertyrender.AlignmentRender;
import nextapp.echo2.webrender.output.CssStyle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;


/**
 * The peer for {@link AccessKeyButton}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AccessKeyButtonPeer extends PushButtonPeer {

    /**
     * Renders the content of the button, i.e., its text, icon, and/or state
     * icon.
     *
     * @param rc                     the relevant <code>RenderingContext</code>
     * @param buttonContainerElement the <code>Element</code> which will contain the content
     * @param button                 the <code>ButtonEx</code> being rendered
     */
    @Override
    protected void renderButtonContent(RenderingContext rc,
                                       Element buttonContainerElement,
                                       ButtonEx button, Style fallbackStyle) {
        Node contentNode;
        String elementId = ContainerInstance.getElementId(button);

        ImageReference icon = (ImageReference) rc.getRP(
                button.isRenderEnabled() ? ButtonEx.PROPERTY_ICON : ButtonEx.PROPERTY_DISABLED_ICON,
                fallbackStyle);

        // Create entities.
        Node textNode = getTextNode(rc, button, fallbackStyle);

        Element iconElement;
        if (icon == null) {
            iconElement = null;
        } else {
            iconElement = ImageManager.createImgE(rc, icon);
            iconElement.setAttribute("id", elementId + "_icon");
        }

        int entityCount = (textNode == null ? 0 : 1) + (iconElement == null ? 0 : 1);

        Extent iconTextMargin;
        Alignment textPosition;

        switch (entityCount) {
            case 1:
                if (textNode != null) {
                    contentNode = textNode;
                } else {
                    contentNode = iconElement;
                }
                break;
            case 2:

                iconTextMargin = (Extent) rc.getRP(
                        ButtonEx.PROPERTY_ICON_TEXT_MARGIN, fallbackStyle);
                TriCellTable tct;
                textPosition = (Alignment) rc.getRP(
                        ButtonEx.PROPERTY_TEXT_POSITION, fallbackStyle);
                int orientation = convertIconTextPositionToOrientation(
                        textPosition, button);

                tct = new TriCellTable(rc.getDocument(), elementId, orientation,
                                       iconTextMargin);

                renderCellText(rc, tct, textNode, button, fallbackStyle);
                renderCellIcon(rc, tct, iconElement, 1, fallbackStyle);

                Element tableElement = tct.getTableElement();
                tableElement.setAttribute("id", elementId + "_table");
                contentNode = tableElement;
                break;
            default:
                // 0 element button.
                contentNode = null;
        }

        if (contentNode != null) {
            buttonContainerElement.appendChild(contentNode);
        }
    }

    /**
     * Returns a node containing the button text.
     *
     * @param rc            the rendering context
     * @param button        the button
     * @param fallbackStyle the fallback style
     * @return a node containing the text, or <code>null</code> if there is no text
     */
    protected Node getTextNode(RenderingContext rc, ButtonEx button,
                               Style fallbackStyle) {
        String text = (String) rc.getRP(ButtonEx.PROPERTY_TEXT, fallbackStyle);
        Node textNode;
        if (text != null) {
            Document document = rc.getDocument();
            int pos;
            if (button.getAccessKey() != null && (pos = text.indexOf(
                    button.getAccessKey())) != -1) {
                Element span = document.createElement("span");
                if (pos != 0) {
                    Text first = document.createTextNode(
                            text.substring(0, pos));
                    span.appendChild(first);
                }
                Element underlined = document.createElement("span");
                underlined.setAttribute("style", "text-decoration:underline");
                underlined.appendChild(
                        document.createTextNode(text.substring(pos, pos + 1)));
                span.appendChild(underlined);
                if (pos + 1 < text.length()) {
                    Text last = document.createTextNode(
                            text.substring(pos + 1));
                    span.appendChild(last);
                }
                textNode = span;
            } else {
                textNode = document.createTextNode(text);
            }
        } else {
            textNode = null;
        }
        return textNode;
    }

    /**
     * Renders the content of the <code>TriCellTable</code> cell which
     * contains the button's text. Text is always rendered in cell #0 of the
     * table.
     *
     * @param tct      the <code>TriCellTable</code> to update
     * @param textNode the text
     * @param button   the <code>ButtonEx</code> being rendered
     */
    protected void renderCellText(RenderingContext rc, TriCellTable tct,
                                  Node textNode, ButtonEx button,
                                  Style fallbackStyle) {
        Element textTdElement = tct.getTdElement(0);
        CssStyle textTdCssStyle = new CssStyle();
        textTdCssStyle.setAttribute("padding", "0px");
        AlignmentRender.renderToStyle(textTdCssStyle, (Alignment) rc.getRP(
                ButtonEx.PROPERTY_TEXT_ALIGNMENT, fallbackStyle));
        if (!rc.getRP(ButtonEx.PROPERTY_LINE_WRAP, fallbackStyle, false)) {
            textTdCssStyle.setAttribute("white-space", "nowrap");
        }
        textTdElement.setAttribute("style", textTdCssStyle.renderInline());
        textTdElement.appendChild(textNode);
    }

}
