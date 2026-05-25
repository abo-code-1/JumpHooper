package com.starbots.starjump.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

import com.starbots.starjump.model.platform.PlatformKind;

/**
 * Loads and owns every texture, font and sound. Fonts are generated from the
 * original {@code .ttf}/{@code .otf} files via gdx-freetype and cached by
 * "family#size" so screens can request any size cheaply.
 */
public final class Assets implements Disposable {

    // Glyphs the Portuguese trivia needs beyond plain ASCII (accents, curly
    // quotes, ellipsis, arrow, dashes).
    private static final String CHARS =
            " ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
          + "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
          + "áàâãäéèêëíìîïóòôõöúùûüçñ"
          + "ÁÀÂÃÄÉÈÊËÍÌÎÏÓÒÔÕÖÚÙÛÜÇÑ"
          + "ºª°“”’‘…—–→";

    // Textures
    public Texture astronaut;
    public Texture background;       // in-game space backdrop
    public Texture startBackground;  // menu backdrop
    public Texture splashBackground; // game-over / menus backdrop
    public Texture logo;
    public Texture starbots;
    private final ObjectMap<PlatformKind, Texture> platformTextures = new ObjectMap<>();

    // Sound
    public Sound swoosh;

    // Font generators + cache
    private final ObjectMap<String, FreeTypeFontGenerator> generators = new ObjectMap<>();
    private final ObjectMap<String, BitmapFont> fontCache = new ObjectMap<>();

    public static final String NASALIZATION = "nasalization";
    public static final String THALEAH = "thaleah";
    public static final String MONTSERRAT = "montserrat";
    public static final String DYSLEXIC = "dyslexic";

    public void load() {
        astronaut        = texture("astronaut.png");
        background       = texture("background.jpg");
        startBackground  = texture("start.jpg");
        splashBackground = texture("splash_background.jpg");
        logo             = texture("logo.png");
        starbots         = texture("starbots.png");

        platformTextures.put(PlatformKind.GRASS,        texture("ground_grass.png"));
        platformTextures.put(PlatformKind.GRASS_BROKEN, texture("ground_grass_broken.png"));
        platformTextures.put(PlatformKind.STONE,        texture("ground_stone.png"));
        platformTextures.put(PlatformKind.STONE_BROKEN, texture("ground_stone_broken.png"));
        platformTextures.put(PlatformKind.LAVA,         texture("ground_lava.png"));

        swoosh = Gdx.audio.newSound(Gdx.files.internal("swoosh.mp3"));

        registerGenerator(NASALIZATION, "fonts/nasalization.ttf");
        registerGenerator(THALEAH, "fonts/ThaleahFat.ttf");
        registerGenerator(MONTSERRAT, "fonts/Montserrat.ttf");
        registerGenerator(DYSLEXIC, "fonts/OpenDyslexicAlta-Bold.otf");
    }

    private Texture texture(String path) {
        Texture t = new Texture(Gdx.files.internal(path), true);
        t.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        return t;
    }

    private void registerGenerator(String key, String path) {
        FileHandle fh = Gdx.files.internal(path);
        generators.put(key, new FreeTypeFontGenerator(fh));
    }

    public Texture platform(PlatformKind kind) {
        return platformTextures.get(kind);
    }

    /** Get (or generate + cache) a white font of the given family and size. */
    public BitmapFont font(String family, int size) {
        String key = family + '#' + size;
        BitmapFont cached = fontCache.get(key);
        if (cached != null) return cached;

        FreeTypeFontGenerator gen = generators.get(family);
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size = size;
        param.characters = CHARS;
        param.color = Color.WHITE;
        param.minFilter = Texture.TextureFilter.Linear;
        param.magFilter = Texture.TextureFilter.Linear;
        BitmapFont font = gen.generateFont(param);
        fontCache.put(key, font);
        return font;
    }

    @Override
    public void dispose() {
        astronaut.dispose();
        background.dispose();
        startBackground.dispose();
        splashBackground.dispose();
        logo.dispose();
        starbots.dispose();
        for (Texture t : platformTextures.values()) t.dispose();
        if (swoosh != null) swoosh.dispose();
        for (BitmapFont f : fontCache.values()) f.dispose();
        for (FreeTypeFontGenerator g : generators.values()) g.dispose();
    }
}
