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

package org.openvpms.tools.archetype.loader;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Tracks changes made to archetype descriptors by the {@link ArchetypeLoader}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Changes {

    /**
     * The changes.
     */
    public Map<String, Change> changes = new HashMap<String, Change>();


    /**
     * Registers the previous instance of an archetype descriptor.
     *
     * @param descriptor the descriptor
     */
    public void addOldVersion(ArchetypeDescriptor descriptor) {
        Change change = getChange(descriptor.getShortName());
        change.setOldVersion(descriptor);
    }

    /**
     * Registers the current instance of an archetype descriptor.
     *
     * @param descriptor the descriptor
     */
    public void addNewVersion(ArchetypeDescriptor descriptor) {
        Change change = getChange(descriptor.getShortName());
        change.setNewVersion(descriptor);
    }

    /**
     * Returns the archetype descriptors that have changed.
     *
     * @return the archetype descriptors that have changed
     */
    public Collection<Change> getChanges() {
        return changes.values();
    }

    /**
     * Returns the change for the specified archetype short name,
     * creating one if it doesn't exist
     *
     * @param shortName the archetype short name
     * @return the corresponding change
     */
    private Change getChange(String shortName) {
        Change change = changes.get(shortName);
        if (change == null) {
            change = new Change();
        }
        return change;
    }

}
