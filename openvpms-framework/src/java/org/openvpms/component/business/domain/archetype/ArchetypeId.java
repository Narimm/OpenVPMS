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

// commons-lang
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
     * The archetype name
     */
    private String name;

    /**
     * The version number
     */
    private String version;

    /**
     * The empty constructor
     */
    protected ArchetypeId() {
        // do nothing
    }

    /**
     * Generate an archetype id with namespace, name and version.
     * 
     * @param namespace
     * @param name
     * @param version
     */
    public ArchetypeId(String namespace, String name, String version) {
        this.namespace = namespace;
        this.name = name;
        this.version = version;
    }

    /**
     * Generate an archetype id with namespace and name.
     * 
     * @param namespace
     * @param name
     */
    public ArchetypeId(String namespace, String name) {
        this.namespace = namespace;
        this.name = name;
    }

    /**
     * Generate an archetype id with name.
     * 
     * @param name
     */
    public ArchetypeId(String name) {
        this.name = name;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the namespace.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @param namespace
     *            The namespace to set.
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * @return Returns the version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version
     *            The version to set.
     */
    public void setVersion(String version) {
        this.version = version;
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
            .appendSuper(super.equals(obj))
            .append(namespace, rhs.namespace)
            .append(name, rhs.name)
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
            .append(version)
            .append(name)
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
            .append("name", name)
            .append("version", version)
            .toString();
    }
}