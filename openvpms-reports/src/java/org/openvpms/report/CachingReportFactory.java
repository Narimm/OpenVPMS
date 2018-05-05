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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.report;

import org.openvpms.archetype.function.factory.ArchetypeFunctionsFactory;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.service.archetype.CachingReadOnlyArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.cache.LRUIMObjectCache;

/**
 * An {@link ReportFactory} that caches objects to improve performance.
 * <p/>
 * This implementation supports caching of objects returned by the {@link IArchetypeService#get(org.openvpms.component.model.object.Reference)}
 * method. A {@link LRUIMObjectCache} is constructed for each report.
 *
 * @author Tim Anderson
 */
public class CachingReportFactory extends ReportFactory {

    /**
     * The maximum cache size.
     */
    private final int maxSize;

    /**
     * Constructs an {@link CachingReportFactory}.
     *
     * @param maxSize  the maximum cache size
     * @param service  the archetype service
     * @param lookups  the lookup service
     * @param handlers the document handlers
     * @param factory  the factory for JXPath extension functions
     */
    public CachingReportFactory(int maxSize, IArchetypeService service, ILookupService lookups,
                                DocumentHandlers handlers, ArchetypeFunctionsFactory factory) {
        super(service, lookups, handlers, factory);
        this.maxSize = maxSize;
    }

    /**
     * Proxies the archetype service.
     *
     * @param service service
     * @return the proxied service
     */
    @Override
    protected IArchetypeService proxy(IArchetypeService service) {
        return new CachingReadOnlyArchetypeService(maxSize, service);
    }

}
