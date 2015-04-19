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
 * A fully qualified archetype identifier. It consists of the
 * following components:
 * <p><tt>&lt;entityName&gt;.&lt;concept&gt;.&lt;version&gt;</tt>
 * <p>where:
 * <ul>
 * <li>entityName - is the entity name</li>
 * <li>concept - is the concept attached to the archetype</li>
 * <li>version - is the version of the archetype, and is optional</li>
 * </ul>
 * <p>Examples:
 * <ul>
 * <li>party.customer.1.0</li>
 * <li>contact.phoneNumber.1.0</li>
 * <li>contact.location.1.0</li>
 * </ul> * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 *
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class QualifiedName {

    /**
     * The short name.
     */
    private final ShortName shortName;

    /**
     * The archetype version. May be <tt>null</tt>.
     */
    private final String version;

    /**
     * The qualified name. This is the concatentation of the short name and
     * version, cached for performance reasons.
     */
    private final String qualifiedName;

    /**
     * A cache of QualifiedNames keyed on qualifiedName, to avoid the expense of
     * parsing. These are weakly referenced so they can be reclaimed if memory
     * is low.
     */
    @SuppressWarnings("unchecked")
    private static final Map<String, QualifiedName> cache
            = Collections.synchronizedMap(new ReferenceMap());

    /**
     * Creates a new <tt>QualifiedName</tt>.
     *
     * @param shortName     the archetype short name
     * @param version       the archetype version. May be <tt>null</tt>
     * @param qualifiedName the qualified name. Must be the concatenation of
     *                      the short name and version
     */
    private QualifiedName(ShortName shortName, String version,
                          String qualifiedName) {
        this.shortName = shortName;
        this.version = version;
        this.qualifiedName = qualifiedName;
    }

    /**
     * Returns the archetype short name.
     *
     * @return the archetype short name
     */
    public String getShortName() {
        return shortName.getShortName();
    }

    /**
     * Returns the archetype version.
     *
     * @return the archetype version. May be <tt>null</tt>
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the qualified name. This is the concatenation of the archetype
     * short name and version, separated by a '.'.
     *
     * @return the qualified name.
     */
    public String getQualifiedName() {
        return qualifiedName;
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

        if (!(obj instanceof QualifiedName)) {
            return false;
        }
        QualifiedName rhs = (QualifiedName) obj;
        if (shortName.equals(rhs.getShortName())) {
            return ObjectUtils.equals(version, rhs.version);
        }
        return false;
    }

    /**
     * (non-Javadoc)
     *
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        int versionHash = (version != null) ? version.hashCode() : 0;
        return shortName.hashCode() + versionHash;
    }

    /**
     * Returns a <tt>QualifiedName</tt> for its corresponding qualified name
     * string.
     *
     * @param qname the qualified name string
     * @return the corresponding <tt>QualifiedName</tt>
     * @throws ArchetypeIdException if an illegal qualified name has been
     *                              specified
     */
    public static QualifiedName get(String qname) {
        QualifiedName result = cache.get(qname);
        if (result == null) {
            result = parse(qname);
            cache.put(qname, result);
        }
        return result;
    }

    /**
     * Parses a qualified archetype id.
     *
     * @param qname the qualified archetype id
     * @throws ArchetypeIdException if an illegal qualified name has been
     *                              specified
     */
    private static QualifiedName parse(String qname) {
        if (StringUtils.isEmpty(qname)) {
            throw new ArchetypeIdException(
                    ArchetypeIdException.ErrorCode.EmptyQualifiedName);
        }

        // the qname is made up of entity name, concept and version
        StringTokenizer tokens = new StringTokenizer(qname, ".");
        if (tokens.countTokens() < 2) {
            throw new ArchetypeIdException(
                    ArchetypeIdException.ErrorCode.InvalidQNameFormat, qname);
        }

        String entityName = tokens.nextToken();
        String concept = tokens.nextToken();
        ShortName shortName = ShortName.get(entityName, concept);
        String version = null;

        if (tokens.hasMoreTokens()) {
            // all the rest have to be the version number which may
            // have a '.'
            StringBuffer buf = new StringBuffer(tokens.nextToken());
            while (tokens.hasMoreTokens()) {
                buf.append(".").append(tokens.nextToken());
            }
            version = buf.toString();
        }
        return new QualifiedName(shortName, version, qname);
    }

}
