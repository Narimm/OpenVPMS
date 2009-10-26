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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.util;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Helper to cache <em>lookup.macro</em> instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 * @see MacroEvaluator
 */
public class MacroCache {

    /**
     * The macros. A map of codes and their corresponding jxpath expressions.
     */
    private Map<String, String> macros = new HashMap<String, String>();

    /**
     * The archetype service.
     */
    private final IArchetypeService archetypeService;

    /**
     * Lookup macro archetype short name.
     */
    private static final String LOOKUP_MACRO = "lookup.macro";


    /**
     * Constructs a new <tt>MacroCache</tt>.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    public MacroCache() {
        this(LookupServiceHelper.getLookupService(),
             ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Constructs a new <tt>MacroCache</tt>.
     *
     * @param service          the lookup service
     * @param archetypeService the archetype service
     * @throws ArchetypeServiceException for any archetype service error
     */
    public MacroCache(ILookupService service,
                      IArchetypeService archetypeService) {
        this.archetypeService = archetypeService;

        Collection<Lookup> lookups = service.getLookups(LOOKUP_MACRO);
        for (Lookup lookup : lookups) {
            if (lookup.isActive()) {
                add(lookup);
            }
        }
        archetypeService.addListener(LOOKUP_MACRO, new AbstractArchetypeServiceListener() {
            public void saved(IMObject object) {
                onSaved((Lookup) object);
            }

            public void removed(IMObject object) {
                delete((Lookup) object);
            }
        });
    }

    /**
     * Gets the jxpath expression for a given macro code.
     *
     * @param code the macro code
     * @return the corresponding expression, or <tt>null</tt> if none is found
     */
    public synchronized String getExpression(String code) {
        return macros.get(code);
    }

    /**
     * Invoked when a lookup is saved.
     *
     * @param lookup the lookup
     */
    private void onSaved(Lookup lookup) {
        if (lookup.isActive()) {
            add(lookup);
        } else {
            delete(lookup);
        }
    }

    /**
     * Adds a lookup to the cache.
     *
     * @param lookup the lookup to add
     */
    private synchronized void add(Lookup lookup) {
        IMObjectBean bean = new IMObjectBean(lookup, archetypeService);
        macros.put(lookup.getCode(), bean.getString("expression"));
    }

    /**
     * Removes a lookup from the cache.
     *
     * @param lookup the lookup to remove
     */
    private synchronized void delete(Lookup lookup) {
        macros.remove(lookup.getCode());
    }
}
