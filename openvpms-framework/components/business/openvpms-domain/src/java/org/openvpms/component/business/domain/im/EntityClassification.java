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

package org.openvpms.component.business.domain.im;

// openehr java kernel
import java.util.Set;

import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.common.archetyped.Link;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datatypes.quantity.DvInterval;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.ObjectID;

/**
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EntityClassification extends Locatable {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1430690868255233290L;

    /**
     * The period that this classification is valid for
     */
    private DvInterval<DvDateTime> activePeriod;

    /**
     * The associated {@link Entity}
     */
    private Entity entity;

    /**
     * The corresponding {@link Classification}
     */
    private Classification classification;

    /**
     * Constructs an entity classification.
     * 
     * @param uid
     *            a unique object identity
     * @param archetypeNodeId
     *            the node id for this archetype
     * @param name
     *            the name of this archetype
     * @param archetypeDetails
     *            descriptive meta data for the achetype
     * @param links
     *            null if not specified
     * @param entity
     *            the associated {@link Entity}
     * @param classification
     *            the corresponding classification
     * @param activePeriod
     *            the period that this is active                        
     * @throws IllegalArgumentException
     *             thrown if the preconditions are not met.
     */
    @FullConstructor
    public EntityClassification(
            @Attribute(name = "uid", required = true) ObjectID uid, 
            @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId, 
            @Attribute(name = "name", required = true)DvText name, 
            @Attribute(name = "archetypeDetails") Archetyped archetypeDetails, 
            @Attribute(name = "links") Set<Link> links, 
            @Attribute(name = "entity", required = true) Entity entity,
            @Attribute(name = "classification", required = true) Classification classification,
            @Attribute(name = " activePeriod", required = true) DvInterval<DvDateTime> activePeriod) {
        super(uid, archetypeNodeId, name, archetypeDetails, null, links);
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

    /*
     * (non-Javadoc)
     * 
     * @see org.openehr.rm.common.archetyped.Locatable#pathOfItem(org.openehr.rm.common.archetyped.Locatable)
     */
    @Override
    public String pathOfItem(Locatable item) {
        // TODO Auto-generated method stub
        return null;
    }

}
