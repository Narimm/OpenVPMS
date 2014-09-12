package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v25.datatype.CE;
import ca.uhn.hl7v2.model.v25.datatype.XCN;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.hl7.PatientContext;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class PopulateHelper {

    public static void populateClinicianSegment(XCN xcn, PatientContext context) throws DataTypeException {
        xcn.getIDNumber().setValue(Long.toString(context.getClinicianId()));
        xcn.getGivenName().setValue(context.getClinicianFirstName());
        xcn.getFamilyName().getSurname().setValue(context.getClinicianLastName());
    }

    public static void populateProduct(CE ce, Product product) throws DataTypeException {
        populateCE(ce, product.getId(), product.getName());
    }

    public static void populateCE(CE ce, Long id, String text) throws DataTypeException {
        populateCE(ce, Long.toString(id), text);
    }

    public static void populateCE(CE ce, String id, String text) throws DataTypeException {
        populateCE(ce, id, text, "OpenVPMS");
    }

    public static void populateCE(CE ce, String id, String text, String codingSystem) throws DataTypeException {
        ce.getIdentifier().setValue(id);
        ce.getText().setValue(text);
        ce.getNameOfCodingSystem().setValue(codingSystem);
    }

}
