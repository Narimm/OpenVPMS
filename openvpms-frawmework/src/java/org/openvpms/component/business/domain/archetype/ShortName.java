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

package org.openvpms.component.business.domain.archetype;

import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.StringTokenizer;


/**
 * An archetype short name. It consists of the
 * following components:
 * <p><tt>&lt;entityName&gt;.&lt;concept&gt;</tt>
 * <p>where:
 * <ul>
 * <li>entityName - is the entity name</li>
 * <li>concept - is the concept attached to the archetype</li>
 * </ul>
 * <p>Examples:
 * <ul>
 * <li>party.customer</li>
 * <li>contact.phoneNumber</li>
 * <li>contact.location</li>
 * </ul>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class ShortName {

    /**
     * The entity name.
     */
    private final String entityName;

    /**
     * The archetype concept.
     */
    private final String concept;

    /**
     * The short name. This is the concatenation of the entityName and
     * concept, cached for performance reasons.
     */
    private final String shortName;

    /**
     * A cache of ShortNames keyed on shortName, to avoid the expense of
     * parsing. These are weakly referenced so they can be reclaimed if memory
     * is low.
     */
    @SuppressWarnings("unchecked")
    private static final Map<String, ShortName> cache
            = Collections.synchronizedMap(new ReferenceMap());


    /**
     * Creates a new <tt>QualifiedName</tt>.
     *
     * @param entityName the entity name
     * @param concept    the archetype concept
     * @param shortName  the archetype short name Must be the concatenation
     *                   of the entity name and concept
     */
    private ShortName(String entityName, String concept, String shortName) {
        this.entityName = entityName;
        this.concept = concept;
        this.shortName = shortName;
    }

    /**
     * Returns the entity name.
     *
     * @return the entity name
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Returns the archetype concept.
     *
     * @return the archetype concept
     */
    public String getConcept() {
        return concept;
    }

    /**
     * Returns the short name. This is the concatenation of the entity name
     * and concept, separated by a '.'.
     *
     * @return the qualified name.
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * (non-Javadoc)
     *
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ShortName)) {
            return false;
        }
        ShortName rhs = (ShortName) obj;
        return ObjectUtils.equals(shortName, rhs.getShortName());
    }

    /**
     * (non-Javadoc)
     *
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return shortName.hashCode();
    }

    /**
     * Returns a <tt>ShortName</tt> for its corresponding short name string.
     *
     * @param shortName the short name name string
     * @return the corresponding <tt>ShortName</tt>
     * @throws ArchetypeIdException if an illegal qualified name has been
     *                              specified
     */
    public static ShortName get(String shortName) {
        ShortName result = cache.get(shortName);
        if (result == null) {
            result = parse(shortName);
            cache.put(shortName, result);
        }
        return result;
    }

    /**
     * Returns a <tt>ShortName</tt> for its corresponding entity name and
     * concept.
     *
     * @param entityName the entity name
     * @param concept    the concept
     * @return the corresponding <tt>ShortName</tt>
     */
    public static ShortName get(String entityName, String concept) {
        String shortName = entityName + "." + concept;
        ShortName result = cache.get(shortName);
        if (result == null) {
            result = new ShortName(entityName, concept, shortName);
            cache.put(shortName, result);
        }
        return result;
    }

    /**
     * Parses a short name.
     *
     * @param shortName the short name
     * @throws ArchetypeIdException if an illegal short name has been specified
     */
    private static ShortName parse(String shortName) {
        if (StringUtils.isEmpty(shortName)) {
            throw new ArchetypeIdException(
                    ArchetypeIdException.ErrorCode.EmptyShortName);
        }

        // the short name is made up of entity name and concept
        StringTokenizer tokens = new StringTokenizer(shortName, ".");
        if (tokens.countTokens() != 2) {
            throw new ArchetypeIdException(
                    ArchetypeIdException.ErrorCode.InvalidShortNameFormat,
                    shortName);
        }

        String entityName = tokens.nextToken();
        String concept = tokens.nextToken();
        return new ShortName(entityName, concept, shortName);
    }
}
