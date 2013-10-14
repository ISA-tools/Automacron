package org.isatools.macros.gui.motifdrawer;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.handler.mxCellHandler;
import com.mxgraph.swing.handler.mxEdgeHandler;
import com.mxgraph.swing.handler.mxElbowEdgeHandler;
import com.mxgraph.swing.handler.mxVertexHandler;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxMorphing;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
import org.isatools.isacreator.common.CommonMouseAdapter;
import org.isatools.isacreator.common.UIHelper;
import org.isatools.isacreator.effects.HUDTitleBar;
import org.isatools.macros.gui.common.AutoMacronUIHelper;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 05/11/2012
 *         Time: 13:25
 */
public class MotifDrawer extends JFrame {

    public static final int NODE_SIZE = 20;

    public static final String ROUNDED_LIGHT_BLUE = "ROUNDED-LIGHT-BLUE";
    public static final String HEXAGON_ORANGE = "HEXAGON-ORANGE";
    public static final String ROUNDED_GREEN = "ROUNDED-GREEN";
    public static final String SQUARE_LIGHT_ORANGE = "SQUARE-LIGHT-ORANGE";
    public static final String ROUNDED_RED = "ROUNDED-RED";
    public static final String TRIANGLE_GREY = "TRIANGLE-GREY";

    @InjectedResource
    private ImageIcon addVertex, addVertexOver, addEdge, addEdgeOver, remove, removeOver,
            materialVertexButton, protocolVertexButton, chemicalVertexButton, dataVertexButton,
            closeVertexChoiceWindow, closeVertexChoiceWindowOver,
            search, searchOver;

    @InjectedResource
    private Image headerLogo, closeIcon, closeOverIcon, closePressedIcon;

    private JLabel addVertexButton, addEdgeButton, removeButton;
    private mxGraph mxGraph;
    private Object parent;
    private Object selectedCell;
    private mxGraphComponent graphComponent;

    private VertexChoiceWindow vertexChoiceWindow;

    private int lastXPosition, lastYPosition = 10;

    static {
        ResourceInjector.addModule("org.jdesktop.fuse.swing.SwingModule");
        ResourceInjector.get("ui-package.style").load(
                MotifDrawer.class.getResource("/dependency_injections/ui-package.properties"));
    }

    public MotifDrawer() {
        ResourceInjector.get("ui-package.style").inject(this);
        mxGraph = new mxGraph();
        parent = mxGraph.getDefaultParent();

        applyEdgeDefaults();
        createGUI();
    }

    private void createGUI() {
        setBackground(UIHelper.BG_COLOR);
        setPreferredSize(new Dimension(700, 600));
        setUndecorated(true);

        vertexChoiceWindow = new VertexChoiceWindow();
        vertexChoiceWindow.createVertexChoiceGUI();

        createTopPanel();
        initialiseGraph();
        createBottomPanel();

        pack();

    }

    public mxGraph getMxGraph() {
        return mxGraph;
    }

    public void showUI(Container parent) {
        setVisible(true);
        setLocationRelativeTo(parent);
    }

    private void createCellStyles() {
        mxStylesheet stylesheet = mxGraph.getStylesheet();

        Hashtable<String, Object> roundedGreenStyle = new Hashtable<String, Object>();
        applyVertexDefaults(roundedGreenStyle);
        roundedGreenStyle.put(mxConstants.STYLE_FILLCOLOR, "#81A32B");
        roundedGreenStyle.put(mxConstants.STYLE_FONTCOLOR, "#81A32B");
        stylesheet.putCellStyle(ROUNDED_GREEN, roundedGreenStyle);

        Hashtable<String, Object> roundedLightBlueStyle = new Hashtable<String, Object>();
        applyVertexDefaults(roundedLightBlueStyle);
        roundedLightBlueStyle.put(mxConstants.STYLE_FILLCOLOR, "#A8DCD9");
        roundedLightBlueStyle.put(mxConstants.STYLE_FONTCOLOR, "#A8DCD9");
        stylesheet.putCellStyle(ROUNDED_LIGHT_BLUE, roundedLightBlueStyle);

        Hashtable<String, Object> roundedOrangeStyle = new Hashtable<String, Object>();
        applyVertexDefaults(roundedOrangeStyle);
        roundedOrangeStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_HEXAGON);
        roundedOrangeStyle.put(mxConstants.STYLE_FILLCOLOR, "#F26A21");
        roundedOrangeStyle.put(mxConstants.STYLE_FONTCOLOR, "#F26A21");
        stylesheet.putCellStyle(HEXAGON_ORANGE, roundedOrangeStyle);

        Hashtable<String, Object> roundedLightOrangeStyle = new Hashtable<String, Object>();
        applyVertexDefaults(roundedLightOrangeStyle);
        roundedLightOrangeStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        roundedLightOrangeStyle.put(mxConstants.STYLE_FILLCOLOR, "#F2852F");
        roundedLightOrangeStyle.put(mxConstants.STYLE_FONTCOLOR, "#F2852F");
        stylesheet.putCellStyle(SQUARE_LIGHT_ORANGE, roundedLightOrangeStyle);

        Hashtable<String, Object> roundedRedStyle = new Hashtable<String, Object>();
        applyVertexDefaults(roundedRedStyle);
        roundedRedStyle.put(mxConstants.STYLE_FILLCOLOR, "#F2852F");
        roundedRedStyle.put(mxConstants.STYLE_FONTCOLOR, "#F2852F");
        stylesheet.putCellStyle(ROUNDED_RED, roundedRedStyle);

        Hashtable<String, Object> triangleGreyStyle = new Hashtable<String, Object>();
        applyVertexDefaults(triangleGreyStyle);
        triangleGreyStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_TRIANGLE);
        triangleGreyStyle.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_SOUTH);
        triangleGreyStyle.put(mxConstants.STYLE_FILLCOLOR, "#666666");
        triangleGreyStyle.put(mxConstants.STYLE_FONTCOLOR, "#666666");
        stylesheet.putCellStyle(TRIANGLE_GREY, triangleGreyStyle);
    }

    private void createTopPanel() {
        HUDTitleBar titleBar = new HUDTitleBar(headerLogo, headerLogo, closeIcon, closeIcon, closeOverIcon, closePressedIcon);
        add(titleBar, BorderLayout.NORTH);
        titleBar.installListeners();
    }

    private void createBottomPanel() {
        Box bottomPanel = Box.createHorizontalBox();
        bottomPanel.setOpaque(true);
        bottomPanel.setBackground(new Color(241, 242, 241));

        addVertexButton = new JLabel(addVertex);
        addVertexButton.addMouseListener(new CommonMouseAdapter() {
            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                super.mouseExited(mouseEvent);
                addVertexButton.setIcon(addVertex);

            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                super.mouseEntered(mouseEvent);
                addVertexButton.setIcon(addVertexOver);

            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);
                addVertexButton.setIcon(addVertex);
                vertexChoiceWindow.showVertexChoiceWindow(addVertexButton.getLocationOnScreen());

            }
        });

        addEdgeButton = new JLabel(addEdge);
        addEdgeButton.addMouseListener(new CommonMouseAdapter() {
            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                super.mouseExited(mouseEvent);
                addEdgeButton.setIcon(addEdge);
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                super.mouseEntered(mouseEvent);
                addEdgeButton.setIcon(addEdgeOver);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);
                addEdgeButton.setIcon(addEdge);
            }
        });

        removeButton = new JLabel(remove);
        removeButton.addMouseListener(new CommonMouseAdapter() {
            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                super.mouseExited(mouseEvent);
                removeButton.setIcon(remove);

            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                super.mouseEntered(mouseEvent);
                removeButton.setIcon(removeOver);

            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);
                removeButton.setIcon(remove);
                mxGraph.removeCells(new Object[]{selectedCell});
                removeButton.setVisible(false);
            }
        });


        final JLabel saveImageButton = new JLabel("Save as PNG");

        saveImageButton.addMouseListener(new CommonMouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);
                try {
                    DrawerUtils.saveGraphAsImage(mxGraph, new File("data/imageexport.png"));
                } catch (IOException e) {
                    JDialog dialog = new JDialog(MotifDrawer.this, "Error Occurred", Dialog.ModalityType.MODELESS);
                    dialog.setVisible(true);
                }
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                super.mouseEntered(mouseEvent);    //To change body of overridden methods use File | Settings | File Templates.
                saveImageButton.setBorder(new MatteBorder(0, 0, 2, 0, AutoMacronUIHelper.DARK_BLUE_COLOR));
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                super.mouseExited(mouseEvent);    //To change body of overridden methods use File | Settings | File Templates.
                saveImageButton.setBorder(null);
            }
        });
        UIHelper.renderComponent(saveImageButton, AutoMacronUIHelper.getCustomFont(AutoMacronUIHelper.PACIFICO_10), AutoMacronUIHelper.DARK_BLUE_COLOR, false);

        final JLabel autoLayout = new JLabel("Auto layout");
        autoLayout.addMouseListener(new CommonMouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);
                layoutGraph();
                autoLayout.setBorder(null);
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                super.mouseEntered(mouseEvent);
                autoLayout.setBorder(new MatteBorder(0, 0, 2, 0, AutoMacronUIHelper.DARK_ORANGE_COLOR));
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                super.mouseExited(mouseEvent);
                autoLayout.setBorder(null);
            }
        });

        UIHelper.renderComponent(autoLayout, AutoMacronUIHelper.getCustomFont(AutoMacronUIHelper.PACIFICO_10), AutoMacronUIHelper.DARK_ORANGE_COLOR, false);

        removeButton.setVisible(false);

        bottomPanel.add(addVertexButton);
        bottomPanel.add(addEdgeButton);
        bottomPanel.add(removeButton);
        bottomPanel.add(Box.createHorizontalStrut(50));
        bottomPanel.add(saveImageButton);
        bottomPanel.add(Box.createHorizontalStrut(5));
        bottomPanel.add(autoLayout);

        final JLabel searchButton = new JLabel(search);
        searchButton.addMouseListener(new CommonMouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);
                searchButton.setIcon(search);
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                super.mouseEntered(mouseEvent);
                searchButton.setIcon(searchOver);
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                super.mouseExited(mouseEvent);
                searchButton.setIcon(search);
            }
        });

        bottomPanel.add(Box.createHorizontalStrut(290));
        bottomPanel.add(searchButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    public Object insertVertex(int x, int y, String type) {
        return insertVertex("", x, y, type);
    }

    public Object insertVertex(String nodeName, int x, int y, String type) {
        mxGraph.getModel().beginUpdate();
        try {
            lastXPosition = x;
            lastYPosition = y;
            return mxGraph.insertVertex(parent, null, nodeName, x, y, NODE_SIZE, NODE_SIZE, type);

        } finally {
            mxGraph.getModel().endUpdate();
        }
    }

    public Object insertEdge(Object v1, Object v2, String strokeColor) {
        mxGraph.getModel().beginUpdate();
        try {
            return mxGraph.insertEdge(parent, null, "", v1, v2, "strokeColor=" + strokeColor);
        } finally {
            mxGraph.getModel().endUpdate();
        }
    }

    private void initialiseGraph() {
        createCellStyles();

        graphComponent = new mxGraphComponent(mxGraph) {
            Color selectionColor = AutoMacronUIHelper.LIGHT_GREY_COLOR;
            Stroke selectionStroke = new BasicStroke(2.0f);

            @Override
            public mxCellHandler createHandler(mxCellState state) {
                if (graph.getModel().isVertex(state.getCell())) {
                    return new mxVertexHandler(this, state) {
                        @Override
                        public Color getSelectionColor() {
                            return selectionColor;
                        }

                        @Override
                        public Stroke getSelectionStroke() {
                            return selectionStroke;
                        }

                        @Override
                        protected Color getHandleFillColor(int i) {
                            return AutoMacronUIHelper.LIGHT_GREY_COLOR;
                        }

                        @Override
                        protected Color getHandleBorderColor(int i) {
                            return AutoMacronUIHelper.LIGHT_BLUE_COLOR;
                        }
                    };
                } else if (graph.getModel().isEdge(state.getCell())) {
                    mxEdgeStyle.mxEdgeStyleFunction style = graph.getView().getEdgeStyle(state, null, null, null);
                    if (graph.isLoop(state) || style == mxEdgeStyle.ElbowConnector
                            || style == mxEdgeStyle.SideToSide || style == mxEdgeStyle.TopToBottom) {
                        return new mxElbowEdgeHandler(this, state);
                    }
                    return new mxEdgeHandler(this, state) {
                        @Override
                        public Color getSelectionColor() {
                            return selectionColor;
                        }

                        @Override
                        public Stroke getSelectionStroke() {
                            return selectionStroke;
                        }

                        @Override
                        protected Color getHandleFillColor(int i) {
                            return AutoMacronUIHelper.LIGHT_GREY_COLOR;
                        }
                    };
                }
                return new mxCellHandler(this, state);
            }
        };


        graphComponent.setFont(UIHelper.VER_8_BOLD);
        graphComponent.setBorder(null);
        graphComponent.setZoomPolicy(mxGraphComponent.ZOOM_POLICY_WIDTH);

        getContentPane().add(graphComponent);
        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {

            private Object getCell(MouseEvent e) {
                return graphComponent.getCellAt(e.getX(), e.getY());
            }

            public void mousePressed(MouseEvent e) {

                Object cell = getCell(e);

                if (cell != null) {
                    selectedCell = cell;
                    System.out.println(mxGraph.getLabel(cell));
                    removeButton.setVisible(true);

                } else {
                    removeButton.setVisible(false);
                }
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                Object cell = getCell(mouseEvent);

                if (cell != null) {
                    Point position = ((mxCell) cell).getGeometry().getPoint();
                    lastYPosition = position.y;
                    lastXPosition = position.x;
                }
            }

        });
    }

    public void layoutGraph() {
        mxHierarchicalLayout layout = new mxHierarchicalLayout(mxGraph);
        Object cell = mxGraph.getDefaultParent();
        if (cell == null || mxGraph.getModel().getChildCount(cell) == 0) {
            cell = mxGraph.getDefaultParent();
        }

        layout.setDisableEdgeStyle(false);
        layout.setIntraCellSpacing(15);
        layout.setInterHierarchySpacing(15);
        layout.setInterRankCellSpacing(15);
        mxGraph.getModel().beginUpdate();
        try {
            layout.execute(cell);
        } finally {

            mxMorphing morph = new mxMorphing(graphComponent, 20, 1.2, 20);
            morph.addListener(mxEvent.DONE,
                    new mxEventSource.mxIEventListener() {
                        public void invoke(Object sender, mxEventObject evt) {
                            mxGraph.getModel().endUpdate();
                        }
                    });
            morph.startAnimation();
            mxGraph.getModel().endUpdate();
            mxGraph.refresh();
        }
    }

    private void applyEdgeDefaults() {
        Map<String, Object> edge = new HashMap<String, Object>();
        edge.put(mxConstants.STYLE_ROUNDED, true);
        edge.put(mxConstants.STYLE_ORTHOGONAL, false);
        edge.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_TOPTOBOTTOM);
        edge.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
        edge.put(mxConstants.STYLE_ENDARROW, mxConstants.NONE);
        edge.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_RIGHT);
        edge.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
        edge.put(mxConstants.STYLE_FONTFAMILY, "Verdana");
        edge.put(mxConstants.STYLE_STROKECOLOR, "#808285");
        edge.put(mxConstants.STYLE_FONTCOLOR, "#808285");
        edge.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, "bottom");
        mxStylesheet edgeStyle = new mxStylesheet();
        edgeStyle.setDefaultEdgeStyle(edge);
        mxGraph.setStylesheet(edgeStyle);
    }

    private void applyVertexDefaults(Map<String, Object> vertexProperties) {
        vertexProperties.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_RIGHT);
        vertexProperties.put(mxConstants.STYLE_STROKECOLOR, "#808285");
        vertexProperties.put(mxConstants.STYLE_FONTCOLOR, "#808285");
        vertexProperties.put(mxConstants.STYLE_FONTFAMILY, "Verdana");
        vertexProperties.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
        vertexProperties.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, "bottom");
        vertexProperties.put(mxConstants.STYLE_STROKECOLOR, "#FFFFFF");
        vertexProperties.put(mxConstants.STYLE_OPACITY, 100);
        vertexProperties.put(mxConstants.STYLE_FONTSIZE, "10");
    }

    class VertexChoiceWindow extends JWindow {
        VertexChoiceWindow() {
            createVertexChoiceGUI();
        }

        private void createVertexChoiceGUI() {
            setLayout(new FlowLayout());
            setPreferredSize(new Dimension(400, 40));
            setBackground(UIHelper.BG_COLOR);
            setAlwaysOnTop(true);
            setResizable(false);

            add(createLabel(materialVertexButton, ROUNDED_GREEN));
            add(createLabel(dataVertexButton, SQUARE_LIGHT_ORANGE));
            add(createLabel(protocolVertexButton, TRIANGLE_GREY));
            add(createLabel(chemicalVertexButton, HEXAGON_ORANGE));

            final JLabel closeWindowButton = new JLabel(closeVertexChoiceWindow);
            closeWindowButton.addMouseListener(new CommonMouseAdapter() {
                @Override
                public void mousePressed(MouseEvent mouseEvent) {
                    super.mousePressed(mouseEvent);
                    closeWindowButton.setIcon(closeVertexChoiceWindow);
                    hideVertexChoiceWindow();
                }

                @Override
                public void mouseEntered(MouseEvent mouseEvent) {
                    super.mouseEntered(mouseEvent);
                    closeWindowButton.setIcon(closeVertexChoiceWindowOver);
                }

                @Override
                public void mouseExited(MouseEvent mouseEvent) {
                    super.mouseExited(mouseEvent);
                    closeWindowButton.setIcon(closeVertexChoiceWindow);
                }
            });

            add(closeWindowButton);

            pack();
        }

        private JLabel createLabel(ImageIcon image, final String associatedIconType) {
            JLabel label = new JLabel(image);
            label.addMouseListener(new CommonMouseAdapter() {
                @Override
                public void mousePressed(MouseEvent mouseEvent) {
                    super.mousePressed(mouseEvent);
                    insertVertex(lastXPosition + 40, lastYPosition, associatedIconType);
                }
            });

            return label;
        }

        void hideVertexChoiceWindow() {
            setVisible(false);
        }

        void showVertexChoiceWindow(Point location) {
            setLocation((int) location.getX(), (int) location.getY() - 45);
            setVisible(true);
        }


    }

    public static void main(String[] args) {
        new MotifDrawer().showUI(null);
    }
}
