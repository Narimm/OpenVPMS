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

package org.openvpms.web.component.help;

import org.openvpms.web.resource.i18n.message.DefaultMessageResource;
import org.openvpms.web.resource.i18n.message.KeyMessageSource;

import java.util.Set;

/**
 * Help topics.
 *
 * @author Tim Anderson
 */
public class HelpTopics extends DefaultMessageResource {

    /**
     * Constructs a {@link HelpTopics}.
     */
    public HelpTopics() {
        this(createDefaultSource());
    }

    /**
     * Constructs a {@link HelpTopics}.
     *
     * @param source the topic source
     */
    public HelpTopics(KeyMessageSource source) {
        super(source);
    }

    /**
     * Returns the topics for the default locale.
     *
     * @return the topics
     */
    public Set<String> getKeys() {
        return ((KeyMessageSource) getSource()).getKeys(getLocale());
    }

    /**
     * Creates a default message source.
     *
     * @return a new message source
     */
    private static KeyMessageSource createDefaultSource() {
        KeyMessageSource result = new KeyMessageSource();
        result.setBasename("localisation.help");
        return result;
    }

}