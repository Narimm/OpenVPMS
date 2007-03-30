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

package org.openvpms.etl;

import java.util.List;


/**
 * Encapsulates the archetype short name and node names refererenced
 * by {@link ETLValue} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ETLArchetype {

    /**
     * The archetype short name.
     */
    private final String archetype;

    /**
     * The node names.
     */
    private final List<String> names;


    /**
     * Constructs a new <tt>ETLArchetype</tt>.
     *
     * @param archetype the archetype
     * @param names     the node names
     */
    public ETLArchetype(String archetype, List<String> names) {
        this.archetype = archetype;
        this.names = names;
    }

    /**
     * Returns the archetype short name.
     *
     * @return the archetype short name
     */
    public String getArchetype() {
        return archetype;
    }

    /**
     * Returns the node names.
     *
     * @return the node names
     */
    public List<String> getNames() {
        return names;
    }
}
