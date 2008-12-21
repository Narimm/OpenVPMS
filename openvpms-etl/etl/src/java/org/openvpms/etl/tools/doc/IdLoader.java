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
import org.apache.commons.io.FilenameUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Document loader that:
 * <ul>
 * <li>recursively searches a source directory for files
 * <li>for each file, parses its name for a <em>DocumentAct</em> identifier
 * <li>retrieves the corresponding <em>DocumentAct</em> and attaches the
 * file as a new <em>Document</em>
 * </ul>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class IdLoader extends AbstractLoader {

    /**
     * The document act short names.
     */
    private final String[] shortNames;

    /**
     * The file iterator.
     */
    private Iterator<File> iterator;


    /**
     * Constructs a new <tt>IdLoader</tt>.
     *
     * @param dir the source directory
     * @param service the archetype service
     * @param factory the document factory
     * @param recurse if <tt>true</tt> recursively scan the source dir
     */
    @SuppressWarnings("unchecked")
    public IdLoader(File dir, IArchetypeService service,
                    DocumentFactory factory,
                    boolean recurse) {
        super(service, factory);
        this.shortNames = getDocumentActShortNames();
        List<File> files = new ArrayList<File>(
                FileUtils.listFiles(dir, null, recurse));
        Collections.sort(files);
        iterator = files.iterator();
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
        String name = file.getName();
        DocumentAct act;
        if (name.startsWith("C")) {
            String id = FilenameUtils.getBaseName(name.substring(1));
            act = getAct(id, "act.customerDocument*");
        } else if (name.startsWith("P") || name.startsWith("V")) {
            String id = FilenameUtils.getBaseName(name.substring(1));
            act = getAct(id, "act.patientDocument*");
        } else {
            String id = FilenameUtils.getBaseName(name);
            act = getAct(id);
        }
        if (act != null) {
            if (act.getDocument() == null) {
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
            act.setMimeType(mimeType);
            act.setFileName(file.getName());
            act.setStatus(ActStatus.COMPLETED);
            Document doc = createDocument(file, mimeType);
            act.setDocument(doc.getObjectReference());
            save(act, doc);
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
    private DocumentAct getAct(String id) {
        try {
            Long value = Long.valueOf(id);
            ArchetypeQuery query = new ArchetypeQuery(shortNames, true, true);
            query.add(new NodeConstraint("id", value));
            IPage<IMObject> page = getService().get(query);
            List<IMObject> results = page.getResults();
            if (!results.isEmpty()) {
                return (DocumentAct) results.get(0);
            }
        } catch (NumberFormatException ignore) {
            // do nothing
        }
        return null;
    }

    /**
     * Returns the act for the specified id and short name.
     *
     * @param id        the identifier
     * @param shortName the archetype short name. May contain wildcards.
     * @return the corresponding act, or <tt>null</tt> if none is found
     */
    private DocumentAct getAct(String id, String shortName) {
        DocumentAct act = getAct(id);
        DocumentAct result = null;
        if (act != null) {
            if (TypeHelper.isA(act, shortName)) {
                result = act;
            }
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
        List<ArchetypeDescriptor> descriptors
                = getService().getArchetypeDescriptors();
        for (ArchetypeDescriptor descriptor : descriptors) {
            if (descriptor.getClazz().isAssignableFrom(DocumentAct.class)) {
                result.add(descriptor.getType().getShortName());
            }
        }
        return result.toArray(new String[0]);
    }

    /**
     * Returns the mime type for a file.
     *
     * @param file the file
     * @return the mime type of the file
     * @throws IOException           for any I/O error
     * @throws MalformedURLException for any URL error
     */
    private String getMimeType(File file) throws IOException,
                                                 MalformedURLException {
        URLConnection uc = file.toURL().openConnection();
        String mimeType = uc.getContentType();
        uc.getInputStream().close();
        return mimeType;
    }
}
