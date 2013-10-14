package org.isatools.macros.gui.export_gui;

import org.isatools.isacreator.common.UIHelper;
import org.isatools.macros.exporters.Exporter;
import org.isatools.macros.exporters.html.HTMLExporter;
import org.isatools.macros.gui.macro.Macro;
import org.isatools.macros.macrofile.exporter.MacroFileExporter;
import org.isatools.macros.manager.AutoMacronApplicationManager;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;


public class ExportResultsUI extends JFrame {

    public static final int WIDTH = 400;
    public static final int HEIGHT = 300;

    private JFileChooser fileChooser;

    @InjectedResource
    private ImageIcon exportLogo, exportHTML, exportHTMLOver, exportXML, exportXMLOver, cancel, cancelOver;
    private Set<Macro> addedMacros;

    public ExportResultsUI(Set<Macro> addedMacros) {
        ResourceInjector.get("ui-package.style").inject(this);

        this.addedMacros = addedMacros;

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

        add(UIHelper.wrapComponentInPanel(new JLabel(exportLogo)), BorderLayout.NORTH);

        JPanel buttonContainer = new JPanel(new BorderLayout());
        buttonContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        buttonContainer.setOpaque(false);

        fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        final JLabel loadFromDirectoryButton = new JLabel(exportXML);
        loadFromDirectoryButton.addMouseListener(new CursorChangeMouseAdapter());
        loadFromDirectoryButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                loadFromDirectoryButton.setIcon(exportXML);
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                loadFromDirectoryButton.setIcon(exportXMLOver);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                loadFromDirectoryButton.setIcon(exportXML);
                fileChooser.setDialogTitle("Choose Export Directory...");
                openFileChooser();

                if (fileChooser.getSelectedFile() != null) {
                    MacroFileExporter exporter = new MacroFileExporter();
                    try {
                        File resultsDirectory = new File(fileChooser.getSelectedFile().getAbsolutePath() + File.separator + "automacron-xml");
                        resultsDirectory.mkdirs();
                        exporter.exportMacros(new File(resultsDirectory.getAbsolutePath() + File.separator + "automacron-output.xml"), addedMacros);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        buttonContainer.add(loadFromDirectoryButton, BorderLayout.WEST);

        final JLabel loadFromFileButton = new JLabel(exportHTML);
        loadFromFileButton.addMouseListener(new CursorChangeMouseAdapter());
        loadFromFileButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                loadFromFileButton.setIcon(exportHTML);
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                loadFromFileButton.setIcon(exportHTMLOver);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                loadFromFileButton.setIcon(exportHTML);
                fileChooser.setDialogTitle("Choose Export Directory...");

                openFileChooser();

                if (fileChooser.getSelectedFile() != null) {
                    Exporter exporter = new HTMLExporter();

                    File resultsDirectory = new File(fileChooser.getSelectedFile().getAbsolutePath() + File.separator + "automacron-html/");
                    resultsDirectory.mkdirs();

                    exporter.export(addedMacros, resultsDirectory);

                }

                // show file chooser UI.
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

        JPanel cancelButtonContainer = new JPanel();
        cancelButtonContainer.add(cancelButton, BorderLayout.CENTER);
        add(cancelButtonContainer, BorderLayout.SOUTH);
    }

    private void openFileChooser() {
        fileChooser.showOpenDialog(this);

        if (fileChooser.getSelectedFile() != null) {
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