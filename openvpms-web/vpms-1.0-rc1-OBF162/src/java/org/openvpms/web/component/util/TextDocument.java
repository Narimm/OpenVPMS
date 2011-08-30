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

package org.openvpms.web.component.util;

import nextapp.echo2.app.event.DocumentEvent;
import nextapp.echo2.app.text.AbstractDocument;
import org.apache.commons.lang.ObjectUtils;


/**
 * Implementation of the <em>Document</em> interface that only
 * fires <em>DocumentEvents</em> if the new text is different to the prior
 * text. This is a replacement for the default <em>StringDocument<em>
 * implementation that performs no difference check.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TextDocument extends AbstractDocument {

    /**
     * The text.
     */
    private String text;


    /**
     * Sets the text of the document.
     *
     * @param text the new text of the document
     */
    public void setText(String text) {
        if (!ObjectUtils.equals(this.text, text)) {
            this.text = text;
            DocumentEvent e = new DocumentEvent(this);
            fireDocumentUpdate(e);
        }
    }

    /**
     * Returns the text of the document.
     * This method should return ab empty string in the event the document
     * contains no text.  Null may not be returned.
     *
     * @return the text of the document.
     */
    public String getText() {
        return text == null ? "" : text;
    }

}
