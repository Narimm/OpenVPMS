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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.tools.doc;

import org.apache.commons.io.FileUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Document loader that:
 * <ul>
 * <li>recursively searches a source directory for files</li>
 * <li>for each file, parses its name for a <em>DocumentAct</em> identifier</li>
 * <li>retrieves the corresponding <em>DocumentAct</em> and attaches the file as a new <em>Document</em></li>
 * </ul>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class IdLoader extends AbstractLoader {

    /**
     * The default regular expression for extracting act ids from file names.
     */
    public static final String DEFAULT_REGEXP = "[^\\d]*(\\d+).*";

    /**
     * The document act short names.
     */
    private final String[] shortNames;

    /**
     * The file iterator.
     */
    private final Iterator<File> iterator;

    /**
     * Determines if existing documents should be overwritten.
     */
    private final boolean overwrite;

    /**
     * The pattern to extract act ids from file names.
     */
    private final Pattern pattern;

    /**
     * The default pattern to extract act ids from file names.
     */
    private static final Pattern DEFAULT_PATTERN = Pattern.compile(DEFAULT_REGEXP);


    /**
     * Constructs a new <tt>IdLoader</tt>.
     *
     * @param dir       the source directory
     * @param service   the archetype service
     * @param factory   the document factory
     * @param recurse   if <tt>true</tt> recursively scan the source dir
     * @param overwrite if <tt>true</tt> overwrite existing documents
     */
    public IdLoader(File dir, IArchetypeService service, DocumentFactory factory, boolean recurse,
                    boolean overwrite) {
        this(dir, service, factory, recurse, overwrite, DEFAULT_PATTERN);
    }

    /**
     * Constructs a new <tt>IdLoader</tt>.
     *
     * @param dir       the source directory
     * @param service   the archetype service
     * @param factory   the document factory
     * @param recurse   if <tt>true</tt> recursively scan the source dir
     * @param overwrite if <tt>true</tt> overwrite existing documents
     * @param pattern   the pattern to extract act ids from file names
     */
    public IdLoader(File dir, IArchetypeService service, DocumentFactory factory, boolean recurse,
                    boolean overwrite, Pattern pattern) {
        super(service, factory);
        shortNames = getDocumentActShortNames();
        this.overwrite = overwrite;
        List<File> files = getFiles(dir, recurse);
        iterator = files.iterator();
        this.pattern = pattern;
    }

    /**
     * Determines if there is a document to load.
     *
     * @return <tt>true</tt> if there is a document to load, otherwise
     *         <tt>false</tt>
     */
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * Loads the next document.
     *
     * @return <tt>true</tt> if the document was loaded successfully
     */
    public boolean loadNext() {
        boolean result = false;
        File file = iterator.next();
        long id = getId(file.getName());
        DocumentAct act = (id != -1) ? getAct(id) : null;
        if (act != null) {
            if (act.getDocument() == null || overwrite) {
                result = load(file, act);
            } else {
                notifyAlreadyLoaded(file);
            }
        } else {
            notifyMissingAct(file);
        }
        return result;
    }

    /**
     * Returns the files in the specified directory, ordered increasing modified timestamp and name.
     *
     * @param dir     the directory
     * @param recurse if <tt>true</tt>, recurse subdirectories
     * @return a list of files
     */
    @SuppressWarnings("unchecked")
    private static List<File> getFiles(File dir, boolean recurse) {
        List<File> files = new ArrayList<File>(FileUtils.listFiles(dir, null, recurse));

        // sort the files on increasing last modified timestamp and name
        Collections.sort(files, new Comparator<File>() {
            public int compare(File o1, File o2) {
                int result = (int) (o1.lastModified() - o2.lastModified());
                if (result == 0) {
                    result = o1.compareTo(o2);
                }
                return result;
            }
        });
        return files;
    }

    /**
     * Returns the act id from a file name.
     *
     * @param name the file name
     * @return the act id, or <tt>-1</tt> if none can be found
     */
    private long getId(String name) {
        long result = -1;
        Matcher matcher = pattern.matcher(name);
        if (matcher.matches()) {
            result = Long.valueOf(matcher.group(1));
        }
        return result;
    }

    /**
     * Creates a document from a file, associating it with the supplied act.
     *
     * @param file the file
     * @param act  the document act
     * @return <tt>true</tt> if the file was loaded
     */
    private boolean load(File file, DocumentAct act) {
        boolean result = false;
        try {
            String mimeType = getMimeType(file);
            Document doc = createDocument(file, mimeType);
            act.setStatus(ActStatus.COMPLETED);
            addDocument(act, doc);
            notifyLoaded(file);
            result = true;
        } catch (Exception exception) {
            notifyError(file, exception);
        }
        return result;
    }

    /**
     * Returns the act for the specified id.
     *
     * @param id the identifier
     * @return the corresponding act, or <tt>null</tt> if none is found
     */
    private DocumentAct getAct(long id) {
        DocumentAct result = null;
        ArchetypeQuery query = new ArchetypeQuery(shortNames, true, true);
        query.add(new NodeConstraint("id", id));
        IPage<IMObject> page = getService().get(query);
        List<IMObject> results = page.getResults();
        if (!results.isEmpty()) {
            result = (DocumentAct) results.get(0);
        }
        return result;
    }

    /**
     * Returns all document act archetype short names.
     *
     * @return the document act  archetype short names
     */
    private String[] getDocumentActShortNames() {
        List<String> result = new ArrayList<String>();
        List<ArchetypeDescriptor> descriptors = getService().getArchetypeDescriptors();
        for (ArchetypeDescriptor descriptor : descriptors) {
            if (DocumentAct.class.isAssignableFrom(descriptor.getClazz())) {
                result.add(descriptor.getType().getShortName());
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Returns the mime type for a file.
     *
     * @param file the file
     * @return the mime type of the file
     * @throws IOException           for any I/O error
     * @throws MalformedURLException for any URL error
     */
    private String getMimeType(File file) throws IOException {
        URLConnection uc = file.toURL().openConnection();
        String mimeType = uc.getContentType();
        uc.getInputStream().close();
        return mimeType;
    }

}
