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

// openehr-java-kernel
import org.openehr.am.archetype.Archetype;
import org.openehr.rm.common.archetyped.Archetyped;

/**
 * This interface defines the services that are provided by the archetype
 * service. The client is able to return a archetype by name or by archetype
 * idenity.
 * <p>
 * This class depends on the acode implementation of the Java kernel.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface IArchetypeService {
    /**
     * Retrieve the {@link org.openehr.rm.common.archetyped.Archetyped} from a
     * name. The mapping from name to the actual Archetyped instance is a
     * deployment time issue.
     * <p>
     * If there is no Archetyped instance for the specified name then return 
     * null. 
     * 
     * @param name
     *            the common name
     * @return Archetyped
     */
    public Archetyped getArchetypeInfoForName(String name);
    
    /**
     * Retrieve the {@link Archetype} definition for the specified name. The 
     * mappinh between name and the archetype definition is a deployment time
     * issue.
     * <p>
     * If the specified name cannot be mapped then return null.
     * 
     * @param name
     *            the common name
     * @param Archetype            
     */
    public Archetype getArchetypeForName(String name);
}
