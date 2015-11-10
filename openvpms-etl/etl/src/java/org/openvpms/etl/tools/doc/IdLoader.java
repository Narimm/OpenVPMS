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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
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
public class IdLoader extends AbstractLoader {

    /**
     * The default regular expression for extracting act ids from file names.
     */
    public static final String DEFAULT_REGEXP = "[^\\d]*(\\d+).*";

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
     * Constructs an {@link IdLoader}.
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
     * Constructs an {@link IdLoader}.
     *
     * @param dir                the source directory
     * @param shortNames         the document archetype(s) that may be loaded to. May be {@code null}, or contain
     *                           wildcards
     * @param service            the archetype service
     * @param transactionManager the transaction manager
     * @param recurse            if {@code true} recursively scan the source dir
     * @param overwrite          if {@code true} overwrite existing documents
     */
    public IdLoader(File dir, String[] shortNames, IArchetypeService service,
                    PlatformTransactionManager transactionManager, boolean recurse, boolean overwrite,
                    Pattern pattern) {
        this(dir, shortNames, service, new DefaultDocumentFactory(), transactionManager, recurse, overwrite, pattern);
    }

    /**
     * Constructs an {@link IdLoader}.
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
        super(dir, shortNames, service, factory, transactionManager, recurse, overwrite);
        this.pattern = pattern;
        if (log.isDebugEnabled()) {
            log.debug("pattern=" + pattern);
        }
    }

    /**
     * Loads a document.
     *
     * @param file      the document
     * @param overwrite if {@code true} overwrite existing documents
     * @return {@code true} if the document was loaded successfully
     */
    @Override
    protected boolean load(File file, boolean overwrite) {
        boolean result = false;
        long id = getId(file.getName());
        DocumentAct act = (id != -1) ? getAct(id) : null;
        if (act != null) {
            result = load(act, file, overwrite);
        } else {
            notifyMissingAct(file, id);
        }
        return result;
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
     * Returns the act for the specified id.
     *
     * @param id the identifier
     * @return the corresponding act, or {@code null} if none is found
     */
    private DocumentAct getAct(long id) {
        DocumentAct result = null;
        ArchetypeQuery query = new ArchetypeQuery(getShortNames(), true, true);
        query.add(new NodeConstraint("id", id));
        IPage<IMObject> page = getService().get(query);
        List<IMObject> results = page.getResults();
        if (!results.isEmpty()) {
            result = (DocumentAct) results.get(0);
        }
        return result;
    }

}
