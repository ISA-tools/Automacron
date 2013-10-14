package org.isatools.macros.gui;

import org.isatools.isacreator.common.UIHelper;
import org.isatools.macros.AutoMacronProperties;
import org.isatools.macros.gui.common.AutoMacronUIHelper;
import org.isatools.macros.motiffinder.Motif;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;

import javax.swing.*;
import java.awt.*;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 18/07/2012
 *         Time: 13:02
 */
public class ExperimentListRenderer extends JPanel implements ListCellRenderer {

    @InjectedResource
    private ImageIcon bioIcon, workingIcon, completeIcon;

    private JLabel workingIndicator, text;

    public ExperimentListRenderer() {
        ResourceInjector.get("ui-package.style").inject(this);
        createGUI();
    }

    private void createGUI() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setOpaque(true);

        workingIndicator = new JLabel(workingIcon);
        workingIndicator.setOpaque(false);


        text = UIHelper.createLabel("", UIHelper.VER_9_PLAIN, AutoMacronUIHelper.GREY_COLOR);

        add(workingIndicator);
        add(Box.createHorizontalStrut(5));
        add(Box.createHorizontalStrut(5));
        add(text);
    }


    public Component getListCellRendererComponent(JList jList, Object value, int index, boolean selected, boolean b1) {

        text.setText(value.toString());
        text.setFont(selected ? UIHelper.VER_9_BOLD : UIHelper.VER_9_PLAIN);

        DBGraph dbGraph = (DBGraph) value;

        text.setForeground(AutoMacronUIHelper.GREY_COLOR);

        if (AutoMacronProperties.getProperty("selected_motif") != null) {
            int selectedMotif = (Integer) AutoMacronProperties.getProperty("selected_motif");
            if (dbGraph.getAssociatedMotifs().contains(selectedMotif)) {
                text.setForeground(AutoMacronUIHelper.DARK_ORANGE_COLOR);
            }
        }

        workingIndicator.setIcon(dbGraph.isUpdating() ? workingIcon : completeIcon);
//        workingIndicator.setIcon(completeIcon);

        return this;
    }
}
