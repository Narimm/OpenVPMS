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
import org.oasis.ubl.common.IdentifierType;
import org.oasis.ubl.common.aggregate.AddressType;
import org.oasis.ubl.common.aggregate.CustomerPartyType;
import org.oasis.ubl.common.aggregate.ItemIdentificationType;
import org.oasis.ubl.common.aggregate.ItemType;
import org.oasis.ubl.common.aggregate.LineItemType;
import org.oasis.ubl.common.aggregate.OrderLineType;
import org.oasis.ubl.common.aggregate.PartyType;
import org.oasis.ubl.common.aggregate.SupplierPartyType;
import org.oasis.ubl.common.basic.CopyIndicatorType;
import org.oasis.ubl.common.basic.CustomerAssignedAccountIDType;
import org.oasis.ubl.common.basic.IDType;
import org.oasis.ubl.common.basic.IssueDateType;
import org.oasis.ubl.common.basic.NameType;
import org.oasis.ubl.common.basic.StreetNameType;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * Maps acts to UBL Orders and vice-versa.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderMapper {

    private final IArchetypeService service;

    private final PartyRules partyRules;

    private final LocationRules locationRules;

    public OrderMapper() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    public OrderMapper(IArchetypeService service) {
        this.service = service;
        partyRules = new PartyRules(service);
        locationRules = new LocationRules(service);
    }

    public OrderType map(Act order) {
        OrderType result = new OrderType();
        result.setID(createID(order.getId()));
        result.setCopyIndicator(createCopyIndicatorType(false));
        result.setIssueDate(createIssueDate(order.getActivityStartTime()));
        ActBean bean = new ActBean(order, service);
        Entity stockLocation = bean.getNodeParticipant("stockLocation");
        Party location = getLocation(stockLocation);
        Party supplier = (Party) bean.getNodeParticipant("supplier");
        result.setBuyerCustomerParty(getCustomer(location));
        result.setSellerSupplierParty(getSupplier(supplier));
        for (Act item : bean.getNodeActs("items")) {
            result.getOrderLine().add(getOrderLine(item, supplier));
        }
        return result;
    }

    private OrderLineType getOrderLine(Act act, Party supplier) {
        ActBean bean = new ActBean(act, service);
        Product product = (Product) bean.getNodeParticipant("product");
        OrderLineType orderLine = new OrderLineType();
        LineItemType lineItem = new LineItemType();
        lineItem.setID(createID(act.getId()));
        ItemType item = new ItemType();
        item.setBuyersItemIdentification(createItemIdentificationType(product));
        item.setSellersItemIdentification(createSellersItemIdentification(product, supplier));
        item.setName(createNameType(product.getName()));
        lineItem.setItem(item);
        orderLine.setLineItem(lineItem);
        return orderLine;
    }

    private ItemIdentificationType createSellersItemIdentification(Product product, Party supplier) {
        ItemIdentificationType result = null;
        EntityBean bean = new EntityBean(supplier, service);
        EntityRelationship relationship = bean.getRelationship(product);
        if (relationship != null) {
            IMObjectBean relBean = new IMObjectBean(relationship, service);
            String id = relBean.getString("reorderCode");
            if (!StringUtils.isEmpty(id)) {
                result = new ItemIdentificationType();
                result.setID(createID(id));
            }
        }
        return result;
    }


    private NameType createNameType(String name) {
        NameType result = new NameType();
        result.setValue(name);
        return result;
    }

    private ItemIdentificationType createItemIdentificationType(Product product) {
        ItemIdentificationType result = new ItemIdentificationType();
        result.setID(createID(product.getId()));
        return result;
    }

    private SupplierPartyType getSupplier(Party supplier) {
        SupplierPartyType result = new SupplierPartyType();
        result.setCustomerAssignedAccountID(setId(new CustomerAssignedAccountIDType(), supplier.getId()));
        return result;
    }

    private Party getLocation(Entity stockLocation) {
        EntityBean bean = new EntityBean(stockLocation, service);
        // TODO - there could be more than one location which refers to different party.organisationLocation 
        return (Party) bean.getNodeSourceEntity("locations");
    }

    private CustomerPartyType getCustomer(Party location) {
        CustomerPartyType result = new CustomerPartyType();
        result.setCustomerAssignedAccountID(setId(new CustomerAssignedAccountIDType(), location.getId()));
        result.setParty(createPartyType(location));
        return result;
    }

    private PartyType createPartyType(Party location) {
        PartyType result = new PartyType();
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
        result.setPostalAddress(createAddressType(contact));
        return result;
    }

    private AddressType createAddressType(Contact contact) {
        IMObjectBean bean = new IMObjectBean(contact, service);
        AddressType result = new AddressType();
        StreetNameType street = new StreetNameType();
        street.setValue(bean.getString("address"));
        result.setStreetName(street);
        return result;
    }

    private IssueDateType createIssueDate(Date activityStartTime) {
        IssueDateType result = new IssueDateType();
        DatatypeFactory factory;
        try {
            factory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException exception) {
            throw new IllegalStateException(exception);
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(DateRules.getDate(activityStartTime));
        XMLGregorianCalendar xml = factory.newXMLGregorianCalendar(calendar);
        xml.setHour(DatatypeConstants.FIELD_UNDEFINED);
        xml.setMinute(DatatypeConstants.FIELD_UNDEFINED);
        xml.setSecond(DatatypeConstants.FIELD_UNDEFINED);
        xml.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
        xml.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
        result.setValue(xml);
        return result;
    }

    private CopyIndicatorType createCopyIndicatorType(boolean value) {
        CopyIndicatorType result = new CopyIndicatorType();
        result.setValue(value);
        return result;
    }

    private <T extends IdentifierType> T setId(T id, long value) {
        id.setValue(Long.toHexString(value));
        return id;
    }

    private <T extends IDType> T setId(T id, long value) {
        id.setValue(Long.toHexString(value));
        return id;
    }

    private IDType createID(long id) {
        IDType result = new IDType();
        return setId(result, id);
    }

    private IDType createID(String id) {
        IDType result = new IDType();
        result.setValue(id);
        return result;
    }

}
