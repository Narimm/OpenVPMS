package org.openvpms.web.workspace.reporting.insurance;

import org.openvpms.archetype.rules.insurance.InsuranceArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.DefaultContextSwitchListener;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.component.workspace.QueryBrowserCRUDWorkspace;

/**
 * Insurance claim workspace.
 *
 * @author Tim Anderson
 */
public class InsuranceWorkspace extends QueryBrowserCRUDWorkspace<Act, Act> {

    /**
     * Constructs an {@link InsuranceWorkspace}.
     *
     * @param context the context
     */
    public InsuranceWorkspace(Context context, MailContext mailContext) {
        super("reporting.insurance", context, false);
        setArchetypes(Archetypes.create(InsuranceArchetypes.CLAIM, Act.class));
        setChildArchetypes(getArchetypes());
        setMailContext(mailContext);
    }

    /**
     * Creates a new query to populate the browser.
     *
     * @return a new query
     */
    @Override
    protected Query<Act> createQuery() {
        return new ClaimQuery(new DefaultLayoutContext(getContext(), getHelpContext()));
    }

    /**
     * /**
     * Creates a new browser.
     *
     * @param query the query
     * @return a new browser
     */
    @Override
    protected Browser<Act> createBrowser(Query<Act> query) {
        DefaultLayoutContext context = new DefaultLayoutContext(getContext(), getHelpContext());
        context.setContextSwitchListener(DefaultContextSwitchListener.INSTANCE);
        return new ClaimBrowser((ClaimQuery) query, context);
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    @Override
    protected CRUDWindow<Act> createCRUDWindow() {
        return new ClaimCRUDWindow(getContext(), getHelpContext(), getBrowser().getQuery());
    }

    /**
     * Returns the browser.
     *
     * @return the browser, or {@code null} if none has been registered
     */
    @Override
    protected ClaimBrowser getBrowser() {
        return (ClaimBrowser) super.getBrowser();
    }

    /**
     * Determines if the workspace should be refreshed.
     *
     * @return {@code true}
     */
    @Override
    protected boolean refreshWorkspace() {
        return true;
    }

    /**
     * Determines if the parent object is optional (i.e may be {@code null},
     * when laying out the workspace.
     * <p>
     * This implementation always returns {@code true}.
     *
     * @return {@code true}
     */
    @Override
    protected boolean isParentOptional() {
        return true;
    }
}

