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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.history;

import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.query.criteria.CriteriaBuilder;
import org.openvpms.component.query.criteria.CriteriaQuery;
import org.openvpms.component.query.criteria.Root;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A predicate that evaluates true if an act has product with a matching product type.
 *
 * @author Tim Anderson
 */
public class ProductTypeSearch implements Predicate<Act> {

    /**
     * The product types.
     */
    private final Set<Reference> productTypes;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a {@link ProductTypeSearch}.
     *
     * @param productTypes the product types to search for
     * @param service      the archetype service
     */
    public ProductTypeSearch(Set<Reference> productTypes, IArchetypeService service) {
        this.productTypes = productTypes;
        this.service = service;
    }

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param act the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    @Override
    public boolean test(Act act) {
        boolean result = false;
        IMObjectBean bean = service.getBean(act);
        if (bean.hasNode("product")) {
            Reference product = bean.getTargetRef("product");
            if (product != null) {
                CriteriaBuilder cb = service.getCriteriaBuilder();
                CriteriaQuery<Reference> query = cb.createQuery(Reference.class);
                Root<Product> root = query.from(Product.class, product.getArchetype()).alias("product");
                query.select(root.join("type").get("target"));
                query.where(cb.equal(root.reference(), product));
                List<Reference> references = service.createQuery(query).setMaxResults(1).getResultList();
                result = (!references.isEmpty()) && productTypes.contains(references.get(0));
            }
        }
        return result;
    }
}
