package org.openvpms.web.workspace.reporting.report;

import org.junit.Test;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.report.ParameterType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests the {@link ParameterEvaluator} class.
 *
 * @author Tim Anderson
 */
public class ParameterEvaluatorTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link ParameterEvaluator#evaluate(Set, Map)} method.
     */
    @Test
    public void testEvaluator() {
        Party customer = TestHelper.createCustomer("Foo", "Bar", false);
        Act appointment = (Act) create(ScheduleArchetypes.APPOINTMENT);
        appointment.setActivityStartTime(TestHelper.getDatetime("2015-04-04 10:30:00"));
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("OpenVPMS.customer", customer);
        variables.put("OpenVPMS.appointment", appointment);
        variables.put("OpenVPMS.task", null);

        ParameterEvaluator evaluator = new ParameterEvaluator(getArchetypeService(), getLookupService());
        Set<ParameterType> set = new HashSet<ParameterType>();
        set.add(new ParameterType("firstName", String.class, null, "$OpenVPMS.customer.firstName"));
        set.add(new ParameterType("id", String.class, null, "$OpenVPMS.customer.id"));
        set.add(new ParameterType("startTime", String.class, null, "$OpenVPMS.appointment.startTime"));
        set.add(new ParameterType("lastName", String.class, null, true, "$OpenVPMS.customer.lastName"));
        set.add(new ParameterType("patient", String.class, null, "$OpenVPMS.patient.name"));
        set.add(new ParameterType("badtype", Long.class, null, "$OpenVPMS.customer.id"));
        set.add(new ParameterType("string", String.class, null, "ABC"));
        set.add(new ParameterType("long", Long.class, null, 1L));
        set.add(new ParameterType("task", String.class, null, "$OpenVPMS.task.startTime"));

        Set<ParameterType> evaluated = evaluator.evaluate(set, variables);

        check(evaluated, "firstName", "Foo");
        check(evaluated, "id", "-1");
        check(evaluated, "startTime", "2015-04-04 10:30:00.0");
        check(evaluated, "lastName", "$OpenVPMS.customer.lastName");  // not evaluated - system parameter
        check(evaluated, "patient", null);                            // not present
        check(evaluated, "badtype", "$OpenVPMS.customer.id");         // not evaluated - must be a string
        check(evaluated, "string", "ABC");                            // not evaluated
        check(evaluated, "long", 1L);                                 // not evaluated
        check(evaluated, "task", null);                               // not present
    }

    /**
     * Checks a parameter's default value matches that expected.
     *
     * @param types the parameters
     * @param name  the parameter name
     * @param value the expected value
     */
    private void check(Set<ParameterType> types, String name, Object value) {
        ParameterType type = getParameter(types, name);
        assertEquals(value, type.getDefaultValue());
    }

    /**
     * Returns a parameter given its name.
     *
     * @param types the parameters
     * @param name  the parameter name
     * @return the parameter
     */
    private ParameterType getParameter(Set<ParameterType> types, String name) {
        for (ParameterType type : types) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        fail("Parameter " + name + " not found");
        return null;
    }

}
