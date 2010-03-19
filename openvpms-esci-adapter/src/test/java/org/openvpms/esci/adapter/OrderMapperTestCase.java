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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.esci.adapter;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.oasis.ubl.OrderType;
import org.oasis.ubl.common.AmountType;
import org.oasis.ubl.common.IdentifierType;
import org.oasis.ubl.common.QuantityType;
import org.oasis.ubl.common.aggregate.AddressType;
import org.oasis.ubl.common.aggregate.CustomerPartyType;
import org.oasis.ubl.common.aggregate.ItemType;
import org.oasis.ubl.common.aggregate.LineItemType;
import org.oasis.ubl.common.aggregate.OrderLineType;
import org.oasis.ubl.common.aggregate.PartyType;
import org.oasis.ubl.common.aggregate.SupplierPartyType;
import org.oasis.ubl.common.basic.PayableAmountType;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.archetype.rules.supplier.AbstractSupplierTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.ubl.io.UBLDocumentContext;
import org.openvpms.ubl.io.UBLDocumentWriter;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Date;


/**
 * Tests the {@link OrderMapper} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderMapperTestCase extends AbstractSupplierTest {

    /**
     * The practice location contact.
     */
    private Contact practiceContact;

    /**
     * The supplier contact.
     */
    private Contact supplierContact;

    /**
     * The product supplier relationship.
     */
    private ProductSupplier productSupplier;


    /**
     * Verifies that an <em>act.supplierOrder</em> can be mapped to a UBL order, and serialized.
     *
     * @throws Exception for any error
     */
    @Test
    public void testMap() throws Exception {
        BigDecimal quantity = new BigDecimal(5);
        BigDecimal unitPrice = new BigDecimal(10);

        // create an order with a single item, and post it
        FinancialAct actItem = createOrderItem(quantity, 1, unitPrice);
        FinancialAct act = createOrder(actItem);
        act.setStatus(ActStatus.POSTED);
        save(act);

        OrderMapper mapper = new OrderMapper();
        OrderType order = mapper.map(act);

        // serialize the order and re-read it, to ensure it passes validation
        UBLDocumentContext context = new UBLDocumentContext();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        context.createWriter().write(order, stream);
        order = context.createReader().read(new ByteArrayInputStream(stream.toByteArray()), OrderType.class);

        UBLDocumentWriter writer = context.createWriter();
        writer.setFormat(true);
        writer.write(order, System.out);

        // verify the order has the expected content
        assertEquals("2.0", order.getUBLVersionID().getValue());
        checkID(order.getID(), act.getId());
        assertEquals(false, order.getCopyIndicator().isValue());

        checkDate(order.getIssueDate().getValue(), act.getActivityStartTime());

        checkCustomer(order.getBuyerCustomerParty(), getPractice(), practiceContact);
        checkSupplier(order.getSellerSupplierParty(), getSupplier(), supplierContact);

        PayableAmountType amount = order.getAnticipatedMonetaryTotal().getPayableAmount();
        assertEquals(new BigDecimal("50"), amount.getValue());
        assertEquals("AUD", amount.getCurrencyID().value());

        assertEquals(1, order.getOrderLine().size());
        OrderLineType line1 = order.getOrderLine().get(0);
        LineItemType item1 = line1.getLineItem();
        checkID(item1.getID(), actItem.getId());
        checkQuantity(item1.getQuantity(), "BX", 5);
        checkAmount(item1.getLineExtensionAmount(), 50);
        checkAmount(item1.getTotalTaxAmount(), 0);
        checkAmount(item1.getPrice().getPriceAmount(), 10);
        checkQuantity(item1.getPrice().getBaseQuantity(), "BX", 1);
        checkItem(item1.getItem(), getProduct());
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        Party practice = TestHelper.getPractice();
        PartyRules rules = new PartyRules();
        practiceContact = rules.getContact(practice, ContactArchetypes.LOCATION, "BILLING");
        if (practiceContact == null) {
            practiceContact = (Contact) create(ContactArchetypes.LOCATION);
            practice.addContact(practiceContact);
        } else {
            practice.removeContact(practiceContact);
        }
        practiceContact = TestHelper.createLocationContact("1 Broadwater Avenue", "CAPE_WOOLAMAI", "VIC", "3925");
        practice.addContact(practiceContact);
        save(practice);

        supplierContact = TestHelper.createLocationContact("2 Peko Rd", "TENNANT_CREEK", "NT", "0862");
        Party supplier = getSupplier();
        supplier.addContact(supplierContact);

        // add a product supplier relationship
        Product product = getProduct();
        ProductRules productRules = new ProductRules();
        productSupplier = productRules.createProductSupplier(product, supplier);
        productSupplier.setReorderCode("AREORDERCODE");
        productSupplier.setReorderDescription("A reorder description");

        save(supplier, product);
    }

    /**
     * Verifies a customer matches that expected.
     *
     * @param customer the customer to check
     * @param expected the expected customer
     * @param contact  the expected contact
     */
    private void checkCustomer(CustomerPartyType customer, Party expected, Contact contact) {
        checkID(customer.getCustomerAssignedAccountID(), expected.getId());
        checkParty(customer.getParty(), expected.getName(), contact);
    }

    /**
     * Verifies that a supplier matches that expected.
     *
     * @param supplier the supplier
     * @param expected the expected supplier
     * @param contact  the expected contact
     */
    private void checkSupplier(SupplierPartyType supplier, Party expected, Contact contact) {
        checkID(supplier.getCustomerAssignedAccountID(), expected.getId());
        checkParty(supplier.getParty(), expected.getName(), contact);
    }

    /**
     * Verifies an ID matches that expected.
     *
     * @param id       the to check
     * @param expected the expected id
     */
    private void checkID(IdentifierType id, long expected) {
        assertEquals(Long.toString(expected), id.getValue());
    }

    /**
     * Verifies a date matches that expected.
     *
     * @param calendar     the date to check
     * @param expectedDate the expected value
     */
    private void checkDate(XMLGregorianCalendar calendar, Date expectedDate) {
        java.sql.Date date = new java.sql.Date(expectedDate.getTime());
        assertEquals(date.toString(), calendar.toString());
    }

    /**
     * Checks a quanity.
     *
     * @param quantity the quantity to check
     * @param code     the expected code
     * @param value    the expected value
     */
    private void checkQuantity(QuantityType quantity, String code, int value) {
        assertEquals(code, quantity.getUnitCode());
        assertEquals(value, quantity.getValue().toBigInteger().intValue());
    }

    /**
     * Checks an amount.
     *
     * @param amount the amount to check
     * @param value  the expected value
     */
    private void checkAmount(AmountType amount, double value) {
        assertEquals("AUD", amount.getCurrencyID().value());
        assertTrue(new BigDecimal(value).compareTo(amount.getValue()) == 0);
    }

    /**
     * Checks an item.
     *
     * @param item     the item to check
     * @param product  the expected product
     */
    private void checkItem(ItemType item, Product product) {
        assertEquals(product.getName(), item.getName().getValue());
        checkID(item.getBuyersItemIdentification().getID(), product.getId());
        assertEquals(productSupplier.getReorderCode(), item.getSellersItemIdentification().getID().getValue());
        assertEquals(productSupplier.getReorderDescription(), item.getDescription().get(0).getValue());
    }

    /**
     * Checks a party.
     *
     * @param party   the party to check
     * @param name    the expected party name
     * @param contact the expected contact
     */
    private void checkParty(PartyType party, String name, Contact contact) {
        assertEquals(1, party.getPartyName().size());
        assertEquals(name, party.getPartyName().get(0).getName().getValue());

        IMObjectBean bean = new IMObjectBean(contact);
        String address = bean.getString("address");
        String cityName = ArchetypeServiceFunctions.lookup(contact, "suburb");
        String countrySubentity = ArchetypeServiceFunctions.lookup(contact, "state");
        String postalZone = bean.getString("postcode");

        AddressType postalAddress = party.getPostalAddress();
        assertEquals(1, postalAddress.getAddressLine().size());
        assertEquals(address, postalAddress.getAddressLine().get(0).getLine().getValue());
        assertEquals(cityName, postalAddress.getCityName().getValue());
        assertEquals(postalZone, postalAddress.getPostalZone().getValue());
        assertEquals(countrySubentity, postalAddress.getCountrySubentity().getValue());
    }
}
