package org.openvpms.web.app;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.ResourceImageReference;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.layout.RowLayoutData;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.web.component.ButtonFactory;
import org.openvpms.web.component.ComponentFactory;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.RowFactory;

/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class TitlePane extends ContentPane {

    /**
     * The title image resource path.
     */
    private final String PATH
            = "/org/openvpms/web/resource/image/openvpms.gif";

    public void init() {
        doLayout();
    }

    protected void doLayout() {
        ComponentFactory.setDefaults(this);
        Label title = LabelFactory.create(new ResourceImageReference(PATH));
        Label label = LabelFactory.create("Welcome <foo>");
        Button logout = ButtonFactory.create("logout");
        logout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                OpenVPMSApp.getInstance().logout();
            }
        });

        Row logoutRow = RowFactory.create(label, logout);
        RowLayoutData layout = new RowLayoutData();
        layout.setAlignment(new Alignment(Alignment.RIGHT, Alignment.DEFAULT));
        layout.setWidth(new Extent(100, Extent.PERCENT));
        logoutRow.setLayoutData(layout);
        Row wrapper = RowFactory.create(title, logoutRow);

        add(wrapper);
    }
}
