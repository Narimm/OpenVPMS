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
 *  Copyright 2008-2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.etl.tools.doc;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.doc.DocumentRules;
import org.openvpms.archetype.rules.patient.InvestigationActStatus;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
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
 * <p/>
 * <h4>Duplicate documents</h4>
 * When {@code overwrite} is {@code true}, the following applies to files that have the same content as
 * documents already loaded for any act:
 * <ul>
 * <li>if they have the same file name, they will be skipped
 * <li>if they have a different file name, the existing document will be removed, and the new file loaded
 * </ul>
 *
 * @author Tim Anderson
 */
class IdLoader extends AbstractLoader {

    /**
     * The default regular expression for extracting act ids from file names.
     */
    public static final String DEFAULT_REGEXP = "[^\\d]*(\\d+).*";

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager transactionManager;

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
     * The logger.
     */
    private static final Log log = LogFactory.getLog(IdLoader.class);


    /**
     * Constructs a new {@code IdLoader}.
     *
     * @param dir                the source directory
     * @param shortNames         the document archetype(s) that may be loaded to. May be {@code null}, or contain
     *                           wildcards
     * @param service            the archetype service
     * @param factory            the document factory
     * @param transactionManager the transaction manager
     * @param recurse            if {@code true} recursively scan the source dir
     * @param overwrite          if {@code true} overwrite existing documents
     */
    public IdLoader(File dir, String[] shortNames, IArchetypeService service, DocumentFactory factory,
                    PlatformTransactionManager transactionManager, boolean recurse, boolean overwrite) {
        this(dir, shortNames, service, factory, transactionManager, recurse, overwrite, DEFAULT_PATTERN);
    }

    /**
     * Constructs a new {@code IdLoader}.
     *
     * @param dir                the source directory
     * @param shortNames         the document archetype(s) that may be loaded to. May be {@code null}, or contain
     *                           wildcards
     * @param service            the archetype service
     * @param factory            the document factory
     * @param recurse            if {@code true} recursively scan the source dir
     * @param transactionManager the transaction manager
     * @param overwrite          if {@code true} overwrite existing documents
     * @param pattern            the pattern to extract act ids from file names
     * @throws IllegalArgumentException if {@code shortName} doesn't represent an archetype that may have documents
     *                                  loaded to it
     */
    public IdLoader(File dir, String[] shortNames, IArchetypeService service, DocumentFactory factory,
                    PlatformTransactionManager transactionManager, boolean recurse, boolean overwrite,
                    Pattern pattern) {
        super(service, factory);
        this.transactionManager = transactionManager;
        this.shortNames = getDocumentActShortNames(shortNames);
        if (this.shortNames.length == 0) {
            throw new IllegalArgumentException("Argument 'shortNames' doesn't refer to any valid archetype for loading "
                                               + "documents to: " + ArrayUtils.toString(shortNames));
        }
        this.overwrite = overwrite;
        if (log.isDebugEnabled()) {
            log.debug("dir=" + dir);
            log.debug("recurse=" + recurse);
        }
        List<File> files = getFiles(dir, recurse);
        iterator = files.iterator();
        this.pattern = pattern;
        if (log.isDebugEnabled()) {
            log.debug("shortNames=" + StringUtils.join(this.shortNames, ", "));
            log.debug("overwrite=" + overwrite);
            log.debug("pattern=" + pattern);
        }
    }

    /**
     * Determines if there is a document to load.
     *
     * @return {@code true} if there is a document to load, otherwise
     *         {@code false}
     */
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * Loads the next document.
     *
     * @return {@code true} if the document was loaded successfully
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
                notifyAlreadyLoaded(file, id);
            }
        } else {
            notifyMissingAct(file, id);
        }
        return result;
    }

    /**
     * Returns the files in the specified directory, ordered increasing modified timestamp and name.
     *
     * @param dir     the directory
     * @param recurse if {@code true}, recurse subdirectories
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
     * @return the act id, or {@code -1} if none can be found
     */
    private long getId(String name) {
        long result = -1;
        Matcher matcher = pattern.matcher(name);
        if (matcher.matches()) {
            result = Long.valueOf(matcher.group(1));
            if (log.isDebugEnabled()) {
                log.debug("match: " + name + ", id=" + result);
            }
        } else if (log.isDebugEnabled()) {
            log.debug("no match: " + name);
        }
        return result;
    }

    /**
     * Creates a document from a file, associating it with the supplied act.
     *
     * @param file the file
     * @param act  the document act
     * @return {@code true} if the file was loaded
     */
    private boolean load(File file, DocumentAct act) {
        boolean result = false;
        try {
            Document doc = createDocument(file);
            DocumentAct duplicate = getDuplicate(act, doc);
            if (duplicate != null && file.getName().equals(duplicate.getFileName())) {
                // identical content and file name
                notifyAlreadyLoaded(file, act.getId());
            } else {
                if (TypeHelper.isA(act, InvestigationArchetypes.PATIENT_INVESTIGATION)) {
                    act.setStatus(InvestigationActStatus.RECEIVED);
                } else {
                    act.setStatus(ActStatus.COMPLETED);
                }
                boolean version = (duplicate != act);
                addDocument(act, doc, version);
                notifyLoaded(file, act.getId());
                result = true;
            }
        } catch (Exception exception) {
            notifyError(file, exception);
        }
        return result;
    }

    private DocumentAct getDuplicate(DocumentAct act, Document document) {
        DocumentRules rules = getRules();
        if (rules.isDuplicate(act, document)) {
            return act;
        }
        for (DocumentAct version : rules.getVersions(act)) {
            if (rules.isDuplicate(version, document)) {
                return version;
            }
        }
        return null;
    }

    /**
     * Adds a document to a document act, and saves it.
     *
     * @param act      the act
     * @param document the document to add
     * @param version  if {@code true} version any old document if the act supports it
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *          for any archetype service error
     */
    protected void addDocument(final DocumentAct act, final Document document, final boolean version) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.execute(new TransactionCallback<Object>() {
            public Object doInTransaction(TransactionStatus status) {
                IArchetypeService service = getService();
                removeDuplicate(act, document);
                List<IMObject> objects = getRules().addDocument(act, document, version);
                service.save(objects);
                return null;
            }
        });
    }

    /**
     * Removes any document act and document that duplicates that supplied.
     *
     * @param act      the act
     * @param document the new document
     */
    private void removeDuplicate(DocumentAct act, Document document) {
        DocumentRules rules = getRules();
        IArchetypeService service = getService();
        ActBean bean = new ActBean(act, service);
        for (DocumentAct version : rules.getVersions(act)) {
            if (rules.isDuplicate(version, document)) {
                ActRelationship r = bean.getRelationship(version);
                version.removeActRelationship(r);
                bean.removeRelationship(r);
                service.save(act);
                service.remove(version);
                Document dupDoc = (Document) service.get(version.getDocument());
                if (dupDoc != null) {
                    service.remove(dupDoc);
                }
                break;
            }
        }
    }

    /**
     * Returns the act for the specified id.
     *
     * @param id the identifier
     * @return the corresponding act, or {@code null} if none is found
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

}
