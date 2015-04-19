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


package org.openvpms.component.system.common.query;


/**
 * Defines an archetype constraint, which can be constrained by primaryOnly and
 * activeOnly attributes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeConstraint extends BaseArchetypeConstraint {

    /**
     * Default SUID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Default constructor.
     */
    public ArchetypeConstraint() {
        this(false);
    }

    /**
     * Define a constraint using the specified parameter.
     *
     * @param activeOnly if <code>true</code> only deal with active entities
     */
    ArchetypeConstraint(boolean activeOnly) {
        this(false, activeOnly);
    }

    /**
     * Define a constraint using the specified parameters.
     *
     * @param primaryOnly if <code>true</code> only deal with primary archetypes
     * @param activeOnly  if <code>true</code> only deal with active entities
     */
    public ArchetypeConstraint(boolean primaryOnly, boolean activeOnly) {
        this(null, primaryOnly, activeOnly);
    }

    /**
     * Constructs a new <code>ArchetypeConstraint</code>.
     *
     * @param alias       the type alias. May be <code>null</code>
     * @param primaryOnly if <code>true</code> only deal with primary archetypes
     * @param activeOnly  if <code>true</code> only deal with active entities
     */
    public ArchetypeConstraint(String alias, boolean primaryOnly,
                               boolean activeOnly) {
        super(alias, primaryOnly, activeOnly);
    }

}
