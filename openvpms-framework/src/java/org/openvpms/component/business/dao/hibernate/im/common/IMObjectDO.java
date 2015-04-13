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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.common;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;

import java.util.Date;
import java.util.Map;


/**
 * Data object interface corresponding to the {@link IMObject} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface IMObjectDO {

    /**
     * Returns the archetype identifier.
     *
     * @return the archetype identifier
     */
    ArchetypeId getArchetypeId();

    /**
     * Sets the archetype identifier.
     *
     * @param archetypeId the archetype identifier
     */
    void setArchetypeId(ArchetypeId archetypeId);

    /**
     * Returns the object identifier.
     * <p/>
     * This is assigned when the object is made persistent.
     *
     * @return the object identifer, or <tt>-1</tt> if the object is not
     *         persistent
     */
    long getId();

    /**
     * Sets the object identifier.
     *
     * @param id the object identifier. If <tt>-1</tt>, indicates that the
     *           object is not persistent
     */
    void setId(long id);

    /**
     * Returns the object link identifier.
     * <p/>
     * This is a UUID that is used to link objects until they can be made
     * persistent, and to provide support for object equality.
     *
     * @return the link identifier
     */
    String getLinkId();

    /**
     * Sets the object link identifier.
     *
     * @param linkId the link identifier
     */
    void setLinkId(String linkId);

    /**
     * Returns an object reference for this object.
     *
     * @return the object reference
     */
    IMObjectReference getObjectReference();

    /**
     * Returns the object version.
     * <p/>
     * This is the persistent version of the object in the database, and is
     * incremented each time the object is committed. It is used to prevent
     * concurrent modification.
     *
     * @return returns the version
     */
    long getVersion();

    /**
     * Sets the object version.
     *
     * @param version the version
     */
    void setVersion(long version);

    /**
     * Returns the object name.
     *
     * @return the name. May be <tt>null</tt>
     */
    String getName();

    /**
     * Sets the object name.
     *
     * @param name the object name. May be <tt>null</tt>
     */
    void setName(String name);

    /**
     * Returns the object description.
     *
     * @return the description. May be <tt>null</tt>
     */
    String getDescription();

    /**
     * Sets the object description.
     *
     * @param description The description. May be <tt>null</tt>
     */
    void setDescription(String description);

    /**
     * Returns the timestamp when the object was last modified.
     *
     * @return the last modified timestamp. May be <tt>null</tt>
     */
    Date getLastModified();

    /**
     * Sets the timestamp when the object was last modified.
     *
     * @param lastModified the last modified timestamp
     */
    void setLastModified(Date lastModified);

    /**
     * Determines if the object is active.
     *
     * @return <tt>true</tt> if the object is active, <tt>false</tt> if it
     *         is inactive
     */
    boolean isActive();

    /**
     * Determines if the object is active.
     *
     * @param active if <tt>true</tt>, the object is active, otherwise it is
     *               inactive
     */
    void setActive(boolean active);

    /**
     * Determines if the object is new. A new object is one that has not
     * been made persistent.
     *
     * @return <tt>true</tt> if the object is new, <tt>false</tt> if it has
     *         been made persistent
     */
    boolean isNew();

    /**
     * Returns a map of named objects, used to represent the dynamic details
     * of this.
     *
     * @return the details
     */
    Map<String, Object> getDetails();

    /**
     * Sets a map of named objects, used to represent the dynamic details
     * of the this.
     *
     * @param details the details to set
     */
    void setDetails(Map<String, Object> details);

}
