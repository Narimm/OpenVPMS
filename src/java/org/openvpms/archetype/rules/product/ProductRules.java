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

package org.openvpms.archetype.rules.product;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;

import java.util.List;

/**
 * Product rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z` $
 */
public class ProductRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Creates a new <tt>ProductRules</tt>.
     */
    public ProductRules() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>ProductRules</tt>.
     *
     * @param service the archetype service
     */
    public ProductRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Copies a product.
     *
     * @param product the product to copy
     * @return a copy of <tt>product</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Product copy(Product product) {
        IMObjectCopier copier = new IMObjectCopier(new ProductCopyHandler());
        List<IMObject> objects = copier.apply(product);
        Product copy = (Product) objects.get(0);
        String newName = "Copy Of " + copy.getName();
        copy.setName(newName);
        service.save(objects);
        return copy;
    }

}
