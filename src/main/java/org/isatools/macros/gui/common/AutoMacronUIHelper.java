package org.isatools.macros.gui.common;

import org.isatools.isacreator.common.UIHelper;
import org.isatools.macros.utils.MotifStats;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static java.awt.Font.TRUETYPE_FONT;
import static java.awt.Font.createFont;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 06/06/2012
 *         Time: 16:46
 */
public class AutoMacronUIHelper {

    private static InputStream PACIFICO_FONT = AutoMacronUIHelper.class.getResourceAsStream("/font/Pacifico.ttf");
    

    public static final Color DARK_BLUE_COLOR = new Color(72, 174, 192);
    public static final Color LIGHT_BLUE_COLOR = new Color(167, 219, 216);
    public static final Color CREME_COLOR = new Color(224, 228, 204);
    public static final Color LIGHT_ORANGE_COLOR = new Color(243, 134, 48);
    public static final Color DARK_ORANGE_COLOR = new Color(250, 105, 0);
    public static final Color GREEN_COLOR = new Color(129, 163, 43);
    public static final Color RED_COLOR = new Color(190, 30, 45);


    public static final Color BG_COLOR = Color.WHITE;
    public static final Color MEDIUM_GREY_COLOR = new Color(188, 190, 192);
    public static final Color LIGHT_GREY_COLOR = new Color(241, 242, 242);
    public static final Color GREY_COLOR = new Color(88, 89, 91);

    public static final Map<String, Font> CUSTOM_FONTS = new HashMap<String, Font>();

    public static final String PACIFICO_8 = "PACIFICO_8";
    public static final String PACIFICO_10 = "PACIFICO_10";
    public static final String PACIFICO_12 = "PACIFICO_12";

    public static final Font VER_6_PLAIN = new Font("Verdana", Font.PLAIN, 6);

    static {

        try {
            CUSTOM_FONTS.put("PACIFICO", createFont(TRUETYPE_FONT, PACIFICO_FONT));
            
            CUSTOM_FONTS.put(PACIFICO_8, CUSTOM_FONTS.get("PACIFICO").deriveFont(Font.PLAIN, 8));
            CUSTOM_FONTS.put(PACIFICO_10, CUSTOM_FONTS.get("PACIFICO").deriveFont(Font.PLAIN, 10));
            CUSTOM_FONTS.put(PACIFICO_12, CUSTOM_FONTS.get("PACIFICO").deriveFont(Font.PLAIN, 12));

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(CUSTOM_FONTS.get("PACIFICO"));
        } catch (FontFormatException e) {
            e.printStackTrace(); 
        } catch (IOException e) {
            e.printStackTrace(); 
        }

    }
    
    public static Font getCustomFont(String fontName) {
        if(CUSTOM_FONTS.containsKey(fontName)) {
            return CUSTOM_FONTS.get(fontName);
        }
        return CUSTOM_FONTS.get(PACIFICO_8);
    }

    private static Color[] COLOR_GRADIENT = new Color[]{DARK_BLUE_COLOR, LIGHT_BLUE_COLOR, CREME_COLOR,
            LIGHT_ORANGE_COLOR, DARK_ORANGE_COLOR};

    public static Color selectColorForValue(int value) {
        if (value < MotifStats.getMeanUsage()) {
            if (value == MotifStats.getMinUsage()) {
                return COLOR_GRADIENT[0];
            } else {
                return COLOR_GRADIENT[1];
            }
        } else if (value > MotifStats.getMeanUsage()) {
            if (value == MotifStats.getMaxUsage()) {
                return COLOR_GRADIENT[4];
            } else {
                return COLOR_GRADIENT[3];
            }
        } else {
            return COLOR_GRADIENT[2];
        }
    }


    public static ImageIcon scaleImageIcon(String filePath, double maxHeight) {
        ImageIcon icon = new ImageIcon(filePath);
        double scaleFactor = maxHeight / icon.getIconHeight();
        Image image = icon.getImage().getScaledInstance((int) (icon.getIconWidth() * scaleFactor), (int) maxHeight, Image.SCALE_SMOOTH);
        icon.setImage(image);
        return icon;
    }

    public static ImageIcon scaleImageIcon(String filePath, int maxHeight, int maxWidth) {
        ImageIcon icon = new ImageIcon(filePath);

        Dimension largestDimension = new Dimension(maxWidth, maxHeight);

        // Original size
        int imageWidth = icon.getIconWidth();
        int imageHeight = icon.getIconHeight();

        float aspectRatio = (float) imageWidth / imageHeight;

        if (imageWidth > maxWidth || imageHeight > maxHeight) {
            if ((float) largestDimension.width / largestDimension.height > aspectRatio) {
                largestDimension.width = (int) Math.ceil(largestDimension.height * aspectRatio);
            }
            else {
                largestDimension.height = (int) Math.ceil(largestDimension.width / aspectRatio);
            }

            imageWidth = largestDimension.width;
            imageHeight = largestDimension.height;
        }

        Image image = icon.getImage().getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);
        icon.setImage(image);
        return icon;
    }

    public static Container createTitlePanel(String text, Dimension size) {
        Box headerPanel = Box.createHorizontalBox();
        headerPanel.setPreferredSize(size);
        headerPanel.setOpaque(false);
        headerPanel.add(Box.createHorizontalStrut(5));
        JLabel titleLabel = UIHelper.createLabel(text, UIHelper.VER_9_BOLD, AutoMacronUIHelper.GREY_COLOR);
        titleLabel.setBackground(AutoMacronUIHelper.DARK_BLUE_COLOR);
        headerPanel.add(titleLabel);

        return headerPanel;
    }


}
