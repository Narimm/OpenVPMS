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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v25.datatype.CE;
import ca.uhn.hl7v2.model.v25.datatype.DTM;
import ca.uhn.hl7v2.model.v25.datatype.XCN;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.hl7.PatientContext;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Helper to populate HL7 primitives.
 *
 * @author Tim Anderson
 */
public class PopulateHelper {

    /**
     * Populates a DTM with a calendar.
     *
     * @param dtm    the date/time to populate
     * @param value  the value to populate with
     * @param config the message population configuration
     * @throws DataTypeException for any error
     */
    public static void populateDTM(DTM dtm, Calendar value, MessageConfig config) throws DataTypeException {
        if (!config.isIncludeMillis()) {
            value.set(Calendar.MILLISECOND, 0);
        }
        dtm.setValue(value);
        if (!config.isIncludeTimeZone()) {
            // TODO - doesn't appear to be a clean way of doing this
            String formatted = dtm.getValue();
            formatted = formatted.substring(0, formatted.indexOf("+"));
            dtm.setValue(formatted);
        }
    }

    /**
     * Populates a DTM with a date.
     *
     * @param dtm    the date/time to populate
     * @param value  the value to populate with
     * @param config the message population configuration
     * @throws DataTypeException for any error
     */
    public static void populateDTM(DTM dtm, Date value, MessageConfig config)
            throws DataTypeException {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(value);
        populateDTM(dtm, calendar, config);
    }

    /**
     * Populates an {@code XCN} with a clinician.
     *
     * @param xcn     the extended composite number and name to populate
     * @param context the patient context
     * @throws DataTypeException for any error
     */
    public static void populateClinician(XCN xcn, PatientContext context) throws DataTypeException {
        xcn.getIDNumber().setValue(Long.toString(context.getClinicianId()));
        xcn.getGivenName().setValue(context.getClinicianFirstName());
        xcn.getFamilyName().getSurname().setValue(context.getClinicianLastName());
    }

    /**
     * Populates a {@code CE} with a product.
     *
     * @param ce      the coded element to populate
     * @param product the product
     * @throws DataTypeException for any error
     */
    public static void populateProduct(CE ce, Product product) throws DataTypeException {
        populateCE(ce, product.getId(), product.getName());
    }

    /**
     * Populates a {@code CE}.
     *
     * @param ce   the coded element to populate
     * @param id   the id
     * @param text the text
     * @throws DataTypeException for any error
     */
    public static void populateCE(CE ce, long id, String text) throws DataTypeException {
        populateCE(ce, Long.toString(id), text);
    }

    /**
     * Populates a {@code CE}.
     *
     * @param ce   the coded element to populate
     * @param id   the id
     * @param text the text
     * @throws DataTypeException for any error
     */
    public static void populateCE(CE ce, String id, String text) throws DataTypeException {
        populateCE(ce, id, text, "OpenVPMS");
    }

    /**
     * Populates a {@code CE}.
     *
     * @param ce           the coded element to populate
     * @param id           the id
     * @param text         the text
     * @param codingSystem the coding system
     * @throws DataTypeException for any error
     */
    public static void populateCE(CE ce, String id, String text, String codingSystem) throws DataTypeException {
        ce.getIdentifier().setValue(id);
        ce.getText().setValue(text);
        ce.getNameOfCodingSystem().setValue(codingSystem);
    }

}
