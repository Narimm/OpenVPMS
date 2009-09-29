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

package org.openvpms.archetype.rules.act;

import org.openvpms.archetype.rules.util.MappingCopyHandler;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;


/**
 * A {@link MappingCopyHandler MappingCopyHandler} for acts.
 * <p/>
 * By default, this copies all {@link Act Acts},
 * {@link ActRelationship ActRelationships} and
 * {@link Participation Participations}, and reference all other objects.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class ActCopyHandler extends MappingCopyHandler {

    /**
     * Creates a new <tt>ActCopyHandler</tt>.
     */
    public ActCopyHandler() {
        this(null);
    }

    /**
     * Creates a new <tt>ActCopyHandler</tt>.
     *
     * @param shortNameMap a list of short name pairs, indicating the short name
     *                     to map from and to. If the 'to' short name is null,
     *                     then any instance of the 'from' is ignored
     */
    public ActCopyHandler(String[][] shortNameMap) {
        super(shortNameMap);
        setCopy(Act.class, ActRelationship.class, Participation.class);
        setDefaultTreatment(Treatment.REFERENCE);
    }

    /**
     * Helper to determine if a node is copyable.
     * <p/>
     * If the superclass implementation returns <tt>true</tt>, this implementation delegates to {@link #checkCopyable}.
     *
     * @param archetype the archetype descriptor
     * @param node      the node descriptor
     * @param source    if <tt>true</tt> the node is the source; otherwise its
     *                  the target
     * @return <tt>true</tt> if the node is copyable; otherwise <tt>false</tt>
     */
    @Override
    protected boolean isCopyable(ArchetypeDescriptor archetype, NodeDescriptor node, boolean source) {
        boolean result = super.isCopyable(archetype, node, source);
        if (result) {
            result = checkCopyable(archetype, node);
        }
        return result;
    }

    /**
     * Determines if a node is copyable.
     * <p/>
     * This implementation excludes act nodes named <em>startTime</em>, <em>status</em>, and <em>printed</em>.
     *
     * @param archetype the node's archetype descriptor
     * @param node the node
     * @return <tt>true</tt> if the node is copyable, otherwise <tt>false</tt>
     */
    protected boolean checkCopyable(ArchetypeDescriptor archetype, NodeDescriptor node) {
        boolean result = true;
        if (TypeHelper.matches(archetype.getShortName(), "act.*")) {
            String name = node.getName();
            result = !"startTime".equals(name) && !"status".equals(name) && !"printed".equals(name);
        }
        return result;
    }

}
