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
     * Tests the behaviour of using --byname
     */
    public void testByName() {
        File source = new File(parent, "sdocs" + System.currentTimeMillis());
        File target = new File(parent, "tdocs" + System.currentTimeMillis());
        assertTrue(source.mkdirs());
        assertTrue(target.mkdirs());

        String[] args = {"--byname", "-s", source.getPath(), "-d", target.getPath()};
        DocumentLoader loader = new DocumentLoader(args, service);
        loader.load();
    }

    /**
     * Verifies that an exception is thrown if no arguments are specified.
     */
    public void testNoArgs() {
        String[] noArgs = {};
        checkConstructException(noArgs, DocumentLoaderException.ErrorCode.InvalidArguments);
    }

    /**
     * Tests the behaviour of using --byid with various combinations of invalid directory araguemnts.
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
        File source = new File(parent, "sdocs" + System.currentTimeMillis());
        File target = new File(parent, "tdocs" + System.currentTimeMillis());
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

        // load all files which have a an <act id>.gif extension
        String[] args = {"--byid", "-s", source.getPath(), "-d", target.getPath(), "--regexp", "(\\d+).gif"};
        DocumentLoader loader = new DocumentLoader(args, service);
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
            new DocumentLoader(args, service);
            fail("Expected a DocumentLoaderException");
        } catch (DocumentLoaderException exception) {
            assertEquals(expected, exception.getErrorCode());
        }
    }
}
