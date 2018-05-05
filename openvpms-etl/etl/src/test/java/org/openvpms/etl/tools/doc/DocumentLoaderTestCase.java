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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.etl.tools.doc;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 * Tests the {@link DocumentLoader} class.
 *
 * @author Tim Anderson
 */
public class DocumentLoaderTestCase extends AbstractLoaderTest {

    /**
     * Verifies that an exception is thrown if no arguments are specified.
     */
    @Test
    public void testNoArgs() {
        String[] noArgs = {};
        checkConstructException(noArgs, DocumentLoaderException.ErrorCode.InvalidArguments);
    }

    /**
     * Tests the behaviour of using --byname.
     *
     * @throws Exception for any error
     */
    @Test
    public void testByName() throws Exception {
        File source = folder.newFolder("source");
        File target = folder.newFolder("target");

        File file1 = new File(source, "file1-" + System.currentTimeMillis() + ".gif");
        File file2 = new File(source, "file2-" + System.currentTimeMillis() + ".gif");
        File file3 = new File(source, "file3-" + System.currentTimeMillis() + ".gif");
        File file4 = new File(source, "file4-" + System.currentTimeMillis() + ".gif");

        DocumentAct act1 = createPatientDocAct(file1.getName());
        DocumentAct act2 = createPatientDocAct(file2.getName());
        DocumentAct act3 = createPatientDocAct(file3.getName());
        DocumentAct act4 = createPatientDocAct(file4.getName());

        FileUtils.touch(file1);
        FileUtils.touch(file2);
        FileUtils.touch(file3);
        FileUtils.touch(file4);

        String[] args = {"--byname", "-s", source.getPath(), "-d", target.getPath()};
        DocumentLoader loader = new DocumentLoader(args, getArchetypeService(), transactionManager);
        loader.load();

        // verify documents have been loaded.
        checkAct(act1);
        checkAct(act2);
        checkAct(act3);
        checkAct(act4);

        // verify files have been moved from source to target
        checkFiles(source);
        checkFiles(target, file1, file2, file3, file4);
    }

    /**
     * Tests the behaviour of using --byid with --recurse.
     *
     * @throws Exception for any error
     */
    @Test
    public void testByIdRecurse() throws Exception {
        File root = folder.newFolder("root");
        File sub1 = folder.newFolder("root", "sub1");
        File sub2 = folder.newFolder("root", "sub2");
        File target = folder.newFolder("target");

        DocumentAct act1 = createPatientDocAct();
        DocumentAct act2 = createPatientDocAct();
        DocumentAct act3 = createPatientDocAct();
        DocumentAct act4 = createPatientDocAct();

        File file1 = createFile(act1, sub1);
        File file2 = createFile(act2, sub1);
        File file3 = createFile(act3, sub2);
        File file4 = createFile(act4, sub2);

        String[] args = {"--byid", "-s", root.getPath(), "-d", target.getPath(), "--recurse"};
        DocumentLoader loader = new DocumentLoader(args, getArchetypeService(), transactionManager);
        loader.load();

        checkFiles(target, file1, file2, file3, file4);
        checkFiles(sub1);
        checkFiles(sub2);
    }

    /**
     * Tests the behaviour of using --byid with various combinations of invalid directory arguments.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testByIdInvalidDirs() throws IOException {
        String[] args1 = {"--byid", "-s", "target/invalidsource"};
        checkConstructException(args1, DocumentLoaderException.ErrorCode.InvalidArguments);  // invalid directory

        String[] args2 = {"--byid", "-d", "target/invalidtarget"};
        checkConstructException(args2, DocumentLoaderException.ErrorCode.InvalidArguments);  // invalid directory

        File parent = folder.newFolder("parent");
        File target = folder.newFolder("parent", "target");

        String[] args3 = {"--byid", "-s", parent.getPath(), "-d", target.getPath()};
        checkConstructException(args3, DocumentLoaderException.ErrorCode.TargetChildOfSource);

        String[] args4 = {"--byid", "-s", parent.getPath(), "-d", parent.getPath()};
        checkConstructException(args4, DocumentLoaderException.ErrorCode.SourceTargetSame);

        String[] args5 = {"--byid", "-s", parent.getPath(), "--err", target.getPath()};
        checkConstructException(args5, DocumentLoaderException.ErrorCode.ErrorChildOfSource);

        String[] args6 = {"--byid", "-s", parent.getPath(), "--err", parent.getPath()};
        checkConstructException(args6, DocumentLoaderException.ErrorCode.SourceErrorSame);
    }

    /**
     * Tests the behaviour of using --byid with a custom regular expression.
     *
     * @throws Exception for any error
     */
    @Test
    public void testByIdCustomRegexp() throws Exception {
        File source = folder.newFolder("source");
        File target = folder.newFolder("target");

        DocumentAct act1 = createPatientDocAct();
        DocumentAct act2 = createPatientDocAct();
        DocumentAct act3 = createPatientDocAct();
        DocumentAct act4 = createPatientDocAct();

        // create files with varying file names for each act
        File act1File = createFile(act1, source);
        File act2File = createFile(act2, source, "V");
        File act3File = createFile(act3, source, null, "-12345");
        File act4File = createFile(act4, source, "P", "-123456");

        // load all files which have an <act id>.gif extension
        String[] args = {"--byid", "-s", source.getPath(), "-d", target.getPath(), "--regexp", "(\\d+).gif"};
        DocumentLoader loader = new DocumentLoader(args, getArchetypeService(), transactionManager);
        loader.load();

        // verify only act1 was processed
        checkFiles(target, act1File);                     // act1File moved as it matched
        checkFiles(source, act2File, act3File, act4File); // pattern won't have matched these files
    }

    /**
     * Tests the behaviour of using --byid with --type.
     *
     * @throws Exception for any error
     */
    @Test
    public void testByIdAndType() throws Exception {
        File source = folder.newFolder("source");
        File target = folder.newFolder("target");

        DocumentAct act1 = createPatientDocAct();
        DocumentAct act2 = createPatientDocAct();
        DocumentAct act3 = createPatientDocAct();
        DocumentAct act4 = createPatientDocAct();

        File file1 = createFile(act1, source);
        File file2 = createFile(act2, source);
        File file3 = createFile(act3, source);
        File file4 = createFile(act4, source);

        String[] args = {"--byid", "--type", "act.customerDocumentAttachment", "-s", source.getPath(), "-d",
                         target.getPath()};
        DocumentLoader loader = new DocumentLoader(args, getArchetypeService(), transactionManager);
        loader.load();

        checkFiles(source, file1, file2, file3, file4);
        checkFiles(target);

        String[] args2 = {"--byid", "--type", "act.patientDocumentAttachment", "-s", source.getPath(), "-d",
                          target.getPath()};
        DocumentLoader loader2 = new DocumentLoader(args2, getArchetypeService(), transactionManager);
        loader2.load();

        checkFiles(source);
        checkFiles(target, file1, file2, file3, file4);
    }

    /**
     * Verifies that <em>act.documentTemplate</em> acts aren't loaded to by default, when "--byid" is used.
     *
     * @throws Exception for any error
     */
    @Test
    public void testDocumentTemplateActNotLoadedByDefault() throws Exception {
        File source = folder.newFolder("source");
        File target = folder.newFolder("target");
        File error = folder.newFolder("error");

        // create an act.documentTemplate and associated template
        DocumentAct act = (DocumentAct) create("act.documentTemplate");
        Entity template = (Entity) create("entity.documentTemplate");
        ActBean actBean = new ActBean(act);
        actBean.setValue("description", "A description");
        actBean.addNodeParticipation("template", template);
        template.setName("X Test template");
        save(act, template);

        // create a file with an id the same as the act
        File file = createFile(act, source);

        // verify the file isn't loaded with the default --type value of "act.*Document*".
        String[] args = {"--byid", "-s", source.getPath(), "-d", target.getPath(),
                         "--err", error.getPath()};
        DocumentLoader loader = new DocumentLoader(args, getArchetypeService(), transactionManager);
        loader.load();

        checkFiles(source);
        checkFiles(target);
        checkFiles(error, file);

        // verify that specifying act.documentTemplate throws an IllegalArgumentException as it doesn't have a
        // document node
        try {
            String[] args2 = {"--byid", "--type", "act.documentTemplate", "-s", source.getPath(), "-d",
                              target.getPath()};
            new DocumentLoader(args2, getArchetypeService(), transactionManager);
            fail("Expected DocumentLoader constructor to fail");
        } catch (IllegalArgumentException expected) {
            // the expected behaviour
        }
    }

    /**
     * Tests the behaviour of the --rename option.
     *
     * @throws Exception for any error
     */
    @Test
    public void testRenameDuplicates() throws Exception {
        File source = folder.newFolder("source");
        File target = folder.newFolder("target");

        DocumentAct act = createPatientDocAct();

        createFile(act, source);
        File file1 = createFile(act, target);
        File file2 = new File(target, act.getId() + "(1).gif");

        String[] args = {"--byid", "-s", source.getPath(), "-d", target.getPath(), "--rename", "--overwrite"};
        DocumentLoader loader = new DocumentLoader(args, getArchetypeService(), transactionManager);
        loader.load();

        checkFiles(source);
        checkFiles(target, file1, file2);

        createFile(act, source);
        File file3 = new File(target, act.getId() + "(2).gif");

        loader = new DocumentLoader(args, getArchetypeService(), transactionManager);
        loader.load();
        checkFiles(source);
        checkFiles(target, file1, file2, file3);
    }

    /**
     * Verifies that constructing a {@link DocumentLoader} with the supplied arguments throws an exception.
     *
     * @param args     the arguments
     * @param expected the expected error code
     */
    private void checkConstructException(String[] args, DocumentLoaderException.ErrorCode expected) {
        try {
            new DocumentLoader(args, getArchetypeService(), transactionManager);
            fail("Expected a DocumentLoaderException");
        } catch (DocumentLoaderException exception) {
            assertEquals(expected, exception.getErrorCode());
        }
    }
}
