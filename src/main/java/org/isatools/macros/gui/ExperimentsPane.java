package org.isatools.macros.gui;

import org.isatools.isacreator.autofilteringlist.ExtendedJList;
import org.isatools.isacreator.common.UIHelper;
import org.isatools.macros.gui.common.AutoMacronUIHelper;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 18/07/2012
 *         Time: 10:05
 */
public class ExperimentsPane extends JPanel {

    private ExtendedJList experimentList;

    @InjectedResource
    private ImageIcon loadIcon, loadIconOver, unloadIcon, unloadIconOver,
            refreshIcon, refreshIconOver, filterIcon;

    public ExperimentsPane() {
        ResourceInjector.get("ui-package.style").inject(this);
        experimentList = new ExtendedJList(new ExperimentListRenderer());

        experimentList.addPropertyChangeListener("itemSelected", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                firePropertyChange("itemSelected", null, experimentList.getSelectedValue());
            }
        });
    }

    public void createGUI() {
        setPreferredSize(new Dimension(240, 550));
        setOpaque(true);
        setLayout(new BorderLayout());

        add(createListContentPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }


    private Container createListContentPanel() {
        JPanel listAndFilterContainer = new JPanel(new BorderLayout());
        listAndFilterContainer.setOpaque(true);
        listAndFilterContainer.setBackground(AutoMacronUIHelper.BG_COLOR);

        Box filterPanel = Box.createHorizontalBox();

        UIHelper.renderComponent(experimentList.getFilterField(), UIHelper.VER_10_PLAIN, AutoMacronUIHelper.GREY_COLOR, false);
        experimentList.getFilterField().setBorder(new LineBorder(AutoMacronUIHelper.LIGHT_GREY_COLOR, 2));
        filterPanel.add(new JLabel(filterIcon));
        filterPanel.add(Box.createHorizontalStrut(5));
        filterPanel.add(experimentList.getFilterField());

        listAndFilterContainer.add(filterPanel, BorderLayout.NORTH);

        JScrollPane scroller = new JScrollPane(experimentList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroller.setBackground(UIHelper.BG_COLOR);
        scroller.getViewport().setOpaque(false);
        scroller.setBorder(new EmptyBorder(2, 2, 2, 2));
        scroller.setPreferredSize(new Dimension(249, 300));
        listAndFilterContainer.add(scroller, BorderLayout.CENTER);

        return listAndFilterContainer;
    }


    /**
     * This should probably be a different object capable of holding information about whether or
     * not an experiment has been analysed.
     *
     * @param DBGraphs - Experiments to add to the list
     */
    public void loadExperiments(List<DBGraph> DBGraphs) {
        experimentList.clearItems();
        for (DBGraph DBGraph : DBGraphs) {
            experimentList.addItem(DBGraph);
        }
        experimentList.repaint();
    }
    
    private Container createButtonPanel() {
        Box buttonContainer = Box.createHorizontalBox();
        buttonContainer.setOpaque(false);

        final JLabel loadButton = createLoadButton();

        final JLabel unloadButton = createUnloadButton();

        final JLabel refreshButton = createRefreshButton();

        buttonContainer.add(Box.createHorizontalStrut(4));
        buttonContainer.add(loadButton);
        buttonContainer.add(Box.createHorizontalStrut(5));
        buttonContainer.add(unloadButton);
        buttonContainer.add(Box.createHorizontalStrut(5));
        buttonContainer.add(refreshButton);
        
        return buttonContainer;
    }

    private void setCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void unsetCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private JLabel createRefreshButton() {
        final JLabel refreshButton = new JLabel(refreshIcon);
        refreshButton.setToolTipText("<html>Analyse content.</html>");
        refreshButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                refreshButton.setIcon(refreshIconOver);
                setCursor();
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                refreshButton.setIcon(refreshIcon);
                unsetCursor();
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                refreshButton.setIcon(refreshIcon);


                firePropertyChange("analyse", false, true);

                unsetCursor();
            }
        });
        return refreshButton;
    }

    private JLabel createUnloadButton() {
        final JLabel unloadButton = new JLabel(unloadIcon);
        unloadButton.setToolTipText("<html>Remove content.</html>");
        unloadButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                unloadButton.setIcon(unloadIconOver);
                setCursor();
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                unloadButton.setIcon(unloadIcon);
                unsetCursor();
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                unloadButton.setIcon(unloadIcon);
                firePropertyChange("unload", "", experimentList.getSelectedValue());
                experimentList.removeItem(experimentList.getSelectedValue());
                unsetCursor();
            }
        });
        return unloadButton;
    }

    private JLabel createLoadButton() {
        final JLabel loadButton = new JLabel(loadIcon);
        loadButton.setToolTipText("<html>Add more content.</html>");
        loadButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                loadButton.setIcon(loadIconOver);
                setCursor();
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                loadButton.setIcon(loadIcon);
                unsetCursor();
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                loadButton.setIcon(loadIcon);
                firePropertyChange("load", false, true);
                unsetCursor();
            }
        });
        return loadButton;
    }

    public void updateExperimentList() {
        experimentList.updateUI();
    }
}
