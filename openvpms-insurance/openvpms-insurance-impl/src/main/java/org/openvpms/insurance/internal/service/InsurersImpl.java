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

package org.openvpms.insurance.internal.service;

import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.insurance.service.Insurers;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.shortName;

/**
 * Default implementation of the {@link Insurers} interface.
 *
 * @author Tim Anderson
 */
public class InsurersImpl implements Insurers {

    /**
     * The archetype service.
     */
    private final IArchetypeRuleService service;

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * Constructs an {@link InsurersImpl}.
     *
     * @param service            the archetype service
     * @param transactionManager the transaction manager
     */
    public InsurersImpl(IArchetypeRuleService service, PlatformTransactionManager transactionManager) {
        this.service = service;
        this.transactionManager = transactionManager;
    }

    /**
     * Returns an insurer.
     *
     * @param archetype the insurer identity archetype
     * @param insurerId the insurer identifier
     * @return the insurer, or {@code null} if none is found. The returned insurer may be inactive
     */
    @Override
    public Party getInsurer(String archetype, String insurerId) {
        if (!TypeHelper.matches(archetype, "entityIdentity.insurer*")) {
            throw new IllegalStateException("Invalid insurer identity archetype: " + archetype);
        }
        ArchetypeQuery query = new ArchetypeQuery(SupplierArchetypes.INSURER, false);
        query.add(join("insurerId", shortName(archetype, false)).add(eq("identity", insurerId)));
        query.add(Constraints.sort("id"));
        query.setMaxResults(1);
        IMObjectQueryIterator<IMObject> iterator = new IMObjectQueryIterator<>(service, query);
        return iterator.hasNext() ? (Party) iterator.next() : null;
    }

    /**
     * Returns all insurers with the specified insurer identity archetype.
     *
     * @param archetype  the insurer identity archetype
     * @param activeOnly if {@code true}, only return active insurers
     * @return the insurers
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Party> getInsurers(String archetype, boolean activeOnly) {
        if (!TypeHelper.matches(archetype, "entityIdentity.insurer*")) {
            throw new IllegalStateException("Invalid insurer identity archetype: " + archetype);
        }
        ArchetypeQuery query = new ArchetypeQuery(SupplierArchetypes.INSURER, activeOnly);
        query.add(join("insurerId", shortName(archetype, false)));
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        IPage page = service.get(query);
        return page.getResults();
    }

    /**
     * Creates and saves an insurer.
     *
     * @param archetype        the insurer identity archetype
     * @param insurerId        the insurer identifier. This must be unique
     * @param name             the insurer name
     * @param description      the insurer description. May be {@code null}
     * @param insuranceService the service that manages claims for this insurer
     * @return a new insurer
     */
    @Override
    public Party createInsurer(final String archetype, final String insurerId, final String name,
                               String description, final Entity insuranceService) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        return template.execute(transactionStatus -> {
            Party existing = getInsurer(archetype, insurerId);
            if (existing != null) {
                throw new IllegalStateException("An insurer already exists with insurerId=" + insurerId);
            }
            Party insurer = (Party) service.create(SupplierArchetypes.INSURER);
            EntityIdentity identity = (EntityIdentity) service.create(archetype);
            if (identity == null) {
                throw new IllegalStateException("Invalid archetype: " + archetype);
            }
            identity.setIdentity(insurerId);
            insurer.setName(name);
            insurer.setDescription(description);
            insurer.addIdentity(identity);
            IMObjectBean bean = service.getBean(insurer);
            bean.addTarget("service", insuranceService);
            bean.save();
            return insurer;
        });
    }

    /**
     * Returns the insurer identifier.
     *
     * @param insurer the insurer
     * @return the insurer identifier, or {@code null} if the insurer doesn't have one
     */
    @Override
    public String getInsurerId(Party insurer) {
        IMObjectBean bean = service.getBean(insurer);
        EntityIdentity id = bean.getObject("insurerId", EntityIdentity.class);
        return id != null ? id.getIdentity() : null;
    }
}
