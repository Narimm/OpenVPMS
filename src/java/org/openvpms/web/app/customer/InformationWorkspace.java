package org.openvpms.web.app.customer;

import nextapp.echo2.app.Component;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.subsystem.CRUDWorkspace;
import org.openvpms.web.component.app.Context;


/**
 * Customer information workspace.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class InformationWorkspace extends CRUDWorkspace {

    /**
     * Construct a new <code>InformationWorkspace</code>.
     */
    public InformationWorkspace() {
        super("customer", "info", "party", "party", "customer*");
    }

    /**
     * Renders the workspace summary.
     *
     * @return the component representing the workspace summary, or
     *         <code>null</code> if there is no summary
     */
    @Override
    public Component getSummary() {
        return CustomerSummary.getSummary((Party) getObject());
    }

    /**
     * Determines if the workspace should be refreshed. This implementation
     * returns true if the current customer has changed.
     *
     * @return <code>true</code> if the workspace should be refreshed, otherwise
     *         <code>false</code>
     */
    @Override
    protected boolean refreshWorkspace() {
        Party customer = Context.getInstance().getCustomer();
        return (customer != getObject());
    }

    /**
     * Invoked when an object is selected.
     *
     * @param object the selected object
     */
    @Override
    protected void onSelected(IMObject object) {
        super.onSelected(object);
        Context.getInstance().setCustomer((Party) object);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(IMObject object, boolean isNew) {
        super.onSaved(object, isNew);
        Context.getInstance().setCustomer((Party) object);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    @Override
    protected void onDeleted(IMObject object) {
        super.onDeleted(object);
        Context.getInstance().setCustomer(null);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }
}
