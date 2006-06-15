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


package org.openvpms.component.business.service.ruleengine;

// springmodules-jsr94
import org.springmodules.jsr94.rulesource.AbstractRuleSource;

/**
 * All rule source classes must extend this abstract class, which adds 
 * one additional method over the {@link AbstractRuleSource} class. 
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public abstract class BaseRuleSource extends AbstractRuleSource {
    /**
     * Indicates whether there is a rule set defined for the specified 
     * uri
     * 
     * @param uri
     *            the rule set uri
     */
    public abstract boolean hasRuleExecutionSet(String uri);
}
