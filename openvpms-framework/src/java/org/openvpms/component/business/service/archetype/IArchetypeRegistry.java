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

package org.openvpms.component.business.service.archetype;


/**
 * This registry is used to manage the list of valid {@link ArchetypeRecord} for 
 * the system. The record binds an archetype to a information model version and
 * corresponding information model class.
 * <p>
 * Although there is no constraint on the value of name we would recommend using
 * a scheme that is easily readable (i.e. model.archetype). The name
 * person.consumer, would refer to the consumer archetype, which is based on the
 * person object.
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface IArchetypeRegistry {
    /**
     * Retrieve the {@link ArchetypeRecord} given a name
     * 
     * @param name 
     *            the name of the archetype
     * @return ArchetypeRecord
     *            the associated object or null if one does not exist.
     */
    public ArchetypeRecord getArchetypeRecord(String name);
}
