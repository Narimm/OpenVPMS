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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.delete;

import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link EntityDeletionHandler}.
 *
 * @author Tim Anderson
 */
public class EntityDeletionHandlerTestCase extends AbstractAppTest {

    /**
     * Tests deletion of entities with entity relationships.
     */
    @Test
    public void testDeleteWithRelationships() {
        Party customer = TestHelper.createCustomer();

        EntityDeletionHandler<Party> customerHandler = createDeletionHandler(customer);
        assertTrue(customerHandler.canDelete());

        Party patient = TestHelper.createPatient(customer);
        assertFalse(customerHandler.canDelete());

        // verify the patient can be deleted, as it is the target of the relationship
        EntityDeletionHandler<Party> patientHandler = createDeletionHandler(patient);
        assertTrue(patientHandler.canDelete());

        patientHandler.delete(new LocalContext(), new HelpContext("foo", null));

        assertNull(get(patient));      // verify the patient is deleted
        customer = get(customer);      // verify the customer isn't
        assertNotNull(customer);

        // verify the customer can now be deleted, as it has no more relationships
        customerHandler = createDeletionHandler(customer);
        assertTrue(customerHandler.canDelete());
        customerHandler.delete(new LocalContext(), new HelpContext("foo", null));
        assertNull(get(customer));
    }

    /**
     * Tests deletion of entities with entity links.
     */
    @Test
    public void testDeleteWithLinks() {
        Entity type = ProductTestHelper.createProductType();
        Product product = ProductTestHelper.createMedication(type);

        EntityDeletionHandler<Product> productHandler = createDeletionHandler(product);
        assertTrue(productHandler.canDelete());

        EntityDeletionHandler<Entity> typeHandler = createDeletionHandler(type);
        assertFalse(typeHandler.canDelete());

        productHandler.delete(new LocalContext(), new HelpContext("foo", null));
        assertNull(get(product));
        assertNotNull(get(type));

        assertTrue(typeHandler.canDelete());
        typeHandler.delete(new LocalContext(), new HelpContext("foo", null));
        assertNull(get(type));
    }

    /**
     * Tests deletion of an entity with participation relationships.
     */
    @Test
    public void testDeletionWithParticipations() {
        Party customer = TestHelper.createCustomer();
        List<FinancialAct> invoice = FinancialTestHelper.createChargesCounter(new BigDecimal("100"),
                                                                              customer, TestHelper.createProduct(),
                                                                              ActStatus.POSTED);
        save(invoice);     // customer has participation relationships to the charge

        EntityDeletionHandler<Party> customerHandler = createDeletionHandler(customer);
        assertFalse(customerHandler.canDelete());
    }

    /**
     * Verifies that the {@link IMObjectDeletionHandlerFactory} returns {@link EntityDeletionHandler} for entities.
     */
    @Test
    public void testFactory() {
        IMObjectDeletionHandlerFactory factory = new IMObjectDeletionHandlerFactory(getArchetypeService());
        factory.setApplicationContext(applicationContext);

        Entity entity = ProductTestHelper.createProductType();
        Party party = TestHelper.createCustomer(false);
        User user = TestHelper.createClinician(false);
        Product product = TestHelper.createProduct();

        assertTrue(factory.create(entity) instanceof EntityDeletionHandler);
        assertTrue(factory.create(party) instanceof EntityDeletionHandler);
        assertTrue(factory.create(user) instanceof EntityDeletionHandler);
        assertTrue(factory.create(product) instanceof EntityDeletionHandler);
    }

    /**
     * Creates a new deletion handler for an entity.
     *
     * @param entity the entity
     * @return a new deletion handler
     */
    protected <T extends Entity> EntityDeletionHandler<T> createDeletionHandler(T entity) {
        IMObjectEditorFactory factory = applicationContext.getBean(IMObjectEditorFactory.class);
        return new EntityDeletionHandler<>(entity, factory, ServiceHelper.getTransactionManager(),
                                           ServiceHelper.getArchetypeService());
    }

}
