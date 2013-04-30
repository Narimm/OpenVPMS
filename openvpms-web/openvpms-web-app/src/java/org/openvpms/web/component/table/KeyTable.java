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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.table;

import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.event.ActionEvent;

import java.util.EventListener;


/**
 * Table implementation that supports keyboard navigation.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class KeyTable extends Table {

    public static final String PROPERTY_SELECTION_BLUR_FOREGROUND
        = "selectionBlurForeground";

    public static final String PROPERTY_SELECTION_BLUR_BACKGROUND
        = "selectionBlurBackground";

    public static final String PROPERTY_SELECTION_BLUR_FONT
        = "selectionBlurFont";

    protected static final String PAGE_ACTION = "page";


    /**
     * Returns the row selection blur foreground color.
     *
     * @return the foreground color
     */
    public Color getPropertySelectionBlurForeground() {
        return (Color) getProperty(PROPERTY_SELECTION_BLUR_FOREGROUND);
    }

    /**
     * Sets the row selection blur foreground color.
     *
     * @param newValue the new foreground color
     */
    public void setSelectionBlurForeground(Color newValue) {
        setProperty(PROPERTY_SELECTION_BLUR_FOREGROUND, newValue);
    }

    /**
     * Returns the row selection blur background color.
     *
     * @return the background color
     */
    public Color getPropertySelectionBlurBackground() {
        return (Color) getProperty(PROPERTY_SELECTION_BLUR_BACKGROUND);
    }

    /**
     * Sets the row selection blur background color.
     *
     * @param newValue the new background color
     */
    public void setSelectionBlurBackground(Color newValue) {
        setProperty(PROPERTY_SELECTION_BLUR_BACKGROUND, newValue);
    }

    /**
     * Returns the row selection blur font.
     *
     * @return the font
     */
    public Font getSelectionBlurFont() {
        return (Font) getProperty(PROPERTY_SELECTION_BLUR_FONT);
    }

    /**
     * Sets the row selection blur font.
     *
     * @param newValue the new font
     */
    public void setSelectionBlurFont(Font newValue) {
        setProperty(PROPERTY_SELECTION_BLUR_FONT, newValue);
    }

    /**
     * Adds a page listener.
     *
     * @param listener the listener to add
     */
    public void addPageListener(PageListener listener) {
        getEventListenerList().addListener(PageListener.class, listener);
    }

    /**
     * Removes a page listener.
     *
     * @param listener the listener to remove
     */
    public void removePageListener(PageListener listener) {
        getEventListenerList().removeListener(PageListener.class, listener);
    }

    /**
     * Determines if any page listeners are registered.
     *
     * @return <tt>true</tt> if listeners are registered
     */
    public boolean hasPageListeners() {
        return getEventListenerList().getListenerCount(PageListener.class) != 0;
    }

    /**
     * @see Component#processInput(String, Object)
     */
    @Override
    public void processInput(String inputName, Object inputValue) {
        super.processInput(inputName, inputValue);
        if (PAGE_ACTION.equals(inputName) && inputValue instanceof String) {
            firePageEvent((String) inputValue);
        }
    }

    /**
     * Fires an page event to all listeners.
     *
     * @param page the page command
     */
    private void firePageEvent(String page) {
        if (!hasEventListenerList()) {
            return;
        }
        EventListener[] listeners = getEventListenerList().getListeners(
            PageListener.class);
        ActionEvent event = null;
        for (EventListener listener : listeners) {
            if (event == null) {
                event = new ActionEvent(this, page);
            }
            ((PageListener) listener).actionPerformed(event);
        }
    }


}
