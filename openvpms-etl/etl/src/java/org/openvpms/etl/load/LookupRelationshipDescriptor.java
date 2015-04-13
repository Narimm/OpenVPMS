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

import org.openvpms.component.business.domain.im.lookup.LookupRelationship;

import java.util.HashSet;
import java.util.Set;


/**
 * Lookup relationship descriptor used by the {@link LookupHandler} to
 * create {@link LookupRelationship}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupRelationshipDescriptor {

    /**
     * The relationship short name.
     */
    private final String archetype;

    /**
     * The source lookup.
     */
    private final LookupDescriptor source;

    /**
     * The target lookup.
     */
    private final LookupDescriptor target;

    /**
     * The code pairs.
     */
    private final Set<Pair> pairs = new HashSet<Pair>();


    /**
     * Constructs a new <tt>LookupRelationshipDescriptor</tt>.
     *
     * @param archetype the relationship archetype short name
     * @param source    the source lookup
     * @param target    the target lookup
     */
    public LookupRelationshipDescriptor(String archetype,
                                        LookupDescriptor source,
                                        LookupDescriptor target) {
        this.archetype = archetype;
        this.source = source;
        this.target = target;
    }

    /**
     * Returns the relationship archetype short name.
     *
     * @return the relationship archetype short name
     */
    public String getArchetype() {
        return archetype;
    }

    /**
     * Returns the source lookup descriptor.
     *
     * @return the source lookup descriptor
     */
    public LookupDescriptor getSource() {
        return source;
    }

    /**
     * Returns the target lookup descriptor.
     *
     * @return the target lookup descriptor
     */
    public LookupDescriptor getTarget() {
        return target;
    }

    /**
     * Adds a code pair.
     *
     * @param sourceCode the source code
     * @param targetCode the target code
     */
    public void add(String sourceCode, String targetCode) {
        pairs.add(new Pair(sourceCode, targetCode));
    }

    /**
     * Returns the code pairs.
     *
     * @return the code pairs
     */
    public Set<Pair> getPairs() {
        return pairs;
    }

    /**
     * Clears the code pairs.
     */
    public void clear() {
        pairs.clear();
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
        if (!(other instanceof LookupRelationshipDescriptor)) {
            return false;
        }
        LookupRelationshipDescriptor object
                = (LookupRelationshipDescriptor) other;
        return archetype.equals(object.archetype);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return archetype.hashCode();
    }

}
