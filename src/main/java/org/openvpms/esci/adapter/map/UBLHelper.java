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
package org.openvpms.esci.adapter.map;

import org.oasis.ubl.common.AmountType;
import org.oasis.ubl.common.CodeType;
import org.oasis.ubl.common.CurrencyCodeContentType;
import org.oasis.ubl.common.IdentifierType;
import org.oasis.ubl.common.QuantityType;
import org.oasis.ubl.common.TextType;
import org.oasis.ubl.common.aggregate.OrderReferenceType;
import org.oasis.ubl.common.basic.IDType;
import org.oasis.ubl.common.basic.IssueDateType;
import org.oasis.ubl.common.basic.IssueTimeType;
import org.oasis.ubl.common.basic.NameType;
import org.oasis.ubl.common.basic.PercentType;
import org.openvpms.archetype.rules.math.Currencies;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * UBL helper methods.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class UBLHelper {

    /**
     * Returns the currency associated with the practice.
     *
     * @param rules      the practice rules
     * @param currencies the currencies
     * @param factory    the bean factory
     * @return the currency
     */
    public static Currency getCurrency(PracticeRules rules, Currencies currencies, IMObjectBeanFactory factory) {
        Party practice = rules.getPractice();
        if (practice == null) {
            throw new IllegalStateException("No party.organisationPractice defined");
        }
        IMObjectBean bean = factory.createBean(practice);
        String code = bean.getString("currency");
        return currencies.getCurrency(code);
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
     * <p/>
     * This ensures that the value is expressed to the correct no. of decimal places.
     *
     * @param amount   the amount to initialise
     * @param value    the value
     * @param currency the currency
     * @return the amount
     */
    public static <T extends AmountType> T initAmount(T amount, BigDecimal value, Currency currency) {
        CurrencyCodeContentType code = CurrencyCodeContentType.valueOf(currency.getCode());
        BigDecimal rounded = currency.round(value);
        if (rounded.compareTo(value) != 0) {
            throw new ESCIAdapterException(ESCIAdapterMessages.amountTooManyDecimalPlaces(value));
        }
        amount.setCurrencyID(code);
        amount.setValue(rounded);
        return amount;
    }

    /**
     * Helper to initialise a <tt>QuantityType</tt>.
     * <p/>
     * This ensures that quantities are restricted to 2 decimal places.
     *
     * @param quantity the quantity to initialise
     * @param value    the value
     * @param unitCode the quantity unit code. May be <tt>null</tt>
     * @return the quantity
     * @throws ESCIAdapterException if the value loses precision when rounded
     */
    public static <T extends QuantityType> T initQuantity(T quantity, BigDecimal value, String unitCode) {
        BigDecimal rounded = MathRules.round(value, 2);
        if (rounded.compareTo(value) != 0) {
            throw new ESCIAdapterException(ESCIAdapterMessages.quantityTooManyDecimalPlaces(value));
        }
        quantity.setValue(rounded);
        quantity.setUnitCode(unitCode);
        return quantity;
    }

    /**
     * Creates a new <tt>PercentType</tt>.
     *
     * @param value the percentage value
     * @return a new <tt>PercentType</tt>
     */
    public static PercentType createPercent(BigDecimal value) {
        PercentType result = new PercentType();
        result.setValue(value);
        return result;
    }

    /**
     * Creates a new <tt>IssueDateType</tt> from a date.
     *
     * @param date    the date
     * @param factory the XML data type factory
     * @return a new <tt>IssueDateType</tt>
     */
    public static IssueDateType createIssueDate(Date date, DatatypeFactory factory) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return createIssueDate(calendar, factory);
    }

    /**
     * Creates a new <tt>IssueDateType</tt> from a date.
     *
     * @param calendar the calendar
     * @param factory  the XML data type factory
     * @return a new <tt>IssueDateType</tt>
     */
    public static IssueDateType createIssueDate(GregorianCalendar calendar, DatatypeFactory factory) {
        IssueDateType result = new IssueDateType();
        result.setValue(getDate(calendar, factory));
        return result;
    }

    /**
     * Creates a new <tt>IssueTimeType</tt> from a date.
     *
     * @param date    the date
     * @param factory the XML data type factory
     * @return a new <tt>IssueTimeType</tt>
     */
    public static IssueTimeType createIssueTime(Date date, DatatypeFactory factory) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return createIssueTime(calendar, factory);
    }

    /**
     * Creates a new <tt>IssueTimeType</tt> from a calendar.
     *
     * @param calendar the calendar
     * @param factory  the XML data type factory
     * @return a new <tt>IssueTimeType</tt>
     */
    public static IssueTimeType createIssueTime(GregorianCalendar calendar, DatatypeFactory factory) {
        IssueTimeType result = new IssueTimeType();
        result.setValue(getTime(calendar, factory));
        return result;
    }

    /**
     * Creates a new <tt>OrderReferenceType</tt>.
     *
     * @param id the identifier
     * @return a new <tt>OrderReferenceType</tt>
     */
    public static OrderReferenceType createOrderReference(long id) {
        OrderReferenceType result = new OrderReferenceType();
        result.setID(createID(id));
        return result;
    }

    /**
     * Initialises a code.
     *
     * @param code  the code to initialise
     * @param value the code value
     * @return the code
     */
    public static <T extends CodeType> T initCode(T code, String value) {
        code.setValue(value);
        return code;
    }

    /**
     * Helper to create a new <tt>XMLGregorianCalendar</tt> from an <tt>GregorianCalendar</tt>, populating just
     * the date components.
     *
     * @param calendar the calendar
     * @param factory  the XML data type factory
     * @return a new <tt>XMLGregorianCalendar</tt>
     */
    private static XMLGregorianCalendar getDate(GregorianCalendar calendar, DatatypeFactory factory) {
        XMLGregorianCalendar xml = factory.newXMLGregorianCalendar(calendar);
        xml.setHour(DatatypeConstants.FIELD_UNDEFINED);
        xml.setMinute(DatatypeConstants.FIELD_UNDEFINED);
        xml.setSecond(DatatypeConstants.FIELD_UNDEFINED);
        xml.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
        xml.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
        return xml;
    }

    /**
     * Helper to create a new <tt>XMLGregorianCalendar</tt> from an <tt>GregorianCalendar</tt>, populating just
     * the time components.
     *
     * @param calendar the calendar
     * @param factory  the XML data type factory
     * @return a new <tt>XMLGregorianCalendar</tt>
     */
    private static XMLGregorianCalendar getTime(GregorianCalendar calendar, DatatypeFactory factory) {
        XMLGregorianCalendar xml = factory.newXMLGregorianCalendar(calendar);
        xml.setDay(DatatypeConstants.FIELD_UNDEFINED);
        xml.setMonth(DatatypeConstants.FIELD_UNDEFINED);
        xml.setYear(DatatypeConstants.FIELD_UNDEFINED);
        xml.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
        return xml;
    }

}
