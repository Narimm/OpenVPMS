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
import org.openvpms.component.business.domain.im.common.IMObjectReference;

import java.util.Date;
import java.util.Map;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface IMObjectDO {
    /**
     * @return Returns the archetypeId.
     */
    ArchetypeId getArchetypeId();

    /**
     * Create and return an {@link IMObjectReference} for this object
     *
     * @return IMObjectReference
     */
    IMObjectReference getObjectReference();

    /**
     * @return Returns the description.
     */
    String getDescription();

    /**
     * @return Returns the lastModified.
     */
    Date getLastModified();

    /**
     * @return Returns the name.
     */
    String getName();

    /**
     * @return Returns the id.
     */
    long getId();

    /**
     * @param id The id to set.
     */
    void setId(long id);

    void setLinkId(String linkId);

    String getLinkId();

    /**
     * @return Returns the version.
     */
    long getVersion();

    /**
     * @return Returns the active.
     */
    boolean isActive();

    /**
     * Return true if this is a new object and false otherwise. A new object
     * is one that has been created but not yet persisted
     *
     * @return boolean
     */
    boolean isNew();

    /**
     * @param active The active to set.
     */
    void setActive(boolean active);

    /**
     * @param archetypeId The archetypeId to set.
     */
    void setArchetypeId(ArchetypeId archetypeId);

    /**
     * @param description The description to set.
     */
    void setDescription(String description);

    /**
     * @param lastModified The lastModified to set.
     */
    void setLastModified(Date lastModified);

    /**
     * @param name The name to set.
     */
    void setName(String name);

    /**
     * @return Returns the details.
     */
    Map<String, Object> getDetails();

    /**
     * @param details The details to set.
     */
    void setDetails(Map<String, Object> details);

    /**
     * @param version The version to set.
     */
    void setVersion(long version);
}
