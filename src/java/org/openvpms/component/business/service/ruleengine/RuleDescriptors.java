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

// jav core
import java.util.ArrayList;
import java.util.List;

/**
 * Holds a collection of {@link RuleDescriptor} instances
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class RuleDescriptors {
    /**
     * A container for {@link RuleDescriptor} instances
     */
    private List<RuleDescriptor> ruleDescriptors = 
        new ArrayList<RuleDescriptor>();
    
    
    /**
     * Default constructor
     */
    public RuleDescriptors() {
        // do nothing
    }

    /**
     * @return Returns the ruleDescriptors.
     */
    public List<RuleDescriptor> getRuleDescriptors() {
        return ruleDescriptors;
    }

    /**
     * @param ruleDescriptors The ruleDescriptors to set.
     */
    public void setRuleDescriptors(List<RuleDescriptor> ruleDescriptors) {
        this.ruleDescriptors = ruleDescriptors;
    }
    
    /**
     * Add a new {@link RuleDescriptor}
     * 
     * @param descriptor
     *            the descriptor to add
     */
    public void addRuleDescriptor(RuleDescriptor descriptor) {
        this.ruleDescriptors.add(descriptor);
    }
    
    /**
     * Remove the specified {@link RuleDescriptor}
     * 
     * @param descriptor
     *            the descriptor to remove
     * @return boolean
     *            true if it was successfully removed            
     */
    public boolean removeRuleDescriptor(RuleDescriptor descriptor) {
        return this.ruleDescriptors.remove(descriptor);
    }
}
