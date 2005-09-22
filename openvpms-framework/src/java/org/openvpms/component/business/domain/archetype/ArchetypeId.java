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
 * This class define an archetype id, which can later be resolved to a set of
 * archetype constraints. An archetype id is defined by a namespace, name and
 * version.
 * <p>
 * Both namespace and version are optional. The empty namespace is also known as
 * the default namespace.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeId implements Serializable {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The namespace
     */
    private String namespace;
    
    /**
     * The reference model name
     */
    private String rmName;
    
    /**
     * The archetype name
     */
    private String concept;
    
    /**
     * The domain class name
     */
    private String imClass;

    /**
     * The version number
     */
    private String version;
    
    /**
     * The fully qualified name
     */
    private String qName;

    /**
     * The empty constructor
     */
    protected ArchetypeId() {
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
        if (StringUtils.isEmpty(qname)) {
            throw new ArchetypeIdException(
                    ArchetypeIdException.ErrorCode.EmptyQualifiedName);
        }
        
        // tokenise using the hyphen
        StringTokenizer tokens = new StringTokenizer(qname, "-");
        if (tokens.countTokens() != 5) {
            throw new ArchetypeIdException(
                    ArchetypeIdException.ErrorCode.InvalidQNameFormat,
                    new Object[] {qname});
        }
        
        this.namespace = tokens.nextToken();
        this.rmName = tokens.nextToken();
        this.imClass = tokens.nextToken();
        this.concept = tokens.nextToken();
        this.version = tokens.nextToken();
        this.qName = qname;
        
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
     * @param concept
     *            the concept that the archetype denotes
     * @param imClass
     *            the information model class name
     * @param version
     *            the version of the archetype
     * @throws ArchetypeIdExcpetion
     *            if a legal archetype id cannot be constructed.            
     */
    public ArchetypeId(String namespace, String rmName, String concept, 
        String imClass, String version) {
        
        if ((StringUtils.isEmpty(namespace)) ||
            (StringUtils.isEmpty(rmName)) ||
            (StringUtils.isEmpty(concept)) ||
            (StringUtils.isEmpty(imClass)) ||
            (StringUtils.isEmpty(version))) {
            throw new ArchetypeIdException(
                    ArchetypeIdException.ErrorCode.EmptyElement);
        }
        
        this.namespace = namespace;
        this.rmName = rmName;
        this.concept = concept;
        this.imClass = imClass;
        this.version = version;
        
        //concatenate the elements
        this.qName = 
            new StringBuffer(namespace)
                .append("-")
                .append(rmName)
                .append("-")
                .append(imClass)
                .append("-")
                .append(concept)
                .append("-")
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
     * @return Returns the imClass.
     */
    public String getImClass() {
        return imClass;
    }

    /**
     * @return Returns the qName.
     */
    public String getQName() {
        return qName;
    }

    /**
     * @return Returns the rmName.
     */
    public String getRmName() {
        return rmName;
    }

    /**
     * Return the fully qualified name of the matching java class
     * 
     * @return String
     */
    public String getJavaClassName() {
        if (this.getNamespace().equals("org.openvpms")) {
            return new StringBuilder()
                .append("org.openvpms.component.business.domain.im.")
                .append(this.getImClass())
                .toString();
        } else {
            return this.getImClass();
        }
    }
    
    /**
     * Return the short name, which is the concatenation of the rmName and 
     * the concept
     * 
     * @return String
     */
    public String getShortName() {
        return new StringBuffer(rmName)
            .append(".")
            .append(concept)
            .toString();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        // ensure they are the same type
        if (obj instanceof ArchetypeId == false) {
            return false;
        }

        // if they are the same object then return true
        if (this == obj) {
            return true;
        }
        
        ArchetypeId rhs = (ArchetypeId) obj;
        return new EqualsBuilder()
            .append(namespace, rhs.namespace)
            .append(concept, rhs.concept)
            .append(rmName, rhs.rmName)
            .append(imClass, rhs.imClass)
            .append(version, rhs.version)
            .isEquals();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(namespace)
            .append(concept)
            .append(rmName)
            .append(imClass)
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
            .append("namespace", namespace)
            .append("concept", concept)
            .append("rmName", rmName)
            .append("imClass", imClass)
            .append("version", version)
            .toString();
    }
}