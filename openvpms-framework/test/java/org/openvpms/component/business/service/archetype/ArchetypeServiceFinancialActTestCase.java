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

package org.openvpms.component.business.service.archetype;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.math.BigDecimal;
import java.util.Date;


/**
 * Test that ability to create and query on financial acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@SuppressWarnings("HardCodedStringLiteral")
public class ArchetypeServiceFinancialActTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    private ArchetypeService service;

    /**
     * Test the creation of a FinancialAct using the {@link NodeDescriptor}s.
     */
    public void testFinancialActCreationThruNodeDescriptors()
            throws Exception {
        FinancialAct act = (FinancialAct) service.create("financial.act");
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(
                "financial.act");
        NodeDescriptor ndesc;

        // set the name node
        ndesc = adesc.getNodeDescriptor("name");
        assertNotNull(ndesc);
        ndesc.setValue(act, "name.doc");
        assertTrue(act.getName().equals("name.doc"));

        // set the quantity node
        ndesc = adesc.getNodeDescriptor("quantity");
        assertNotNull(ndesc);
        ndesc.setValue(act, new BigDecimal(1));
        assertTrue(act.getQuantity().equals(new BigDecimal(1)));

        // set the fixed amount node
        ndesc = adesc.getNodeDescriptor("fixedAmount");
        assertNotNull(ndesc);
        ndesc.setValue(act, new Money(1));
        assertTrue(act.getFixedAmount().equals(new Money(1)));

        // set the unit amount node
        ndesc = adesc.getNodeDescriptor("unitAmount");
        assertNotNull(ndesc);
        ndesc.setValue(act, new Money(2));
        assertTrue(act.getUnitAmount().equals(new Money(2)));

        // set the fixed cost node
        ndesc = adesc.getNodeDescriptor("fixedCost");
        assertNotNull(ndesc);
        ndesc.setValue(act, new Money(3));
        assertTrue(act.getFixedCost().equals(new Money(3)));

        // set the unit cost node
        ndesc = adesc.getNodeDescriptor("unitCost");
        assertNotNull(ndesc);
        ndesc.setValue(act, new Money(4));
        assertTrue(act.getUnitCost().equals(new Money(4)));

        // set the tax amount node
        ndesc = adesc.getNodeDescriptor("taxAmount");
        assertNotNull(ndesc);
        ndesc.setValue(act, new Money(5));
        assertTrue(act.getTaxAmount().equals(new Money(5)));

        // set the total node
        ndesc = adesc.getNodeDescriptor("total");
        assertNotNull(ndesc);
        ndesc.setValue(act, new Money(6));
        assertTrue(act.getTotal().equals(new Money(6)));

        // set the allocated amount node
        ndesc = adesc.getNodeDescriptor("allocatedAmount");
        assertNotNull(ndesc);
        ndesc.setValue(act, new Money(7));
        assertTrue(act.getAllocatedAmount().equals(new Money(7)));

        // set the credit node
        ndesc = adesc.getNodeDescriptor("credit");
        assertNotNull(ndesc);
        ndesc.setValue(act, true);
        assertTrue(act.isCredit());

        // set the printed node
        ndesc = adesc.getNodeDescriptor("printed");
        assertNotNull(ndesc);
        ndesc.setValue(act, false);
        assertTrue(!act.isPrinted());

        // set the start time node
        ndesc = adesc.getNodeDescriptor("startTime");
        assertNotNull(ndesc);
        ndesc.setValue(act, "1963-12-20 0:0:0 GMT");
        assertTrue(ndesc.getValue(act) instanceof Date);

        // save the document
        service.save(act);
    }

    /**
     * Test the creation of a simple financial act
     */
    public void testSimpleFinancialActCreation()
            throws Exception {
        FinancialAct act = createFinancialAct(new BigDecimal(1), new Money(1),
                                              new Money(2), new Money(3),
                                              new Money(4), new Money(5),
                                              new Money(6), new Money(7),
                                              true, false);
        service.save(act);

        // now retrieve it
        FinancialAct act1 = (FinancialAct) service.get(
                act.getObjectReference());
        assertTrue(act1 != null);
        assertTrue(act.getName().equals(act1.getName()));
        assertTrue(act.getDescription().equals(act1.getDescription()));
        assertTrue(act.getQuantity().compareTo(act1.getQuantity()) == 0);
        assertTrue(act.getFixedAmount().compareTo(act1.getFixedAmount()) == 0);
        assertTrue(act.getUnitAmount().compareTo(act1.getUnitAmount()) == 0);
        assertTrue(act.getFixedCost().compareTo(act1.getFixedCost()) == 0);
        assertTrue(act.getUnitCost().compareTo(act1.getUnitCost()) == 0);
        assertTrue(act.getTaxAmount().compareTo(act1.getTaxAmount()) == 0);
        assertTrue(act.getTotal().compareTo(act1.getTotal()) == 0);
        assertTrue(act.getAllocatedAmount().compareTo(
                act1.getAllocatedAmount()) == 0);
        assertTrue(act.isCredit() == act1.isCredit());
        assertTrue(act.isPrinted() == act1.isPrinted());
    }

    /**
     * Test the creation of a simple financial act. Then retrieve the object and
     * compare using NodeDescriptors.
     */
    public void testFinancialActCreationAndRetrieval()
            throws Exception {
        FinancialAct act = createFinancialAct(new BigDecimal(1), new Money(1),
                                              new Money(2), new Money(3),
                                              new Money(4), new Money(5),
                                              new Money(6), new Money(7),
                                              true, false);
        service.save(act);

        // now retrieve it
        FinancialAct act1 = (FinancialAct) service.get(
                act.getObjectReference());
        assertTrue(act1 != null);

        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(
                "financial.act");
        NodeDescriptor ndesc;

        // set the name node
        ndesc = adesc.getNodeDescriptor("name");
        assertNotNull(ndesc);
        String name = (String) ndesc.getValue(act1);
        assertTrue(act.getName().equals(name));

        // set the quantity node
        ndesc = adesc.getNodeDescriptor("quantity");
        assertNotNull(ndesc);
        BigDecimal quantity = (BigDecimal) ndesc.getValue(act1);
        assertTrue(act.getQuantity().compareTo(quantity) == 0);

        // set the fixed amount node
        ndesc = adesc.getNodeDescriptor("fixedAmount");
        assertNotNull(ndesc);
        Money fixedAmount = (Money) ndesc.getValue(act1);
        assertTrue(act.getFixedAmount().compareTo(fixedAmount) == 0);

        // set the unit amount node
        ndesc = adesc.getNodeDescriptor("unitAmount");
        assertNotNull(ndesc);
        Money unitAmount = (Money) ndesc.getValue(act1);
        assertTrue(act.getUnitAmount().compareTo(unitAmount) == 0);

        // set the tax amount node
        ndesc = adesc.getNodeDescriptor("taxAmount");
        assertNotNull(ndesc);
        Money taxAmount = (Money) ndesc.getValue(act1);
        assertTrue(act.getTaxAmount().compareTo(taxAmount) == 0);

        // set the total node
        ndesc = adesc.getNodeDescriptor("total");
        assertNotNull(ndesc);
        Money total = (Money) ndesc.getValue(act1);
        assertTrue(act.getTotal().compareTo(total) == 0);

        // set the total node
        ndesc = adesc.getNodeDescriptor("allocatedAmount");
        assertNotNull(ndesc);
        Money allocatedAmount = (Money) ndesc.getValue(act1);
        assertTrue(act.getAllocatedAmount().compareTo(allocatedAmount) == 0);

        // set the credit node
        ndesc = adesc.getNodeDescriptor("credit");
        assertNotNull(ndesc);
        Boolean credit = (Boolean) ndesc.getValue(act1);
        assertTrue(act.isCredit() == credit);

        // set the printed node
        ndesc = adesc.getNodeDescriptor("printed");
        assertNotNull(ndesc);
        Boolean printed = (Boolean) ndesc.getValue(act1);
        assertTrue(act.isPrinted() == printed);
    }

    /**
     * Test the creation of multiple acts.
     */
    public void testMultipleFinancialActCreation()
            throws Exception {
        for (int index = 0; index < 10; index++) {
            FinancialAct act = createFinancialAct(new BigDecimal(1),
                                                  new Money(1),
                                                  new Money(2), new Money(3),
                                                  new Money(4), new Money(5),
                                                  new Money(6), new Money(7),
                                                  true, false);
            service.save(act);
        }
    }

    /**
     * Test creation and retrieval of a financial act.
     */
    public void testFinancialActRetrieval()
            throws Exception {
        FinancialAct act = createFinancialAct(new BigDecimal(1), new Money(1),
                                              new Money(2), new Money(3),
                                              new Money(4), new Money(5),
                                              new Money(6), new Money(7),
                                              true, false);
        service.save(act);

        // retrieve it
        ArchetypeQuery query = new ArchetypeQuery(new ObjectRefConstraint(
                act.getObjectReference()));
        query.setCountResults(true);
        IPage<IMObject> page = service.get(query);
        assertEquals(1, page.getTotalResults());

        FinancialAct newAct = (FinancialAct) page.getResults().iterator().next();
        assertTrue(
                newAct.getObjectReference().equals(act.getObjectReference()));
    }

    /**
     * Test the modification of a financial act.
     */
    public void testFinancialActModification()
            throws Exception {
        FinancialAct act = createFinancialAct(new BigDecimal(1), new Money(1),
                                              new Money(2), new Money(3),
                                              new Money(4), new Money(5),
                                              new Money(6), new Money(7),
                                              true, false);
        service.save(act);

        // retrieve it
        ArchetypeQuery query = new ArchetypeQuery(new ObjectRefConstraint(
                act.getObjectReference()));
        query.setCountResults(true);
        IPage<IMObject> page = service.get(query);
        assertTrue(page.getTotalResults() == 1);

        FinancialAct newAct = (FinancialAct) page.getResults().iterator().next();
        newAct.setCredit(false);
        newAct.setQuantity(new BigDecimal(123));
        service.save(act);
    }

    /**
     * Test the deletion of a financial act.
     */
    public void testFinancialActDeletion()
            throws Exception {
        FinancialAct act = createFinancialAct(new BigDecimal(1), new Money(1),
                                              new Money(2), new Money(3),
                                              new Money(4), new Money(5),
                                              new Money(6), new Money(7),
                                              true, false);
        service.save(act);

        // retrieve it
        ArchetypeQuery query = new ArchetypeQuery(new ObjectRefConstraint(
                act.getObjectReference()));
        query.setCountResults(true);
        IPage<IMObject> page = service.get(query);
        assertEquals(1, page.getTotalResults());

        // delete it
        FinancialAct newAct = (FinancialAct) page.getResults().iterator().next();
        service.remove(newAct);

        // try and retrieve it again
        page = service.get(query);
        assertEquals(0, page.getTotalResults());
    }

    /**
     * Test that we can use a date node from an existing node to set the date
     * node of another node.
     */
    public void testOBF45()
            throws Exception {
        // create an act
        FinancialAct act = createFinancialAct(new BigDecimal(1), new Money(1),
                                              new Money(2), new Money(3),
                                              new Money(4), new Money(5),
                                              new Money(6), new Money(7),
                                              true, false);
        act.setActivityStartTime(new Date());
        service.save(act);

        // retrieve the act
        ArchetypeQuery query = new ArchetypeQuery(new ObjectRefConstraint(
                act.getObjectReference()));
        query.setCountResults(true);
        IPage<IMObject> page = service.get(query);
        assertEquals(1, page.getTotalResults());
        act = (FinancialAct) page.getResults().iterator().next();

        // use the descriptors to set the value of endTime
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(
                "financial.act");
        NodeDescriptor etndesc = adesc.getNodeDescriptor("endTime");
        NodeDescriptor stndesc = adesc.getNodeDescriptor("startTime");
        etndesc.setValue(act, stndesc.getValue(act));
        assertTrue(act.getActivityStartTime().equals(act.getActivityEndTime()));
    }

    /**
     * Tests that a null value for the fixedAmount, unitAmount, and taxAmount
     * fields doesn't affect the total field.
     */
    public void testOBF112() {
        Money fixedAmount = null;
        Money unitAmount = null;
        Money fixedCost = null;
        Money unitCost = null;
        Money taxAmount = null;
        Money total = new Money(1);
        Money allocatedAmount = null;
        FinancialAct act = createFinancialAct(new BigDecimal(1), fixedAmount,
                                              unitAmount, fixedCost, unitCost,
                                              taxAmount, total, allocatedAmount,
                                              true, false);
        service.save(act);
        FinancialAct loaded
                = (FinancialAct) service.get(act.getObjectReference());
        assertNotNull(loaded);
        assertTrue(total.compareTo(loaded.getTotal()) == 0);
    }

    /**
     * Creates a financial act given the specified info.
     *
     * @param quantity        the quantity for this act
     * @param fixedAmount     the fixed amount
     * @param unitAmount      the unit amount
     * @param fixedCost       the fixed cost
     * @param unitCost        the unit cost
     * @param taxAmount       the tax amount
     * @param total           the total amount
     * @param allocatedAmount the allocated amount
     * @param credit          whether it is a credit amount
     * @param printed         whether it has been printed
     * @return a new financial act
     */
    public FinancialAct createFinancialAct(BigDecimal quantity,
                                           Money fixedAmount,
                                           Money unitAmount,
                                           Money fixedCost,
                                           Money unitCost,
                                           Money taxAmount,
                                           Money total, Money allocatedAmount,
                                           boolean credit, boolean printed) {
        FinancialAct act = (FinancialAct) service.create("financial.act");
        act.setName("financialAct1");
        act.setDescription("This is the first financial act");
        act.setQuantity(quantity);
        act.setFixedAmount(fixedAmount);
        act.setUnitAmount(unitAmount);
        act.setFixedCost(fixedCost);
        act.setUnitCost(unitCost);
        act.setTaxAmount(taxAmount);
        act.setTotal(total);
        act.setAllocatedAmount(allocatedAmount);
        act.setCredit(credit);
        act.setPrinted(printed);

        return act;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[]{
                "org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml"
        };
    }

    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        this.service = (ArchetypeService) applicationContext.getBean(
                "archetypeService");
    }

}
