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

// java core
import java.util.ArrayList;
import java.util.List;

// openvpms-framework
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;

/**
 * This is a helper class for the archetype service.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ArchetypeServiceHelper {
    /**
     * This is a static method that will return a list of candidate children 
     * given an reference to the archetype service, the node descriptor for 
     * the node in question and the context object.
     * 
     * @param service
     *            the archetype service
     * @param ndesc
     *            the node descriptor
     * @param context
     *            the context object            
     * @return List<IMObject>
     */
    public static List<IMObject> getCandidateChildren(IArchetypeService service,
            NodeDescriptor ndesc, IMObject context) {

        // find the node descriptor
        if (ndesc == null) {
            return null;
        }
        
        // check that the node is a collection and that the parentChild
        // attribute is set to false
        if (!(ndesc.isCollection()) ||
            (ndesc.isParentChild())) {
            return null;
        }
                
        // now there are two ways that candidate children cna be specified
        // Firstly they can be specified using the candidateChildren assertion
        // and secondly they can be specified using the archetypeRange
        // assertion
        List<IMObject> children = new ArrayList<IMObject>();
        if (ndesc.containsAssertionType("candidateChildren")) {
            children = ndesc.getCandidateChildren(context);
        } else if (ndesc.containsAssertionType("archetypeRange")) {
            children = service.get(ndesc.getArchetypeRange(), true);
        }
        
        return children;
    }

}
