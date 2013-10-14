package org.isatools.macros.gui.macro.selection_util;

import com.google.common.collect.Sets;
import org.isatools.errorreporter.ui.borders.RoundedBorder;
import org.isatools.errorreporter.ui.utils.UIHelper;
import org.isatools.isacreator.common.CommonMouseAdapter;
import org.isatools.isacreator.effects.FooterPanel;
import org.isatools.isacreator.effects.HUDTitleBar;
import org.isatools.macros.gui.common.AutoMacronUIHelper;
import org.isatools.macros.gui.loading_gui.StatusUI;
import org.isatools.macros.gui.macro.Macro;
import org.isatools.macros.gui.macro.MacroUI;
import org.isatools.macros.motiffinder.Motif;
import org.isatools.macros.utils.MotifProcessingUtils;
import org.isatools.macros.utils.SetUtils;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 04/12/2012
 *         Time: 23:50
 */
public class MacroSelectionUtilUI extends JFrame implements ListSelectionListener {

    @InjectedResource
    private Image selectorHeader, closeIcon, closeOverIcon, closePressedIcon;
    // Filters

    @InjectedResource
    private ImageIcon allMotifs, allMotifsOver, mergeMotifs, mergeMotifsOver, linearMotifs, linearMotifsOver,
            branchMotifs, branchMotifsOver;

    @InjectedResource
    private ImageIcon resizeIcon;

    Map<Integer, CustomListModel> partitionToListModel;
    Map<Integer, JList> partitionToList;
    Map<Integer, Container> partitionToListPanel;
    Map<MacroUI, Set<PenaltyRecord>> selectedMacroToPenalties;
    Set<Filters> enabledFilters;
    private JPanel selectedMacroContainer;
    private int startSize;
    private int endSize;
    private StatusUI loadingIndicator;

    private Set<MacroUI> selectedMacros;

    static {
        ResourceInjector.addModule("org.jdesktop.fuse.swing.SwingModule");
        ResourceInjector.get("ui-package.style").load(
                MacroSelectionUtilUI.class.getResource("/dependency_injections/ui-package.properties"));
    }

    private JLabel linearMotifsFilter, branchMotifsFilter, mergeMotifsFilter, allMotifsFilter;

    public MacroSelectionUtilUI(int startSize, int endSize) {
        ResourceInjector.get("ui-package.style").inject(this);
        this.startSize = startSize;
        this.endSize = endSize;
        partitionToListModel = new HashMap<Integer, CustomListModel>();
        partitionToList = new HashMap<Integer, JList>();
        partitionToListPanel = new HashMap<Integer, Container>();
        selectedMacros = new HashSet<MacroUI>();
        enabledFilters = new HashSet<Filters>();
        selectedMacroToPenalties = new HashMap<MacroUI, Set<PenaltyRecord>>();
        loadingIndicator = new StatusUI();
    }

    public void createGUI() {
        setLayout(new BorderLayout());
        setUndecorated(true);
        setPreferredSize(new Dimension(800, 760));
        setBackground(UIHelper.BG_COLOR);

        createTopPanel();
        createCentralPanel();
        createSouthPanel();

        pack();
    }

    public void showUI(Component component, Set<MacroUI> selectedMacros) {
        this.selectedMacros.clear();
        this.selectedMacros.addAll(selectedMacros);

        populateSelectedMacroPanel();
        setLocationRelativeTo(component);
        setVisible(true);
    }

    private void populateSelectedMacroPanel() {

        if (selectedMacros.size() > 0) {

            for (MacroUI macroUI : selectedMacros) {

                SelectedMacroItem macroSelectionUI = new SelectedMacroItem(macroUI);
                macroSelectionUI.addPropertyChangeListener("removeItem", new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                        SelectedMacroItem macroToRemove = (SelectedMacroItem) propertyChangeEvent.getNewValue();
                        selectedMacros.remove(macroToRemove.getMacroUI());
                        selectedMacroContainer.remove(macroToRemove.getMacroUI());
                        selectedMacroContainer.updateUI();

                        partitionToListModel.get(macroToRemove.getMacroUI().getMacro().getMotif().getDepth()).fireContentsChanged();

                        correctPenaltyForMacro(macroToRemove);

                        firePropertyChange("motifsUpdated", null, selectedMacros);
                    }
                });

                selectedMacroContainer.add(macroSelectionUI);
                selectedMacroContainer.updateUI();
            }
        }
    }

    private void correctPenaltyForMacro(SelectedMacroItem macroToRemove) {
        Set<Integer> affectedDepths = new HashSet<Integer>();
        if (selectedMacroToPenalties.containsKey(macroToRemove.getMacroUI())) {
            for (PenaltyRecord record : selectedMacroToPenalties.get(macroToRemove.getMacroUI())) {
                Macro macro = record.getMacroUI().getMacro();
                // resetting penalty...or decreasing it. Could be one of a set of contributing macros.
                macro.setPenalty(macro.getPenalty() - record.getPenaltyRecorded());
                affectedDepths.add(macro.getMotif().getDepth());
            }
        }
        selectedMacroToPenalties.remove(macroToRemove.getMacroUI());

        for (Integer depthIndex : affectedDepths) {
            partitionToListModel.get(depthIndex).fireContentsChanged();
        }
    }

    private void createTopPanel() {
        HUDTitleBar topBar = new HUDTitleBar(selectorHeader, selectorHeader,
                closeIcon, closeIcon, closeOverIcon, closePressedIcon);
        add(topBar, BorderLayout.NORTH);
        topBar.installListeners();
    }


    private void createCentralPanel() {

        JPanel primaryContainer = new JPanel(new BorderLayout());

        JPanel container = new JPanel(new FlowLayout());
        container.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.setBackground(UIHelper.BG_COLOR);

        for (int index = startSize; index <= endSize; index++) {
            CustomListModel model = new CustomListModel();
            partitionToListModel.put(index, model);

            JList newList = new JList(model);
            newList.addListSelectionListener(this);
            newList.setCellRenderer(new MacroListCellRenderer());

            JPanel listContainer = new JPanel(new BorderLayout());

            JLabel motifSizeLabel = UIHelper.createLabel("Depth " + index, UIHelper.VER_10_BOLD, AutoMacronUIHelper.GREY_COLOR, JLabel.CENTER);

            Box sizeContainer = Box.createHorizontalBox();
            sizeContainer.add(motifSizeLabel);
            sizeContainer.setBorder(new RoundedBorder(
                    AutoMacronUIHelper.LIGHT_GREY_COLOR, AutoMacronUIHelper.LIGHT_GREY_COLOR, 5));
            sizeContainer.setAlignmentX(Box.CENTER_ALIGNMENT);

            listContainer.add(sizeContainer, BorderLayout.NORTH);

            JScrollPane listScroller = new JScrollPane(newList);
            listScroller.setBorder(new MatteBorder(0, 2, 0, 0, AutoMacronUIHelper.LIGHT_GREY_COLOR));
            listScroller.setPreferredSize(new Dimension(350, 500));
            listScroller.setBorder(new EmptyBorder(5, 3, 5, 0));

            listContainer.add(listScroller, BorderLayout.CENTER);
            container.add(listContainer);

            partitionToList.put(index, newList);
            partitionToListPanel.put(index, listContainer);
        }

        JScrollPane listScroller = new JScrollPane(container, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        listScroller.setBorder(null);

        primaryContainer.add(createFilterPanel(), BorderLayout.NORTH);
        primaryContainer.add(listScroller, BorderLayout.CENTER);
        primaryContainer.add(createSelectedMacroPanel(), BorderLayout.SOUTH);

        add(primaryContainer, BorderLayout.CENTER);

        // when an item is selected in some list, it's corresponding motifs are selected in the other lists so
        // users can filter out any motifs they do not wish to show.
    }

    private Container createFilterPanel() {
        Box filters = Box.createHorizontalBox();
        filters.setBackground(AutoMacronUIHelper.LIGHT_GREY_COLOR);

        Hashtable<Integer, JComponent> minLabels = new Hashtable<Integer, JComponent>();
        Hashtable<Integer, JComponent> maxLabels = new Hashtable<Integer, JComponent>();

        for (int index = startSize; index <= endSize; index++) {
            minLabels.put(index, UIHelper.createLabel(Integer.toString(index), UIHelper.VER_10_BOLD, AutoMacronUIHelper.GREY_COLOR, JLabel.CENTER));
            maxLabels.put(index, UIHelper.createLabel(Integer.toString(index), UIHelper.VER_10_BOLD, AutoMacronUIHelper.GREY_COLOR, JLabel.CENTER));
        }

        final JSlider minSlider = new JSlider(startSize, endSize, startSize);
        minSlider.setPaintLabels(true);
        minSlider.setPaintTicks(true);
        minSlider.setSnapToTicks(true);
        minSlider.setLabelTable(minLabels);
        filters.add(minSlider);

        final JSlider maxSlider = new JSlider(startSize, endSize, endSize);
        maxSlider.setPaintLabels(true);
        maxSlider.setPaintTicks(true);
        maxSlider.setSnapToTicks(true);
        maxSlider.setLabelTable(maxLabels);
        filters.add(maxSlider);

        minSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                JSlider source = (JSlider) changeEvent.getSource();
                if (!source.getValueIsAdjusting()) {
                    updateShownLists(minSlider.getValue(), maxSlider.getValue());

                    if (minSlider.getValue() > maxSlider.getValue()) {
                        maxSlider.setValue(minSlider.getValue());
                    }
                }
            }
        });

        maxSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                JSlider source = (JSlider) changeEvent.getSource();
                if (!source.getValueIsAdjusting()) {
                    if (maxSlider.getValue() < minSlider.getValue()) {
                        maxSlider.setValue(minSlider.getValue());
                    }
                    int sliderValue = maxSlider.getValue();

                    if (!maxSlider.getValueIsAdjusting())
                        updateShownLists(minSlider.getValue(), sliderValue);
                }
            }
        });


        filters.add(Box.createHorizontalStrut(100));

        createFilterButtons();

        filters.add(allMotifsFilter);
        filters.add(linearMotifsFilter);
        filters.add(branchMotifsFilter);
        filters.add(mergeMotifsFilter);
        filters.add(Box.createHorizontalStrut(200));
        filters.add(Box.createHorizontalGlue());

        JPanel filterContainer = new JPanel(new BorderLayout());
        filterContainer.setBorder(new MatteBorder(1, 0, 1, 0, AutoMacronUIHelper.LIGHT_GREY_COLOR));
        filterContainer.add(filters, BorderLayout.WEST);

        return filterContainer;
    }

    private void createFilterButtons() {
        allMotifsFilter = new JLabel(allMotifs);
        allMotifsFilter.addMouseListener(new CommonMouseAdapter() {
            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                super.mouseExited(mouseEvent);
                allMotifsFilter.setBorder(null);
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                super.mouseEntered(mouseEvent);
                allMotifsFilter.setBorder(new MatteBorder(1, 0, 0, 0, AutoMacronUIHelper.DARK_ORANGE_COLOR));
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);
                clearFilters();
                enabledFilters.clear();
                allMotifsFilter.setIcon(allMotifsOver);

                allMotifsFilter.setIcon(allMotifsOver);
                loadingIndicator.showUI(StatusUI.WORKING, MacroSelectionUtilUI.this);

                Thread filterThread = new Thread(new Runnable() {
                    public void run() {
                        performFiltering();
                    }
                });
                filterThread.start();
            }
        });

        linearMotifsFilter = new JLabel(linearMotifs);
        attachMouseListener(linearMotifsFilter, Filters.LINEAR, linearMotifs, linearMotifsOver);

        branchMotifsFilter = new JLabel(branchMotifs);
        attachMouseListener(branchMotifsFilter, Filters.BRANCH, branchMotifs, branchMotifsOver);

        mergeMotifsFilter = new JLabel(mergeMotifs);
        attachMouseListener(mergeMotifsFilter, Filters.MERGE, mergeMotifs, mergeMotifsOver);

    }

    private void attachMouseListener(final JLabel label, final Filters filter, final ImageIcon icon, final ImageIcon overIcon) {
        label.addMouseListener(new CommonMouseAdapter() {
            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                super.mouseExited(mouseEvent);
                label.setBorder(null);
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                super.mouseEntered(mouseEvent);
                label.setBorder(new MatteBorder(1, 0, 0, 0, AutoMacronUIHelper.DARK_ORANGE_COLOR));
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);


                if (enabledFilters.contains(filter)) {
                    label.setIcon(icon);
                } else {
                    label.setIcon(overIcon);
                }

                if (enabledFilters.contains(filter)) {
                    enabledFilters.remove(filter);
                } else {
                    enabledFilters.add(filter);
                }

                allMotifsFilter.setIcon(enabledFilters.isEmpty() ? allMotifsOver : allMotifs);
                loadingIndicator.showUI(StatusUI.WORKING, MacroSelectionUtilUI.this);

                Thread filterThread = new Thread(new Runnable() {
                    public void run() {
                        performFiltering();
                    }
                });
                filterThread.start();

            }
        });
    }

    private void performFiltering() {

        for (int index = startSize; index <= endSize; index++) {
            if (enabledFilters.isEmpty()) {
                // reset list model to original
                if (partitionToList.get(index) != null) {
                    partitionToList.get(index).setModel(partitionToListModel.get(index));
                    partitionToList.get(index).updateUI();
                }
            } else {
                // only modify the lists in view, this will make things quicker when filtering has already been introduced.
                if (partitionToListPanel.get(index) != null) {
                    DefaultListModel originalModel = partitionToListModel.get(index);

                    if (originalModel != null) {
                        DefaultListModel filteredModel = new DefaultListModel();
                        for (int listIndex = 0; listIndex < originalModel.getSize(); listIndex++) {
                            // check conditions.
                            MacroUI macroValue = (MacroUI) originalModel.get(listIndex);

                            String macroStringRepresentation = MotifProcessingUtils.findAndCollapseMergeEvents(macroValue.getMacro().getMotif().getStringRepresentation());
                            if (enabledFilters.contains(Filters.BRANCH)) {
                                if (macroStringRepresentation.contains(",")) {
                                    filteredModel.addElement(originalModel.get(listIndex));
                                }
                            }
                            if (enabledFilters.contains(Filters.LINEAR)) {
                                if (!macroStringRepresentation.contains(",")) {
                                    filteredModel.addElement(originalModel.get(listIndex));
                                }
                            }

                            if (enabledFilters.contains(Filters.MERGE)) {
                                if (macroStringRepresentation.contains("ref_")) {
                                    filteredModel.addElement(originalModel.get(listIndex));
                                }
                            }

                        }
                        partitionToList.get(index).setModel(filteredModel);
                        partitionToList.get(index).updateUI();
                    }
                }
            }
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                loadingIndicator.setVisible(false);
            }
        });
    }

    private void clearFilters() {
        allMotifsFilter.setIcon(allMotifsOver);
        linearMotifsFilter.setIcon(linearMotifs);
        branchMotifsFilter.setIcon(branchMotifs);
        mergeMotifsFilter.setIcon(mergeMotifs);
    }

    private void updateShownLists(int minValue, int maxValue) {

        for (int index = startSize; index <= endSize; index++) {
            if (index >= minValue && index <= maxValue) {
                partitionToListPanel.get(index).setVisible(true);
            } else {
                partitionToListPanel.get(index).setVisible(false);
            }
        }
    }

    private Container createSelectedMacroPanel() {
        selectedMacroContainer = new JPanel(new FlowLayout());

        selectedMacroContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        selectedMacroContainer.setBackground(UIHelper.BG_COLOR);

        JScrollPane selectedMacroScroller = new JScrollPane(selectedMacroContainer, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        selectedMacroScroller.setBorder(new EmptyBorder(2, 2, 2, 2));
        selectedMacroScroller.setPreferredSize(new Dimension(500, 100));

        JPanel sectionContainer = new JPanel(new BorderLayout());
        sectionContainer.setBorder(new MatteBorder(2, 0, 2, 0, AutoMacronUIHelper.LIGHT_GREY_COLOR));

        sectionContainer.add(UIHelper.wrapComponentInPanel(UIHelper.createLabel("Selected Macros",
                AutoMacronUIHelper.getCustomFont(AutoMacronUIHelper.PACIFICO_12),
                AutoMacronUIHelper.GREY_COLOR, JLabel.CENTER)), BorderLayout.WEST);
        sectionContainer.add(selectedMacroScroller, BorderLayout.CENTER);

        return sectionContainer;
    }

    public void addMacroToSelectedMacroPanel(SelectedMacroItem selectedMacroItem) {
        if (!selectedMacros.contains(selectedMacroItem.getMacroUI())) {
            selectedMacros.add(selectedMacroItem.getMacroUI());
            selectedMacroItem.getMacroUI().setSelected(true);
            selectedMacroItem.addPropertyChangeListener("removeItem", new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    SelectedMacroItem macroToRemove = (SelectedMacroItem) propertyChangeEvent.getNewValue();
                    macroToRemove.getMacroUI().setSelected(false);
                    selectedMacros.remove(macroToRemove.getMacroUI());
                    selectedMacroContainer.remove(macroToRemove.getMacroUI());
                    selectedMacroContainer.updateUI();

                    if(partitionToListModel.containsKey(macroToRemove.getMacroUI().getMacro().getMotif().getDepth()))
                        partitionToListModel.get(macroToRemove.getMacroUI().getMacro().getMotif().getDepth()).fireContentsChanged();

                    correctPenaltyForMacro(macroToRemove);

                    firePropertyChange("motifsUpdated", null, selectedMacros);
                }
            });

            updateSubMotifsWithPenalty(selectedMacroItem);

            selectedMacroContainer.add(selectedMacroItem);
            selectedMacroContainer.updateUI();
            firePropertyChange("motifsUpdated", null, selectedMacros);
        }
    }

    private void updateSubMotifsWithPenalty(SelectedMacroItem selectedMacroItem) {
        int depth = selectedMacroItem.getMacroUI().getMacro().getMotif().getDepth();
        // only iterate records below current depth selection
        Motif motif = selectedMacroItem.getMacroUI().getMacro().getMotif();
        Collection<Set<Long>> nodesInMotif = motif.getNodesInMotif();
        Set<Long> flattenedCollection = SetUtils.flattenCollection(nodesInMotif);

        selectedMacroToPenalties.put(selectedMacroItem.getMacroUI(), new HashSet<PenaltyRecord>());
        for (int listIndex = startSize; listIndex < depth; listIndex++) {
            if (partitionToListModel.containsKey(listIndex)) {
                CustomListModel model = partitionToListModel.get(listIndex);

                boolean modificationMade = false;
                for (int itemIndex = 0; itemIndex < model.getSize(); itemIndex++) {

                    MacroUI macroUI = (MacroUI) model.getElementAt(itemIndex);
                    // this is not a string matching problem, this is a set comparison problem to determine if there
                    // is a complete intersection in nodes.
                    Set<Long> macroFlattenedCollection = SetUtils.flattenCollection(macroUI.getMacro().getMotif().getNodesInMotif());
                    int size = Sets.intersection(macroFlattenedCollection, flattenedCollection).size();
                    if (size == macroFlattenedCollection.size()) {
                        double difference = macroUI.getMacro().getMotif().getCumulativeUsage() - motif.getCumulativeUsage();

                        double penalty;
                        if (difference == 0) {
                            penalty = 1;
                            macroUI.getMacro().setPenalty(macroUI.getMacro().getPenalty() + penalty);
                        } else {
                            penalty = difference / motif.getCumulativeUsage();
                            macroUI.getMacro().setPenalty(penalty);
                        }
                        selectedMacroToPenalties.get(selectedMacroItem.getMacroUI()).add(new PenaltyRecord(macroUI, penalty));
                        modificationMade = true;
                    }
                }

                if (modificationMade) model.fireContentsChanged();
            }
        }
    }

    private void createSouthPanel() {
        FooterPanel footer = new FooterPanel(MacroSelectionUtilUI.this, UIHelper.BG_COLOR, resizeIcon);
        add(footer, BorderLayout.SOUTH);
    }

    public void addMacro(int length, MacroUI macroUI) {
        if (partitionToListModel.containsKey(length)) {
            partitionToListModel.get(length).addElement(macroUI);
        }
    }

    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        JList list = (JList) listSelectionEvent.getSource();
        if (list.getSelectedValue() != null) {
            addMacroToSelectedMacroPanel(new SelectedMacroItem((MacroUI) list.getSelectedValue()));
        }
    }

    enum Filters {
        NONE, MERGE, BRANCH, LINEAR, MERGE_AND_BRANCH;
    }

    class CustomListModel extends DefaultListModel {

        public void fireContentsChanged() {
            super.fireContentsChanged(this, 0, getSize() - 1);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }

    class PenaltyRecord {

        private final MacroUI macroUI;
        private final double penaltyRecorded;

        PenaltyRecord(MacroUI macroUI, double penaltyRecorded) {
            this.macroUI = macroUI;
            this.penaltyRecorded = penaltyRecorded;
        }

        public MacroUI getMacroUI() {
            return macroUI;
        }

        public double getPenaltyRecorded() {
            return penaltyRecorded;
        }
    }

}
