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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.doc;

import echopointng.image.URLImageReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Retrieves image documents from the archetype service so that they can be displayed in the UI.
 * For performance, images are cached in the filesystem. The directory may be specified at construction, otherwise
 * the they are cached in a directory named <em>ImageCache</em> under <em>"javax.servlet.context.tempdir"</em>.
 *
 * @author Tim Anderson
 */
public class ImageCache implements ServletContextAware {

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The servlet context.
     */
    private ServletContext context;

    /**
     * The cache directory.
     */
    private File dir;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ImageCache.class);

    /**
     * The property of the temporary directory used to store images under.
     */
    private static final String TEMP_DIR = "javax.servlet.context.tempdir";

    /**
     * Constructs an {@link ImageCache}.
     *
     * @param handlers the document handlers
     */
    public ImageCache(DocumentHandlers handlers) {
        this(handlers, null);
    }

    /**
     * Constructs an {@link ImageCache}.
     *
     * @param handlers the document handlers
     * @param dir      the directory to cache images. May be {@code null}
     */
    public ImageCache(DocumentHandlers handlers, File dir) {
        this.handlers = handlers;
        this.dir = dir;
        if (dir != null && !dir.exists() && !dir.mkdirs()) {
            throw new IllegalArgumentException(
                    "Argument 'dir' specifies a directory that does not exist, and cannot be created: " + dir);
        }
    }

    /**
     * Returns a reference to an image.
     *
     * @param act the image act
     * @return the image reference, or {@code null} if the image cannot be located or cached
     */
    public URLImageReference getImage(DocumentAct act) {
        URLImageReference result = null;
        File dir = getCacheDir();
        if (dir != null) {
            result = getImage(act, dir);
        }
        return result;
    }

    /**
     * Set the ServletContext that this object runs in.
     *
     * @param context ServletContext object to be used by this object
     */
    @Override
    public void setServletContext(ServletContext context) {
        this.context = context;
    }

    /**
     * Returns a reference to an image.
     *
     * @param act the image act
     * @param dir the directory to store and load the image from
     * @return the image reference, or {@code null} if the image cannot be located or cached
     */
    private synchronized URLImageReference getImage(DocumentAct act, File dir) {
        String fileName = act.getId() + "_" + act.getVersion() + "_" + act.getFileName();
        File file = new File(dir, fileName);
        URLImageReference result = null;
        try {
            if (!file.exists()) {
                Document document = (Document) IMObjectHelper.getObject(act.getDocument());
                if (document != null) {
                    DocumentHandler handler = handlers.get(document);
                    File tmp = File.createTempFile("img", null, dir);
                    FileCopyUtils.copy(handler.getContent(document), new FileOutputStream(tmp));
                    if (tmp.renameTo(file)) {
                        result = new URLImageReference(file.toURI().toURL());
                    } else {
                        log.error("Failed to rename " + tmp + " to " + file);
                    }
                }
            } else {
                result = new URLImageReference(file.toURI().toURL());
            }
        } catch (Throwable exception) {
            log.error("Failed to retrieve image: " + fileName, exception);
        }
        return result;
    }

    /**
     * Returns the image cache directory, creating it if required.
     *
     * @return the image cache directory, or {@code null} if one doesn't exist and can't be created
     */
    private synchronized File getCacheDir() {
        if (dir == null) {
            File root = (context != null) ? (File) context.getAttribute(TEMP_DIR) : new File("./");
            File cache = new File(root, "ImageCache");
            if (!cache.exists() && !cache.mkdirs()) {
                log.error("Failed to create ImageCache directory: " + cache);
            } else {
                dir = cache;
            }
        }
        return dir;
    }
}
