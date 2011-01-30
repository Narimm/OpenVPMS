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
package org.openvpms.esci.adapter.map.order;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.oasis.ubl.OrderType;
import org.oasis.ubl.common.AmountType;
import org.oasis.ubl.common.IdentifierType;
import org.oasis.ubl.common.QuantityType;
import org.oasis.ubl.common.aggregate.AddressType;
import org.oasis.ubl.common.aggregate.ContactType;
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
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.esci.adapter.AbstractESCITest;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.ubl.io.UBLDocumentContext;
import org.openvpms.ubl.io.UBLDocumentWriter;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Date;


/**
 * Tests the {@link OrderMapperImpl} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderMapperTestCase extends AbstractESCITest {

    /**
     * The practice location contact.
     */
    private Contact practiceContact;

    /**
     * Phone contact.
     */
    private Contact phoneContact;

    /**
     * Fax contact.
     */
    private Contact faxContact;

    /**
     * Email contact.
     */
    private Contact emailContact;

    /**
     * The supplier contact.
     */
    private Contact supplierContact;

    /**
     * Order author.
     */
    private User author;

    /**
     * Id for the customer, assigned by the supplier.
     */
    private static final String SUPPLIER_ACCOUNT_ID = "ANACCOUNTID";


    /**
     * Verifies that an <em>act.supplierOrder</em> can be mapped to a UBL order, and serialized.
     *
     * @throws Exception for any error
     */
    @Test
    public void testMap() throws Exception {
        BigDecimal quantity = new BigDecimal(5);
        BigDecimal unitPrice = new BigDecimal(10);
        String reorderCode = "ABC123";
        String reorderDesc = "A reorderDesc";

        // create an order with a single item, and post it
        FinancialAct actItem = createOrderItem(quantity, 1, unitPrice);
        ActBean itemBean = new ActBean(actItem);
        itemBean.setValue("reorderCode", reorderCode);
        itemBean.setValue("reorderDescription", reorderDesc);

        FinancialAct act = createOrder(actItem);
        act.setStatus(ActStatus.POSTED);
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("author", author);
        bean.save();

        OrderMapper mapper = createMapper();
        OrderType order = mapper.map(act);

        // serialize the order and re-read it, to ensure it passes validation
        UBLDocumentContext context = new UBLDocumentContext();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        context.createWriter().write(order, stream);
        order = (OrderType) context.createReader().read(new ByteArrayInputStream(stream.toByteArray()));

        UBLDocumentWriter writer = context.createWriter();
        writer.setFormat(true);
        writer.write(order, System.out);

        // verify the order has the expected content
        assertEquals("2.0", order.getUBLVersionID().getValue());
        checkID(order.getID(), act.getId());
        assertEquals(false, order.getCopyIndicator().isValue());

        checkDate(order.getIssueDate().getValue(), act.getActivityStartTime());

        checkCustomer(order.getBuyerCustomerParty(), getPractice().getName(), getStockLocation(), practiceContact);
        checkContact(order.getBuyerCustomerParty().getParty().getContact(), author.getName(), phoneContact, faxContact,
                     emailContact);
        checkSupplier(order.getSellerSupplierParty(), getSupplier(), supplierContact);

        PayableAmountType amount = order.getAnticipatedMonetaryTotal().getPayableAmount();
        checkAmount(amount, 50);

        assertEquals(1, order.getOrderLine().size());
        OrderLineType line1 = order.getOrderLine().get(0);
        LineItemType item1 = line1.getLineItem();
        checkID(item1.getID(), actItem.getId());
        checkQuantity(item1.getQuantity(), "BX", 5);
        checkAmount(item1.getLineExtensionAmount(), 50);
        checkAmount(item1.getTotalTaxAmount(), 0);
        checkAmount(item1.getPrice().getPriceAmount(), 10);
        checkQuantity(item1.getPrice().getBaseQuantity(), "BX", 1);
        checkItem(item1.getItem(), getProduct(), reorderCode, reorderDesc);
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is thrown if there is no relationship between a supplier
     * and stock location.
     */
    @Test
    public void testNoSupplierStockLocationRelationship() {
        FinancialAct order = createOrder();

        // remove the supplier/stock location
        Party supplier = getSupplier();
        Party location = getStockLocation();
        EntityBean bean = new EntityBean(supplier);
        EntityRelationship relationship = bean.getRelationship(location);
        supplier.removeEntityRelationship(relationship);
        location.removeEntityRelationship(relationship);
        save(supplier, location);

        String expected = "ESCIA-0001: e-Supply Chain Interface support is not configured for " + supplier.getName()
                          + " (" + supplier.getId() + ") and " + location.getName() + " (" + location.getId() + ")";
        checkMappingException(order, expected);
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is thrown if the product has no supplier order code.
     */
    @Test
    public void testNoSupplierOrderCode() {
        // create an order with a single item, and no reorder code, and post it
        FinancialAct orderItem = createOrderItem(BigDecimal.ONE, 1, BigDecimal.ONE);
        ActBean itemBean = new ActBean(orderItem);
        itemBean.setValue("reorderCode", null);

        FinancialAct order = createOrder(orderItem);
        order.setStatus(ActStatus.POSTED);
        ActBean bean = new ActBean(order);
        bean.addNodeParticipation("author", author);
        bean.save();

        Product product = getProduct();
        Party supplier = getSupplier();
        String expected = "ESCIA-0300: Supplier " + supplier.getName() + " (" + supplier.getId()
                          + ") has no order code for product " + product.getName() + " (" + product.getId() + ")";
        checkMappingException(order, expected);
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        Party practice = getPractice();
        practice.getContacts().clear();
        practiceContact = TestHelper.createLocationContact("1 Broadwater Avenue", "CAPE_WOOLAMAI", "VIC", "3925");
        practice.addContact(practiceContact);

        phoneContact = createContact(ContactArchetypes.PHONE, "telephoneNumber", "59527054");
        practice.addContact(phoneContact);

        faxContact = createContact(ContactArchetypes.FAX, "faxNumber", "59527053");
        practice.addContact(faxContact);

        emailContact = createContact(ContactArchetypes.EMAIL, "emailAddress", "foo@bar.com");
        practice.addContact(emailContact);

        save(practice);

        supplierContact = TestHelper.createLocationContact("2 Peko Rd", "TENNANT_CREEK", "NT", "0862");
        Party supplier = getSupplier();
        supplier.addContact(supplierContact);

        // create a user for associating with orders
        author = TestHelper.createUser();

        // add a supplier/stock location relationship
        Party stockLocation = getStockLocation();
        EntityBean supplierBean = new EntityBean(supplier);
        EntityRelationship relationship = supplierBean.addRelationship(
                SupplierArchetypes.SUPPLIER_STOCK_LOCATION_RELATIONSHIP_ESCI, stockLocation);
        IMObjectBean bean = new IMObjectBean(relationship);
        bean.setValue("accountId", SUPPLIER_ACCOUNT_ID);
        bean.setValue("serviceURL", "https://foo.openvpms.org/orderservice");

        save(supplier, stockLocation);
    }

    /**
     * Creates a new order mapper.
     *
     * @return a new mapper
     */
    private OrderMapper createMapper() {
        OrderMapperImpl mapper = new OrderMapperImpl();
        mapper.setPracticeRules(new PracticeRules());
        mapper.setLocationRules(new LocationRules());
        mapper.setPartyRules(new PartyRules());
        mapper.setSupplierRules(new SupplierRules());
        mapper.setLookupService(LookupServiceHelper.getLookupService());
        mapper.setBeanFactory(new IMObjectBeanFactory(getArchetypeService()));
        return mapper;
    }

    /**
     * Verifies that an invalid order fails mapping with a exception.
     *
     * @param order           the order
     * @param expectedMessage the expected error message
     */
    private void checkMappingException(FinancialAct order, String expectedMessage) {
        OrderMapper mapper = createMapper();
        try {
            mapper.map(order);
            fail("Expected mapping to fail");
        } catch (ESCIAdapterException exception) {
            assertEquals(expectedMessage, exception.getMessage());
        }
    }

    /**
     * Verifies a customer matches that expected.
     *
     * @param customer the customer to check
     * @param name     the expected customer name
     * @param expected the expected customer
     * @param contact  the expected contact
     */
    private void checkCustomer(CustomerPartyType customer, String name, Party expected, Contact contact) {
        checkID(customer.getCustomerAssignedAccountID(), expected.getId());
        assertEquals(SUPPLIER_ACCOUNT_ID, customer.getSupplierAssignedAccountID().getValue());
        checkParty(customer.getParty(), name, contact);
    }

    /**
     * Verifies a contact matches that expected.
     *
     * @param contact      the contact to check
     * @param name         the expected contact name
     * @param phoneContact the expected phone contact
     * @param faxContact   the expected fax contact
     * @param emailContact the expected email contact
     */
    private void checkContact(ContactType contact, String name, Contact phoneContact, Contact faxContact,
                              Contact emailContact) {
        assertEquals(name, contact.getName().getValue());
        assertEquals(phoneContact.getDetails().get("telephoneNumber"), contact.getTelephone().getValue());
        assertEquals(faxContact.getDetails().get("faxNumber"), contact.getTelefax().getValue());
        assertEquals(emailContact.getDetails().get("emailAddress"), contact.getElectronicMail().getValue());
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
     * @param item        the item to check
     * @param product     the expected product
     * @param reorderCode the expected reorder code
     * @param reorderDesc the expected reorder description
     */
    private void checkItem(ItemType item, Product product, String reorderCode, String reorderDesc) {
        assertEquals(product.getName(), item.getName().getValue());
        checkID(item.getBuyersItemIdentification().getID(), product.getId());
        assertEquals(reorderCode, item.getSellersItemIdentification().getID().getValue());
        assertEquals(reorderDesc, item.getDescription().get(0).getValue());
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

    /**
     * Helper to create a new POSTED order.
     *
     * @return a new order
     */
    @Override
    protected FinancialAct createOrder() {
        BigDecimal quantity = new BigDecimal(5);
        BigDecimal unitPrice = new BigDecimal(10);

        // create an order with a single item, and post it
        FinancialAct actItem = createOrderItem(quantity, 1, unitPrice);
        FinancialAct act = createOrder(actItem);
        act.setStatus(ActStatus.POSTED);
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("author", author);
        bean.save();
        return act;
    }

    /**
     * Helper to create a new contact.
     *
     * @param shortName the contact archetype short name
     * @param name      the node name to populate
     * @param value     the value to populate the node with
     * @return a new contact
     */
    private Contact createContact(String shortName, String name, String value) {
        Contact contact = (Contact) create(shortName);
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue(name, value);
        return contact;
    }
}
