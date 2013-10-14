package org.isatools.macros.gui.macro.selection_util;

import org.isatools.isacreator.common.UIHelper;
import org.isatools.isacreator.effects.borders.RoundedBorder;
import org.isatools.macros.gui.common.AutoMacronUIHelper;
import org.isatools.macros.gui.macro.MacroDetail;
import org.isatools.macros.gui.macro.MacroUI;
import org.isatools.macros.gui.macro.renderer.MotifGraphRenderer;
import org.isatools.macros.gui.macro.renderer.RenderingFactory;
import org.isatools.macros.gui.macro.renderer.RenderingType;
import org.isatools.macros.utils.MotifProcessingUtils;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 05/12/2012
 *         Time: 16:56
 */
public class SelectedMacroItem extends JPanel {

    private MacroUI macroUI;
    private MacroDetail detail = null;

    static {
        ResourceInjector.addModule("org.jdesktop.fuse.swing.SwingModule");
        ResourceInjector.get("ui-package.style").load(
                SelectedMacroItem.class.getResource("/dependency_injections/ui-package.properties"));
    }


    @InjectedResource
    private ImageIcon detailIcon, detailIconOver, removeIcon, removeIconOver, smallAnnotateIcon,
            smallAnnotateIconOver, annotateIcon, annotateIconOver, cancelIcon, cancelIconOver;

    SelectedMacroItem(MacroUI macroUI) {
        ResourceInjector.get("ui-package.style").inject(this);
        this.macroUI = macroUI;
        createGUI();
    }

    private void createGUI() {
        setLayout(new BorderLayout());
        setSize(new Dimension(70, 80));

        // add image to the central panel
        // add control box to the south panel.
        createCentralPanel();
        createControlPanel();
    }

    private void createCentralPanel() {
        JLabel macroImagePanel = new JLabel();

        JPanel centralPanel = new JPanel(new BorderLayout());

        try {
            ImageIcon icon = AutoMacronUIHelper.scaleImageIcon(macroUI.getMacro().getGlyph(RenderingType.DETAILED).getAbsolutePath(), 60, 60);
            macroImagePanel.setIcon(icon);
        } catch (Exception e) {
            // ignore
        }

        centralPanel.add(macroImagePanel, BorderLayout.CENTER);
        centralPanel.setBorder(new LineBorder(AutoMacronUIHelper.LIGHT_GREY_COLOR));
        add(centralPanel, BorderLayout.CENTER);
    }


    private void createControlPanel() {

        JPanel macroInformationPanel = new JPanel(new BorderLayout());
        macroInformationPanel.setBackground(AutoMacronUIHelper.LIGHT_GREY_COLOR);

        // add motif score

        JLabel score = UIHelper.createLabel(String.format(" %.2f ", macroUI.getMacro().getMotif().getScore()), UIHelper.VER_8_BOLD, UIHelper.BG_COLOR);

        Box scoreContainer = Box.createHorizontalBox();
        scoreContainer.add(score);
        Color scoreColor = macroUI.getMacro().getMotif().getScore() < 0 ? AutoMacronUIHelper.DARK_BLUE_COLOR : AutoMacronUIHelper.DARK_ORANGE_COLOR;

        scoreContainer.setBorder(new HighlightBorder(scoreColor, scoreColor));

        macroInformationPanel.add(scoreContainer, BorderLayout.WEST);

        // add detail button
        // add remove button
        final JLabel detailButton = new JLabel(detailIcon);
        detailButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                detailButton.setIcon(detailIcon);
                if (detail != null) {
                    detail.setVisible(false);
                }
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                detailButton.setIcon(detailIconOver);
            }

            @Override
            public void mousePressed(final MouseEvent mouseEvent) {
                detailButton.setIcon(detailIcon);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        detail = new MacroDetail(macroUI.getMacro());
                        System.out.println(MotifProcessingUtils.findAndCollapseMergeEvents(macroUI.getMacro().getMotif().getStringRepresentation()));
                        detail.setLocation(mouseEvent.getLocationOnScreen().x - 150, mouseEvent.getLocationOnScreen().y - 310);
                        detail.setVisible(true);
                    }
                });
            }
        });

        final JLabel annotateButton = new JLabel(smallAnnotateIcon);
        annotateButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                annotateButton.setIcon(smallAnnotateIcon);

            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                annotateButton.setIcon(smallAnnotateIconOver);
            }

            @Override
            public void mousePressed(final MouseEvent mouseEvent) {
                annotateButton.setIcon(smallAnnotateIcon);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        InsertAnnotationUI insertAnnotationUI = new InsertAnnotationUI();
                        Point displayLocation = new Point(mouseEvent.getXOnScreen(), mouseEvent.getYOnScreen() + 30);
                        insertAnnotationUI.createGUI(displayLocation, macroUI.getMacro().getAnnotation());
                    }
                });
            }
        });

        final JLabel removeButton = new JLabel(removeIcon);
        removeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                removeButton.setIcon(removeIcon);
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                removeButton.setIcon(removeIconOver);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                removeButton.setIcon(removeIcon);
                SelectedMacroItem.this.setVisible(false);
                firePropertyChange("removeItem", null, SelectedMacroItem.this);
                macroUI.setSelected(false);
            }
        });


        Box buttonContainer = Box.createHorizontalBox();
        buttonContainer.add(detailButton);
        buttonContainer.add(Box.createHorizontalStrut(2));
        buttonContainer.add(annotateButton);
        buttonContainer.add(Box.createHorizontalStrut(2));
        buttonContainer.add(removeButton);

        macroInformationPanel.add(buttonContainer, BorderLayout.EAST);

        add(macroInformationPanel, BorderLayout.SOUTH);

    }

    public MacroUI getMacroUI() {
        return macroUI;
    }

    public class InsertAnnotationUI extends JFrame {

        public void createGUI(Point position, String existingAnnotation) {
            setUndecorated(true);
            setAlwaysOnTop(true);
            setPreferredSize(new Dimension(300, 30));
            setLayout(new BorderLayout());
            setBorder(new RoundedBorder(AutoMacronUIHelper.DARK_BLUE_COLOR, 4));

            Box container = Box.createHorizontalBox();
            container.setBorder(new EmptyBorder(4, 4, 4, 4));
            setLocation(position);

            JTextField annotationField = new JTextField((existingAnnotation == null || existingAnnotation.isEmpty())
                    ? "Annotation..." : existingAnnotation, 30);

            UIHelper.renderComponent(annotationField, UIHelper.VER_11_BOLD, UIHelper.GREY_COLOR, false);
            annotationField.setBorder(new MatteBorder(0, 0, 2, 0, AutoMacronUIHelper.DARK_BLUE_COLOR));

            annotationField.setBackground(UIHelper.BG_COLOR);
            annotationField.setSelectedTextColor(UIHelper.BG_COLOR);
            annotationField.setSelectionColor(AutoMacronUIHelper.DARK_BLUE_COLOR);
            UIHelper.renderComponent(annotationField, UIHelper.VER_12_PLAIN, AutoMacronUIHelper.GREY_COLOR, false);

            container.add(annotationField);

            createButtonPanel(container, annotationField);

            add(container, BorderLayout.NORTH);

            pack();
            setVisible(true);
        }

        private void createButtonPanel(Box container, final JTextField annotationField) {
            final JLabel annotateButton = new JLabel(annotateIcon);
            annotateButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent mouseEvent) {
                    annotateButton.setIcon(annotateIconOver);
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent mouseEvent) {
                    annotateButton.setIcon(annotateIcon);
                    setCursor(Cursor.getDefaultCursor());
                }

                @Override
                public void mousePressed(MouseEvent mouseEvent) {
                    annotateButton.setIcon(annotateIcon);
                    macroUI.getMacro().setAnnotation(annotationField.getText());
                    Thread updateIconThread = new Thread(new Runnable() {
                        public void run() {
                            macroUI.generateMacros();
                        }
                    });
                    updateIconThread.start();

                    setVisible(false);
                    setCursor(Cursor.getDefaultCursor());
                }
            });

            final JLabel cancelButton = new JLabel(cancelIcon);
            cancelButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent mouseEvent) {
                    cancelButton.setIcon(cancelIconOver);
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent mouseEvent) {
                    cancelButton.setIcon(cancelIcon);
                    setCursor(Cursor.getDefaultCursor());
                }

                @Override
                public void mousePressed(MouseEvent mouseEvent) {
                    cancelButton.setIcon(cancelIcon);
                    setCursor(Cursor.getDefaultCursor());
                    setVisible(false);
                    InsertAnnotationUI.this.dispose();
                }
            });

            container.add(annotateButton);
            container.add(cancelButton);
        }
    }

}
