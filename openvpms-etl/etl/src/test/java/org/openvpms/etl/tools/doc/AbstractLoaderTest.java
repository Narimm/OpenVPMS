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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.etl.tools.doc;

import org.apache.commons.io.FileUtils;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


/**
 * Base class for {@link Loader} test cases.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AbstractLoaderTest extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    protected IArchetypeService service;


    /**
     * Returns the location of the spring config files.
     *
     * @return an array of config locations
     */
    protected String[] getConfigLocations() {
        return new String[]{"applicationContext.xml"};
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        service = (IArchetypeService) applicationContext.getBean("archetypeService");
    }

    /**
     * Returns the file in a directory.
     *
     * @param dir the directory
     * @return the files in the directory
     */
    @SuppressWarnings("unchecked")
    protected Set<File> getFiles(File dir) {
        return new HashSet<File>(FileUtils.listFiles(dir, null, true));
    }

    /**
     * Creates a dummy <em>.gif</em> file for a document act.
     *
     * @param act    the act
     * @param dir    the parent directory
     * @param prefix the file name prefix. May be <tt>null</tt>
     * @return a new file
     * @throws java.io.IOException for any I/O error
     */
    protected File createFile(DocumentAct act, File dir, String prefix) throws IOException {
        return createFile(act, dir, prefix, null);
    }

    /**
     * Creates a dummy <em>.gif</em> file for a document act.
     *
     * @param act    the act
     * @param dir    the parent directory
     * @param prefix the file name prefix. May be <tt>null</tt>
     * @param suffix the file name suffix (pre extension). May be <tt>null</tt>
     * @return a new file
     * @throws java.io.IOException for any I/O error
     */
    protected File createFile(DocumentAct act, File dir, String prefix, String suffix) throws IOException {
        StringBuffer buff = new StringBuffer();
        if (prefix != null) {
            buff.append(prefix);
        }
        buff.append(act.getId());
        if (suffix != null) {
            buff.append(suffix);
        }
        buff.append(".gif");
        File file = new File(dir, buff.toString());
        FileUtils.touch(file);
        return file;
    }

    /**
     * Creates a new document act.
     *
     * @param shortName the document act short name
     * @return a new document act
     */
    protected DocumentAct createPatientDocAct(String shortName) {
        return createPatientDocAct(shortName, null);
    }

    /**
     * Creates a new patient document act.
     *
     * @param shortName the document act short name
     * @param fileName  the file name. May be <tt>null</tt>
     * @return a new document act
     */
    protected DocumentAct createPatientDocAct(String shortName, String fileName) {
        Party patient = (Party) service.create("party.patientpet");
        patient.setName("ZTestPet-" + System.currentTimeMillis());
        IMObjectBean bean = new IMObjectBean(patient);
        bean.setValue("species", "CANINE");
        bean.save();
        DocumentAct act = (DocumentAct) service.create(shortName);
        assertNotNull(act);
        act.setFileName(fileName);
        ActBean actBean = new ActBean(act);
        actBean.addParticipation("participation.patient", patient);
        actBean.save();
        return act;
    }

    /**
     * Creates a new customer document act.
     *
     * @param shortName the document act short name
     * @return a new document act
     */
    protected DocumentAct createCustomerDocAct(String shortName) {
        Party customer = (Party) service.create("party.customerperson");
        IMObjectBean bean = new IMObjectBean(customer);
        bean.setValue("firstName", "Foo");
        bean.setValue("lastName", "ZBar");
        bean.save();
        DocumentAct act = (DocumentAct) service.create(shortName);
        assertNotNull(act);
        ActBean actBean = new ActBean(act);
        actBean.addParticipation("participation.customer", customer);
        actBean.save();
        return act;
    }

    /**
     * Loads documents.
     *
     * @param loader   the loader
     * @param listener the load listener to notify
     */
    protected void load(Loader loader, LoaderListener listener) {
        loader.setListener(listener);
        DocumentLoader docLoader = new DocumentLoader(loader);
        docLoader.setFailOnError(false);
        docLoader.load();
    }
}
