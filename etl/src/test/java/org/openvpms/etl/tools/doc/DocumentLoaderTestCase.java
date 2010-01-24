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

import java.io.File;


/**
 * Tests the {@link DocumentLoader} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentLoaderTestCase extends AbstractLoaderTest {

    /**
     * The parent directory for test files.
     */
    private File parent;


    /**
     * Verifies that an exception is thrown if no arguments are specified.
     */
    public void testNoArgs() {
        String[] noArgs = {};
        checkConstructException(noArgs, DocumentLoaderException.ErrorCode.InvalidArguments);
    }

    /**
     * Tests the behaviour of using --byname.
     *
     * @throws Exception for any error
     */
    public void testByName() throws Exception {
        File source = new File(parent, "sdocs1" + System.currentTimeMillis());
        File target = new File(parent, "tdocs1" + System.currentTimeMillis());
        assertTrue(source.mkdirs());
        assertTrue(target.mkdirs());

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
        DocumentLoader loader = new DocumentLoader(args, service, transactionManager);
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
    public void testByIdRecurse() throws Exception {
        File root = new File(parent, "root" + System.currentTimeMillis());
        File sub1 = new File(root, "sub1");
        File sub2 = new File(root, "sub2");
        File target = new File(parent, "tdocs2" + System.currentTimeMillis());
        assertTrue(root.mkdirs());
        assertTrue(target.mkdirs());
        assertTrue(sub1.mkdirs());
        assertTrue(sub2.mkdirs());

        DocumentAct act1 = createPatientDocAct();
        DocumentAct act2 = createPatientDocAct();
        DocumentAct act3 = createPatientDocAct();
        DocumentAct act4 = createPatientDocAct();

        File file1 = createFile(act1, sub1);
        File file2 = createFile(act2, sub1);
        File file3 = createFile(act3, sub2);
        File file4 = createFile(act4, sub2);

        String[] args = {"--byid", "-s", root.getPath(), "-d", target.getPath(), "--recurse"};
        DocumentLoader loader = new DocumentLoader(args, service, transactionManager);
        loader.load();

        checkFiles(target, file1, file2, file3, file4);
        checkFiles(sub1);
        checkFiles(sub2);
    }

    /**
     * Tests the behaviour of using --byid with various combinations of invalid directory arguments.
     */
    public void testByIdInvalidDirs() {
        String[] args1 = {"--byid", "-s", "target/invalidsource"};
        checkConstructException(args1, DocumentLoaderException.ErrorCode.InvalidArguments);  // invalid directory

        String[] args2 = {"--byid", "-d", "target/invalidtarget"};
        checkConstructException(args2, DocumentLoaderException.ErrorCode.InvalidArguments);  // invalid directory

        File target = new File(parent, "sdocs" + System.currentTimeMillis());
        assertTrue(target.mkdirs());

        String[] args3 = {"--byid", "-s", parent.getPath(), "-d", target.getPath()};
        checkConstructException(args3, DocumentLoaderException.ErrorCode.TargetChildOfSource);

        String[] args4 = {"--byid", "-s", parent.getPath(), "-d", parent.getPath()};
        checkConstructException(args4, DocumentLoaderException.ErrorCode.SourceTargetSame);
    }

    /**
     * Tests the behaviour of using --byid with a custom regular expression.
     *
     * @throws Exception for any error
     */
    public void testByIdCustomRegexp() throws Exception {
        File source = new File(parent, "sdocs2" + System.currentTimeMillis());
        File target = new File(parent, "tdocs2" + System.currentTimeMillis());
        assertTrue(source.mkdirs());
        assertTrue(target.mkdirs());

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
        DocumentLoader loader = new DocumentLoader(args, service, transactionManager);
        loader.load();

        // verify only act1 was processed
        checkFiles(target, act1File);                     // act1File moved as it matched
        checkFiles(source, act2File, act3File, act4File); // pattern won't have matched these files
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        parent = new File("target");
        if (!parent.exists()) {
            assertTrue(parent.mkdir());
        }
    }

    /**
     * Verifies that constructing a <tt>DocumentLoader</tt> with the supplied arguments throws an exception.
     *
     * @param args     the arguments
     * @param expected the expected error code
     */
    private void checkConstructException(String[] args, DocumentLoaderException.ErrorCode expected) {
        try {
            new DocumentLoader(args, service, transactionManager);
            fail("Expected a DocumentLoaderException");
        } catch (DocumentLoaderException exception) {
            assertEquals(expected, exception.getErrorCode());
        }
    }
}
