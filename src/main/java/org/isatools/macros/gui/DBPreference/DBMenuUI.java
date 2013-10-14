package org.isatools.macros.gui.DBPreference;

import org.isatools.isacreator.common.UIHelper;
import org.isatools.macros.exporters.Exporter;
import org.isatools.macros.exporters.html.HTMLExporter;
import org.isatools.macros.graph.graphloader.GraphLoader;
import org.isatools.macros.gui.MacroGraphUI;
import org.isatools.macros.gui.loading_gui.StatusUI;
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


public class DBMenuUI extends JFrame {

    public static final int WIDTH = 400;
    public static final int HEIGHT = 300;

    private JFileChooser fileChooser;

    @InjectedResource
    private ImageIcon dbLogo, createNewDB, createNewDBOver, loadExistingDB, loadExistingDBOver, automacronLogo;
    private Set<Macro> addedMacros;


    public DBMenuUI() {
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

        add(UIHelper.wrapComponentInPanel(new JLabel(dbLogo)), BorderLayout.NORTH);

        JPanel buttonContainer = new JPanel(new BorderLayout());
        buttonContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        buttonContainer.setOpaque(false);

        fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        final JLabel loadExistingDBFromDirectory = new JLabel(loadExistingDB);
        loadExistingDBFromDirectory.addMouseListener(new CursorChangeMouseAdapter());
        loadExistingDBFromDirectory.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                loadExistingDBFromDirectory.setIcon(loadExistingDB);
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                loadExistingDBFromDirectory.setIcon(loadExistingDBOver);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                loadExistingDBFromDirectory.setIcon(loadExistingDB);
                fileChooser.setDialogTitle("Choose Neo4J Graph Directory...");

                openFileChooser();

                if (fileChooser.getSelectedFile() != null) {
                    File graphDirectory = new File(fileChooser.getSelectedFile().getAbsolutePath());
                    setVisible(false);
                    loadApplication(graphDirectory);
                }
            }
        });


        final JLabel createNewDB = new JLabel(this.createNewDB);
        createNewDB.addMouseListener(new CursorChangeMouseAdapter());
        createNewDB.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                createNewDB.setIcon(DBMenuUI.this.createNewDB);
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                createNewDB.setIcon(createNewDBOver);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                createNewDB.setIcon(DBMenuUI.this.createNewDB);
                fileChooser.setDialogTitle("Choose Directory to Create Database...");

                openFileChooser();

                if (fileChooser.getSelectedFile() != null) {

                    File graphDirectory = new File(fileChooser.getSelectedFile().getAbsolutePath() + File.separator + "neo4j-" + System.currentTimeMillis() +"/");
                    graphDirectory.mkdirs();

                    setVisible(false);

                    loadApplication(graphDirectory);
                }

            }
        });

        buttonContainer.add(createNewDB, BorderLayout.WEST);
        buttonContainer.add(loadExistingDBFromDirectory, BorderLayout.EAST);

        add(buttonContainer, BorderLayout.CENTER);

        add(UIHelper.wrapComponentInPanel(new JLabel(automacronLogo)), BorderLayout.SOUTH);
    }

    private void loadApplication(File graphDirectory) {
        GraphLoader graphLoader = new GraphLoader(graphDirectory.getAbsolutePath());
        MacroGraphUI macroGraphUI = new MacroGraphUI(graphLoader);
        macroGraphUI.createGUI();
        AutoMacronApplicationManager.setCurrentApplicationInstance(macroGraphUI);
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