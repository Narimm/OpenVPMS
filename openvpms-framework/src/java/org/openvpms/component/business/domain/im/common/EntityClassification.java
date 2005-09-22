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

package org.openvpms.component.business.domain.im.common;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.datatypes.quantity.DvInterval;
import org.openvpms.component.business.domain.im.datatypes.quantity.datetime.DvDateTime;

/**
 * This class is used to broker the manay-to-many relationship between 
 * {@link Entity} and {@link Classification}.
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EntityClassification extends IMObject {

    /**
     * Generated SUID.
     */
    private static final long serialVersionUID = 1430690868255233290L;

    /**
     * The period that this classification is valid for.
     */
    private DvInterval<DvDateTime> activePeriod;

    /**
     * The associated {@link Entity}.
     */
    private Entity entity;

    /**
     * The corresponding {@link Classification}.
     */
    private Classification classification;

    
    /**
     * Default constructor
     */
    public EntityClassification() {
        // default constructor 
    }
    
    /**
     * Constructs an entity classification.
     * 
     * @param uid
     *            uniquely identifies this object
     * @param archetypeId
     *            the archetype id constraining this object
     * @param entity
     *            the associated {@link Entity}
     * @param classification
     *            the corresponding classification
     * @param activePeriod
     *            the period that this is active                        
     */
    public EntityClassification(String uid, ArchetypeId archetypeId, 
            Entity entity, Classification classification,
            DvInterval<DvDateTime> activePeriod) {
        super(uid, archetypeId);
        this.entity = entity;
        this.classification = classification;
        this.activePeriod = activePeriod;
    }
    
    
    /**
     * @return Returns the activePeriod.
     */
    public DvInterval<DvDateTime> getActivePeriod() {
        return activePeriod;
    }

    /**
     * @param activePeriod
     *            The activePeriod to set.
     */
    public void setActivePeriod(DvInterval<DvDateTime> activePeriod) {
        this.activePeriod = activePeriod;
    }

    /**
     * @return Returns the classification.
     */
    public Classification getClassification() {
        return classification;
    }

    /**
     * @param classification
     *            The classification to set.
     */
    public void setClassification(Classification classification) {
        this.classification = classification;
    }

    /**
     * @return Returns the entity.
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * @param entity
     *            The entity to set.
     */
    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}
