package org.openvpms.web.app;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.ResourceImageReference;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.layout.RowLayoutData;

import org.openvpms.web.component.ButtonFactory;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.RowFactory;
import org.openvpms.web.util.Messages;


/**
 * Title pane.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class TitlePane extends ContentPane {

    /**
     * The project logo.
     */
    private final String PATH = "/org/openvpms/web/resource/image/openvpms.png";

    /**
     * The style name.
     */
    private static final String STYLE = "TitlePane";


    /**
     * Construct a new <code>TitlePane</code>.
     */
    public TitlePane() {
        doLayout();
    }

    /**
     * Lay out the component.
     */
    protected void doLayout() {
        setStyleName(STYLE);

        Label logo = LabelFactory.create(new ResourceImageReference(PATH));

        Label label = LabelFactory.create();
        label.setText(Messages.get("label.welcome", "<foo>"));
        Button logout = ButtonFactory.create("logout", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                OpenVPMSApp.getInstance().logout();
            }
        });

        Row logoutRow = RowFactory.create(label, logout);
        RowLayoutData right = new RowLayoutData();
        right.setAlignment(new Alignment(Alignment.RIGHT, Alignment.DEFAULT));
        right.setWidth(new Extent(100, Extent.PERCENT));
        logoutRow.setLayoutData(right);

        Row layout = RowFactory.create(logo, logoutRow);
        add(layout);
    }
}
