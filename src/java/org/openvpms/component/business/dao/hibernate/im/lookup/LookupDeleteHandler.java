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

package org.openvpms.component.business.dao.hibernate.im.lookup;

import org.hibernate.Session;
import org.openvpms.component.business.dao.hibernate.im.common.AbstractDeleteHandler;
import org.openvpms.component.business.dao.hibernate.im.common.CompoundAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DeleteHandler;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;
import org.openvpms.component.business.dao.im.common.IMObjectDAOException;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;


/**
 * Implementation of {@link DeleteHandler} for {@link Lookup}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupDeleteHandler extends AbstractDeleteHandler {

    /**
     * The archetypes.
     */
    private IArchetypeDescriptorCache archetypes;

    /**
     * Creates a new <tt>LookupDeleteHandler<tt>.
     *
     * @param assembler  the assembler
     * @param archetypes the archetype descriptor cache
     */
    public LookupDeleteHandler(CompoundAssembler assembler,
                               IArchetypeDescriptorCache archetypes) {
        super(assembler);
        this.archetypes = archetypes;
    }

    /**
     * Deletes an object.
     *
     * @param object  the object to delete
     * @param session the session
     * @param context the assembly context
     */
    @Override
    public void delete(IMObject object, Session session, Context context) {
        if (isInUse((Lookup) object, session)) {
            throw new IMObjectDAOException(IMObjectDAOException.ErrorCode.CannotDeleteInUseLookup,
                                           object.getObjectReference());
        }
        super.delete(object, session, context);
    }

    /**
     * Deletes an object.
     * <p/>
     * This implementation removes relationships associated with the lookup
     * prior to its deletion.
     *
     * @param object  the object to delete
     * @param session the session
     * @param context the assembly context
     */
    @Override
    protected void delete(IMObjectDO object, Session session, Context context) {
        LookupDO lookup = (LookupDO) object;
        // remove relationships where the lookup is the source.
        LookupRelationshipDO[] relationships
                = lookup.getSourceLookupRelationships().toArray(
                new LookupRelationshipDO[lookup.getSourceLookupRelationships().size()]);
        for (LookupRelationshipDO relationhip : relationships) {
            lookup.removeSourceLookupRelationship(relationhip);
            LookupDO target = (LookupDO) relationhip.getTarget();
            if (target != null) {
                target.removeTargetLookupRelationship(relationhip);
            }
        }

        // now remove relationships where the lookup is the target
        relationships = lookup.getTargetLookupRelationships().toArray(
                new LookupRelationshipDO[lookup.getTargetLookupRelationships().size()]);
        for (LookupRelationshipDO relationship : relationships) {
            lookup.removeTargetLookupRelationship(relationship);
            LookupDO source = (LookupDO) relationship.getSource();
            if (source != null) {
                source.removeSourceLookupRelationship(relationship);
            }
        }
        context.remove(lookup);
    }

    private boolean isInUse(Lookup lookup, Session session) {
        LookupReplacer replacer = new LookupReplacer(archetypes);
        return replacer.isUsed(lookup, session);

    }

}
