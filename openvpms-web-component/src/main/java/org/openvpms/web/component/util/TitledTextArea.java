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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.util;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.TextArea;
import org.apache.commons.lang.StringUtils;
import org.openvpms.web.echo.style.Styles;


/**
 * A <tt>TextArea</tt> with a title label displayed above it.
 * <p/>
 * By default, text area will fill the available width.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TitledTextArea extends Column {

    /**
     * The text area.
     */
    private TextArea textArea;

    /**
     * The minimum lines to display.
     */
    private int minHeight = MIN_HEIGHT;

    /**
     * The maximum lines to display.
     */
    private int maxHeight = MAX_HEIGHT;

    /**
     * The minumum no. of lines to display.
     */
    public static final int MIN_HEIGHT = 6;

    /**
     * The maximum no. of lines to display.
     */
    private static final int MAX_HEIGHT = 15;


    /**
     * Constructs a new <tt>TextAreaBox</tt>.
     *
     * @param title the box title
     */
    public TitledTextArea(String title) {
        setStyleName("CellSpacing");
        textArea = TextComponentFactory.createTextArea();
        textArea.setWidth(new Extent(100, Extent.PERCENT)); // 100% width
        textArea.setStyleName(Styles.EDIT);
        Label label = LabelFactory.create();
        label.setText(title);
        add(label);
        add(textArea);
    }

    /**
     * Sets the enabled state of the <code>Component</code>.
     *
     * @param newValue the new state
     * @see #isEnabled
     */
    @Override
    public void setEnabled(boolean newValue) {
        textArea.setEnabled(newValue);
    }

    /**
     * Updates the text area, changing the no. of lines displayed if required.
     *
     * @param text the text. May be <tt>null</tt>
     */
    public void setText(String text) {
        textArea.setText(text);
        textArea.setHeight(new Extent(getLinesToDisplay(text), Extent.EM));
    }

    /**
     * Determines the no. of lines of text to display.
     *
     * @param text the text. May be <tt>null</tt>
     * @return the lines to display
     */
    protected int getLinesToDisplay(String text) {
        int lines = minHeight;
        if (text != null) {
            lines = StringUtils.countMatches(text, "\n");
            if (lines > maxHeight) {
                lines = maxHeight;
            } else if (lines < minHeight) {
                lines = minHeight;
            }
        }
        return lines;
    }

}
