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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.doc;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.filetransfer.UploadListener;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.web.component.edit.AbstractPropertyEditor;
import org.openvpms.web.component.edit.Cancellable;
import org.openvpms.web.component.edit.Deletable;
import org.openvpms.web.component.edit.Saveable;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.select.BasicSelector;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.ErrorHelper;

import java.util.Iterator;


/**
 * Editor for {@link IMObjectReference}s of type <em>document.*</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentEditor extends AbstractPropertyEditor
        implements Saveable, Cancellable, Deletable {

    /**
     * The upload selector.
     */
    private BasicSelector<Document> selector;

    /**
     * Manages old document references to avoid orphaned documents.
     */
    private final DocReferenceMgr refMgr;

    /**
     * Indicates if the object has been saved.
     */
    private boolean saved = false;

    /**
     * Cached file name.
     */
    private String name;

    /**
     * Cached mime type.
     */
    private String mimeType;


    /**
     * Construct a new <tt>DocumentEditor</tt>.
     *
     * @param property the property being edited
     * @throws ArchetypeServiceException for any archetype service error
     */
    public DocumentEditor(Property property) {
        super(property);

        selector = new BasicSelector<Document>();
        selector.getSelect().addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onSelect();
            }
        });
        IMObjectReference original = (IMObjectReference) property.getValue();
        if (original != null) {
            init(original);
        }
        refMgr = new DocReferenceMgr(original);
    }

    /**
     * Returns the edit component.
     *
     * @return the edit component
     */
    public Component getComponent() {
        return selector.getComponent();
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return selector.getFocusGroup();
    }

    /**
     * Sets the document.
     *
     * @param document the new document
     * @throws ArchetypeServiceException for any error
     */
    public void setDocument(Document document) {
        setDocument(document, false);
    }

    /**
     * Sets the document.
     *
     * @param document the new document
     * @param keepOld  if <tt>true</tt> any existing document won't be deleted at commit
     * @throws ArchetypeServiceException for any error
     */
    protected void setDocument(Document document, boolean keepOld) {
        IMObjectReference old = getReference();
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        service.save(document);

        // update cached properties
        name = document.getName();
        mimeType = document.getMimeType();

        // update the reference. This will notify any registered listeners
        IMObjectReference ref = document.getObjectReference();
        getProperty().setValue(ref);

        if (old != null && keepOld) {
            refMgr.remove(old);
        }

        // queue for addition
        refMgr.add(ref);

        // update the selector
        selector.setObject(document);
    }

    /**
     * Returns the document file name.
     *
     * @return the file name. May be <tt>null</tt>
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the document mime type.
     *
     * @return the mime type. May be <tt>null</tt>
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Returns the document reference.
     *
     * @return the document reference. May be <tt>null</tt>
     */
    public IMObjectReference getReference() {
        return (IMObjectReference) getProperty().getValue();
    }

    /**
     * Save any edits.
     *
     * @return <tt>true</tt> if the save was successful
     */
    public boolean save() {
        boolean result;
        try {
            refMgr.commit();
            saved = true;
            result = true;
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
            result = false;
        }
        return result;
    }

    /**
     * Determines if any edits have been saved.
     *
     * @return <tt>true</tt> if edits have been saved.
     */
    public boolean isSaved() {
        return saved;
    }

    /**
     * Cancel any edits.
     */
    public void cancel() {
        try {
            refMgr.rollback();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Perform deletion.
     *
     * @return <tt>true</tt> if deletion was successful
     */
    public boolean delete() {
        boolean result;
        try {
            refMgr.delete();
            result = true;
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
            result = false;
        }
        return result;
    }

    /**
     * Invoked when the select button is pressed.
     */
    private void onSelect() {
        UploadListener listener = new DocumentUploadListener() {
            protected void upload(Document document) {
                setDocument(document);
            }
        };
        UploadDialog dialog = new UploadDialog(listener);
        dialog.show();
    }

    /**
     * Initialise the selector and cached document properties, without loading
     * the document content.
     *
     * @param reference the document reference
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void init(IMObjectReference reference) {
        ArchetypeQuery query = new ArchetypeQuery(
                new ObjectRefConstraint("doc", reference));
        query.add(new NodeSelectConstraint("doc.name"));
        query.add(new NodeSelectConstraint("doc.description"));
        query.add(new NodeSelectConstraint("doc.mimeType"));
        Iterator<ObjectSet> iter = new ObjectSetQueryIterator(query);
        if (iter.hasNext()) {
            ObjectSet set = iter.next();
            name = set.getString("doc.name");
            mimeType = set.getString("doc.mimeType");
            String description = set.getString("doc.description");
            selector.setObject(name, description, true);
        }
    }

}
