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

package org.openvpms.etl.load;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.lookup.Lookup;

import java.util.HashSet;
import java.util.Set;


/**
 * Lookup descriptor used by the {@link LookupHandler} to create
 * {@link Lookup}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupDescriptor {

    /**
     * The lookup node descriptor.
     */
    private final NodeDescriptor descriptor;

    /**
     * The lookup archetype short name.
     */
    private final String archetype;

    /**
     * Lookup code/name pairs.
     */
    private final Set<CodeName> lookups = new HashSet<CodeName>();


    /**
     * Constructs a new <tt>LookupDescriptor</tt>.
     *
     * @param descriptor the lookup node descriptor
     * @param archetype  the lookup archetype short name
     */
    public LookupDescriptor(NodeDescriptor descriptor, String archetype) {
        this.descriptor = descriptor;
        this.archetype = archetype;
    }

    /**
     * Returns the lookup node descriptor.
     *
     * @return the lookup node descriptor
     */
    public NodeDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Returns the lookup archetype short name.
     *
     * @return the archetype short name
     */
    public String getArchetype() {
        return archetype;
    }

    /**
     * Adds a lookup.
     *
     * @param lookup the lookup code/name pair
     */
    public void add(CodeName lookup) {
        lookups.add(lookup);
    }

    /**
     * Returns the lookups.
     *
     * @return the lookups
     */
    public Set<CodeName> getLookups() {
        return lookups;
    }

    /**
     * Clears the lookups.
     */
    public void clear() {
        lookups.clear();
    }

    /**
     * Determines if this equals another object.
     *
     * @param other the other object
     * @return <tt>true</tt> if this equals <tt>other</tt>
     */
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof LookupDescriptor)) {
            return false;
        }
        LookupDescriptor object = (LookupDescriptor) other;
        return descriptor.equals(object.descriptor);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return descriptor.hashCode();
    }
}
