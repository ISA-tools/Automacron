package org.isatools.macros.gui;

import org.isatools.isacreator.common.UIHelper;
import org.isatools.isacreator.effects.FooterPanel;
import org.isatools.macros.AutoMacronProperties;
import org.isatools.macros.graph.graphloader.GraphFunctions;
import org.isatools.macros.graph.graphloader.GraphLoader;
import org.isatools.macros.gui.export_gui.ExportResultsUI;
import org.isatools.macros.gui.loading_gui.LoadGraphUI;
import org.isatools.macros.gui.loading_gui.StatusUI;
import org.isatools.macros.gui.macro.*;
import org.isatools.macros.gui.macro.selection_util.MacroSelectionUtilUI;
import org.isatools.macros.gui.motif_analysis_menu.MotifAnalysisMenuUI;
import org.isatools.macros.gui.motifdrawer.MotifDrawer;
import org.isatools.macros.gui.titlebar.AutoMacronTitleBar;
import org.isatools.macros.io.graphml.compression.CompressedGraphMLCreator;
import org.isatools.macros.loaders.isa.BatchISAWorkflowLoader;
import org.isatools.macros.motiffinder.Motif;
import org.isatools.macros.motiffinder.ThreadedMotifFinderImpl;
import org.isatools.macros.motiffinder.listeningutils.MotifFinderObserver;
import org.isatools.macros.plugin.workflowvisualization.NodeDetail;
import org.isatools.macros.plugin.workflowvisualization.graph.GraphView;
import org.isatools.macros.utils.MotifProcessingUtils;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;
import prefuse.Constants;
import prefuse.controls.ControlAdapter;
import prefuse.data.Graph;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.VisualItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 07/06/2012
 *         Time: 11:40
 */
public class MacroGraphUI extends JFrame implements MotifFinderObserver {


    private static final int WIDTH = 1000;
    private static final int HEIGHT = 800;
    public static final int DEFAULT_MOTIF_DEPTH = 8;

    private GraphLoader graphLoader;
    private LoadGraphUI loadGraphUI;
    private GraphView view;

    private LightMacroDetailViewer lightMacroDetailViewer;

    @InjectedResource
    private ImageIcon logo, sorry, viewOriginal, viewOriginalOver, viewCompressed,
            viewCompressedOver, viewSwatchBoard, viewSwatchBoardOver, exportMotifs, exportMotifsOver, resizeIcon;

    private Set<VisualItem> itemsToHighlight;
    private List<DBGraph> dbGraphs;
    private Set<Macro> macros;
    private Map<String, Motif> motifs;

    private File currentGraphFileInView;

    private ExperimentsPane experimentsPane;
    private SwatchBoard swatches;
    private CentralPanel graphPanel;
    private StatusUI loadingIndicator;
    private Box buttonContainer;
    private MotifAnalysisMenuUI analyseMenuUI;
    private MotifDrawer motifDrawer;
    private JLabel viewSwatchBoardButton;
    private JLabel exportSelectMotifsButton;

    public MacroGraphUI(GraphLoader graph) {
        this.graphLoader = graph;
        ResourceInjector.get("ui-package.style").inject(this);
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        motifs = new HashMap<String, Motif>();
        macros = new HashSet<Macro>();

        dbGraphs = GraphFunctions.loadExperiments(this.graphLoader.getNeo4JConnector().getGraphDB());
    }

    public void setMacros(Set<Macro> macros) {
        this.macros = macros;
    }

    public void createGUI() {
        loadGraphUI = new LoadGraphUI();
        analyseMenuUI = new MotifAnalysisMenuUI();
        motifDrawer = new MotifDrawer();

        createTopPanel();
        createDatabaseContentAndMotifPresencePane();
        createCentralPanel();

        FooterPanel footerPanel = new FooterPanel(this, UIHelper.BG_COLOR, resizeIcon);
        add(footerPanel, BorderLayout.SOUTH);

        loadingIndicator = new StatusUI();

        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(WIDTH, HEIGHT));
        setVisible(true);
    }

    private void createCentralPanel() {

        JPanel centralPanel = new JPanel(new BorderLayout());

        buttonContainer = Box.createHorizontalBox();
        buttonContainer.setOpaque(false);
        final JLabel viewOriginalButton = new JLabel(viewOriginal);
        viewOriginalButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                viewOriginalButton.setIcon(viewOriginalOver);
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                viewOriginalButton.setIcon(viewOriginal);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                viewOriginalButton.setIcon(viewOriginal);
                viewOriginalGraph();
            }
        });
        buttonContainer.add(viewOriginalButton);

        final JLabel viewCompressedButton = new JLabel(viewCompressed);
        viewCompressedButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                viewCompressedButton.setIcon(viewCompressedOver);
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                viewCompressedButton.setIcon(viewCompressed);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                viewCompressedButton.setIcon(viewCompressed);
                viewCompressedGraph();
            }
        });
        buttonContainer.add(viewCompressedButton);

        buttonContainer.setVisible(false);

        JPanel southCentralPanelContainer = new JPanel(new BorderLayout());
        southCentralPanelContainer.add(buttonContainer, BorderLayout.EAST);

        centralPanel.add(southCentralPanelContainer, BorderLayout.SOUTH);
        // add view compressed, view uncompressed buttons.

        graphPanel = new CentralPanel();

        JPanel additionalButtonContainer = new JPanel(new BorderLayout());
        additionalButtonContainer.setBackground(UIHelper.BG_COLOR);

        exportSelectMotifsButton = new JLabel(exportMotifs);

        exportSelectMotifsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                exportSelectMotifsButton.setIcon(exportMotifsOver);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                exportSelectMotifsButton.setIcon(exportMotifs);
                setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                exportSelectMotifsButton.setIcon(exportMotifs);

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        ExportResultsUI exportResults = new ExportResultsUI(swatches.getAddedMacros());
                        exportResults.createGUI();
                        exportResults.showUI();
                    }
                });

            }
        });

        additionalButtonContainer.add(exportSelectMotifsButton, BorderLayout.EAST);

        centralPanel.add(additionalButtonContainer, BorderLayout.NORTH);
        centralPanel.add(graphPanel, BorderLayout.CENTER);
        add(centralPanel, BorderLayout.CENTER);
    }

    private void createDatabaseContentAndMotifPresencePane() {
        experimentsPane = new ExperimentsPane();
        experimentsPane.createGUI();

        // add option to view compressed representation. Stick in thread and
        // show loading panel.

        dbGraphs = GraphFunctions.loadExperiments(graphLoader.getNeo4JConnector().getGraphDB());
        experimentsPane.loadExperiments(dbGraphs);


        experimentsPane.addPropertyChangeListener("analyse", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                analyseMenuUI.showUI();
                analyseMenuUI.addPropertyChangeListener("analyseAllMotifs", new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                        if (((JFrame) propertyChangeEvent.getSource()).isShowing()) {

                            analyseMenuUI.setVisible(false);

                            Thread analysisThread = new Thread(new Runnable() {
                                public void run() {
//                                    SwingUtilities.invokeLater(new Runnable() {
//                                        public void run() {
//                                            loadingIndicator.showUI(StatusUI.WORKING);
//                                            loadingIndicator.setVisible(true);
//                                        }
//                                    });


                                    ThreadedMotifFinderImpl motifFinder = new ThreadedMotifFinderImpl(DEFAULT_MOTIF_DEPTH);

                                    motifs.clear();

                                    motifFinder.setMotifs(motifs);
                                    motifFinder.registerObserver(MacroGraphUI.this);

                                    List<DBGraph> dbGraphSubset = new ArrayList<DBGraph>();

                                    for (DBGraph dbGraph : dbGraphs) {
                                        dbGraphSubset.add(dbGraph);
                                        dbGraph.setUpdating(true);
                                    }
                                    motifFinder.performAnalysis(dbGraphSubset);

//                                    SwingUtilities.invokeLater(new Runnable() {
//                                        public void run() {
//                                            loadingIndicator.setVisible(false);
//                                        }
//                                    });
                                }
                            });

                            analysisThread.start();

                        }
                    }
                });

                analyseMenuUI.addPropertyChangeListener("defineAndFind", new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                        analyseMenuUI.setVisible(false);
                        motifDrawer.showUI(MacroGraphUI.this);
                    }
                });
            }
        });

        experimentsPane.addPropertyChangeListener("itemSelected", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                // we'll load the graph for the selected item into the graph panel and view it.
                createGraphPanel(new File(ThreadedMotifFinderImpl.PROGRAM_DATA_GRAPHML + propertyChangeEvent.getNewValue().toString() + ".xml"));
            }
        });

        experimentsPane.addPropertyChangeListener("unload", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                DBGraph experiment = (DBGraph) propertyChangeEvent.getNewValue();
                dbGraphs.remove(experiment);
                GraphFunctions.deleteExperiment(graphLoader.getNeo4JConnector().getGraphDB(), experiment);
                System.out.println("All traces removed of experiment...");

            }
        });

        experimentsPane.addPropertyChangeListener("load", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                loadGraphUI.showUI();
                loadGraphUI.addPropertyChangeListener("fileSelected", new PropertyChangeListener() {
                    public void propertyChange(final PropertyChangeEvent propertyChangeEvent) {
                        if (propertyChangeEvent.getNewValue() instanceof File) {

                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    loadingIndicator.showUI(StatusUI.LOADING);
                                    loadGraphUI.setVisible(false);
                                }
                            });

                            Thread loadingThread = new Thread(new Runnable() {
                                public void run() {

                                    File toLoad = (File) propertyChangeEvent.getNewValue();

                                    graphLoader.loadGraph(new BatchISAWorkflowLoader(graphLoader.getNeo4JConnector().getGraphDB()), toLoad);

                                    dbGraphs = GraphFunctions.loadExperiments(graphLoader.getNeo4JConnector().getGraphDB());

                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            experimentsPane.loadExperiments(dbGraphs);

                                            loadingIndicator.setVisible(false);
                                            loadGraphUI.setVisible(true);
                                        }
                                    });
                                }
                            });

                            loadingThread.start();

                        }
                    }
                });
            }
        });

        JPanel westPanel = new JPanel(new BorderLayout());
        westPanel.add(experimentsPane, BorderLayout.CENTER);
        westPanel.add(createSwatchPanel(), BorderLayout.SOUTH);
        add(westPanel, BorderLayout.WEST);
    }

    private void viewCompressedGraph() {
        // check for existence of compressed graph xml before doing anything else. 
        Thread compressGraphThread = new Thread(new Runnable() {
            public void run() {
                loadingIndicator.showUI(StatusUI.COMPRESSING);
                CompressedGraphMLCreator compressedGraphMLCreator = new CompressedGraphMLCreator(swatches.getAddedMacros());
                File compressedFile = compressedGraphMLCreator.compressFile(currentGraphFileInView);

                System.out.println(compressedFile);
                if (compressedFile != null && compressedFile.exists()) {
                    createGraphPanel(compressedFile);
                } else {
                    currentGraphFileInView = null;
                    graphPanel.switchView(UIHelper.wrapComponentInPanel(new JLabel(sorry)));
                }
                loadingIndicator.setVisible(false);
            }
        });

        compressGraphThread.start();

    }

    private void viewOriginalGraph() {
        if (currentGraphFileInView.getName().contains("compressed")) {
            File uncompressedFile = CompressedGraphMLCreator.getUnCompressedFile(currentGraphFileInView);
            System.out.println(uncompressedFile.getAbsolutePath());
            createGraphPanel(uncompressedFile);
        }
    }


    private void createGraphPanel(File graphMLFile) {
        String datafile = graphMLFile.getAbsolutePath();
        currentGraphFileInView = graphMLFile;

        try {
            final Graph g = new GraphMLReader().readGraph(datafile);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    String label = "image";
                    view = new GraphView(g, label, Constants.ORIENT_TOP_BOTTOM, new Dimension(600, 500));
                    view.getDisplay().addControlListener(new ControlAdapter() {

                        @Override
                        public void itemClicked(VisualItem visualItem, MouseEvent mouseEvent) {


                            if (visualItem.canGetString("type")) {
                                System.out.println(visualItem.get("type"));
                                if (visualItem.get("type").equals("macro")) {
                                    if (lightMacroDetailViewer == null) {
                                        lightMacroDetailViewer = new LightMacroDetailViewer();
                                        lightMacroDetailViewer.createGUI();
                                    }

                                    File fullImage = new File(visualItem.get("image@F").toString());
                                    File abstractImage = new File(visualItem.get("image@L").toString());
                                    File mediumImage = new File(visualItem.get("image@M").toString());
                                    File detailedImage = new File(visualItem.get("image@S").toString());

                                    lightMacroDetailViewer.updateImages(fullImage, abstractImage, mediumImage, detailedImage);

                                    if(!lightMacroDetailViewer.isShowing()){
                                        lightMacroDetailViewer.setVisible(true);
                                    }
                                }
                            }
                        }

                        @Override
                        public void itemEntered(VisualItem visualItem, MouseEvent mouseEvent) {
                            if (visualItem.canGetString("id")) {
                                TupleSet focused = view.getDisplay().getVisualization().getFocusGroup("highlighted");
                                focused.addTuple(visualItem);
                                view.getDisplay().getVisualization().run("draw");
                            }
                        }

                        @Override
                        public void itemExited(VisualItem visualItem, MouseEvent mouseEvent) {
                            if (visualItem.canGetString("id")) {
                                TupleSet focused = view.getDisplay().getVisualization().getFocusGroup("highlighted");
                                focused.removeTuple(visualItem);
                                view.getDisplay().getVisualization().run("draw");
                            }
                        }
                    });
                    graphPanel.switchView(view);
                    buttonContainer.setVisible(true);
                    // add compress button to view.
                }
            });

        } catch (DataIOException e) {
            e.printStackTrace();
            System.err.println("Graph for " + graphMLFile.getName() + " does not exist.");
            buttonContainer.setVisible(false);
            currentGraphFileInView = null;
            graphPanel.switchView(UIHelper.wrapComponentInPanel(new JLabel(sorry)));
        }
    }

    private Set<VisualItem> getVisualItemForNode(Set<String> nodeIds) {
        Set<VisualItem> visualItems = new HashSet<VisualItem>();

        Iterator itemsInVisualization = view.getDisplay().getVisualization().items();

        while (itemsInVisualization.hasNext()) {
            Object item = itemsInVisualization.next();
            if (item instanceof VisualItem) {
                if (((VisualItem) item).canGetString("id") && nodeIds.contains(((VisualItem) item).get("id").toString())) {
                    visualItems.add((VisualItem) item);
                }
            }
        }

        return visualItems;
    }

    private Container createSwatchPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setPreferredSize(new Dimension(253, 220));
        swatches = new SwatchBoard();
        swatches.setVisible(false);
        swatches.createGUI();

        swatches.addPropertyChangeListener("swatchClicked", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (propertyChangeEvent.getNewValue() instanceof MacroUI) {
                    MacroUI selectedMacro = (MacroUI) propertyChangeEvent.getNewValue();

                    int hash = MotifProcessingUtils.findAndCollapseMergeEvents(selectedMacro.getMacro().toString()).hashCode();
                    AutoMacronProperties.setProperty("selected_motif", hash);
                    experimentsPane.updateUI();
                    highlightNodesInGlyph(selectedMacro);
                }
            }
        });

        swatches.addPropertyChangeListener("swatchHovered", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (propertyChangeEvent.getNewValue() instanceof MacroUI) {
                    MacroUI selectedMacro = (MacroUI) propertyChangeEvent.getNewValue();
                    highlightNodesInGlyph(selectedMacro);
                }
            }
        });

        swatches.addPropertyChangeListener("swatchExited", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                unHighlightNodes();
            }
        });


        container.add(swatches, BorderLayout.CENTER);

        viewSwatchBoardButton = new JLabel(viewSwatchBoard);

        viewSwatchBoardButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                viewSwatchBoardButton.setIcon(viewSwatchBoardOver);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                viewSwatchBoardButton.setIcon(viewSwatchBoard);
                setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                viewSwatchBoardButton.setIcon(viewSwatchBoard);

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        loadingIndicator.showUI(StatusUI.WORKING);
                    }
                });

                Thread doMacroCreation = new Thread(new Runnable() {
                    public void run() {
                        MacroSelectionUtilUI swatchBoard = new MacroSelectionUtilUI(1, DEFAULT_MOTIF_DEPTH);
                        swatchBoard.createGUI();
                        swatchBoard.addPropertyChangeListener("motifsUpdated", new PropertyChangeListener() {
                            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                                if (propertyChangeEvent.getNewValue() instanceof Set) {
                                    swatches.clearSwatches();
                                    Set<MacroUI> motifs = (Set<MacroUI>) propertyChangeEvent.getNewValue();
                                    addGlyphToSwatchBoard(motifs);
                                    repaint();
                                    swatches.updateUI();
                                }
                            }
                        });

                        Map<Integer, List<MacroUI>> partitionedMacros = new HashMap<Integer, List<MacroUI>>();
                        for (Macro macro : macros) {
                            if (macro != null) {
                                int depth = MotifProcessingUtils.getNumberOfGroupsInMotifString(macro.getMotif().getStringRepresentation());
                                macro.getMotif().setDepth(depth);
                                if (!partitionedMacros.containsKey(depth)) {
                                    partitionedMacros.put(depth, new ArrayList<MacroUI>());
                                }

                                partitionedMacros.get(depth).add(new MacroUI(macro));
                            }
                        }

                        for (int partition : partitionedMacros.keySet()) {
                            List<MacroUI> macros = partitionedMacros.get(partition);
                            Collections.sort(macros);
                            for (MacroUI macroUI : macros) {
                                swatchBoard.addMacro(partition, macroUI);
                            }
                        }
                        loadingIndicator.setVisible(false);
                        swatchBoard.showUI(MacroGraphUI.this, swatches.getAddedMacroUIs());
                    }
                });
                doMacroCreation.start();

            }
        });


        viewSwatchBoardButton.setVisible(false);

        Box labelWrapper = Box.createHorizontalBox();
        labelWrapper.add(viewSwatchBoardButton);

        container.add(UIHelper.wrapComponentInPanel(labelWrapper), BorderLayout.SOUTH);
        container.setBorder(new EmptyBorder(3, 3, 3, 3));

        return container;
    }

    private void addGlyphToSwatchBoard(Collection<MacroUI> macros) {
        for (MacroUI macro : macros) {
            addGlyphToSwatchBoard(new MacroUI(macro));
        }
    }

    private void addGlyphToSwatchBoard(MacroUI macro) {
        swatches.addGlyph(macro);
    }

    private void unHighlightNodes() {
        if (itemsToHighlight != null) {
            TupleSet focused = view.getDisplay().getVisualization().getFocusGroup("selected");
            for (VisualItem visualItem : itemsToHighlight) {
                focused.removeTuple(visualItem);
            }
            view.getDisplay().getVisualization().run("draw");
        }
    }

    private void highlightNodesInGlyph(MacroUI selectedMacro) {
        if (view != null) {
            TupleSet focused = view.getDisplay().getVisualization().getFocusGroup("selected");
            Set<String> nodeIds = selectedMacro.getMacro().getNodeIdsInMacroAsString();
            itemsToHighlight = getVisualItemForNode(nodeIds);

            for (VisualItem visualItem : itemsToHighlight) {
                focused.addTuple(visualItem);
            }
            view.getDisplay().getVisualization().run("draw");
        }
    }


    private void createTopPanel() {
        AutoMacronTitleBar titleBar = new AutoMacronTitleBar(true);
        add(titleBar, BorderLayout.NORTH);
        titleBar.installListeners();
        setUndecorated(true);
    }

    public void notifyOfEvent(final Collection<Motif> motifs) {
        // we want to get the current glyphs are construct the image for it. And add it to the view.
        // event should give latest list of motifs.
        System.out.println("Updating motifs, of which there are " + motifs.size());

        swatches.clearSwatches();
        macros.clear();

        Thread updateSwatchThread = new Thread(new Runnable() {
            public void run() {

                for (Motif motif : motifs) {

                    if (motif.getDepth() <= DEFAULT_MOTIF_DEPTH) {
                        Macro macro = new Macro(motif);
                        macros.add(macro);
                    }
                }

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        System.out.println("Updating swatches with " + motifs.size() + " items.");
                        repaint();
                        swatches.updateUI();
                    }
                });
                viewSwatchBoardButton.setVisible(true);
                swatches.setVisible(true);
            }
        });

        updateSwatchThread.start();

    }

    public void setMotifs(Map<String, Motif> motifs) {
        viewSwatchBoardButton.setVisible(true);
        this.motifs = motifs;
    }
}
