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

package org.openvpms.insurance.internal.claim;

import org.openvpms.archetype.rules.patient.insurance.InsuranceArchetypes;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.business.service.security.RunAs;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.claim.Claims;
import org.openvpms.insurance.internal.InsuranceFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.shortName;
import static org.openvpms.component.system.common.query.Constraints.sort;

/**
 * Default implementation of {@link Claims}.
 *
 * @author Tim Anderson
 */
public class ClaimsImpl implements Claims {

    /**
     * The archetype service.
     */
    private final IArchetypeRuleService service;

    /**
     * The insurance factory.
     */
    private final InsuranceFactory factory;

    /**
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * A proxy for the archetype service that ensures that
     */
    private final IArchetypeRuleService proxy;

    /**
     * Constructs a {@link ClaimsImpl}.
     *
     * @param service         the archetype service
     * @param factory         the insurance factory
     * @param practiceService the practice service
     */
    public ClaimsImpl(IArchetypeRuleService service, InsuranceFactory factory, PracticeService practiceService) {
        this.service = service;
        this.factory = factory;
        this.practiceService = practiceService;
        proxy = createProxy();
    }

    /**
     * Returns a claim.
     * <p>
     * A claim can have a single identifier issued by an insurer. To avoid duplicates, each insurance service must
     * provide a unique archetype.
     *
     * @param archetype the identifier archetype. Must have an <em>actIdentity.insuranceClaim</em> prefix.
     * @param id        the claim identifier
     * @return the claim or {@code null} if none is found
     */
    @Override
    public Claim getClaim(String archetype, String id) {
        if (archetype == null || archetype.contains("*") ||
            !TypeHelper.matches(archetype, InsuranceArchetypes.CLAIM_IDENTITIES)) {
            throw new IllegalArgumentException("Argument 'archetype' is not a valid claim identity archetype: "
                                               + archetype);
        }
        ArchetypeQuery query = new ArchetypeQuery(InsuranceArchetypes.CLAIM);
        query.add(join("insurerId", shortName(archetype)).add(eq("identity", id)));
        query.add(sort("id"));
        query.setMaxResults(1);
        IMObjectQueryIterator<Act> iterator = new IMObjectQueryIterator<>(service, query);

        return (iterator.hasNext()) ? factory.createClaim(iterator.next(), proxy) : null;
    }

    /**
     * Proxies the archetype service so that calls are made in the security context of the practice service user.
     * <p>
     * TODO - this is a workaround as the PluginArchetypeService cannot be used until IArchetypeService extends
     * org.openvpms.component.service.archetype.ArchetypeService.
     *
     * @return the archetype service proxy
     */
    private IArchetypeRuleService createProxy() {
        InvocationHandler handler = (proxy, method, args) -> {
            User user = practiceService.getServiceUser();
            if (user == null) {
                throw new IllegalStateException(
                        "Cannot invoke ArchetypeService operation as no Service User has been configured");
            }

            if (method.getName().equals("getBean")) {
                return new IMObjectBean((IMObject) args[0], (IArchetypeRuleService) proxy);
            }
            return RunAs.run(user, () -> method.invoke(service, args));
        };
        Class<IArchetypeRuleService> type = IArchetypeRuleService.class;
        return (IArchetypeRuleService) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, handler);
    }

}
