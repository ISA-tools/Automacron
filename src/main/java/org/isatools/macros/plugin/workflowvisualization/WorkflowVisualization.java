package org.isatools.macros.plugin.workflowvisualization;

import com.explodingpixels.macwidgets.IAppWidgetFactory;
import org.isatools.isacreator.common.CommonMouseAdapter;
import org.isatools.isacreator.common.UIHelper;
import org.isatools.isacreator.effects.AnimatableJFrame;
import org.isatools.isacreator.effects.FooterPanel;
import org.isatools.isacreator.effects.HUDTitleBar;
import org.isatools.isacreator.managers.ApplicationManager;

import org.isatools.macros.gui.macro.Macro;
import org.isatools.macros.gui.macro.MacroDetail;
import org.isatools.macros.plugin.workflowvisualization.graph.GraphView;
import org.isatools.macros.plugin.workflowvisualization.macroinformation.MacroInformationPanel;
import org.isatools.macros.plugin.workflowvisualization.taxonomy.TaxonomyLevel;
import org.isatools.macros.plugin.workflowvisualization.taxonomy.io.TaxonomyLevelLoader;
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
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 06/03/2012
 *         Time: 18:09
 */
public class WorkflowVisualization extends AnimatableJFrame {

    // will visualize a biological workflow. Tree will be formed using prefuse. Images will come from the
    // glyphs created for the visweek paper.

    // we need the unique branches of processing (so doesn't include names, just processes enacted)

    // grouped by pertubation, e.g. factor groups.

    private static List<TaxonomyLevel> taxonomyLevels;

    static {
        ResourceInjector.addModule("org.jdesktop.fuse.swing.SwingModule");

        ResourceInjector.get("workflow-package.style").load(
                WorkflowVisualization.class.getResource("/dependency_injections/workflow-package.properties"));


        TaxonomyLevelLoader.loadTaxonomyLevels();
        taxonomyLevels = TaxonomyLevelLoader.getTaxonomyLevels();

    }

    @InjectedResource
    private Image workflowVisIcon, workflowVisIconInactive;

    @InjectedResource
    private ImageIcon commonMacrosIcon;

    private WorkflowInformation workflowInformation;
    private GraphView view;
    private MacroDetail detail = null;


    public WorkflowVisualization(WorkflowInformation workflowInformation) {
        this.workflowInformation = workflowInformation;
        ResourceInjector.get("workflow-package.style").inject(this);
    }

    private NodeDetail nodeDetailView = null;

    public void createGUI() {
        setUndecorated(true);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));
        setLocationRelativeTo(ApplicationManager.getCurrentApplicationInstance());
        setBackground(UIHelper.BG_COLOR);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ((JComponent) getContentPane()).setBorder(new LineBorder(UIHelper.LIGHT_GREEN_COLOR, 1));
        addTitlePane();
        createCentralPanel();
        addSouthPanel();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void addTitlePane() {
        HUDTitleBar titleBar = new HUDTitleBar(workflowVisIcon, workflowVisIconInactive, false);
        add(titleBar, BorderLayout.NORTH);
        titleBar.installListeners();
    }

    private void createCentralPanel() {
        String datafile = workflowInformation.getFile().getAbsolutePath();

        final Graph g;
        try {
            g = new GraphMLReader().readGraph(datafile);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    String label = "image";
                    view = new GraphView(g, label, Constants.ORIENT_TOP_BOTTOM, new Dimension(750, 900));
                    view.getDisplay().addControlListener(new ControlAdapter() {

                        @Override
                        public void mousePressed(MouseEvent mouseEvent) {
                            // do nothing
                        }

                        @Override
                        public void itemClicked(VisualItem visualItem, MouseEvent mouseEvent) {
                            if (nodeDetailView == null) {
                                nodeDetailView = new NodeDetail(taxonomyLevels);
                                nodeDetailView.createGUI();
                            }

                            setContentForNodeDetail(visualItem);
                            if (!nodeDetailView.isShowing()) {
                                nodeDetailView.setVisible(true);
                            }
                        }

                        @Override
                        public void itemEntered(VisualItem visualItem, MouseEvent mouseEvent) {
                            if (visualItem.canGetString("id")) {
                                TupleSet focused = view.getDisplay().getVisualization().getFocusGroup("highlighted");
                                focused.addTuple(visualItem);
                                view.getDisplay().getVisualization().run("draw");
                                if (nodeDetailView != null && nodeDetailView.isShowing()) {
                                    setContentForNodeDetail(visualItem);
                                }
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

//                    if (workflowInformation != null && !workflowInformation.getMotifs().isEmpty()) {
//                        view.add(createMacroPanel(), BorderLayout.SOUTH);
//                    }
                    add(view, BorderLayout.CENTER);
                }
            });

        } catch (DataIOException e) {
            e.printStackTrace();
            System.err.println("Graph for " + workflowInformation.getFile().getName() + " does not exist.");
        }
    }

    private Container createMacroPanel() {
        Container macroContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));

        for (final String motif : workflowInformation.getMotifs().keySet()) {
            MacroInformationPanel macroInformationPanel = new MacroInformationPanel(motif, workflowInformation);
            macroContainer.add(macroInformationPanel);

            macroInformationPanel.addMouseListener(new CommonMouseAdapter() {
                @Override
                public void mousePressed(final MouseEvent mouseEvent) {
                    super.mousePressed(mouseEvent);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            detail = new MacroDetail(new Macro(workflowInformation.getMotifs().get(motif)));
                            detail.setLocation(mouseEvent.getLocationOnScreen().x - 150, mouseEvent.getLocationOnScreen().y - 310);
                            detail.setVisible(true);
                        }
                    });
                }

                @Override
                public void mouseExited(MouseEvent mouseEvent) {
                    super.mouseExited(mouseEvent);
                    if (detail != null && detail.isShowing()) {
                        detail.setVisible(false);
                    }
                }
            });

        }

        JScrollPane scroller = new JScrollPane(macroContainer,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        scroller.getViewport().setOpaque(false);
        scroller.setOpaque(false);
        scroller.setBorder(new EmptyBorder(1, 1, 1, 1));
        scroller.setPreferredSize(new Dimension(400, 110));

        IAppWidgetFactory.makeIAppScrollPane(scroller);

        JPanel macroPanel = new JPanel(new BorderLayout());
        JLabel macroLogo = new JLabel(commonMacrosIcon);
        macroLogo.setHorizontalAlignment(SwingConstants.LEFT);

        macroPanel.add(macroLogo, BorderLayout.NORTH);

        macroPanel.add(scroller, BorderLayout.CENTER);
        macroPanel.setBorder(new MatteBorder(1, 0, 0, 0, UIHelper.LIGHT_GREEN_COLOR));

        return macroPanel;
    }

    private void setContentForNodeDetail(VisualItem visualItem) {
        try {
            nodeDetailView.setContent(new WorkflowVisualisationNode(visualItem.get("image").toString(),
                    visualItem.get("type").toString(), visualItem.get("value").toString(),
                    visualItem.get("taxonomy").toString()));
        } catch (Exception e) {
            // ignore the error.
        }
    }

    private void addSouthPanel() {
        FooterPanel footerPanel = new FooterPanel(this);
        add(footerPanel, BorderLayout.SOUTH);
    }

}
