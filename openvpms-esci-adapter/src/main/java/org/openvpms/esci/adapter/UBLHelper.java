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
import org.oasis.ubl.common.AmountType;
import org.oasis.ubl.common.CurrencyCodeContentType;
import org.oasis.ubl.common.IdentifierType;
import org.oasis.ubl.common.QuantityType;
import org.oasis.ubl.common.TextType;
import org.oasis.ubl.common.basic.IDType;
import org.oasis.ubl.common.basic.IssueDateType;
import org.oasis.ubl.common.basic.IssueTimeType;
import org.oasis.ubl.common.basic.NameType;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.component.business.service.lookup.ILookupService;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * UBL helper methods.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class UBLHelper {

    /**
     * Returns the <em>lookup.uom</em> code for a given UN/CEFACT unit code.
     *
     * @param unitCode the UN/CEFACT unit code
     * @param service  the lookup service
     * @param factory  the bean factory
     * @return the corresponding unit code or <tt>null</tt> if none is found
     */
    public static String getUnitOfMeasure(String unitCode, ILookupService service, IMObjectBeanFactory factory) {
        Collection<Lookup> lookups = service.getLookups("lookup.uom");
        for (Lookup lookup : lookups) {
            IMObjectBean lookupBean = factory.createBean(lookup);
            String code = lookupBean.getString("unitCode");
            if (StringUtils.equals(unitCode, code)) {
                return lookup.getCode();
            }
        }
        return null;
    }

    /**
     * Returns the currency code associated with the practice.
     *
     * @param rules   the practice rules
     * @param factory the bean factory
     * @return the currency code
     */
    public static String getCurrencyCode(PracticeRules rules, IMObjectBeanFactory factory) {
        Party practice = rules.getPractice();
        if (practice == null) {
            throw new IllegalStateException("No party.organisationPractice defined");
        }
        IMObjectBean bean = factory.createBean(practice);
        return bean.getString("currency");
    }

    /**
     * Returns a new <tt>IDType</tt> with the specified value.
     *
     * @param id the identifier value
     * @return a new <tt>IDType</tt>
     */
    public static IDType createID(String id) {
        IDType result = new IDType();
        result.setValue(id);
        return result;
    }

    /**
     * Returns a new <tt>IDType</tt> with the specified value.
     *
     * @param id the identifier value
     * @return a new <tt>IDType</tt>
     */
    public static IDType createID(long id) {
        IDType result = new IDType();
        return initID(result, id);
    }

    /**
     * Initialises an <tt>IdentifierType</tt> with the specified value.
     *
     * @param id    the identifier to initialise
     * @param value the value
     * @return the id
     */
    public static <T extends IdentifierType> T initID(T id, String value) {
        id.setValue(value);
        return id;
    }

    /**
     * Initialises an <tt>IdentifierType</tt> with the specified value.
     *
     * @param id    the identifier to initialise
     * @param value the value
     * @return the id
     */
    public static <T extends IdentifierType> T initID(T id, long value) {
        return initID(id, Long.toString(value));
    }

    /**
     * Initialises a <tt>TextType></tt> with the specified value.
     *
     * @param text  the text to initialise
     * @param value the value
     * @return the text
     */
    public static <T extends TextType> T initText(T text, String value) {
        text.setValue(value);
        return text;
    }

    /**
     * Returns a <tt>NameType</tt> for the given name.
     *
     * @param name the name
     * @return a new <tt>NameType</tt>
     */
    public static NameType createName(String name) {
        NameType result = new NameType();
        result.setValue(name);
        return result;
    }

    /**
     * Initialises a <tt>NameType></tt> with the specified value.
     *
     * @param name  the name to initialise
     * @param value the value
     * @return the name
     */
    public static <T extends org.oasis.ubl.common.NameType> T initName(T name, String value) {
        name.setValue(value);
        return name;
    }

    /**
     * Helper to initialise an <tt>AmountType</tt>.
     *
     * @param amount       the amount to initialise
     * @param value        the value
     * @param currencyCode the currency code
     * @return the amount
     */
    public static <T extends AmountType> T initAmount(T amount, BigDecimal value,
                                                      CurrencyCodeContentType currencyCode) {
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
    public static <T extends QuantityType> T initQuantity(T quantity, BigDecimal value,
                                                          String unitCode) {
        quantity.setValue(value);
        quantity.setUnitCode(unitCode);
        return quantity;
    }


    public static IssueDateType createIssueDate(Date date, DatatypeFactory factory) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return createIssueDate(calendar, factory);
    }

    public static IssueDateType createIssueDate(GregorianCalendar calendar, DatatypeFactory factory) {
        IssueDateType result = new IssueDateType();
        result.setValue(getDate(calendar, factory));
        return result;
    }

    public static XMLGregorianCalendar getDate(GregorianCalendar calendar, DatatypeFactory factory) {
        XMLGregorianCalendar xml = factory.newXMLGregorianCalendar(calendar);
        xml.setHour(DatatypeConstants.FIELD_UNDEFINED);
        xml.setMinute(DatatypeConstants.FIELD_UNDEFINED);
        xml.setSecond(DatatypeConstants.FIELD_UNDEFINED);
        xml.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
        xml.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
        return xml;
    }

    public static IssueTimeType createIssueTime(Date date, DatatypeFactory factory) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return createIssueTime(calendar, factory);
    }

    public static IssueTimeType createIssueTime(GregorianCalendar calendar, DatatypeFactory factory) {
        IssueTimeType result = new IssueTimeType();
        result.setValue(getTime(calendar, factory));
        return result;
    }

    public static XMLGregorianCalendar getTime(GregorianCalendar calendar, DatatypeFactory factory) {
        XMLGregorianCalendar xml = factory.newXMLGregorianCalendar(calendar);
        xml.setDay(DatatypeConstants.FIELD_UNDEFINED);
        xml.setMonth(DatatypeConstants.FIELD_UNDEFINED);
        xml.setYear(DatatypeConstants.FIELD_UNDEFINED);
        xml.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
        return xml;
    }
}
