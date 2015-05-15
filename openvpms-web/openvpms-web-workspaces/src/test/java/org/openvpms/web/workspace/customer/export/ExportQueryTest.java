package org.openvpms.web.workspace.customer.export;

import org.junit.Test;
import org.openvpms.archetype.rules.export.ExportArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.test.AbstractAppTest;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author benjamincharlton on 13/05/2015.
 */
public class ExportQueryTest extends AbstractAppTest{
    @Test
    public void testQuery(){
        Party customer = TestHelper.createCustomer(true);
        Party patient = TestHelper.createPatient(true);
        EntityBean bean = new EntityBean(customer);
        bean.addRelationship("entityRelationship.patientOwner", patient);
        save(customer, patient);
        ExportQuery query = new ExportQuery(customer);
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        ResultSet<Act> results = query.query();
        int size = results.getResults();
        assertEquals(0, size);
        Act export = TestHelper.createExport(new Date(),customer,new Date(),"AU");
        Party importer = TestHelper.createImporter(true);
        ActBean exportbean = new ActBean(export);
        exportbean.addParticipation(ExportArchetypes.PATIENT_PARTICIPATION, patient);
        exportbean.addParticipation(ExportArchetypes.IMPORTER_PARTICIPATION, importer);
        exportbean.save();
        ExportQuery query2 = new ExportQuery(customer);
        query2.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        ResultSet<Act> results2 = query2.query();
        IPage<Act> page2 = results2.getPage(0);
        List<Act> list2 = page2.getResults();
        assertEquals(1, list2.size());
    }
}
