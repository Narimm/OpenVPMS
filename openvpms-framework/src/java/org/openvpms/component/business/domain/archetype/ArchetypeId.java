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

// java core
import java.io.Serializable;
import java.util.StringTokenizer;

// commons-lang
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Tne archetype id uniquely defines an archetype. It consists of the following
 * components
 * 
 *  <namespace>-<rmName>-<entityName>.<concept>.<version> 
 *  where
 *      namespace - is the originator of the archietype
 *      rmName - is the reference model name 
 *      entityName - is the entity name
 *      concept - is the concept attachd to the archetype
 *      version - is the version of the archetype
 *      
 *  examples      
 *      openvpms-party-person.person.1.0
 *      openvpms-party-address.phoneNumber.1.0
 *      openvpms-party-address.location.1.0
 *      
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeId implements Serializable, Cloneable {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The archetype id for a local lookup
     */
    public static final ArchetypeId LocalLookupId = 
            new ArchetypeId("openvpms-lookup-lookup.local.1.0");

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
     * The archetype name
     */
    private String concept;
    
    /**
     * The version number
     */
    private String version;
    
    /**
     * The fully qualified name
     */
    private String qualifiedName;
    
    /**
     * The short name
     */
    private String shortName;
    
    /**
     * The empty constructor, which can be used to construct a
     * partial archetype id for the purpose of searching. Do not use
     * this to construct a valid archetypeId
     */
    public ArchetypeId() {
        // do nothing
    }

    /**
     * Create an archetypeId from a fully qualified name
     * 
     * @param qname
     *            the fully qualified name
     * @throws ArchetypeIdException
     *            if an illegal archetype id has been specified            
     */
    public ArchetypeId(String qname) {
        parseQualifiedName(qname);
    }
    
    /**
     * Create an archetype id based on the following components. This 
     * information should be sufficient enough to identify a specific 
     * archetype.
     * <p>
     * The namespace, rmName and imClass can be concatenated to prov ide 
     * 
     * @param namespace
     *            the namespace that the archetype belongs too
     * @param rmName
     *            the reference model name 
     * @param entityName
     *            the entity name
     * @param concept
     *            the concept that the archetype denotes
     * @param version
     *            the version of the archetype
     * @throws ArchetypeIdExcpetion
     *            if a legal archetype id cannot be constructed.            
     */
    public ArchetypeId(String namespace, String rmName, String entityName,
        String concept, String version) {
        
        if ((StringUtils.isEmpty(namespace)) ||
            (StringUtils.isEmpty(rmName)) ||
            (StringUtils.isEmpty(concept)) ||
            (StringUtils.isEmpty(entityName)) ||
            (StringUtils.isEmpty(version))) {
            throw new ArchetypeIdException(
                    ArchetypeIdException.ErrorCode.EmptyElement);
        }
        
        this.namespace = namespace;
        this.rmName = rmName;
        this.concept = concept;
        this.entityName = entityName;
        this.version = version;
        
        // short name
        this.shortName =
            new StringBuffer(entityName)
                .append(".")
                .append(concept)
                .toString();
        
        //concatenate the elements
        this.qualifiedName = 
            new StringBuffer(namespace)
                .append("-")
                .append(rmName)
                .append("-")
                .append(entityName)
                .append(".")
                .append(concept)
                .append(".")
                .append(version)
                .toString();
    }

    /**
     * @return Returns the namespace.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @return Returns the version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return Returns the concept.
     */
    public String getConcept() {
        return concept;
    }

    /**
     * @return Returns the entityName.
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * @return Returns the qualifiedName.
     */
    public String getQualifiedName() {
        if (qualifiedName == null) {
            qualifiedName = new StringBuffer(namespace)
                .append("-")
                .append(rmName)
                .append("-")
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
     * @return Returns the rmName.
     */
    public String getRmName() {
        return rmName;
    }

    /**
     * Return the short name, which is the concatenation of the rmName and 
     * the concept and version number
     * 
     * @return String
     */
    public String getShortName() {
        if (shortName == null){
            shortName = new StringBuffer(entityName)
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
        return new EqualsBuilder()
            .append(getQualifiedName(), rhs.getQualifiedName())
            .isEquals();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(namespace)
            .append(rmName)
            .append(concept)
            .append(entityName)
            .append(version)
            .toHashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("qualifiedName", qualifiedName)
            .toString();
    }

    /**
     * @param concept The concept to set.
     */
    @SuppressWarnings("unused")
    private void setConcept(String concept) {
        this.concept = concept;
    }

    /**
     * @param entityName The entityName to set.
     */
    @SuppressWarnings("unused")
    private void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * @param namespace The namespace to set.
     */
    @SuppressWarnings("unused")
    private void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * @param name The qualifiedName to set.
     */
    @SuppressWarnings("unused")
    private void setQualifiedName(String name) {
        parseQualifiedName(name);
    }

    /**
     * @param rmName The rmName to set.
     */
    @SuppressWarnings("unused")
    private void setRmName(String rmName) {
        this.rmName = rmName;
    }

    /**
     * @param shortName The shortName to set.
     */
    @SuppressWarnings("unused")
    private void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * @param version The version to set.
     */
    @SuppressWarnings("unused")
    private void setVersion(String version) {
        this.version = version;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ArchetypeId copy = (ArchetypeId)super.clone();
        copy.concept = this.concept;
        copy.entityName = this.entityName;
        copy.namespace = this.namespace;
        copy.qualifiedName = this.qualifiedName;
        copy.rmName = this.rmName;
        copy.shortName = this.shortName;
        copy.version = this.version;
        
        return copy;
    }
    
    /**
     * parse the qualified archetype id as a string and decompose it in to
     * its constituent parts.
     * 
     * @param qname
     *            the qualified name
     * @throws ArchetypeIdException
     *            if an illegal archetype id has been specified            
     */
    private void parseQualifiedName(String qname) {
        if (StringUtils.isEmpty(qname)) {
            throw new ArchetypeIdException(
                    ArchetypeIdException.ErrorCode.EmptyQualifiedName);
        }
        
        // tokenise using the hyphen
        StringTokenizer tokens = new StringTokenizer(qname, "-");
        if (tokens.countTokens() != 3) {
            throw new ArchetypeIdException(
                    ArchetypeIdException.ErrorCode.InvalidQNameFormat,
                    new Object[] {qname});
        }
        
        namespace = tokens.nextToken();
        rmName = tokens.nextToken();
        
        String remaining = tokens.nextToken();
       
        // the short name is made up of entity name, concept and version
        tokens = new StringTokenizer(remaining, ".");
        if (tokens.countTokens() < 3) {
            throw new ArchetypeIdException(
                    ArchetypeIdException.ErrorCode.InvalidQNameFormat,
                    new Object[] {qname});
        }
        
        entityName = tokens.nextToken();
        concept = tokens.nextToken();
        shortName = entityName + "." + concept;
        
        // all the rest have to be the version number
        // which may have a '.'
        StringBuffer buf = new StringBuffer(tokens.nextToken());
        while (tokens.hasMoreTokens()) {
            buf.append(".").append(tokens.nextToken());
        }
        version = buf.toString();
        
        // store the qualified name
        qualifiedName = qname;
    }
}