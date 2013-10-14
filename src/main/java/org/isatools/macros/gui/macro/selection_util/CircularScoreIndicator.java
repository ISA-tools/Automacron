package org.isatools.macros.gui.macro.selection_util;

import org.isatools.errorreporter.ui.utils.UIHelper;
import org.isatools.macros.gui.common.AutoMacronUIHelper;
import org.isatools.macros.motiffinder.Motif;
import org.isatools.macros.utils.MotifStatCalculator;
import org.isatools.macros.utils.MotifStats;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;


public class CircularScoreIndicator extends JPanel {

    private Motif motif;

    public CircularScoreIndicator(Motif motif) {
        this.motif = motif;
        setBackground(UIHelper.BG_COLOR);
        setPreferredSize(new Dimension(110, 110));
    }

    @Override
    public void paint(Graphics graphics) {

        Graphics2D g2 = (Graphics2D) graphics;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        BasicStroke stroke = new BasicStroke(3.0f);
        g2.setStroke(stroke);


        // scale is between 0 and 270. add this to 90 to get the full extent
        g2.setColor(UIHelper.GREY_COLOR);
        g2.setFont(UIHelper.VER_8_PLAIN);

        g2.drawString("Motifs", 60, 10);

        double extent = 310 * (motif.getCumulativeUsage() / MotifStats.getMaxUsage());
        Color frequencyColor = motif.getCumulativeUsage() < MotifStats.getMeanUsage() ? AutoMacronUIHelper.DARK_BLUE_COLOR : AutoMacronUIHelper.DARK_ORANGE_COLOR;
        // draw track
        g2.setColor(AutoMacronUIHelper.LIGHT_GREY_COLOR);
        g2.draw(new Arc2D.Double(5, 5,
                100,
                100,
                90, 310,
                Arc2D.OPEN));

        // draw value
        g2.setColor(frequencyColor);
        g2.draw(new Arc2D.Double(5, 5,
                100,
                100,
                90, extent,
                Arc2D.OPEN));

        g2.setColor(UIHelper.GREY_COLOR);
        g2.drawString("Workflows", 60, 20);

        // draw track
        Color workflowFrequencyColor = motif.getWorkflowOccurrence() < MotifStats.getTotalWorkflows() ? AutoMacronUIHelper.DARK_BLUE_COLOR : AutoMacronUIHelper.DARK_ORANGE_COLOR;
        g2.setColor(AutoMacronUIHelper.LIGHT_GREY_COLOR);
        g2.draw(new Arc2D.Double(15, 15,
                80,
                80,
                90, 310,
                Arc2D.OPEN));

        // draw value
        double workflowExtent = 310 * ((double) motif.getWorkflowOccurrence() / MotifStats.getTotalWorkflows());
        g2.setColor(workflowFrequencyColor);
        g2.draw(new Arc2D.Double(15, 15,
                80,
                80,
                90, workflowExtent,
                Arc2D.OPEN));

        g2.setColor(UIHelper.GREY_COLOR);
        g2.drawString("MSP", 60, 30);

        // draw track
        g2.setColor(AutoMacronUIHelper.LIGHT_GREY_COLOR);
        g2.draw(new Arc2D.Double(25, 25,
                60,
                60,
                90, 310,
                Arc2D.OPEN));

        // draw value


        double mspForMotif = MotifStatCalculator.calculateMotifMSP(motif);
        double maxMSP = MotifStats.getMaxMSP();
        double mspExtent = 310 * (mspForMotif / maxMSP);


        Color mspColor = mspForMotif < MotifStats.getMeanMSP() ? AutoMacronUIHelper.DARK_BLUE_COLOR : AutoMacronUIHelper.DARK_ORANGE_COLOR;
        g2.setColor(mspColor);
        g2.draw(new Arc2D.Double(25, 25,
                60,
                60,
                90, mspExtent,
                Arc2D.OPEN));

        g2.setColor(AutoMacronUIHelper.GREY_COLOR);
        g2.setStroke(new BasicStroke(1.5f));
    }

}
