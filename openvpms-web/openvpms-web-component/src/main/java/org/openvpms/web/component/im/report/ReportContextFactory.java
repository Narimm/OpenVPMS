package org.openvpms.web.component.im.report;

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.report.IMReport;
import org.openvpms.web.component.app.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates fields from a {@link Context}, to pass to an {@link IMReport} .
 * <p/>
 * The following fields are defined:
 * <ul>
 * <li>OpenVPMS.customer - {@link Context#getCustomer()}</li>
 * <li>OpenVPMS.patient - {@link Context#getPatient()}</li>
 * <li>OpenVPMS.practice - {@link Context#getPractice()}</li>
 * <li>OpenVPMS.location - {@link Context#getLocation()}</li>
 * <li>OpenVPMS.stockLocation - {@link Context#getStockLocation()}</li>
 * <li>OpenVPMS.supplier - {@link Context#getSupplier()}</li>
 * <li>OpenVPMS.product - {@link Context#getProduct()}</li>
 * <li>OpenVPMS.deposit - {@link Context#getDeposit()}</li>
 * <li>OpenVPMS.till - {@link Context#getTill()}</li>
 * <li>OpenVPMS.clinician - {@link Context#getClinician()}</li>
 * <li>OpenVPMS.user - {@link Context#getUser()}</li>
 * <li>OpenVPMS.invoice - {@code context.getObject(CustomerAccountArchetypes.INVOICE)}</li>
 * <li>OpenVPMS.visit - {@code context.getObject(PatientArchetypes.CLINICAL_EVENT)}</li>
 * <li>OpenVPMS.appointment - {@code context.getObject(ScheduleArchetypes.APPOINTMENT)}</li>
 * <li>OpenVPMS.task - {@code context.getObject(ScheduleArchetypes.TASK)}</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public class ReportContextFactory {

    /**
     * Field prefix.
     */
    private static final String PREFIX = "OpenVPMS.";

    /**
     * Creates a map of fields from a context.
     *
     * @param context the context
     * @return the fields
     */
    public static Map<String, Object> create(Context context) {
        Map<String, Object> result = new HashMap<String, Object>();
        add("customer", context.getCustomer(), result);
        add("patient", context.getPatient(), result);
        add("practice", context.getPractice(), result);
        add("location", context.getLocation(), result);
        add("stockLocation", context.getStockLocation(), result);
        add("supplier", context.getSupplier(), result);
        add("product", context.getProduct(), result);
        add("deposit", context.getDeposit(), result);
        add("till", context.getTill(), result);
        add("clinician", context.getClinician(), result);
        add("user", context.getUser(), result);
        add("invoice", context.getObject(CustomerAccountArchetypes.INVOICE), result);
        add("visit", context.getObject(PatientArchetypes.CLINICAL_EVENT), result);
        add("appointment", context.getObject(ScheduleArchetypes.APPOINTMENT), result);
        add("task", context.getObject(ScheduleArchetypes.TASK), result);
        return result;
    }

    /**
     * Helper to add a name/value pair to a map, prefixing the name with {@link #PREFIX}.
     *
     * @param name  the name
     * @param value the value
     * @param map   the map
     */
    private static void add(String name, Object value, Map<String, Object> map) {
        map.put(PREFIX + name, value);
    }

}
