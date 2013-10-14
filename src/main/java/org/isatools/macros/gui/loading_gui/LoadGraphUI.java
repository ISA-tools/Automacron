package org.isatools.macros.gui.loading_gui;

import org.isatools.isacreator.common.UIHelper;
import org.isatools.macros.manager.AutoMacronApplicationManager;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 02/10/2012
 *         Time: 10:19
 */
public class LoadGraphUI extends JFrame {

    public static final int WIDTH = 400;
    public static final int HEIGHT = 300;

    private JFileChooser fileChooser;

    @InjectedResource
    private ImageIcon autoMacronLoadLogo, loadFromDirectory, loadFromDirectoryOver, loadSingleGraph,
            loadSingleGraphOver, cancel, cancelOver;

    public LoadGraphUI() {
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

        add(UIHelper.wrapComponentInPanel(new JLabel(autoMacronLoadLogo)), BorderLayout.NORTH);

        JPanel buttonContainer = new JPanel(new BorderLayout());
        buttonContainer.setBorder(new EmptyBorder(10,10,10,10));
        buttonContainer.setOpaque(false);

        fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        final JLabel loadFromDirectoryButton = new JLabel(loadFromDirectory);
        loadFromDirectoryButton.addMouseListener(new CursorChangeMouseAdapter());
        loadFromDirectoryButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                loadFromDirectoryButton.setIcon(loadFromDirectory);
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                loadFromDirectoryButton.setIcon(loadFromDirectoryOver);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                loadFromDirectoryButton.setIcon(loadFromDirectory);
                fileChooser.setDialogTitle("Load From Directory");
                openFileChooser();
            }
        });
        buttonContainer.add(loadFromDirectoryButton, BorderLayout.WEST);

        final JLabel loadFromFileButton = new JLabel(loadSingleGraph);
        loadFromFileButton.addMouseListener(new CursorChangeMouseAdapter());
        loadFromFileButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                loadFromFileButton.setIcon(loadSingleGraph);
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                loadFromFileButton.setIcon(loadSingleGraphOver);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                loadFromFileButton.setIcon(loadSingleGraph);
                fileChooser.setDialogTitle("Load From File");
                
                // show file chooser UI.
                openFileChooser();
            }
        });
        buttonContainer.add(loadFromFileButton, BorderLayout.EAST);

        add(buttonContainer, BorderLayout.CENTER);

        final JLabel cancelButton = new JLabel(cancel);
        cancelButton.addMouseListener(new CursorChangeMouseAdapter());
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

    private void openFileChooser() {
        fileChooser.showOpenDialog(this);

        if(fileChooser.getSelectedFile() != null) {
            firePropertyChange("fileSelected", null, fileChooser.getSelectedFile());
        }
    }

    class CursorChangeMouseAdapter extends MouseAdapter {
        @Override
        public void mouseEntered(MouseEvent mouseEvent) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public void mouseExited(MouseEvent mouseEvent) {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
}
