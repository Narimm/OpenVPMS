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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.etl.tools.doc;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.DocumentRules;
import org.openvpms.archetype.rules.patient.InvestigationActStatus;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


/**
 * Abstract implementation of the {@link Loader} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractLoader implements Loader {

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
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The document factory.
     */
    private final DocumentFactory factory;

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * The document rules.
     */
    private final DocumentRules rules;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(AbstractLoader.class);


    /**
     * Constructs an {@link AbstractLoader}.
     *
     * @param dir                the source directory
     * @param shortNames         the document act archetype(s) that may be loaded to. May be {@code null}, or contain
     *                           wildcards
     * @param service            the archetype service
     * @param factory            the document factory
     * @param transactionManager the transaction manager
     * @param recurse            if {@code true} recursively scan the source dir
     * @param overwrite          if {@code true} overwrite existing documents
     */
    public AbstractLoader(File dir, String[] shortNames, IArchetypeService service, DocumentFactory factory,
                          PlatformTransactionManager transactionManager, boolean recurse, boolean overwrite) {
        this.shortNames = getDocumentActShortNames(shortNames, service);
        if (this.shortNames.length == 0) {
            throw new IllegalArgumentException("Argument 'shortNames' doesn't refer to any valid archetype for loading "
                                               + "documents to: " + ArrayUtils.toString(shortNames));
        }
        if (log.isInfoEnabled()) {
            log.info("Loading documents for archetypes=" + StringUtils.join(this.shortNames, ", "));
        }
        this.overwrite = overwrite;
        if (log.isDebugEnabled()) {
            log.debug("dir=" + dir);
            log.debug("recurse=" + recurse);
        }
        List<File> files = getFiles(dir, recurse);
        iterator = files.iterator();

        this.service = service;
        this.factory = factory;
        this.transactionManager = transactionManager;
        this.rules = new DocumentRules(service);
    }

    /**
     * Determines if there is a document to load.
     *
     * @return {@code true} if there is a document to load, otherwise
     * {@code false}+
     */
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * Loads the next document.
     *
     * @return {@code true} if the document was loaded successfully
     */
    @Override
    public boolean loadNext() {
        return load(iterator.next(), overwrite);
    }

    /**
     * Returns the document act archetypes that may be loaded to.
     *
     * @return the document act archetype short names
     */
    protected String[] getShortNames() {
        return shortNames;
    }

    /**
     * Loads a document.
     *
     * @param file      the document
     * @param overwrite if {@code true} overwrite existing documents
     * @return {@code true} if the document was loaded successfully
     */
    protected abstract boolean load(File file, boolean overwrite);

    /**
     * Loads a document.
     *
     * @param act       the act to attach the document to
     * @param file      the document
     * @param overwrite if {@code true} overwrite existing documents
     * @param context   the load context
     * @return {@code true} if the document was loaded
     */
    protected boolean load(DocumentAct act, File file, boolean overwrite, LoadContext context) {
        boolean result = false;
        if (act.getDocument() == null || overwrite) {
            result = loadWithDuplicateCheck(act, file, context);
        } else {
            context.alreadyLoaded(file, act.getId());
        }
        return result;
    }

    /**
     * Creates a new document.
     *
     * @param file the file to create the document from
     * @return a new document
     */
    protected Document createDocument(File file) {
        return factory.create(file);
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }

    /**
     * Returns the transaction manager.
     */
    protected PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
     * Creates a document from a file, associating it with the supplied act.
     * <p/>
     * On successful load, {@link FileStrategy#loaded(File)} is invoked. <br/>
     * If this fails to move the file, {@link FileStrategy#error(File)} is invoked. <br/>
     * If the load fails, {@link FileStrategy#error(File)} is invoked.<br/>
     * If the load is successful, but the file cannot be moved, changes are NOT rolled back. <br/>
     * This is to avoid situations where the database changes roll back after the file has moved. <br/>
     * As a result, the loader needs to perform duplicate checks to avoid the same file being loaded multiple times.
     *
     * @param act     the document act
     * @param file    the file
     * @param context the load context
     * @return {@code true} if the file was loaded
     */
    protected boolean loadWithDuplicateCheck(DocumentAct act, File file, LoadContext context) {
        boolean result = false;
        try {
            if (ActStatus.CANCELLED.equals(act.getStatus())) {
                throw new DocumentLoaderException(DocumentLoaderException.ErrorCode.CannotLoadToCancelledAct,
                                                  DescriptorHelper.getDisplayName(act, service), act.getId());
            }
            Document doc = createDocument(file);
            DocumentAct duplicate = getDuplicate(act, doc);
            if (duplicate != null && file.getName().equals(duplicate.getFileName())) {
                // identical content and file name
                context.alreadyLoaded(file, act.getId());
            } else {
                if (TypeHelper.isA(act, InvestigationArchetypes.PATIENT_INVESTIGATION)) {
                    act.setStatus2(InvestigationActStatus.RECEIVED);
                } else if (!ActStatus.POSTED.equals(act.getStatus())) {
                    act.setStatus(ActStatus.COMPLETED);
                }
                boolean version = (duplicate != act);
                addDocument(act, doc, version);
                context.loaded(file, act.getId());
                result = true;
            }
        } catch (Exception exception) {
            context.error(file, exception);
        }
        return result;
    }

    /**
     * Adds a document to a document act, and saves it.
     * <p/>
     * This invokes {@link #addDocument(DocumentAct, Document, boolean, IArchetypeService)} within a transaction.
     *
     * @param act      the act
     * @param document the document to add
     * @param version  if {@code true} version any old document if the act supports it
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected void addDocument(final DocumentAct act, final Document document, final boolean version) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                IArchetypeService service = getService();
                AbstractLoader.this.addDocument(act, document, version, service);
            }
        });
    }

    /**
     * Adds a document to a document act, and saves it.
     * <p/>
     * If the new document is an duplicates an existing version of the document, that version will be removed.
     *
     * @param act      the act
     * @param document the document to add
     * @param version  if {@code true} version any old document if the act supports it
     * @param service  the archetype service
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected void addDocument(DocumentAct act, Document document, boolean version, IArchetypeService service) {
        removeDuplicate(act, document);
        List<IMObject> objects = rules.addDocument(act, document, version);
        service.save(objects);
    }

    /**
     * Returns the act for the specified id, and archetype short name.
     *
     * @param id        the identifier
     * @param shortName the short name
     * @return the corresponding act, or {@code null} if none is found
     */
    protected DocumentAct getAct(long id, String shortName) {
        return getAct(id, new String[]{shortName});
    }

    /**
     * Returns the act for the specified id.
     *
     * @param id         the identifier
     * @param shortNames the supported short names
     * @return the corresponding act, or {@code null} if none is found
     */
    protected DocumentAct getAct(long id, String[] shortNames) {
        DocumentAct result = null;
        ArchetypeQuery query = new ArchetypeQuery(shortNames, true, true);
        query.add(new NodeConstraint("id", id));
        IPage<IMObject> page = service.get(query);
        List<IMObject> results = page.getResults();
        if (!results.isEmpty()) {
            result = (DocumentAct) results.get(0);
        }
        return result;
    }

    /**
     * Returns the act that already contains the specified document.
     *
     * @param act      the root act
     * @param document the document
     * @return {@code act}, if it has the same content as {@code document}, or the first version of {@code act} that
     * has the same content, or {@code null} if the act nor its versions have the same content
     */
    protected DocumentAct getDuplicate(DocumentAct act, Document document) {
        DocumentAct result = null;
        if (rules.isDuplicate(act, document)) {
            result = act;
        } else {
            for (DocumentAct version : rules.getVersions(act)) {
                if (rules.isDuplicate(version, document)) {
                    result = version;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Removes any document act and document that duplicates that supplied.
     *
     * @param act      the act
     * @param document the new document
     */
    protected void removeDuplicate(DocumentAct act, Document document) {
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
     * Returns all primary document act archetype short names that match the supplied short name, and have a "document"
     * node.
     *
     * @param shortNames the short names. May be {@code null} or contain wildcards
     * @param service    the archetype service
     * @return the document act archetype short names
     */
    private static String[] getDocumentActShortNames(String[] shortNames, IArchetypeService service) {
        List<String> result = new ArrayList<>();
        List<ArchetypeDescriptor> descriptors;
        if (shortNames == null || shortNames.length == 0) {
            descriptors = service.getArchetypeDescriptors();
        } else {
            descriptors = DescriptorHelper.getArchetypeDescriptors(shortNames, service);
        }
        for (ArchetypeDescriptor descriptor : descriptors) {
            if (DocumentAct.class.isAssignableFrom(descriptor.getClazz())
                && descriptor.isPrimary()
                && descriptor.getNodeDescriptor("document") != null) {
                result.add(descriptor.getType().getShortName());
            }
        }
        // can't load to document template acts.
        result.remove(DocumentArchetypes.DOCUMENT_TEMPLATE_ACT);
        return result.toArray(new String[result.size()]);
    }

    /**
     * Returns the files in the specified directory, ordered increasing modified timestamp and name.
     *
     * @param dir     the directory
     * @param recurse if {@code true}, recurse subdirectories
     * @return a list of files
     */
    @SuppressWarnings("unchecked")
    protected List<File> getFiles(File dir, boolean recurse) {
        List<File> files = new ArrayList<>(FileUtils.listFiles(dir, null, recurse));

        // sort the files on increasing last modified timestamp and name
        Collections.sort(files, new Comparator<File>() {
            public int compare(File o1, File o2) {
                return Long.compare(o1.lastModified(), o2.lastModified());
            }
        });
        return files;
    }
}
