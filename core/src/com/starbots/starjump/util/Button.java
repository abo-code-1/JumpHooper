package com.starbots.starjump.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/**
 * A tappable button: a white-bordered, vertical-gradient rounded bar with a
 * centred label, matching the original {@code RkButton + LinearGradient} look.
 * Geometry is in y-down world coordinates.
 */
public final class Button {
    public float x, top, w, h;
    public String label;
    public BitmapFont font;
    public Color gradTop, gradBot, textColor = Color.BLACK;

    public Button(float x, float top, float w, float h, String label, BitmapFont font,
                  Color gradTop, Color gradBot) {
        this.x = x; this.top = top; this.w = w; this.h = h;
        this.label = label; this.font = font;
        this.gradTop = gradTop; this.gradBot = gradBot;
    }

    /** Call inside a ShapeRenderer (shapes) session. */
    public void fill(Painter p) {
        p.gradientRect(x, top, w, h, gradTop, gradBot);
        p.border(x, top, w, h, 2f, Color.WHITE);
    }

    /** Call inside a SpriteBatch session. */
    public void drawLabel(Painter p) {
        Color prev = font.getColor().cpy();
        font.setColor(textColor);
        float textTop = top + (h - font.getCapHeight()) / 2f;
        p.textCentered(font, label, x + w / 2f, textTop);
        font.setColor(prev);
    }

    public boolean clicked(Painter p) {
        return p.clicked(x, top, w, h);
    }
}
