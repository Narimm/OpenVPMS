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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.product;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.NodeResolver;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.jxpath.JXPathHelper;

import java.util.Collection;

import static org.openvpms.archetype.rules.product.DemographicUpdateException.ErrorCode.FailedToEvaluate;
import static org.openvpms.archetype.rules.product.DemographicUpdateException.ErrorCode.InvalidDemographicUpdate;
import static org.openvpms.archetype.rules.product.DemographicUpdateException.ErrorCode.NoContext;


/**
 * Evaluates <em>lookup.demographicUpdates</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DemographicUpdater {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Constructs a new <tt>DemographicUpdater</tt>.
     */
    public DemographicUpdater() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Constructs a new <tt>DemographicUpdater</tt>.
     *
     * @param service the archetype service
     */
    public DemographicUpdater(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Evaluates a <em>lookup.demographicUpdate</em>.
     *
     * @param object the object to evaluate the demographic update on
     * @param update the demographic update to evaluate
     * @throws DemographicUpdateException if the update cannot be evaluated
     */
    public void evaluate(IMObject object, Lookup update) {
        if (!TypeHelper.isA(update, "lookup.demographicUpdate")) {
            throw new DemographicUpdateException(
                    InvalidDemographicUpdate, update.getName(),
                    update.getArchetypeId().getShortName());
        }
        try {
            Object context;
            IMObjectBean bean = new IMObjectBean(update, service);
            String expression = bean.getString("expression");
            String node = bean.getString("nodeName");
            if (!StringUtils.isEmpty(node)) {
                context = new NodeResolver(object, service).getObject(node);
            } else {
                context = object;
            }
            if (context == null) {
                throw new DemographicUpdateException(NoContext,
                                                     update.getName());
            }
            JXPathHelper.newContext(context).getValue(expression);
        } catch (DemographicUpdateException exception) {
            throw exception;
        } catch (Throwable exception) {
            throw new DemographicUpdateException(
                    FailedToEvaluate, exception, update.getName());
        }
    }

    /**
     * Evaluates a collection of <em>lookup.demographicUpdates</em>.
     *
     * @param object  the object to evaluate the demographic update on
     * @param updates the demographic updates to evaluate
     * @throws DemographicUpdateException if an update cannot be evaluated
     */
    public void evaluate(IMObject object, Collection<Lookup> updates) {
        for (Lookup update : updates) {
            evaluate(object, update);
        }
    }

}
