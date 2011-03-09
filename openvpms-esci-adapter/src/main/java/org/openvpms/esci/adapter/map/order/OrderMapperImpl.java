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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.math.Currencies;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.map.UBLHelper;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.esci.ubl.common.aggregate.AddressLineType;
import org.openvpms.esci.ubl.common.aggregate.AddressType;
import org.openvpms.esci.ubl.common.aggregate.ContactType;
import org.openvpms.esci.ubl.common.aggregate.CustomerPartyType;
import org.openvpms.esci.ubl.common.aggregate.ItemIdentificationType;
import org.openvpms.esci.ubl.common.aggregate.ItemType;
import org.openvpms.esci.ubl.common.aggregate.LineItemType;
import org.openvpms.esci.ubl.common.aggregate.MonetaryTotalType;
import org.openvpms.esci.ubl.common.aggregate.OrderLineType;
import org.openvpms.esci.ubl.common.aggregate.PartyNameType;
import org.openvpms.esci.ubl.common.aggregate.PartyType;
import org.openvpms.esci.ubl.common.aggregate.PriceType;
import org.openvpms.esci.ubl.common.aggregate.SupplierPartyType;
import org.openvpms.esci.ubl.common.aggregate.TaxTotalType;
import org.openvpms.esci.ubl.common.basic.BaseQuantityType;
import org.openvpms.esci.ubl.common.basic.CityNameType;
import org.openvpms.esci.ubl.common.basic.CopyIndicatorType;
import org.openvpms.esci.ubl.common.basic.CountrySubentityType;
import org.openvpms.esci.ubl.common.basic.CustomerAssignedAccountIDType;
import org.openvpms.esci.ubl.common.basic.DescriptionType;
import org.openvpms.esci.ubl.common.basic.ElectronicMailType;
import org.openvpms.esci.ubl.common.basic.IDType;
import org.openvpms.esci.ubl.common.basic.IssueDateType;
import org.openvpms.esci.ubl.common.basic.IssueTimeType;
import org.openvpms.esci.ubl.common.basic.LineExtensionAmountType;
import org.openvpms.esci.ubl.common.basic.LineType;
import org.openvpms.esci.ubl.common.basic.NameType;
import org.openvpms.esci.ubl.common.basic.PayableAmountType;
import org.openvpms.esci.ubl.common.basic.PostalZoneType;
import org.openvpms.esci.ubl.common.basic.PriceAmountType;
import org.openvpms.esci.ubl.common.basic.QuantityType;
import org.openvpms.esci.ubl.common.basic.SupplierAssignedAccountIDType;
import org.openvpms.esci.ubl.common.basic.TaxAmountType;
import org.openvpms.esci.ubl.common.basic.TelefaxType;
import org.openvpms.esci.ubl.common.basic.TelephoneType;
import org.openvpms.esci.ubl.common.basic.TotalTaxAmountType;
import org.openvpms.esci.ubl.common.basic.UBLVersionIDType;
import org.openvpms.esci.ubl.order.Order;

import javax.annotation.Resource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigDecimal;
import java.util.GregorianCalendar;


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
     * The supplier rules.
     */
    private SupplierRules supplierRules;

    /**
     * The currencies.
     */
    private Currencies currencies;

    /**
     * The bean factory.
     */
    private IMObjectBeanFactory factory;

    /**
     * XML data type factory.
     */
    private DatatypeFactory datatypeFactory;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(OrderMapperImpl.class);

    /**
     * Default package unit code, if none is specified. This corresponds to "each" in UNE/ECE rec 20
     * (http://docs.oasis-open.org/ubl/cs-UBL-2.0/cl/gc/cefact/UnitOfMeasureCode-2.0.gc)
     */
    private static final String DEFAULT_PACKAGE_UNITS = "EA";


    /**
     * Default constructor.
     */
    public OrderMapperImpl() {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException exception) {
            throw new IllegalStateException(exception);
        }
    }

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
     * Registers the supplier rules.
     *
     * @param rules the supplier rules
     */
    @Resource
    public void setSupplierRules(SupplierRules rules) {
        supplierRules = rules;
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
     * Registers the currencies.
     *
     * @param currencies the currencies
     */
    @Resource
    public void setCurrencies(Currencies currencies) {
        this.currencies = currencies;
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
     * @throws ESCIAdapterException      for mapping errors
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Order map(FinancialAct order) {
        Order result = new Order();
        Currency currency = getCurrency();

        UBLVersionIDType version = UBLHelper.initID(new UBLVersionIDType(), "2.0");
        IDType id = UBLHelper.createID(order.getId());
        CopyIndicatorType copyIndicator = getCopyIndicatorType(false);

        GregorianCalendar startTime = new GregorianCalendar();
        startTime.setTime(order.getActivityStartTime());
        IssueDateType issueDate = UBLHelper.createIssueDate(startTime, datatypeFactory);
        IssueTimeType issueTime = UBLHelper.createIssueTime(startTime, datatypeFactory);

        ActBean bean = factory.createActBean(order);
        Entity author = bean.getNodeParticipant("author");
        Party stockLocation = (Party) bean.getNodeParticipant("stockLocation");
        Party location = getLocation(stockLocation);

        Party supplier = (Party) bean.getNodeParticipant("supplier");
        EntityRelationship supplierStockLocation = supplierRules.getSupplierStockLocation(supplier, stockLocation);
        if (supplierStockLocation == null) {
            throw new ESCIAdapterException(ESCIAdapterMessages.ESCINotConfigured(supplier, stockLocation));
        }
        String contactName = (author != null) ? author.getName() : null;
        CustomerPartyType customerParty = getCustomer(contactName, location, stockLocation, supplierStockLocation);
        SupplierPartyType supplierParty = getSupplier(supplier);

        TaxTotalType taxTotal = getTaxTotal(order, currency);
        MonetaryTotalType total = getMonetaryTotal(order, currency);

        result.setUBLVersionID(version);
        result.setID(id);
        result.setCopyIndicator(copyIndicator);
        result.setIssueDate(issueDate);
        result.setIssueTime(issueTime);
        result.setBuyerCustomerParty(customerParty);
        result.setSellerSupplierParty(supplierParty);
        result.getTaxTotal().add(taxTotal);
        result.setAnticipatedMonetaryTotal(total);

        for (Act item : bean.getNodeActs("items")) {
            OrderLineType line = getOrderLine(item, supplier, currency);
            result.getOrderLine().add(line);
        }
        return result;
    }

    /**
     * Returns an <tt>OrderLineType</tt> for an <em>act.supplierOrderItem</em>.
     *
     * @param act      the order item to map
     * @param supplier the supplier
     * @param currency the currency that amounts are expressed in
     * @return a new <tt>OrderLineType</tt> corresponding to the act
     */
    private OrderLineType getOrderLine(Act act, Party supplier, Currency currency) {
        ActBean bean = factory.createActBean(act);
        Product product = (Product) bean.getNodeParticipant("product");

        OrderLineType orderLine = new OrderLineType();
        LineItemType lineItem = new LineItemType();

        ItemType item = getItem(bean, supplier, product);
        lineItem.setItem(item);
        orderLine.setLineItem(lineItem);

        String packageUnits = bean.getString("packageUnits");
        String unitCode = getUnitCode(packageUnits);

        IDType id = UBLHelper.createID(act.getId());
        QuantityType quantity = UBLHelper.initQuantity(new QuantityType(), bean.getBigDecimal("quantity"), unitCode);
        LineExtensionAmountType lineAmount
                = UBLHelper.initAmount(new LineExtensionAmountType(), bean.getBigDecimal("total"), currency);
        TotalTaxAmountType taxAmount
                = UBLHelper.initAmount(new TotalTaxAmountType(), bean.getBigDecimal("tax"), currency);
        PriceType price = getPrice(bean.getBigDecimal("unitPrice"), unitCode, currency);

        lineItem.setID(id);
        lineItem.setQuantity(quantity);
        lineItem.setLineExtensionAmount(lineAmount);
        lineItem.setTotalTaxAmount(taxAmount);
        lineItem.setPrice(price);

        return orderLine;
    }

    /**
     * Returns a <tt>ItemType</tt> for a supplier, order item, and product.
     *
     * @param bean     the order item
     * @param supplier the supplier
     * @param product  the product
     * @return an <tt>ItemType</tt> corresponding to the supplier and product
     */
    private ItemType getItem(ActBean bean, Party supplier, Product product) {
        ItemType result = new ItemType();
        ItemIdentificationType buyersId = getItemIdentification(product.getId());
        String reorderCode = bean.getString("reorderCode");
        String reorderDescription = bean.getString("reorderDescription");
        if (!StringUtils.isEmpty(reorderCode)) {
            ItemIdentificationType sellersId = getItemIdentification(reorderCode);
            result.setSellersItemIdentification(sellersId);
        } else {
            throw new ESCIAdapterException(ESCIAdapterMessages.noSupplierOrderCode(supplier, product));
        }
        if (!StringUtils.isEmpty(reorderDescription)) {
            DescriptionType description = UBLHelper.initText(new DescriptionType(), reorderDescription);
            result.getDescription().add(description);
        }
        NameType name = UBLHelper.initName(new NameType(), product.getName());
        result.setBuyersItemIdentification(buyersId);
        result.setName(name);
        return result;
    }

    /**
     * Returns a <tt>PriceType</tt> for the specified price and unit code.
     *
     * @param price    the price
     * @param unitCode the quantity unit code (UN/CEFACT). May be <tt>null</tt>
     * @param currency the currency
     * @return the corresponding <tt>PriceType</tt> for price and unitCode
     */
    private PriceType getPrice(BigDecimal price, String unitCode, Currency currency) {
        PriceType result = new PriceType();
        PriceAmountType priceAmount = UBLHelper.initAmount(new PriceAmountType(), price, currency);
        result.setPriceAmount(priceAmount);
        result.setBaseQuantity(UBLHelper.initQuantity(new BaseQuantityType(), BigDecimal.ONE, unitCode));
        return result;
    }

    /**
     * Returns a <tt>TaxTotalType</tt> for an order.
     *
     * @param order    the order
     * @param currency the currency
     * @return the corresponding <tt>TaxTotalType</tt>
     */
    private TaxTotalType getTaxTotal(FinancialAct order, Currency currency) {
        TaxTotalType result = new TaxTotalType();
        result.setTaxAmount(UBLHelper.initAmount(new TaxAmountType(), order.getTaxAmount(), currency));
        return result;
    }

    /**
     * Returns a <tt>MonetaryTotalType</tt> for an order.
     *
     * @param order    the order
     * @param currency the currency
     * @return the corresponding <tt>MonetaryTotalType</tt>
     */
    private MonetaryTotalType getMonetaryTotal(FinancialAct order, Currency currency) {
        BigDecimal payableAmount = order.getTotal();
        BigDecimal lineExtensionAmount = payableAmount.subtract(order.getTaxAmount());

        MonetaryTotalType result = new MonetaryTotalType();
        result.setLineExtensionAmount(UBLHelper.initAmount(new LineExtensionAmountType(), lineExtensionAmount,
                                                           currency));
        result.setPayableAmount(UBLHelper.initAmount(new PayableAmountType(), payableAmount, currency));
        return result;
    }

    /**
     * Returns the UN/CEFACT unit code for the given package units code from an <em>lookup.uom</em>.
     * <p/>
     * If no package is specified, defaults to {@link #DEFAULT_PACKAGE_UNITS}.
     *
     * @param packageUnits the package units code
     * @return the corresponding unit code
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
            if (result == null) {
                log.warn("No unit code for package units=" + packageUnits + ". Defaulting to " + DEFAULT_PACKAGE_UNITS);
            }
        }
        if (result == null) {
            result = DEFAULT_PACKAGE_UNITS;
        }
        return result;
    }

    /**
     * Helper to return the location associated with a stock location.
     *
     * @param stockLocation the stock location
     * @return the corresponding location
     * @throws ESCIAdapterException if the stock location isn't associated with a practice location
     */
    private Party getLocation(Party stockLocation) {
        EntityBean bean = factory.createEntityBean(stockLocation);
        // TODO - there could be more than one location which refers to different party.organisationLocation 
        Party result = (Party) bean.getNodeSourceEntity("locations");
        if (result == null) {
            throw new ESCIAdapterException(ESCIAdapterMessages.noPracticeLocationForStockLocation(stockLocation));
        }
        return result;
    }

    /**
     * Returns a <tt>CustomerPartyType</tt> corresponding to the passed <em>party.organisationStockLocation</em>.
     * <p/>
     * The contact details will be either those of the <em>party.organisationLocation</em> or the parent
     * </em>party.organisationPractice</em>. If the location has a <em>contact.location</em>, then the location's
     * details will be used, otherwise the practice's details will be used.
     * <p/>
     * The customer identifier will be that of the stock location.
     * <p/>
     * NOTE: the supplied <em>entityRelationship.supplierStockLocation*</em> relationship may have an optional
     * <em>accountId</em> node, used to populate the <tt>SupplierAssignedAccountIDType</tt>
     *
     * @param contactName           a contact name to supply with telephone, email and fax details, May be <tt>null</tt>
     * @param location              the practice location
     * @param stockLocation         the stock location
     * @param supplierStockLocation an <em>entityRelationship.supplierStockLocation*</em> relationship
     * @return the corresponding <tt>CustomerPartyType</tt>
     */
    private CustomerPartyType getCustomer(String contactName, Party location, Party stockLocation,
                                          EntityRelationship supplierStockLocation) {
        CustomerPartyType result = new CustomerPartyType();
        Party customer;

        Party practice = locationRules.getPractice(location);
        if (practice == null) {
            throw new IllegalStateException("No practice for location: " + location.getId());
        }

        Contact locationContact = partyRules.getContact(location, ContactArchetypes.LOCATION, "BILLING");
        if (locationContact == null) {
            locationContact = partyRules.getContact(practice, ContactArchetypes.LOCATION, "BILLING");
            if (locationContact == null) {
                throw new IllegalStateException("No contact.location for location: " + location.getId());
            }
            customer = practice;
        } else {
            customer = location;
        }
        Contact phoneContact = partyRules.getContact(customer, ContactArchetypes.PHONE, "BILLING");
        Contact faxContact = partyRules.getContact(customer, ContactArchetypes.FAX, "BILLING");
        Contact emailContact = partyRules.getContact(customer, ContactArchetypes.EMAIL, "BILLING");

        CustomerAssignedAccountIDType customerId
                = UBLHelper.initID(new CustomerAssignedAccountIDType(), stockLocation.getId());

        PartyType party = getParty(customer, locationContact);
        party.setContact(getContact(contactName, phoneContact, faxContact, emailContact));

        result.setCustomerAssignedAccountID(customerId);

        IMObjectBean bean = factory.createBean(supplierStockLocation);
        String accountId = bean.getString("accountId");
        if (!StringUtils.isEmpty(accountId)) {
            SupplierAssignedAccountIDType supplierId = UBLHelper.initID(new SupplierAssignedAccountIDType(), accountId);
            result.setSupplierAssignedAccountID(supplierId);
        }

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

        CustomerAssignedAccountIDType accountId
                = UBLHelper.initID(new CustomerAssignedAccountIDType(), supplier.getId());
        Contact contact = partyRules.getContact(supplier, ContactArchetypes.LOCATION, null);

        result.setCustomerAssignedAccountID(accountId);
        result.setParty(getParty(supplier, contact));
        return result;
    }

    /**
     * Returns a <tt>PartyType</tt> for the supplied party and contact.
     *
     * @param party    the party
     * @param location the location contact. May be <tt>null</tt>
     * @return the corresponding <tt>PartyType</tt>
     */
    private PartyType getParty(Party party, Contact location) {
        PartyType result = new PartyType();

        PartyNameType partyName = new PartyNameType();
        partyName.setName(UBLHelper.createName(party.getName()));
        result.getPartyName().add(partyName);
        if (location != null) {
            result.setPostalAddress(getAddress(location));
        }
        return result;
    }

    /**
     * Returns a <tt>ContactType</tt> for the supplied contacts.
     *
     * @param name  a contact name
     * @param phone the phone contact. May be <tt>null</tt>
     * @param fax   the fax contact. May be <tt>null</tt>
     * @param email the email contact. May be <tt>null</tt>
     * @return the corresponding <tt>ContactType</tt>
     */
    private ContactType getContact(String name, Contact phone, Contact fax, Contact email) {
        ContactType contact = new ContactType();
        if (!StringUtils.isEmpty(name)) {
            contact.setName(UBLHelper.initName(new NameType(), name));
        }
        contact.setTelephone(getPhone(phone));
        contact.setTelefax(getFax(fax));
        contact.setElectronicMail(getEmail(email));
        return contact;
    }

    /**
     * Returns an <tt>TelephoneType</tt> for a <em>contact.phoneNumber</em>.
     *
     * @param contact the phone contact. May be <tt>null</tt>
     * @return a new <tt>TelephoneType</tt> or <tt>null</tt> if <tt>contact</tt> is null or unpopulated
     */
    private TelephoneType getPhone(Contact contact) {
        String phone = formatPhone(contact, "areaCode", "telephoneNumber");
        return (phone != null) ? UBLHelper.initText(new TelephoneType(), phone) : null;
    }

    /**
     * Returns an <tt>TelefaxType</tt> for a <em>contact.faxNumber</em>.
     *
     * @param contact the fax contact. May be <tt>null</tt>
     * @return a new <tt>TelefaxType</tt> or <tt>null</tt> if <tt>contact</tt> is null or unpopulated
     */
    private TelefaxType getFax(Contact contact) {
        String fax = formatPhone(contact, "areaCode", "faxNumber");
        return (fax != null) ? UBLHelper.initText(new TelefaxType(), fax) : null;
    }

    /**
     * Helper to format a phone/fax number.
     *
     * @param contact      the phone/fax contact. May be <tt>null</tt>
     * @param areaCodeNode the area code node.
     * @param numberNode   the phone number node
     * @return a formatted number, or <tt>null</tt> if <tt>number</tt> is null
     */
    private String formatPhone(Contact contact, String areaCodeNode, String numberNode) {
        String result = null;
        if (contact != null) {
            IMObjectBean bean = factory.createBean(contact);
            String number = bean.getString(numberNode);
            if (!StringUtils.isEmpty(number)) {
                String areaCode = bean.getString(areaCodeNode);
                if (!StringUtils.isEmpty(areaCode)) {
                    result = "(" + areaCode + ") " + number; // todo - localise
                } else {
                    result = number;
                }
            }
        }
        return result;
    }

    /**
     * Returns an <tt>ElectronicMailType</tt> for a <em>contact.email</em>.
     *
     * @param contact the email contact. May be <tt>null</tt>
     * @return a new <tt>ElectronicMailType</tt> or <tt>null</tt> if <tt>contact</tt> is null or unpopulated
     */
    private ElectronicMailType getEmail(Contact contact) {
        String email = null;
        if (contact != null) {
            IMObjectBean bean = factory.createBean(contact);
            email = StringUtils.trimToNull(bean.getString("emailAddress"));
        }
        return (email != null) ? UBLHelper.initText(new ElectronicMailType(), email) : null;
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
        LineType line = UBLHelper.initText(new LineType(), bean.getString("address"));
        addressLineType.setLine(line);

        String city = lookupService.getName(contact, "suburb");
        CityNameType cityName = UBLHelper.initName(new CityNameType(), city);

        String state = lookupService.getName(contact, "state");
        CountrySubentityType stateName = UBLHelper.initText(new CountrySubentityType(), state);

        PostalZoneType postCode = UBLHelper.initText(new PostalZoneType(), bean.getString("postcode"));

        result.getAddressLine().add(addressLineType);
        result.setCityName(cityName);
        result.setCountrySubentity(stateName);
        result.setPostalZone(postCode);
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
        result.setID(UBLHelper.createID(id));
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
        result.setID(UBLHelper.createID(id));
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
     * Returns the currency associated with the practice.
     *
     * @return the currency code
     */
    private Currency getCurrency() {
        return UBLHelper.getCurrency(practiceRules, currencies, factory);
    }

}
