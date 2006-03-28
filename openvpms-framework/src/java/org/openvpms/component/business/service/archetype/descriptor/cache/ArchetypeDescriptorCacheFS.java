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


package org.openvpms.component.business.service.archetype.descriptor.cache;

// java core
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

// commons-io
import org.apache.commons.io.FileUtils;

// commons-lang
import org.apache.commons.lang.StringUtils;

//log4j
import org.apache.log4j.Logger;

// castor
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptors;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptors;
import org.xml.sax.InputSource;

/**
 * This implementation reads the archetype descriptors from the file system,
 * parses them and caches them in memory.
 * <p>
 * The cache can be 
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ArchetypeDescriptorCacheFS extends BaseArchetypeDescriptorCache
    implements IArchetypeDescriptorCache {
    /** 
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    static final private Logger logger = Logger
            .getLogger(ArchetypeDescriptorCacheFS.class);

    /**
     * The timer is used to schedule tasks
     */
    private Timer timer = new Timer();
    

    /**
     * Construct an instance of this cache by loading and parsing all the
     * archetype definitions in the specified file.
     * <p>
     * The resource specified by archefile must be loadable from the classpath. 
     * A similar constraint applies to the assertFile.
     * 
     * @param archeFile
     *            the file that holds all the archetype records.
     * @param assertFile
     *            the file that holds the assertions
     * @throws ArchetypeDescriptorCacheException
     *             thrown if it cannot bootstrap the cache
     */
    public ArchetypeDescriptorCacheFS(String archeFile, String assertFile) {
        loadAssertionTypeDescriptorsFromFile(assertFile);
        loadArchetypeDescriptorsInFile(archeFile);
    }

    /**
     * Construct the archetype cache by loading and parsing all archetype
     * definitions in the specified directory. Only process files with the
     * specified extensions
     * 
     * @param archeDir
     *            the directory
     * @parsm extensions only process files with these extensions
     * @param assertFile
     *            the file that holds the assertions
     * @throws ArchetypeDescriptorCacheException
     *             thrown if it cannot bootstrap the cache
     */
    public ArchetypeDescriptorCacheFS(String archDir, String[] extensions,
            String assertFile) {
        loadAssertionTypeDescriptorsFromFile(assertFile);
        loadArchetypeDescriptorsInDir(archDir, extensions);
    }

    /**
     * Construct the archetype cache by loading and parsing all archetype
     * definitions in the specified directory. Only process files with the
     * specified extensions.
     * <p>
     * If a scanInterval greater than 0 is specified then a thread will be
     * created to monitor changes in the archetype definition.
     * 
     * @param archeDir
     *            the directory
     * @param extensions 
     *            only process files with these extensions
     * @param assertFile
     *            the file that holds the assertions
     * @param scanInterval
     *            the interval that the archetype directory is scanned.
     * @throws ArchetypeDescriptorCacheException
     *             thrown if it cannot bootstrap the cache
     */
    public ArchetypeDescriptorCacheFS(String archDir, String[] extensions,
            String assertFile, long scanInterval) {
        this(archDir, extensions, assertFile);

        // determine whether we should create a monitor thread
        createArchetypeMonitorThread(archDir, extensions, scanInterval);
    }

    /**
     * Process the archetypes defined in the specified file. Te file must be
     * located in the classpath.
     * 
     * @param afile
     *            the path to the file containing the archetype records
     * @throws ArchetypeDescriptorCacheException
     */
    private void loadArchetypeDescriptorsInFile(String afile) {

        // check that a non-null file was specified
        if (StringUtils.isEmpty(afile)) {
            throw new ArchetypeDescriptorCacheException(
                    ArchetypeDescriptorCacheException.ErrorCode.NoFileSpecified);
        }

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Attempting to process records in " + afile);
            }
            processArchetypeDescriptors(loadArchetypeDescriptors(afile));
        } catch (Exception exception) {
            throw new ArchetypeDescriptorCacheException(
                    ArchetypeDescriptorCacheException.ErrorCode.InvalidFile,
                    new Object[] { afile }, exception);
        }
    }

    /**
     * Load and parse all the archetypes that are defined in the files of the
     * speciied directory. Only process files with the nominated extensions. If
     * the directory is invalid or if any of the files in the directory are
     * invalid throw an exception.
     * <p>
     * The archetypes will be cached in memory.
     * 
     * @param adir
     *            the directory to search
     * @param extensions
     *            the file extensions to check
     * @throws ArchetypeDescriptorCacheException
     */
    private void loadArchetypeDescriptorsInDir(String adir, String[] extensions) {

        // check that a non-null directory was specified
        if (StringUtils.isEmpty(adir)) {
            throw new ArchetypeDescriptorCacheException(
                    ArchetypeDescriptorCacheException.ErrorCode.NoDirSpecified);
        }

        // check that a valid directory was specified
        File dir = FileUtils.toFile(Thread.currentThread()
                .getContextClassLoader().getResource(adir));
        if (dir == null) {
            throw new ArchetypeDescriptorCacheException(
                    ArchetypeDescriptorCacheException.ErrorCode.InvalidDir,
                    new Object[] {adir});
        }
        
        if (!dir.isDirectory()) {
            throw new ArchetypeDescriptorCacheException(
                    ArchetypeDescriptorCacheException.ErrorCode.InvalidDir,
                    new Object[] { adir });
        }

        // process all the files in the directory, that match the filter
        Collection collection = FileUtils.listFiles(dir, extensions, true);
        Iterator files = collection.iterator();
        while (files.hasNext()) {
            File file = (File) files.next();
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Attempting to process records in "
                            + file.getName());
                }

                processArchetypeDescriptors(loadArchetypeDescriptors(new FileReader(
                        file)));
            } catch (Exception exception) {
                // do not throw an exception but log a warning
                logger.warn("Failed to load archetype",
                        new ArchetypeDescriptorCacheException(
                                ArchetypeDescriptorCacheException.ErrorCode.InvalidFile,
                                new Object[] { file.getName() }, exception));
            }

        }

    }

    /**
     * Iterate over all the descriptors and add them to the existing set of
     * descriptores
     * 
     * @param descriptors
     *            the descriptors to process
     * @throws ArchetypeDescriptorCacheException
     */
    private void processArchetypeDescriptors(ArchetypeDescriptors descriptors) {
        for (ArchetypeDescriptor descriptor : descriptors
                .getArchetypeDescriptorsAsArray()) {
            ArchetypeId archId = descriptor.getType();

            if (logger.isDebugEnabled()) {
                logger.debug("Processing archetype record "
                        + archId.getShortName());
            }

            try {
                // make sure that the underlying type is loadable
                Thread.currentThread().getContextClassLoader().loadClass(
                        descriptor.getClassName());
                addArchetypeDescriptor(descriptor, true);
            } catch (ClassNotFoundException excpetion) {
                throw new ArchetypeDescriptorCacheException(
                        ArchetypeDescriptorCacheException.ErrorCode.FailedToLoadClass,
                        new Object[] { descriptor.getClassName() }, excpetion);
            }

            // check that the assertions are specified correctly
            if (descriptor.getNodeDescriptors().size() > 0) {
                checkAssertionsInNode(descriptor.getNodeDescriptors());
            }
        }
    }

    /**
     * Process the file and load all the {@link AssertionTypeRecord} instances
     * 
     * @param assertFile
     *            the name of the file holding the assertion types
     */
    private void loadAssertionTypeDescriptorsFromFile(String assertFile) {
        // check that a non-null file was specified
        if (StringUtils.isEmpty(assertFile)) {
            throw new ArchetypeDescriptorCacheException(
                    ArchetypeDescriptorCacheException.ErrorCode.NoAssertionTypeFileSpecified);
        }

        AssertionTypeDescriptors types = null;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Attempting to process file " + assertFile);
            }
            types = loadAssertionTypeDescriptors(assertFile);
            Iterator iter = types.getAssertionTypeDescriptors().values().iterator();
            while (iter.hasNext()) {
                AssertionTypeDescriptor descriptor = (AssertionTypeDescriptor) iter
                        .next();
                assertionTypes.put(descriptor.getName(), descriptor);

                if (logger.isDebugEnabled()) {
                    logger.debug("Loaded assertion type "
                            + descriptor.getName());
                }
            }
        } catch (Exception exception) {
            throw new ArchetypeDescriptorCacheException(
                    ArchetypeDescriptorCacheException.ErrorCode.InvalidAssertionFile,
                    new Object[] { assertFile }, exception);
        }
    }

    /**
     * Return the {@link ArchetypeDescriptors} declared in the specified file.
     * In this case the file must be a resource in the class path.
     * 
     * @param resourceName
     *            the name of the resource that the archetypes are dfined
     * @return ArchetypeDescriptors
     * @throws Exception
     *             propagate exception to caller
     */
    private ArchetypeDescriptors loadArchetypeDescriptors(String resourceName)
            throws Exception {
        // load the mapping file
        Mapping mapping = new Mapping();

        mapping.loadMapping(new InputSource(new InputStreamReader(
                Thread.currentThread().getContextClassLoader().getResourceAsStream(
                        "org/openvpms/component/business/domain/im/archetype/descriptor/archetype-mapping-file.xml"))));

        return (ArchetypeDescriptors) new Unmarshaller(mapping)
                .unmarshal(new InputSource(new InputStreamReader(Thread
                        .currentThread().getContextClassLoader()
                        .getResourceAsStream(resourceName))));
    }

    /**
     * Return the {@link ArchetypeDescriptors} declared in the specified file.
     * 
     * @param name
     *            the file name
     * @return ArchetypeDescriptors
     * @throws Exception
     *             propagate exception to caller
     */
    private ArchetypeDescriptors loadArchetypeDescriptorsFromFile(String name)
            throws Exception {
        // load the mapping file
        Mapping mapping = new Mapping();

        mapping.loadMapping(new InputSource(new InputStreamReader(
                Thread.currentThread().getContextClassLoader().getResourceAsStream(
                        "org/openvpms/component/business/domain/im/archetype/descriptor/archetype-mapping-file.xml"))));

        return (ArchetypeDescriptors) new Unmarshaller(mapping)
                .unmarshal(new InputSource(new FileInputStream(name)));
    }

    /**
     * Return the {@link ArchetypeDescriptors} declared in the specified file.
     * In this case the file must be a resource in the class path.
     * 
     * @param reader
     *            the reader that declares all the archetypes
     * @return ArchetypeDescriptors
     * @throws Exception
     *             propagate exception to caller
     */
    private ArchetypeDescriptors loadArchetypeDescriptors(Reader reader)
            throws Exception {
        // load the mapping file
        Mapping mapping = new Mapping();

        mapping.loadMapping(new InputSource(new InputStreamReader(
                Thread.currentThread().getContextClassLoader().getResourceAsStream(
                        "org/openvpms/component/business/domain/im/archetype/descriptor/archetype-mapping-file.xml"))));

        return (ArchetypeDescriptors) new Unmarshaller(mapping)
                .unmarshal(reader);
    }

    /**
     * Return the {@link AssertionTypeDescriptors} in the specified file
     * 
     * @param assertFile
     *            the file that declares the assertions
     * @return AssertionTypeDescriptors
     * @throws Exception
     *             propagate exception to caller
     */
    private AssertionTypeDescriptors loadAssertionTypeDescriptors(
            String assertFile) throws Exception {
        // load the mapping file
        Mapping mapping = new Mapping();

        mapping.loadMapping(new InputSource(new InputStreamReader(
                Thread.currentThread().getContextClassLoader().getResourceAsStream(
                        "org/openvpms/component/business/domain/im/archetype/descriptor/assertion-type-mapping-file.xml"))));

        return (AssertionTypeDescriptors) new Unmarshaller(mapping)
                .unmarshal(new InputSource(new InputStreamReader(Thread
                        .currentThread().getContextClassLoader()
                        .getResourceAsStream(assertFile))));
    }

    /**
     * Create athread to monitor changes in archeype definitions. A thread will
     * only be created if an scanInteval greater than 0 is defined.
     * 
     * @param dir
     *            the base directory name
     * @param ext
     *            the extension to look for
     * @param interval
     *            the scan interval
     * 
     */
    private void createArchetypeMonitorThread(String dir, String[] ext,
            long interval) {
        File fdir = new File(dir);
        if (fdir.exists()) {
            if (interval > 0) {
                timer.schedule(new ArchetypeMonitor(fdir, ext), interval,
                        interval);
            }
        } else {
            logger.warn("The directory " + dir + " does not exist.");
        }
    }

    

    /**
     * This class is used to monitor changes in archetypes. It doesn't handle
     * the removal of an archetype definition from the system.
     */
    private class ArchetypeMonitor extends TimerTask {
        /**
         * This is the base directory where the archetypes are stored
         */
        private File dir;

        /**
         * This is the extensions to filter on
         */
        private String[] extensions;

        /**
         * The last time this was run
         */
        private Date lastScan = new Date(System.currentTimeMillis());

        /**
         * Instantiate an instance of this class using a base directory and a
         * list of file extensions. It will only deal with files that fulfill
         * this criteria. By default it will do a recursive search.
         * 
         * @param dir
         *            the base directory
         * @param extensions
         *            only search for these files
         * @param interval
         *            the scan interval
         */
        ArchetypeMonitor(File dir, String[] extensions) {
            this.dir = dir;
            this.extensions = extensions;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        public void run() {
            Date startTime = new Date(System.currentTimeMillis());
            if (logger.isDebugEnabled()) {
                logger.debug("Executing the ArchetypeMonitor");
            }
            List<File> changedFiles = getChangedFiles();
            for (File file : changedFiles) {
                try {
                    loadArchetypeDescriptorsFromFile(file.getPath());
                    logger
                            .info("Reloaded archetypes in file "
                                    + file.getPath());
                } catch (Exception exception) {
                    logger.warn(
                            "Failed to load archetype in " + file.getPath(),
                            exception);
                }
            }

            // update the last scan
            lastScan = startTime;
        }

        /**
         * Return a list of files that have changed since the last run.
         */
        private List<File> getChangedFiles() {
            ArrayList<File> changedFiles = new ArrayList<File>();

            try {
                Collection collection = FileUtils.listFiles(dir, extensions,
                        true);
                Iterator files = collection.iterator();
                while (files.hasNext()) {
                    File file = (File) files.next();
                    if (FileUtils.isFileNewer(file, lastScan)) {
                        changedFiles.add(file);
                    }
                }
            } catch (Exception exception) {
                logger.warn("Failure in getChangedFiles", exception);
            }

            return changedFiles;
        }
    }
}
