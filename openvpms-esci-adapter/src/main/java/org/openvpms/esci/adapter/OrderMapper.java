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

import org.apache.commons.lang.StringUtils;
import org.oasis.ubl.OrderType;
import org.oasis.ubl.common.AmountType;
import org.oasis.ubl.common.CurrencyCodeContentType;
import org.oasis.ubl.common.IdentifierType;
import org.oasis.ubl.common.aggregate.AddressType;
import org.oasis.ubl.common.aggregate.CustomerPartyType;
import org.oasis.ubl.common.aggregate.ItemIdentificationType;
import org.oasis.ubl.common.aggregate.ItemType;
import org.oasis.ubl.common.aggregate.LineItemType;
import org.oasis.ubl.common.aggregate.OrderLineType;
import org.oasis.ubl.common.aggregate.PartyType;
import org.oasis.ubl.common.aggregate.PriceType;
import org.oasis.ubl.common.aggregate.SupplierPartyType;
import org.oasis.ubl.common.basic.BaseQuantityType;
import org.oasis.ubl.common.basic.CopyIndicatorType;
import org.oasis.ubl.common.basic.CustomerAssignedAccountIDType;
import org.oasis.ubl.common.basic.IDType;
import org.oasis.ubl.common.basic.IssueDateType;
import org.oasis.ubl.common.basic.LineExtensionAmountType;
import org.oasis.ubl.common.basic.NameType;
import org.oasis.ubl.common.basic.PriceAmountType;
import org.oasis.ubl.common.basic.QuantityType;
import org.oasis.ubl.common.basic.StreetNameType;
import org.oasis.ubl.common.basic.TotalTaxAmountType;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * Maps <em>act.supplierOrder</em> acts to UBL Orders.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderMapper {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookupService;

    /**
     * Party rules.
     */
    private final PartyRules partyRules;

    /**
     * Location rules.
     */
    private final LocationRules locationRules;

    /**
     * Practice currency code.
     */
    private final CurrencyCodeContentType currencyCode;


    /**
     * Default constructor.
     */
    public OrderMapper() {
        this(ArchetypeServiceHelper.getArchetypeService(),
             LookupServiceHelper.getLookupService());
    }

    /**
     * Constructs an <em>OrderMapper</em>.
     *
     * @param service       the archetype service
     * @param lookupService the lookup service
     */
    public OrderMapper(IArchetypeService service, ILookupService lookupService) {
        this.service = service;
        this.lookupService = lookupService;
        partyRules = new PartyRules(service);
        locationRules = new LocationRules(service);

        PracticeRules practiceRules = new PracticeRules(service);
        Party practice = practiceRules.getPractice();
        if (practice == null) {
            throw new IllegalStateException("No party.organisationPractice defined");
        }
        IMObjectBean bean = new IMObjectBean(practice, service);
        currencyCode = CurrencyCodeContentType.valueOf(bean.getString("currency"));
    }

    /**
     * Maps an <em>act.supplierOrder</em> to an UBL order.
     *
     * @param order the <em>act.supplierOrder</em> to map
     * @return the corresponding UBL order
     */
    public OrderType map(Act order) {
        OrderType result = new OrderType();

        IDType id = getID(order.getId());
        CopyIndicatorType copyIndicator = getCopyIndicatorType(false);
        IssueDateType issueDate = getIssueDate(order.getActivityStartTime());

        ActBean bean = new ActBean(order, service);
        Entity stockLocation = bean.getNodeParticipant("stockLocation");
        Party location = getLocation(stockLocation);
        Party supplier = (Party) bean.getNodeParticipant("supplier");
        CustomerPartyType customerParty = getCustomer(location);
        SupplierPartyType supplierParty = getSupplier(supplier);

        result.setID(id);
        result.setCopyIndicator(copyIndicator);
        result.setIssueDate(issueDate);
        result.setBuyerCustomerParty(customerParty);
        result.setSellerSupplierParty(supplierParty);

        for (Act item : bean.getNodeActs("items")) {
            OrderLineType line = mapOrderItem(item, supplier);
            result.getOrderLine().add(line);
        }
        return result;
    }

    /**
     * Maps an <em>act.supplierOrderItem</em> to a <tt>OrderLineType</tt>.
     *
     * @param act      the order item to map
     * @param supplier the supplier
     * @return a new <tt>OrderLineType</tt> corresponding to the act
     */
    private OrderLineType mapOrderItem(Act act, Party supplier) {
        ActBean bean = new ActBean(act, service);
        Product product = (Product) bean.getNodeParticipant("product");

        OrderLineType orderLine = new OrderLineType();
        LineItemType lineItem = new LineItemType();
        ItemType item = new ItemType();

        lineItem.setItem(item);
        orderLine.setLineItem(lineItem);

        String packageUnits = bean.getString("packageUnits");
        String unitCode = getUnitCode(packageUnits);

        IDType id = getID(act.getId());
        QuantityType quantity = initQuantity(new QuantityType(), bean.getBigDecimal("quantity"), unitCode);
        LineExtensionAmountType lineAmount = initAmount(new LineExtensionAmountType(), bean.getBigDecimal("total"));
        TotalTaxAmountType taxAmount = initAmount(new TotalTaxAmountType(), bean.getBigDecimal("tax"));
        PriceType price = getPrice(bean.getBigDecimal("unitPrice"), unitCode);

        lineItem.setID(id);
        lineItem.setQuantity(quantity);
        lineItem.setLineExtensionAmount(lineAmount);
        lineItem.setTotalTaxAmount(taxAmount);
        lineItem.setPrice(price);

        ItemIdentificationType buyersId = getItemIdentification(product.getId());
        ItemIdentificationType sellersId = getSuppliersItemIdentifier(product, supplier);
        NameType name = getName(product.getName());

        item.setBuyersItemIdentification(buyersId);
        item.setSellersItemIdentification(sellersId);
        item.setName(name);

        return orderLine;
    }

    /**
     * Returns a <tt>PriceType</tt> for the specified price and unit code.
     *
     * @param price    the price
     * @param unitCode the quantity unit code (UN/CEFACT). May be <tt>null</tt>
     * @return a new <tt>PriceType</tt>
     */
    private PriceType getPrice(BigDecimal price, String unitCode) {
        PriceType result = new PriceType();
        PriceAmountType priceAmount = initAmount(new PriceAmountType(), price);
        result.setPriceAmount(priceAmount);
        result.setBaseQuantity(initQuantity(new BaseQuantityType(), BigDecimal.ONE, unitCode));
        return result;
    }

    /**
     * Helper to initialise an <tt>AmountType</tt>.
     *
     * @param amount the amount to initialise
     * @param value  the value
     * @return the amount
     */
    private <T extends AmountType> T initAmount(T amount, BigDecimal value) {
        amount.setCurrencyID(currencyCode);
        amount.setValue(value);
        return amount;
    }

    /**
     * Helper to initialise a <tt>QuantityType</tt>.
     *
     * @param quantity the quantity to initialise
     * @param value    the value
     * @param unitCode the quantity unit code. May be <tt>null</tt>
     * @return the quantity
     */
    private <T extends org.oasis.ubl.common.QuantityType> T initQuantity(T quantity, BigDecimal value,
                                                                         String unitCode) {
        quantity.setValue(value);
        quantity.setUnitCode(unitCode);
        return quantity;
    }

    /**
     * Returns the UN/CEFACT unit code for the given package units code from an <em>lookup.uom</em>.
     *
     * @param packageUnits the package units code
     * @return the corresponding unit code or <tt>null</tt> if none is found
     */
    private String getUnitCode(String packageUnits) {
        String result = null;
        if (!StringUtils.isEmpty("packageUnits")) {
            Lookup lookup = lookupService.getLookup("lookup.uom", packageUnits);
            if (lookup != null) {
                IMObjectBean lookupBean = new IMObjectBean(lookup, service);
                String unitCode = lookupBean.getString("unitCode");
                if (!StringUtils.isEmpty(unitCode)) {
                    result = unitCode;
                }
            }
        }
        return result;
    }

    /**
     * Returns the supplier's identifier for a given product, if available.
     *
     * @param product  the product
     * @param supplier the supplier
     * @return the supplier's identifier for the product, or <tt>null</tt> if none is available
     */
    private ItemIdentificationType getSuppliersItemIdentifier(Product product, Party supplier) {
        ItemIdentificationType result = null;
        EntityBean bean = new EntityBean(supplier, service);
        EntityRelationship relationship = bean.getRelationship(product);
        if (relationship != null) {
            IMObjectBean relBean = new IMObjectBean(relationship, service);
            String id = relBean.getString("reorderCode");
            if (!StringUtils.isEmpty(id)) {
                result = getItemIdentification(id);
            }
        }
        return result;
    }

    /**
     * Returns a <tt>SupplierPartyType</tt> corresponding to the passed supplier.
     *
     * @param supplier the supplier
     * @return the corresponding <tt>SupplierPartyType</tt>
     */
    private SupplierPartyType getSupplier(Party supplier) {
        SupplierPartyType result = new SupplierPartyType();
        CustomerAssignedAccountIDType accountId = initID(new CustomerAssignedAccountIDType(), supplier.getId());
        result.setCustomerAssignedAccountID(accountId);
        return result;
    }

    /**
     * Helper to return the location associated with a stock location.
     *
     * @param stockLocation the stock location
     * @return the corresponding location, or <tt>null</tt> if none is found
     */
    private Party getLocation(Entity stockLocation) {
        EntityBean bean = new EntityBean(stockLocation, service);
        // TODO - there could be more than one location which refers to different party.organisationLocation 
        return (Party) bean.getNodeSourceEntity("locations");
    }

    /**
     * Returns a <tt>CustomerPartyType</tt> corresponding to the passed <em>party.organisationLocation</em>.
     *
     * @param location the practice location
     * @return the corresponding <tt>CustomerPartyType</tt>
     */
    private CustomerPartyType getCustomer(Party location) {
        CustomerPartyType result = new CustomerPartyType();
        CustomerAssignedAccountIDType id = initID(new CustomerAssignedAccountIDType(), location.getId());
        PartyType partyType = new PartyType();
        Contact contact = partyRules.getContact(location, ContactArchetypes.LOCATION, "BILLING");
        if (contact == null) {
            Party practice = locationRules.getPractice(location);
            if (practice != null) {
                contact = partyRules.getContact(practice, ContactArchetypes.LOCATION, "BILLING");
            }
            if (contact == null) {
                throw new IllegalStateException("No contact.location for location: " + location.getId());
            }
        }
        partyType.setPostalAddress(getAddress(contact));

        result.setCustomerAssignedAccountID(id);
        result.setParty(partyType);
        return result;
    }

    /**
     * Returns an <tt>AddressType</tt> for the supplied <em>contact.location</em>.
     *
     * @param contact the location contact
     * @return the corresponding <tt>AddressType</tt>
     */
    private AddressType getAddress(Contact contact) {
        IMObjectBean bean = new IMObjectBean(contact, service);
        AddressType result = new AddressType();
        StreetNameType street = new StreetNameType();
        street.setValue(bean.getString("address"));
        result.setStreetName(street);
        return result;
    }

    /**
     * Returns an <tt>IssueDateType</tt> for the supplied date/time.
     * <p/>
     * Only the date portion will be populated.
     *
     * @param datetime the date/time
     * @return the corresponding <tt>IssueDateType</tt>
     */
    private IssueDateType getIssueDate(Date datetime) {
        IssueDateType result = new IssueDateType();
        DatatypeFactory factory;
        try {
            factory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException exception) {
            throw new IllegalStateException(exception);
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(DateRules.getDate(datetime));
        XMLGregorianCalendar xml = factory.newXMLGregorianCalendar(calendar);
        xml.setHour(DatatypeConstants.FIELD_UNDEFINED);
        xml.setMinute(DatatypeConstants.FIELD_UNDEFINED);
        xml.setSecond(DatatypeConstants.FIELD_UNDEFINED);
        xml.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
        xml.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
        result.setValue(xml);
        return result;
    }

    /**
     * Returns a <tt>NameType</tt> for the given name.
     *
     * @param name the name
     * @return a new <tt>NameType</tt>
     */
    private NameType getName(String name) {
        NameType result = new NameType();
        result.setValue(name);
        return result;
    }

    /**
     * Returns an <tt>ItemIdentificationType</tt> for the given identifier.
     *
     * @param id the identifier
     * @return a new <tt>ItemIdentificationType</tt>
     */
    private ItemIdentificationType getItemIdentification(long id) {
        ItemIdentificationType result = new ItemIdentificationType();
        result.setID(getID(id));
        return result;
    }

    /**
     * Returns an <tt>ItemIdentificationType</tt> for the given identifier.
     *
     * @param id the identifier
     * @return a new <tt>ItemIdentificationType</tt>
     */
    private ItemIdentificationType getItemIdentification(String id) {
        ItemIdentificationType result = new ItemIdentificationType();
        result.setID(getID(id));
        return result;
    }

    /**
     * Returns a <tt>CopyIndicatorType</tt> with the specified value.
     *
     * @param value the indicator value
     * @return a new <tt>CopyIndicatorType</tt>
     */
    private CopyIndicatorType getCopyIndicatorType(boolean value) {
        CopyIndicatorType result = new CopyIndicatorType();
        result.setValue(value);
        return result;
    }

    /**
     * Initialises an <tt>IdentifierType</tt> with the specified value.
     *
     * @param id    the identifier to initialise
     * @param value the value
     * @return the id
     */
    private <T extends IdentifierType> T initID(T id, long value) {
        id.setValue(Long.toHexString(value));
        return id;
    }

    /**
     * Returns a new <tt>IDType</tt> with the specified value.
     *
     * @param id the identifier value
     * @return a new <tt>IDType</tt>
     */
    private IDType getID(long id) {
        IDType result = new IDType();
        return initID(result, id);
    }

    /**
     * Returns a new <tt>IDType</tt> with the specified value.
     *
     * @param id the identifier value
     * @return a new <tt>IDType</tt>
     */
    private IDType getID(String id) {
        IDType result = new IDType();
        result.setValue(id);
        return result;
    }

}
