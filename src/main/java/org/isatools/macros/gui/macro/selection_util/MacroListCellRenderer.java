package org.isatools.macros.gui.macro.selection_util;

import org.isatools.isacreator.common.UIHelper;
import org.isatools.macros.AutoMacronProperties;
import org.isatools.macros.gui.common.AutoMacronUIHelper;
import org.isatools.macros.gui.macro.MacroUI;
import org.isatools.macros.gui.macro.renderer.MotifGraphRenderer;
import org.isatools.macros.gui.macro.renderer.RenderingFactory;
import org.isatools.macros.gui.macro.renderer.RenderingType;
import org.isatools.macros.motiffinder.Motif;
import org.isatools.macros.utils.MotifStats;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.io.File;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 05/12/2012
 *         Time: 08:59
 */
public class MacroListCellRenderer extends JPanel implements ListCellRenderer {

    static MatteBorder border = new MatteBorder(0, 0, 2, 0, AutoMacronUIHelper.LIGHT_GREY_COLOR);
    static MatteBorder border_selected = new MatteBorder(0, 0, 2, 0, AutoMacronUIHelper.DARK_ORANGE_COLOR);

    @InjectedResource
    private ImageIcon selected, demoted;

    private JLabel score, macro, frequency, workflowFrequency, motifPower, selectedIndicator, demotedIndicator;
    private Box frequencyContainer;
    private Box workflowPercentageContainer;
    private Box motifPowerContainer;

    public MacroListCellRenderer() {

        ResourceInjector.get("ui-package.style").inject(this);
        createGUI();
    }

    private void createGUI() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setOpaque(true);

        score = UIHelper.createLabel("", UIHelper.VER_10_BOLD, AutoMacronUIHelper.DARK_ORANGE_COLOR, JLabel.CENTER);
        score.setVerticalAlignment(JLabel.CENTER);
        score.setAlignmentY(JLabel.CENTER_ALIGNMENT);

        macro = new JLabel();
        macro.setVerticalAlignment(SwingConstants.CENTER);

        frequency = UIHelper.createLabel("", UIHelper.VER_10_PLAIN, AutoMacronUIHelper.GREY_COLOR, JLabel.CENTER);
        workflowFrequency = UIHelper.createLabel("", UIHelper.VER_10_PLAIN, AutoMacronUIHelper.GREY_COLOR, JLabel.CENTER);
        motifPower = UIHelper.createLabel("", UIHelper.VER_10_PLAIN, AutoMacronUIHelper.GREY_COLOR, JLabel.CENTER);

        selectedIndicator = new JLabel(selected);
        selectedIndicator.setVisible(false);

        demotedIndicator = new JLabel(demoted);
        demotedIndicator.setVisible(false);

        Box scorePanel = Box.createVerticalBox();

        Box glyphContainer = Box.createHorizontalBox();

        glyphContainer.add(Box.createHorizontalStrut(10));
        glyphContainer.add(selectedIndicator);
        glyphContainer.add(Box.createHorizontalStrut(5));
        glyphContainer.add(demotedIndicator);

        scorePanel.add(glyphContainer);
        scorePanel.add(Box.createVerticalStrut(5));
        scorePanel.add(score);
        scorePanel.setPreferredSize(new Dimension(30, 60));
        add(scorePanel);

        add(Box.createHorizontalStrut(5));

        JPanel macroPanel = new JPanel(new BorderLayout());
        macroPanel.setPreferredSize(new Dimension(60, 60));
        macroPanel.add(macro);

        add(macroPanel);

        add(Box.createHorizontalStrut(5));

        frequencyContainer = Box.createHorizontalBox();
        frequencyContainer.add(frequency);

        add(Box.createHorizontalStrut(5));
        add(frequencyContainer);

        workflowPercentageContainer = Box.createHorizontalBox();
        workflowPercentageContainer.add(workflowFrequency);

        add(Box.createHorizontalStrut(5));
        add(workflowPercentageContainer);

        motifPowerContainer = Box.createHorizontalBox();
        motifPowerContainer.add(motifPower);

        add(Box.createHorizontalStrut(5));
        add(motifPowerContainer);

        setBorder(border);
    }

    public Component getListCellRendererComponent(JList jList, Object value, int indexValue, boolean selected, boolean cellHashFocus) {

        MacroUI macroValue = (MacroUI) value;

        Motif motif = macroValue.getMacro().getMotif();

        selectedIndicator.setVisible(macroValue.isSelected());
        demotedIndicator.setVisible(macroValue.getMacro().getPenalty() > 0);

        ImageIcon icon = AutoMacronUIHelper.scaleImageIcon(macroValue.getMacro().getGlyph(RenderingType.FULL).getAbsolutePath(), 60, 60);
        macro.setIcon(icon);

        setBorder(selected ? border_selected : border);

        // rough colour guidance for now.
        double motifScore = motif.getScore();
        if(macroValue.getMacro().getPenalty() > 0) {
            score.setText("<html><strike>" + String.format("%.2f ", motifScore) + "</strike><br/><strong> " + String.format("%.2f ", (motifScore -(motifScore * Math.min(1,macroValue.getMacro().getPenalty())))) + "  </strong></html>");
        } else {
            score.setText("<html><strong>" + String.format("%.2f ", motifScore) + "</strong><br/></html>");
        }

        Color scoreColor = motifScore < 0 ? AutoMacronUIHelper.DARK_BLUE_COLOR : AutoMacronUIHelper.DARK_ORANGE_COLOR;
        score.setForeground(scoreColor);

        frequency.setText("<html> # <strong> " + motif.getCumulativeUsage() + " </strong></html>");
        Color frequencyColor = motif.getCumulativeUsage() < MotifStats.getMeanUsage() ? AutoMacronUIHelper.DARK_BLUE_COLOR : AutoMacronUIHelper.DARK_ORANGE_COLOR;
        frequencyContainer.setBorder(new MatteBorder(0, 0, 2, 0, frequencyColor));

        workflowFrequency.setText("<html> w <strong> " + motif.getWorkflowOccurrence() + " </strong></html>");
        Color workflowFrequencyColor = motif.getWorkflowOccurrence() < MotifStats.getMeanWorkflowAppearance() ? AutoMacronUIHelper.DARK_BLUE_COLOR : AutoMacronUIHelper.DARK_ORANGE_COLOR;
        workflowPercentageContainer.setBorder(new MatteBorder(0, 0, 2, 0, workflowFrequencyColor));

        motifPower.setText("<html> msp <strong> " + motif.getTotalNodesInvolved() + " </strong></html>");
        Color motifPowerColor = motif.getTotalNodesInvolved() < MotifStats.getMeanMSP() ? AutoMacronUIHelper.DARK_BLUE_COLOR : AutoMacronUIHelper.DARK_ORANGE_COLOR;
        motifPowerContainer.setBorder(new MatteBorder(0, 0, 2, 0, motifPowerColor));

        return this;
    }

}
