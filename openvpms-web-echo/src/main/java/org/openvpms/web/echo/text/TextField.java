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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.echo.text;

import nextapp.echo2.app.text.Document;


/**
 * Workaround for a bug in the echo2 TextComponent javascript implementation.
 * <p/>
 * This should be used instead of the echo2 class.
 * <p/>
 * This exists to enable {@link TextFieldPeer} to be used to specify a corrected javascript file,
 * <em>org/openvpms/web/resource/js/TextComponent.js</em>.
 * The binding is specified in <em>META-INF\nextapp\echo2\SynchronizePeerBindings.properties</em>
 * <p/>
 * See http://jira.openvpms.org/jira/browse/OVPMS-1017 for more details.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class TextField extends nextapp.echo2.app.TextField {

    /**
     * Creates a new <tt>TextField</tt> with an empty <tt>StringDocument</tt> as its model, and default width setting.
     */
    public TextField() {
        super();
    }

    /**
     * Creates a new <tt>TextField</tt> with the specified <tt>Document</tt> model.
     *
     * @param document the document
     */
    public TextField(Document document) {
        super(document);
    }

    /**
     * Creates a new <tt>TextField</tt> with the specified <tt>Document</tt> model, initial text, and column width.
     *
     * @param document the document
     * @param text     the initial text (may be null)
     * @param columns  the number of columns to display
     */
    public TextField(Document document, String text, int columns) {
        super(document, text, columns);
    }
}
                                                           