package org.isatools.macros.gui.titlebar;

import org.isatools.isacreator.common.UIHelper;
import org.isatools.isacreator.effects.DraggablePaneMouseInputHandler;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class AutoMacronTitleBar extends JComponent {

    private int preferredHeight = 50;
    private Image backgroundGradient = new ImageIcon(getClass().getResource("/images/ui/header/bg_active.png")).getImage();
    private Image grip= new ImageIcon(getClass().getResource("/images/ui/header/automacron_logo.png")).getImage();

    private Image close = new ImageIcon(getClass().getResource("/images/ui/header/buttons/close.png")).getImage();
    private Image closeOver = new ImageIcon(getClass().getResource("/images/ui/header/buttons/close_over.png")).getImage();
    private Image closePressed = new ImageIcon(getClass().getResource("/images/ui/header/buttons/close_pressed.png")).getImage();

    private boolean dispose;
    private MouseInputAdapter handler;

    public AutoMacronTitleBar(boolean dispose) {
        this.dispose = dispose;
        setLayout(new GridBagLayout());
        createButtons();
        setBackground(UIHelper.BG_COLOR);
    }

    public void installListeners() {
        handler = new DraggablePaneMouseInputHandler(this);
        Window window = SwingUtilities.getWindowAncestor(this);
        window.addMouseListener(handler);
        window.addMouseMotionListener(handler);

        window.addWindowListener(new WindowHandler());
    }

    public void removeListeners() {
        Window window = SwingUtilities.getWindowAncestor(this);
        window.removeMouseListener(handler);
        window.removeMouseMotionListener(handler);

        for (WindowListener windowListener : window.getWindowListeners()) {
            window.removeWindowListener(windowListener);
        }
    }

    private void createButtons() {
        add(Box.createHorizontalGlue(),
                new GridBagConstraints(0, 0,
                        1, 1,
                        1.0, 1.0,
                        GridBagConstraints.EAST,
                        GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0),
                        0, 0));

        add(createButton(new CloseAction(),
                close, closePressed, closeOver),
                new GridBagConstraints(2, 0,
                        1, 1,
                        0.0, 1.0,
                        GridBagConstraints.NORTHEAST,
                        GridBagConstraints.NONE,
                        new Insets(1, 0, 0, 2),
                        0, 0));
    }

    private JButton createButton(final AbstractAction action,
                                 final Image image,
                                 final Image pressedImage,
                                 final Image overImage) {
        JButton button = new JButton(action);
        button.setIcon(new ImageIcon(image));
        button.setPressedIcon(new ImageIcon(pressedImage));
        button.setRolloverIcon(new ImageIcon(overImage));
        button.setRolloverEnabled(true);
        button.setBorder(null);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setMargin(new Insets(10, 0, 10, 20));
        button.setFocusable(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(image.getWidth(null),
                image.getHeight(null)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void close() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (dispose) {
            w.dispatchEvent(new WindowEvent(w,
                    WindowEvent.WINDOW_CLOSING));
            w.dispose();
        } else {
            w.setVisible(false);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.height = preferredHeight;
        return size;
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension size = super.getMinimumSize();
        size.height = preferredHeight;
        return size;
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension size = super.getMaximumSize();
        size.height = preferredHeight;
        return size;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!isVisible()) {
            return;
        }

        boolean active = SwingUtilities.getWindowAncestor(this).isActive();

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);

        Rectangle clip = g2.getClipBounds();

        g2.drawImage(backgroundGradient, clip.x, 0, clip.width, getHeight(), null);

        g2.drawImage(grip, 0, 0, null);
    }

    private class CloseAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            close();
        }
    }


    private class WindowHandler extends WindowAdapter {
        @Override
        public void windowActivated(WindowEvent ev) {
            getRootPane().repaint();
        }

        @Override
        public void windowDeactivated(WindowEvent ev) {
            getRootPane().repaint();
        }
    }
}