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
public class IdLoader extends AbstractLoader {

    private final String[] shortNames;

    private Iterator<File> iterator;


    /**
     * Constructs a new <tt>DocumentLoader</tt>.
     *
     * @param service the archetype service
     */
    @SuppressWarnings("unchecked")
    public IdLoader(File dir, IArchetypeService service,
                    DocumentFactory factory) {
        super(service, factory);
        this.shortNames = getDocumentActShortNames();
        List<File> files = new ArrayList<File>(
                FileUtils.listFiles(dir, null, true));
        Collections.sort(files);
        iterator = files.iterator();
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

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
            if (act.getDocument() != null) {
                result = load(file, act, name);
            } else {
                notifyAlreadyLoaded(file);
            }
        } else {
            notifyMissingAct(file);
        }
        return result;
    }

    private boolean load(File file, DocumentAct act, String name) {
        boolean result = false;
        try {
            String mimeType = getMimeType(file);
            act.setMimeType(mimeType);
            act.setFileName(name);
            act.setStatus(ActStatus.COMPLETED);
            Document doc = createDocument(file, mimeType);
            save(act, doc);
            notifyLoaded(file);
            result = true;
        } catch (Exception exception) {
            notifyError(file, exception);
        }
        return result;
    }


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

    private String getMimeType(File file)
            throws java.io.IOException, MalformedURLException {
        URLConnection uc = file.toURL().openConnection();
        return uc.getContentType();
    }
}
