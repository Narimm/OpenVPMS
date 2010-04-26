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
import org.oasis.ubl.common.TextType;
import org.oasis.ubl.common.aggregate.AddressLineType;
import org.oasis.ubl.common.aggregate.AddressType;
import org.oasis.ubl.common.aggregate.CustomerPartyType;
import org.oasis.ubl.common.aggregate.ItemIdentificationType;
import org.oasis.ubl.common.aggregate.ItemType;
import org.oasis.ubl.common.aggregate.LineItemType;
import org.oasis.ubl.common.aggregate.MonetaryTotalType;
import org.oasis.ubl.common.aggregate.OrderLineType;
import org.oasis.ubl.common.aggregate.PartyNameType;
import org.oasis.ubl.common.aggregate.PartyType;
import org.oasis.ubl.common.aggregate.PriceType;
import org.oasis.ubl.common.aggregate.SupplierPartyType;
import org.oasis.ubl.common.basic.BaseQuantityType;
import org.oasis.ubl.common.basic.CityNameType;
import org.oasis.ubl.common.basic.CopyIndicatorType;
import org.oasis.ubl.common.basic.CountrySubentityType;
import org.oasis.ubl.common.basic.CustomerAssignedAccountIDType;
import org.oasis.ubl.common.basic.DescriptionType;
import org.oasis.ubl.common.basic.IDType;
import org.oasis.ubl.common.basic.IssueDateType;
import org.oasis.ubl.common.basic.LineExtensionAmountType;
import org.oasis.ubl.common.basic.LineType;
import org.oasis.ubl.common.basic.NameType;
import org.oasis.ubl.common.basic.PayableAmountType;
import org.oasis.ubl.common.basic.PostalZoneType;
import org.oasis.ubl.common.basic.PriceAmountType;
import org.oasis.ubl.common.basic.QuantityType;
import org.oasis.ubl.common.basic.TotalTaxAmountType;
import org.oasis.ubl.common.basic.UBLVersionIDType;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.business.service.lookup.ILookupService;

import javax.annotation.Resource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * Maps <em>act.supplierOrder</em> acts to UBL Orders.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderMapperImpl implements OrderMapper {

    /**
     * The lookup service.
     */
    private ILookupService lookupService;

    /**
     * The practice rules.
     */
    private PracticeRules practiceRules;

    /**
     * Location rules.
     */
    private LocationRules locationRules;

    /**
     * Party rules.
     */
    private PartyRules partyRules;

    /**
     * The product rules.
     */
    private ProductRules productRules;


    /**
     * The bean factory.
     */
    private IMObjectBeanFactory factory;


    /**
     * Registers the practice rules.
     *
     * @param rules the practice rules
     */
    @Resource
    public void setPracticeRules(PracticeRules rules) {
        practiceRules = rules;
    }

    /**
     * Registers the location rules.
     *
     * @param rules the location rules
     */
    @Resource
    public void setLocationRules(LocationRules rules) {
        locationRules = rules;
    }

    /**
     * Registes the party rules.
     *
     * @param rules the party rules
     */
    @Resource
    public void setPartyRules(PartyRules rules) {
        partyRules = rules;
    }

    /**
     * Registers the product rules.
     *
     * @param rules the product rules
     */
    @Resource
    public void setProductRules(ProductRules rules) {
        productRules = rules;
    }

    /**
     * Registers the lookup service.
     *
     * @param service the lookup service
     */
    @Resource
    public void setLookupService(ILookupService service) {
        lookupService = service;
    }

    /**
     * Registers the bean factory.
     *
     * @param factory the bean factory
     */
    @Resource
    public void setBeanFactory(IMObjectBeanFactory factory) {
        this.factory = factory;
    }

    /**
     * Maps an <em>act.supplierOrder</em> to an UBL order.
     *
     * @param order the <em>act.supplierOrder</em> to map
     * @return the corresponding UBL order
     */
    public OrderType map(FinancialAct order) {
        OrderType result = new OrderType();
        CurrencyCodeContentType currencyCode = getCurrencyCode();

        UBLVersionIDType version = initID(new UBLVersionIDType(), "2.0");
        IDType id = getID(order.getId());
        CopyIndicatorType copyIndicator = getCopyIndicatorType(false);
        IssueDateType issueDate = getIssueDate(order.getActivityStartTime());

        ActBean bean = factory.createActBean(order);
        Entity stockLocation = bean.getNodeParticipant("stockLocation");
        Party location = getLocation(stockLocation);
        Party supplier = (Party) bean.getNodeParticipant("supplier");
        CustomerPartyType customerParty = getCustomer(location);
        SupplierPartyType supplierParty = getSupplier(supplier);
        MonetaryTotalType total = getMonetaryTotal(order.getTotal(), currencyCode);

        result.setUBLVersionID(version);
        result.setID(id);
        result.setCopyIndicator(copyIndicator);
        result.setIssueDate(issueDate);
        result.setBuyerCustomerParty(customerParty);
        result.setSellerSupplierParty(supplierParty);
        result.setAnticipatedMonetaryTotal(total);

        for (Act item : bean.getNodeActs("items")) {
            OrderLineType line = getOrderLine(item, supplier, currencyCode);
            result.getOrderLine().add(line);
        }
        return result;
    }

    /**
     * Returns an <tt>OrderLineType</tt> for an <em>act.supplierOrderItem</em>.
     *
     * @param act          the order item to map
     * @param supplier     the supplier
     * @param currencyCode the currency code
     * @return a new <tt>OrderLineType</tt> corresponding to the act
     */
    private OrderLineType getOrderLine(Act act, Party supplier, CurrencyCodeContentType currencyCode) {
        ActBean bean = factory.createActBean(act);
        Product product = (Product) bean.getNodeParticipant("product");

        OrderLineType orderLine = new OrderLineType();
        LineItemType lineItem = new LineItemType();

        ItemType item = getItem(supplier, product);
        lineItem.setItem(item);
        orderLine.setLineItem(lineItem);

        String packageUnits = bean.getString("packageUnits");
        String unitCode = getUnitCode(packageUnits);

        IDType id = getID(act.getId());
        QuantityType quantity = initQuantity(new QuantityType(), bean.getBigDecimal("quantity"), unitCode);
        LineExtensionAmountType lineAmount = initAmount(new LineExtensionAmountType(), bean.getBigDecimal("total"),
                                                        currencyCode);
        TotalTaxAmountType taxAmount = initAmount(new TotalTaxAmountType(), bean.getBigDecimal("tax"), currencyCode);
        PriceType price = getPrice(bean.getBigDecimal("unitPrice"), unitCode, currencyCode);

        lineItem.setID(id);
        lineItem.setQuantity(quantity);
        lineItem.setLineExtensionAmount(lineAmount);
        lineItem.setTotalTaxAmount(taxAmount);
        lineItem.setPrice(price);

        return orderLine;
    }

    /**
     * Returns a <tt>ItemType</tt> for a supplier and product.
     *
     * @param supplier the supplier
     * @param product  the product
     * @return an <tt>ItemType</tt> corresponding to the supplier and product
     */
    private ItemType getItem(Party supplier, Product product) {
        ItemType result = new ItemType();
        ItemIdentificationType buyersId = getItemIdentification(product.getId());
        ProductSupplier ps = getProductSupplier(product, supplier);
        if (ps == null) {
            throw new ESCIAdapterException(ESCIAdapterException.ErrorCode.NoProductSupplier, supplier.getName(),
                                           product.getName());
        }
        String reorderCode = ps.getReorderCode();
        String barCode = ps.getBarCode();
        String reorderDescription = ps.getReorderDescription();
        if (!StringUtils.isEmpty(reorderCode)) {
            ItemIdentificationType sellersId = getItemIdentification(reorderCode);
            result.setSellersItemIdentification(sellersId);
        } else if (!StringUtils.isEmpty("barCode")) {
            ItemIdentificationType sellersId = getItemIdentification(barCode);
            result.setSellersItemIdentification(sellersId);
        } else {
            throw new ESCIAdapterException(ESCIAdapterException.ErrorCode.NoSupplierOrderCode, supplier.getName(),
                                           product.getName());
        }
        if (!StringUtils.isEmpty(reorderDescription)) {
            DescriptionType description = initText(new DescriptionType(), reorderDescription);
            result.getDescription().add(description);
        }
        NameType name = initName(new NameType(), product.getName());
        result.setBuyersItemIdentification(buyersId);
        result.setName(name);
        return result;
    }

    /**
     * Returns a <tt>PriceType</tt> for the specified price and unit code.
     *
     * @param price        the price
     * @param unitCode     the quantity unit code (UN/CEFACT). May be <tt>null</tt>
     * @param currencyCode the currency code
     * @return the corresponding <tt>PriceType</tt> for price and unitCode
     */
    private PriceType getPrice(BigDecimal price, String unitCode, CurrencyCodeContentType currencyCode) {
        PriceType result = new PriceType();
        PriceAmountType priceAmount = initAmount(new PriceAmountType(), price, currencyCode);
        result.setPriceAmount(priceAmount);
        result.setBaseQuantity(initQuantity(new BaseQuantityType(), BigDecimal.ONE, unitCode));
        return result;
    }

    /**
     * Returns a <tt>MonetaryTotalType</tt> for an payable and tax amount.
     *
     * @param payableAmount the payable amount
     * @param currencyCode  currency code
     * @return the corresponding <tt>MonetaryTotalType</tt> for amount
     */
    private MonetaryTotalType getMonetaryTotal(BigDecimal payableAmount, CurrencyCodeContentType currencyCode) {
        MonetaryTotalType result = new MonetaryTotalType();
        result.setPayableAmount(initAmount(new PayableAmountType(), payableAmount, currencyCode));
        return result;
    }

    /**
     * Helper to initialise an <tt>AmountType</tt>.
     *
     * @param amount       the amount to initialise
     * @param value        the value
     * @param currencyCode the currency code
     * @return the amount
     */
    private <T extends AmountType> T initAmount(T amount, BigDecimal value, CurrencyCodeContentType currencyCode) {
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
                IMObjectBean lookupBean = factory.createBean(lookup);
                String unitCode = lookupBean.getString("unitCode");
                if (!StringUtils.isEmpty(unitCode)) {
                    result = unitCode;
                }
            }
        }
        return result;
    }

    /**
     * Returns the product/supplier relationship for the product, if available.
     *
     * @param product  the product
     * @param supplier the supplier
     * @return the product/supplier relationship, or <tt>null</tt> if none is found
     */
    private ProductSupplier getProductSupplier(Product product, Party supplier) {
        List<ProductSupplier> results = productRules.getProductSuppliers(product, supplier);
        return (!results.isEmpty()) ? results.get(0) : null;
    }

    /**
     * Helper to return the location associated with a stock location.
     *
     * @param stockLocation the stock location
     * @return the corresponding location, or <tt>null</tt> if none is found
     */
    private Party getLocation(Entity stockLocation) {
        EntityBean bean = factory.createEntityBean(stockLocation);
        // TODO - there could be more than one location which refers to different party.organisationLocation 
        return (Party) bean.getNodeSourceEntity("locations");
    }

    /**
     * Returns a <tt>CustomerPartyType</tt> corresponding to the passed <em>party.organisationLocation</em>.
     * <p/>
     * The party details will be either those of the <em>party.organisationLocation</em> or the parent
     * </em>party.organisationPractice</em>. If the location has a <em>contact.location</em>, then the location's
     * details will be used, otherwise the practice's details will be used.
     *
     * @param location the practice location
     * @return the corresponding <tt>CustomerPartyType</tt>
     */
    private CustomerPartyType getCustomer(Party location) {
        CustomerPartyType result = new CustomerPartyType();
        Party customer;

        Party practice = locationRules.getPractice(location);
        if (practice == null) {
            throw new IllegalStateException("No practice for location: " + location.getId());
        }

        Contact contact = partyRules.getContact(location, ContactArchetypes.LOCATION, "BILLING");
        if (contact == null) {
            contact = partyRules.getContact(practice, ContactArchetypes.LOCATION, "BILLING");
            if (contact == null) {
                throw new IllegalStateException("No contact.location for location: " + location.getId());
            }
            customer = practice;
        } else {
            customer = location;
        }

        CustomerAssignedAccountIDType id = initID(new CustomerAssignedAccountIDType(), customer.getId());
        PartyType party = getParty(customer, contact);

        result.setCustomerAssignedAccountID(id);
        result.setParty(party);
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
        Contact contact = partyRules.getContact(supplier, ContactArchetypes.LOCATION, null);

        result.setCustomerAssignedAccountID(accountId);
        result.setParty(getParty(supplier, contact));
        return result;
    }

    /**
     * Returns a <tt>PartyType</tt> for the supplied party and contact.
     *
     * @param party   the party
     * @param contact the location contact. May be <tt>null</tt>
     * @return the corresponding <tt>PartyType</tt>
     */
    private PartyType getParty(Party party, Contact contact) {
        PartyType result = new PartyType();

        PartyNameType partyName = new PartyNameType();
        partyName.setName(getName(party.getName()));
        result.getPartyName().add(partyName);
        if (contact != null) {
            result.setPostalAddress(getAddress(contact));
        }
        return result;
    }

    /**
     * Returns an <tt>AddressType</tt> for the supplied <em>contact.location</em>.
     *
     * @param contact the location contact
     * @return the corresponding <tt>AddressType</tt>
     */
    private AddressType getAddress(Contact contact) {
        IMObjectBean bean = factory.createBean(contact);

        AddressType result = new AddressType();
        AddressLineType addressLineType = new AddressLineType();
        LineType line = initText(new LineType(), bean.getString("address"));
        addressLineType.setLine(line);

        String city = lookupService.getName(contact, "suburb");
        CityNameType cityName = initName(new CityNameType(), city);

        String state = lookupService.getName(contact, "state");
        CountrySubentityType stateName = initText(new CountrySubentityType(), state);

        PostalZoneType postCode = initText(new PostalZoneType(), bean.getString("postcode"));

        result.getAddressLine().add(addressLineType);
        result.setCityName(cityName);
        result.setCountrySubentity(stateName);
        result.setPostalZone(postCode);
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
     * Initialises a <tt>NameType></tt> with the specified value.
     *
     * @param name  the name to initialise
     * @param value the value
     * @return the name
     */
    private <T extends org.oasis.ubl.common.NameType> T initName(T name, String value) {
        name.setValue(value);
        return name;
    }

    /**
     * Initialises a <tt>TextType></tt> with the specified value.
     *
     * @param text  the text to initialise
     * @param value the value
     * @return the text
     */
    private <T extends TextType> T initText(T text, String value) {
        text.setValue(value);
        return text;
    }

    /**
     * Initialises an <tt>IdentifierType</tt> with the specified value.
     *
     * @param id    the identifier to initialise
     * @param value the value
     * @return the id
     */
    private <T extends IdentifierType> T initID(T id, long value) {
        return initID(id, Long.toString(value));
    }

    /**
     * Initialises an <tt>IdentifierType</tt> with the specified value.
     *
     * @param id    the identifier to initialise
     * @param value the value
     * @return the id
     */
    private <T extends IdentifierType> T initID(T id, String value) {
        id.setValue(value);
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


    /**
     * Returns the currency code associated with the practice.
     *
     * @return the currency code
     */
    private CurrencyCodeContentType getCurrencyCode() {
        Party practice = practiceRules.getPractice();
        if (practice == null) {
            throw new IllegalStateException("No party.organisationPractice defined");
        }
        IMObjectBean bean = factory.createBean(practice);
        return CurrencyCodeContentType.valueOf(bean.getString("currency"));
    }


}
