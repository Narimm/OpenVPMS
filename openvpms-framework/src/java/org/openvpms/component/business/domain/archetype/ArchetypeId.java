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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.domain.archetype;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.StringTokenizer;


/**
 * The archetype identifier uniquely defines an archetype. It consists of the
 * following components:
 * <p><tt>&lt;entityName&gt;.&lt;concept&gt;.&lt;version&gt;</tt>
 * <p>where:
 * <ul>
 * <li>entityName - is the entity name</li>
 * <li>concept - is the concept attached to the archetype</li>
 * <li>version - is the version of the archetype</li>
 * </ul>
 * <p>Examples:
 * <ul>
 * <li>party.customer.1.0</li>
 * <li>contact.phoneNumber.1.0</li>
 * <li>contact.location.1.0</li>
 * </ul>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeId implements Serializable, Cloneable {

    /**
     * Serialisation version identifier.
     */
    private static final long serialVersionUID = 2L;

    /**
     * The archetype id for a local lookup.
     */
    public static final ArchetypeId LOCAL_LOOKUP_ID
            = new ArchetypeId("lookup.local.1.0");

    /**
     * The namespace
     */
    private String namespace;

    /**
     * The reference model name(i.e. equivalent to the package name)
     */
    private String rmName;

    /**
     * The entity name (i.e. equivalent to the
     */
    private String entityName;

    /**
     * The archetype concept.
     */
    private String concept;

    /**
     * The version number.
     */
    private String version;

    /**
     * The fully qualified name. The concatenation of the entityName,
     * concept and version.
     */
    private String qualifiedName;

    /**
     * The short name. This is the concatenation of the entityName and
     * concept.
     */
    private String shortName;


    /**
     * Constructor provided for serialization purposes.
     */
    protected ArchetypeId() {
        // do nothing
    }

    /**
     * Create an archetypeId from a fully qualified name.
     *
     * @param qname the fully qualified name
     * @throws ArchetypeIdException if an illegal archetype id has been
     *                              specified
     */
    public ArchetypeId(String qname) {
        parseQualifiedName(qname);
    }

    /**
     * Create an archetype id based on the following components.
     *
     * @param entityName the entity name
     * @param concept    the concept that the archetype denotes
     * @param version    the version of the archetype
     * @throws ArchetypeIdException if a legal archetype id cannot be
     *                              constructed.
     */
    public ArchetypeId(String entityName, String concept, String version) {
        if (StringUtils.isEmpty(concept) || StringUtils.isEmpty(entityName)
                || StringUtils.isEmpty(version)) {
            throw new ArchetypeIdException(
                    ArchetypeIdException.ErrorCode.EmptyElement);
        }

        this.concept = concept;
        this.entityName = entityName;
        this.version = version;
    }

    /**
     * Returns the archetype entity name.
     *
     * @return the entity name
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Returns the archetype concept.
     *
     * @return the concept
     */
    public String getConcept() {
        return concept;
    }

    /**
     * Returns the archetype version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the qualified name. This is the concatenation of the
     * entity name, concept and version.
     *
     * @return the qualified name
     */
    public String getQualifiedName() {
        if (qualifiedName == null) {
            qualifiedName = new StringBuffer()
                    .append(entityName)
                    .append(".")
                    .append(concept)
                    .append(".")
                    .append(version)
                    .toString();
        }

        return qualifiedName;
    }

    /**
     * @return the namespace
     * @deprecated no replacement
     */
    public String getNamespace() {
        return namespace;
    }
    
    /**
     * @return Returns the rmName.
     * @deprecated no replacement
     */
    @Deprecated
    public String getRmName() {
        return rmName;
    }

    /**
     * Return the short name, which is the concatenation of the rmName and
     * name, concept and version number.
     *
     * @return the archetype short name
     */
    public String getShortName() {
        if (shortName == null) {
            shortName = new StringBuffer()
                    .append(entityName)
                    .append(".")
                    .append(concept)
                    .toString();
        }

        return shortName;
    }

    /*
    * (non-Javadoc)
    *
    * @see java.lang.Object#equals(java.lang.Object)
    */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ArchetypeId)) {
            return false;
        }
        ArchetypeId rhs = (ArchetypeId) obj;
        return ObjectUtils.equals(getQualifiedName(), rhs.getQualifiedName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return getQualifiedName().hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getQualifiedName();
    }

    /**
     * @param namespace The namespace to set.
     * @deprecated no replacement
     */
    @Deprecated
    protected void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * @param rmName The rmName to set.
     * @deprecated no replacement
     */
    @Deprecated
    protected void setRmName(String rmName) {
        this.rmName = rmName;
    }

    /**
     * Sets the entity name.
     *
     * @param entityName the entity name
     */
    protected void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * Sets the archetype concept.
     *
     * @param concept the concept to set
     */
    protected void setConcept(String concept) {
        this.concept = concept;
    }

    /**
     * Sets the qualified name.
     *
     * @param qname the qualified name
     * @throws ArchetypeIdException if an illegal archetype id has been
     *                              specified
     */
    protected void setQualifiedName(String qname) {
        parseQualifiedName(qname);
    }

    /**
     * Sets the short name.
     *
     * @param shortName the short name to set
     * @throws ArchetypeIdException if an illegal short name has been specified
     */
    protected void setShortName(String shortName) {
        parseShortName(shortName);
    }

    /**
     * Sets the version.
     *
     * @param version the version to set
     */
    protected void setVersion(String version) {
        this.version = version;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ArchetypeId copy = (ArchetypeId) super.clone();
        copy.concept = this.concept;
        copy.entityName = this.entityName;
        copy.qualifiedName = this.qualifiedName;
        copy.shortName = this.shortName;
        copy.version = this.version;

        return copy;
    }

    /**
     * Parses a qualified archetype id.
     *
     * @param qname the qualified archetype id
     * @throws ArchetypeIdException if an illegal archetype id has been
     *                              specified
     */
    protected void parseQualifiedName(String qname) {
        if (StringUtils.isEmpty(qname)) {
            throw new ArchetypeIdException(
                    ArchetypeIdException.ErrorCode.EmptyQualifiedName);
        }

        // the qname is made up of entity name, concept and version
        StringTokenizer tokens = new StringTokenizer(qname, ".");
        if (tokens.countTokens() < 3) {
            throw new ArchetypeIdException(
                    ArchetypeIdException.ErrorCode.InvalidQNameFormat, qname);
        }

        entityName = tokens.nextToken();
        concept = tokens.nextToken();

        // all the rest have to be the version number which may have a '.'
        StringBuffer buf = new StringBuffer(tokens.nextToken());
        while (tokens.hasMoreTokens()) {
            buf.append(".").append(tokens.nextToken());
        }
        version = buf.toString();

        // store the qualified name
        qualifiedName = qname;
    }

    /**
     * Parses a short name.
     *
     * @param shortName the short name
     * @throws ArchetypeIdException if an illegal short name has been specified
     */
    protected void parseShortName(String shortName) {
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

        entityName = tokens.nextToken();
        concept = tokens.nextToken();

        // store the short name
        this.shortName = shortName;
    }

}