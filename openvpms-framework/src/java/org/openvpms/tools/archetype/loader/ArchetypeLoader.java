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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */


package org.openvpms.tools.archetype.loader;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.stringparsers.BooleanStringParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptors;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptors;
import org.openvpms.component.business.domain.im.archetype.descriptor.Descriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorException;
import org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorValidationError;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import static org.openvpms.tools.archetype.loader.ArchetypeLoaderException.ErrorCode.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * This utility will read all the archetypes from the specified directory
 * or file and load them in to the archetype service.
 * <p/>
 * When loading from a directory, all files with a <em>.adl</em> extension
 * will be processed.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeLoader {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Specifies the file extension to filter. Defaults to adl.
     */
    private String extension = "adl";

    /**
     * Indicates whether an archetype should override an existing
     * archetype (i.e. one that is already stored in the databse
     */
    private boolean overwrite;

    /**
     * Determines if the loader should fail when a validation error occurs.
     */
    private boolean failOnError = true;

    /**
     * Determines if verbose logging will be performed.
     */
    private boolean verbose;

    /**
     * The load state.
     */
    private Changes changes = new Changes();

    /**
     * The default name of the application context file.
     */
    private final static String APPLICATION_CONTEXT
            = "archetype-loader-context.xml";

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ArchetypeLoader.class);


    /**
     * Creates a new <tt>ArchetypeLoader</tt>.
     *
     * @param service the archetype service
     */
    public ArchetypeLoader(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Determines if existing descriptors should be overwritten.
     *
     * @param overwrite if <tt>true</tt>, overwrite existing descriptors
     */
    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    /**
     * Determines if loading should fail when a validation error occurs.
     *
     * @param failOnError if <tt>true</tt> fail when a validation error occurs
     */
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    /**
     * Determines if logging will be verbose.
     *
     * @param verbose if <tt>true</tt> perform verbose logging.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Returns the changes that have been made.
     *
     * @return the changes
     */
    public Changes getChanges() {
        return changes;
    }

    /**
     * Deletes all the archetype and assertion type descriptors.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void clean() {
        removeAssertionTypeDescriptors();
        removeArchetypeDescriptors();
    }

    /**
     * Loads assertions from the specified file.
     *
     * @param fileName the file name
     * @throws ArchetypeLoaderException if the assertions cannot be loaded
     */
    public void loadAssertions(String fileName) {
        if (verbose) {
            log.info("Processing assertion type descriptors from: " + fileName);
        }
        try {
            loadAssertions(new FileInputStream(fileName));
        } catch (FileNotFoundException exception) {
            throw new ArchetypeLoaderException(FileNotFound, exception,
                                               fileName);
        }
    }

    /**
     * Loads assertions from the specified stream.
     *
     * @param stream the stream to read
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DescriptorException       if the assertions cannot be read
     */
    public void loadAssertions(InputStream stream) {
        AssertionTypeDescriptors descriptors
                = AssertionTypeDescriptors.read(stream);
        for (AssertionTypeDescriptor descriptor :
                descriptors.getAssertionTypeDescriptors().values()) {
            loadAssertion(descriptor);
        }
    }

    /**
     * Loads all ADL files from the specified directory, optionally recursing
     * sub-directories.
     *
     * @param dirName the directory
     * @param recurse if <tt>true</tt>, scan sub-directories
     * @throws ArchetypeLoaderException  if the directory cannot be found
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DescriptorException       if a descriptor cannot be read
     */
    public void loadArchetypes(String dirName, boolean recurse) {
        IOFileFilter filter = FileFilterUtils.suffixFileFilter(extension);
        loadArchetypes(dirName, filter, recurse);
    }

    /**
     * Loads archetypes from a file.
     *
     * @param fileName the file name
     */
    public void loadArchetypes(String fileName) {
        loadArchetypes(new File(fileName));
    }

    /**
     * Loads an assertion type descriptor.
     *
     * @param descriptor the descriptor to load
     * @throws ArchetypeServiceException for any archetype service exception
     */
    public void loadAssertion(AssertionTypeDescriptor descriptor) {
        if (verbose) {
            log.info("Processing assertion type descriptor: "
                     + descriptor.getName());
        }

        AssertionTypeDescriptor existing
                = service.getAssertionTypeDescriptor(descriptor.getName());
        save(descriptor, existing);
    }

    /**
     * Loads an archetype descriptor.
     *
     * @param descriptor the archetype descriptor to load
     * @throws ArchetypeServiceException for any archetype service exception
     * @throws ArchetypeLoaderException  if the descriptor is invalid, and
     *                                   failOnError is true
     */
    public void loadArchetype(ArchetypeDescriptor descriptor) {
        loadArchetype(descriptor, null);
    }

    /**
     * Main line.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        BasicConfigurator.configure();

        try {
            JSAP parser = createParser();
            JSAPResult config = parser.parse(args);
            if (!config.success()) {
                displayUsage(parser, config);
            } else {
                String contextPath = config.getString("context");

                ApplicationContext context;
                if (!new File(contextPath).exists()) {
                    context = new ClassPathXmlApplicationContext(contextPath);
                } else {
                    context = new FileSystemXmlApplicationContext(contextPath);
                }

                IArchetypeService service = (IArchetypeService) context.getBean(
                        "archetypeService");
                ArchetypeLoader loader = new ArchetypeLoader(service);
                String file = config.getString("file");
                String dir = config.getString("dir");
                boolean recurse = config.getBoolean("subdir");
                loader.setOverwrite(config.getBoolean("overwrite"));
                loader.setFailOnError(config.getBoolean("failOnError"));
                loader.setVerbose(config.getBoolean("verbose"));
                boolean clean = config.getBoolean("clean");
                String mappingFile = config.getString("mappingFile");
                int processed = 0;

                PlatformTransactionManager mgr;
                mgr = (PlatformTransactionManager) context.getBean(
                        "txnManager");
                TransactionStatus status = mgr.getTransaction(
                        new DefaultTransactionDefinition());
                try {
                    if (clean) {
                        loader.clean();
                        ++processed;
                    }
                    if (mappingFile != null) {
                        loader.loadAssertions(mappingFile);
                        ++processed;
                    }
                    if (file != null) {
                        loader.loadArchetypes(file);
                        ++processed;
                    } else if (dir != null) {
                        loader.loadArchetypes(dir, recurse);
                        ++processed;
                    }
                    mgr.commit(status);
                    if (processed == 0) {
                        displayUsage(parser, config);
                    }
                } catch (Throwable throwable) {
                    log.error(throwable, throwable);
                    log.error("Rolling back changes");
                    mgr.rollback(status);
                }
            }
        } catch (Throwable throwable) {
            log.error(throwable, throwable);
        }
    }

    /**
     * Loads archetype descriptors from a file.
     *
     * @param file the file
     * @throws ArchetypeLoaderException  if the file doesn't exist or a
     *                                   descriptor is invalid and failOnError
     *                                   is true
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DescriptorException       if the file cannot be read
     */
    private void loadArchetypes(File file) {
        if (verbose) {
            log.info("Processing file: " + file.getName());
        }

        ArchetypeDescriptors descriptors;
        try {
            descriptors = ArchetypeDescriptors.read(new FileInputStream(file));
        } catch (FileNotFoundException exception) {
            throw new ArchetypeLoaderException(FileNotFound, exception,
                                               file.getName());
        }
        for (ArchetypeDescriptor descriptor
                : descriptors.getArchetypeDescriptorsAsArray()) {
            loadArchetype(descriptor, file.getName());
        }
    }

    /**
     * Loads archetypes matching a file name filter, from the specified
     * directory.
     *
     * @param dirName    the directory
     * @param fileFilter the file name filter to match archetype file names
     * @param recurse    if <tt>true</tt>, scan sub-directories
     * @throws ArchetypeLoaderException  if the directory cannot be found
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DescriptorException       if a descriptor cannot be read
     */
    private void loadArchetypes(String dirName, IOFileFilter fileFilter,
                                boolean recurse) {
        File dir = new File(dirName);
        if (!dir.exists()) {
            throw new ArchetypeLoaderException(DirNotFound, dirName);
        }
        IOFileFilter dirFilter = (recurse) ? TrueFileFilter.INSTANCE : null;
        Collection files = FileUtils.listFiles(dir, fileFilter, dirFilter);
        for (Object file : files) {
            loadArchetypes((File) file);
        }
    }

    /**
     * Loads an archetype descriptor.
     *
     * @param descriptor the archetype descriptor
     * @param fileName   the file name, for error reporting purposes.
     *                   May be <tt>null</tt>
     * @throws ArchetypeLoaderException  if the descriptor is invalid, and
     *                                   failOnError is true
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DescriptorException       if the file cannot be read
     */
    private void loadArchetype(ArchetypeDescriptor descriptor,
                               String fileName) {
        if (verbose) {
            log.info("Processing archetype descriptor: "
                     + descriptor.getName());
        }

        // attempt to validate the archetype descriptor.
        List<DescriptorValidationError> validation = descriptor.validate();
        if (!validation.isEmpty()) {
            StringBuffer buf = new StringBuffer("[Validation Error] ");
            if (fileName != null) {
                buf.append("[").append(fileName).append("]");
            }
            buf.append(" archetype ")
                    .append(descriptor.getName())
                    .append(" had ")
                    .append(validation.size())
                    .append(" errors.\n");
            for (DescriptorValidationError error : validation) {
                buf.append("\ttype:")
                        .append(error.getDescriptorType())
                        .append(" instance:")
                        .append(error.getInstanceName())
                        .append(" attribute:")
                        .append(error.getAttributeName())
                        .append(" error:")
                        .append(error.getError());
            }

            if (failOnError) {
                throw new ArchetypeLoaderException(ValidationError,
                                                   buf.toString());
            }

            log.error(buf);
        } else {
            ArchetypeDescriptor existing
                    = service.getArchetypeDescriptor(descriptor.getShortName());
            save(descriptor, existing);
        }
    }

    /**
     * Saves a descriptor. If the descriptor already exists, it will be
     * replaced if the <tt>overwrite</tt> flag is set.
     *
     * @param descriptor the new descriptor
     * @param existing   the current descriptor. May be <tt>null</tt>
     * @return <tt>true</tt> if the descriptor was saved
     * @throws ArchetypeServiceException for any archetype service exception
     */
    private boolean save(Descriptor descriptor, Descriptor existing) {
        boolean save = true;
        if (existing != null) {
            // make sure using the latest version of the descriptor, rather
            // than a cached one
            existing = (Descriptor) service.get(existing.getObjectReference());
        }
        if (existing != null) {
            if (!overwrite) {
                save = false;
                if (verbose) {
                    log.info(descriptor.getName()
                             + " already exists. Not overwriting");
                }
            } else {
                service.remove(existing);
            }
        }
        if (save) {
            if (verbose) {
                log.info("Saving " + descriptor.getName());
            }
            // NOTE: ideally wouldn't use the deprecated save() method to
            // disable validation, however there is a chicken and egg problem
            // in that some descriptors must exist in order to validate those
            // same descriptors. A better alternative may be able to selectively
            // disable some forms of validation, using a
            // custom archetype service implementation. TODO
            service.save(descriptor, false);
            return true;
        }
        return false;
    }

    /**
     * Creates a new command line parser.
     *
     * @return a new parser
     * @throws JSAPException if the parser can't be created
     */
    private static JSAP createParser() throws JSAPException {
        JSAP parser = new JSAP();
        parser.registerParameter(new FlaggedOption("dir")
                .setShortFlag('d')
                .setLongFlag("dir")
                .setHelp("Directory where ADL files reside."));
        parser.registerParameter(new Switch("subdir")
                .setShortFlag('s')
                .setLongFlag("subdir")
                .setDefault("false")
                .setHelp("Search the subdirectories as well."));
        parser.registerParameter(new FlaggedOption("file")
                .setShortFlag('f')
                .setLongFlag("file")
                .setHelp("Name of file containing archetypes"));
        parser.registerParameter(new Switch("verbose")
                .setShortFlag('v')
                .setLongFlag("verbose")
                .setDefault("false")
                .setHelp("Displays verbose info to the console."));
        parser.registerParameter(new Switch("overwrite")
                .setShortFlag('o')
                .setLongFlag("overwrite")
                .setDefault("false")
                .setHelp("Overwrite archetype if it already exists"));
        parser.registerParameter(new Switch("clean")
                .setShortFlag('c')
                .setLongFlag("clean")
                .setDefault("false")
                .setHelp("Clean all the archetypes before loading"));
        parser.registerParameter(new FlaggedOption("failOnError")
                .setShortFlag('e')
                .setLongFlag("failOnError")
                .setDefault("true")
                .setStringParser(BooleanStringParser.getParser())
                .setHelp("Fail on validation error"));
        parser.registerParameter(new FlaggedOption("context")
                .setLongFlag("context")
                .setDefault(APPLICATION_CONTEXT)
                .setHelp("The application context path"));
        parser.registerParameter(new FlaggedOption("mappingFile")
                .setShortFlag('m')
                .setLongFlag("mappingFile")
                .setHelp("A location of the assertion type mapping file"));
        return parser;
    }

    /**
     * Prints usage information and exits.
     *
     * @param parser the parser
     * @param result the parse result
     */
    private static void displayUsage(JSAP parser, JSAPResult result) {
        Iterator iter = result.getErrorMessageIterator();
        while (iter.hasNext()) {
            System.err.println(iter.next());
        }
        System.err.println();
        System.err.println("Usage: java " + ArchetypeLoader.class.getName());
        System.err.println("                " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
        System.exit(1);
    }

    /**
     * Removes all archetype descriptors.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void removeArchetypeDescriptors() {
        List<ArchetypeDescriptor> descriptors
                = service.getArchetypeDescriptors();
        for (ArchetypeDescriptor descriptor : descriptors) {
            if (verbose) {
                log.info("Deleting " + descriptor.getName());
            }
            service.remove(descriptor);
            changes.addOldVersion(descriptor);
        }
    }

    /**
     * Removes all assertion type descriptors.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void removeAssertionTypeDescriptors() {
        List<AssertionTypeDescriptor> descriptors
                = service.getAssertionTypeDescriptors();
        for (AssertionTypeDescriptor descriptor : descriptors) {
            if (verbose) {
                log.info("Deleting " + descriptor.getName());
            }
            service.remove(descriptor);
        }
    }
}
