package org.isatools.macros.gui.motif_analysis_menu;

import org.isatools.isacreator.common.UIHelper;
import org.isatools.macros.gui.common.CursorChangeMouseAdapter;
import org.isatools.macros.manager.AutoMacronApplicationManager;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 03/11/2012
 *         Time: 13:29
 */
public class MotifAnalysisMenuUI extends JFrame {

    public static final int WIDTH = 400;
    public static final int HEIGHT = 300;

    private JFileChooser fileChooser;

    @InjectedResource
    private ImageIcon findMotifsLogo, findAllMotifs, findAllMotifsOver, defineAndFindMotifs,
            defineAndFindMotifsOver, cancel, cancelOver;

    public MotifAnalysisMenuUI() {
        ResourceInjector.get("ui-package.style").inject(this);

        setLayout(new BorderLayout());
        setBackground(UIHelper.BG_COLOR);
        createGUI();
        setAlwaysOnTop(true);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setUndecorated(true);
        pack();

    }

    public void showUI() {
        setLocationRelativeTo(AutoMacronApplicationManager.getCurrentApplicationInstance());
        setVisible(true);
    }

    public void createGUI() {

        add(UIHelper.wrapComponentInPanel(new JLabel(findMotifsLogo)), BorderLayout.NORTH);

        JPanel buttonContainer = new JPanel(new BorderLayout());
        buttonContainer.setBorder(new EmptyBorder(10,10,10,10));
        buttonContainer.setOpaque(false);

        final JLabel analyseAllMotifsButton = new JLabel(findAllMotifs);
        analyseAllMotifsButton.addMouseListener(new CursorChangeMouseAdapter(this));
        analyseAllMotifsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                analyseAllMotifsButton.setIcon(findAllMotifs);
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                analyseAllMotifsButton.setIcon(findAllMotifsOver);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                analyseAllMotifsButton.setIcon(findAllMotifs);
                firePropertyChange("analyseAllMotifs", false, true);
            }
        });
        buttonContainer.add(analyseAllMotifsButton, BorderLayout.WEST);

        final JLabel defineAndFindMotifsButton = new JLabel(defineAndFindMotifs);
        defineAndFindMotifsButton.addMouseListener(new CursorChangeMouseAdapter(this));
        defineAndFindMotifsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                defineAndFindMotifsButton.setIcon(defineAndFindMotifs);
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                defineAndFindMotifsButton.setIcon(defineAndFindMotifsOver);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                defineAndFindMotifsButton.setIcon(defineAndFindMotifs);
                firePropertyChange("defineAndFind", false, true);
            }
        });
        buttonContainer.add(defineAndFindMotifsButton, BorderLayout.EAST);

        add(buttonContainer, BorderLayout.CENTER);

        final JLabel cancelButton = new JLabel(cancel);
        cancelButton.addMouseListener(new CursorChangeMouseAdapter(this));
        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                cancelButton.setIcon(cancel);
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                cancelButton.setIcon(cancelOver);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                cancelButton.setIcon(cancel);
                setVisible(false);
            }
        });

        JPanel cancelButtonContainer  = new JPanel();
        cancelButtonContainer.add(cancelButton, BorderLayout.CENTER);
        add(cancelButtonContainer, BorderLayout.SOUTH);
    }


    public static void main(String[] args) {
        new MotifAnalysisMenuUI().showUI();
    }

}