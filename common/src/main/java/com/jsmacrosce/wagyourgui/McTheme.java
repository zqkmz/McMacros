package com.jsmacrosce.wagyourgui;

import net.minecraft.client.gui.GuiGraphics;

/**
 * The McMacros look: a dark navy background with glowing blue outlines and panels whose corners are cut
 * at an angle, like a sci-fi control panel.
 *
 * <p>The original screens were written with a handful of fixed colours (white outlines, black panels,
 * translucent white hover fills). Instead of rewriting every screen, the shared widgets pass their colours
 * through {@link #fill}, {@link #border}, {@link #hover} and {@link #text}, which swap those original
 * colours for the theme's and leave any other colour alone. Colours a script chooses on purpose therefore
 * still show up as written.</p>
 */
public final class McTheme {
    private McTheme() {
    }

    /**
     * True while one of McMacros' own screens is open. Screens that scripts build themselves
     * ({@code ScriptScreen}) opt out, so their buttons keep exactly the colours the script chose.
     */
    public static boolean enabled() {
        net.minecraft.client.gui.screens.Screen screen = net.minecraft.client.Minecraft.getInstance().screen;
        return screen instanceof BaseScreen && ((BaseScreen) screen).isMcThemed();
    }

    // Palette (packed ARGB)
    /** Screen background behind everything. */
    public static final int BACKGROUND = 0xF0060E18;
    /** Faint grid lines drawn over the background. */
    public static final int GRID = 0x143FB4FF;
    /** Panel and button fill. */
    public static final int PANEL = 0xF00B1C2D;
    /** Slightly raised panel, used for headers and selected rows. */
    public static final int PANEL_HI = 0xF010304B;
    /** Normal outline. */
    public static final int LINE = 0xFF1F5F92;
    /** Bright outline for focus, hover and the selected item. */
    public static final int LINE_BRIGHT = 0xFF4CBBFF;
    /** Hover fill. */
    public static final int HOVER = 0xFF145184;
    /** Main text. */
    public static final int TEXT = 0xFFD7ECFA;
    /** Secondary text. */
    public static final int TEXT_DIM = 0xFF8FB1C9;
    /** Accent for titles and the logo. */
    public static final int ACCENT = 0xFF7FCBFF;
    /** Running / success. */
    public static final int SUCCESS = 0xFF2BD4AE;
    /** Recording / warning. */
    public static final int WARNING = 0xFFE0533F;
    /** Delete / error. */
    public static final int DANGER = 0xFFC2405E;

    /** Size of the cut-off corner on buttons, in GUI pixels. */
    public static final int CUT_SMALL = 2;
    /** Size of the cut-off corner on panels and overlays, in GUI pixels. */
    public static final int CUT_LARGE = 4;

    /** Swaps the original fill colours for the theme's; any other colour is kept. */
    public static int fill(int color) {
        switch (color) {
            case 0x00000000:
            case 0xFF000000:
                return PANEL;
            default:
                return color;
        }
    }

    /** Swaps the original outline colours for the theme's; any other colour is kept. */
    public static int border(int color) {
        switch (color) {
            case 0xFFFFFFFF:
            case 0x7FFFFFFF:
            case 0x7F7F7F7F:
            case 0xFF7F7F7F:
            case 0xFF707070:
            case 0xFF000000:
                return LINE;
            default:
                return color;
        }
    }

    /** Swaps the original hover colours for the theme's; any other colour is kept. */
    public static int hover(int color) {
        switch (color) {
            case 0x7FFFFFFF:
            case 0x7F7F7F7F:
            case 0x3FFFFFFF:
            case 0x4FFFFFFF:
            case 0xFF33508F:
                return HOVER;
            default:
                return color;
        }
    }

    /** Swaps the original text colours for the theme's; any other colour is kept. */
    public static int text(int color) {
        switch (color) {
            case 0xFFFFFFFF:
            case 0x00FFFFFF:
                return TEXT;
            case 0xFF7F7F7F:
            case 0x007F7F7F:
                return TEXT_DIM;
            default:
                return color;
        }
    }

    /** Fills a rectangle whose four corners are cut off diagonally by {@code cut} pixels. */
    public static void fillCut(GuiGraphics g, int x1, int y1, int x2, int y2, int cut, int color) {
        int h = y2 - y1;
        cut = Math.max(0, Math.min(cut, Math.min(x2 - x1, h) / 2));
        for (int i = 0; i < cut; i++) {
            int inset = cut - i;
            g.fill(x1 + inset, y1 + i, x2 - inset, y1 + i + 1, color);
            g.fill(x1 + inset, y2 - i - 1, x2 - inset, y2 - i, color);
        }
        g.fill(x1, y1 + cut, x2, y2 - cut, color);
    }

    /** Draws a one-pixel outline along a rectangle whose corners are cut off diagonally by {@code cut} pixels. */
    public static void outlineCut(GuiGraphics g, int x1, int y1, int x2, int y2, int cut, int color) {
        cut = Math.max(0, Math.min(cut, Math.min(x2 - x1, y2 - y1) / 2));
        g.fill(x1 + cut, y1, x2 - cut, y1 + 1, color);
        g.fill(x1 + cut, y2 - 1, x2 - cut, y2, color);
        g.fill(x1, y1 + cut, x1 + 1, y2 - cut, color);
        g.fill(x2 - 1, y1 + cut, x2, y2 - cut, color);
        for (int i = 1; i < cut; i++) {
            int inset = cut - i;
            g.fill(x1 + inset, y1 + i, x1 + inset + 1, y1 + i + 1, color);
            g.fill(x2 - inset - 1, y1 + i, x2 - inset, y1 + i + 1, color);
            g.fill(x1 + inset, y2 - i - 1, x1 + inset + 1, y2 - i, color);
            g.fill(x2 - inset - 1, y2 - i - 1, x2 - inset, y2 - i, color);
        }
    }

    /** A filled panel with a cut-corner outline. */
    public static void panel(GuiGraphics g, int x1, int y1, int x2, int y2, int cut, int fill, int outline) {
        fillCut(g, x1, y1, x2, y2, cut, fill);
        outlineCut(g, x1, y1, x2, y2, cut, outline);
    }

    /** The full-screen background: dark navy with a faint grid. */
    public static void background(GuiGraphics g, int width, int height) {
        g.fill(0, 0, width, height, BACKGROUND);
        for (int x = 0; x < width; x += 16) {
            g.fill(x, 0, x + 1, height, GRID);
        }
        for (int y = 0; y < height; y += 16) {
            g.fill(0, y, width, y + 1, GRID);
        }
    }
}
