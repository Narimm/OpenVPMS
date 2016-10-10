/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.prefs;

import org.openvpms.archetype.rules.util.MappingCopyHandler;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.List;

/**
 * Copies preferences.
 *
 * @author Tim Anderson
 */
class PreferencesCopier extends MappingCopyHandler {

    /**
     * Default constructor.
     */
    public PreferencesCopier() {
        setDefaultTreatment(Treatment.REFERENCE);
    }

    /**
     * Copies the supplied preferences, linking them to the specified party.
     *
     * @param prefs   the preferences to copy
     * @param party   the party to link to
     * @param service the archetype service
     * @return the copied preference object
     */
    public static List<IMObject> copy(Entity prefs, IMObjectReference party, IArchetypeService service) {
        IMObjectCopier copier = new IMObjectCopier(new PreferencesCopier(), service);
        List<IMObject> objects = copier.apply(prefs);
        Entity root = (Entity) objects.get(0);
        IMObjectBean bean = new IMObjectBean(root, service);
        bean.addNodeTarget("user", party);
        return objects;
    }

    /**
     * Determines how an object should be handled.
     * <p/>
     * This returns:
     * <ul>
     * <li>{@link Treatment#EXCLUDE EXCLUDE} if the object should be replaced
     * with {@code null}</li>
     * <li>{@link Treatment#REFERENCE REFERENCE} if the object should be
     * referenced</li>
     * <li>{@link Treatment#COPY COPY} if a new instance of the object should be
     * returned so it may be copied</li>
     * </ul>
     *
     * @param object the object
     * @return the type of behaviour to apply to the object
     */
    @Override
    protected Treatment getTreatment(IMObject object) {
        if (TypeHelper.isA(object, PreferenceArchetypes.PREFERENCES, PreferenceArchetypes.PREFERENCE_GROUPS,
                           PreferenceArchetypes.PREFERENCE_GROUP_LINKS)) {
            return Treatment.COPY;
        } else if (TypeHelper.isA(object, PreferenceArchetypes.USER_LINK)) {
            return Treatment.EXCLUDE;
        }
        return super.getTreatment(object);
    }

    /**
     * Helper to determine if a node is copyable.
     * </p>
     * This implementation excludes <em>id</em> nodes, derived nodes where the node is the target, and
     *
     * @param archetype the archetype descriptor
     * @param node      the node descriptor
     * @param source    if {@code true} the node is the source; otherwise its the target
     * @return {@code true} if the node is copyable; otherwise {@code false}
     */
    @Override
    protected boolean isCopyable(ArchetypeDescriptor archetype, NodeDescriptor node, boolean source) {
        return super.isCopyable(archetype, node, source) && !node.isHidden();
    }
}
