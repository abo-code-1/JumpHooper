package com.starbots.starjump.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Synthesizes retro sound effects in code (no audio files shipped). Each effect
 * is rendered to a PCM buffer, wrapped in a WAV container written to a temp
 * file, and loaded as a libGDX {@link Sound}. Everything is null-safe: if
 * synthesis fails the game simply runs without that effect.
 */
public final class SfxSynth {
    private SfxSynth() {}

    private static final int SR = 44100;

    public static Sound jump() {
        return build("jump", sweep(0.18f, 420f, 880f, 0.45f, Wave.SQUARE, 6f));
    }

    public static Sound land() {
        return build("land", sweep(0.12f, 200f, 90f, 0.5f, Wave.SINE, 9f));
    }

    public static Sound hit() {
        return build("hit", sweep(0.13f, 760f, 180f, 0.5f, Wave.SQUARE, 8f));
    }

    public static Sound explosion() {
        float dur = 0.45f;
        float[] buf = new float[(int) (SR * dur)];
        for (int i = 0; i < buf.length; i++) {
            float t = i / (float) SR;
            float env = (float) Math.exp(-7f * t);
            float noise = (float) (Math.random() * 2 - 1);
            float rumble = (float) Math.sin(2 * Math.PI * 60 * t);
            buf[i] = (noise * 0.6f + rumble * 0.4f) * env;
        }
        return build("explosion", buf);
    }

    public static Sound powerup() {
        float[] a = tone(0.09f, 520f, 0.4f, Wave.SINE);
        float[] b = tone(0.09f, 700f, 0.4f, Wave.SINE);
        float[] c = tone(0.12f, 1040f, 0.4f, Wave.SINE);
        return build("powerup", concat(a, b, c));
    }

    /** A springy "boing" for trampolines/springs. */
    public static Sound spring() {
        return build("spring", sweep(0.22f, 320f, 1150f, 0.45f, Wave.SQUARE, 3.5f));
    }

    /** A sustained whoosh for the jetpack. */
    public static Sound jetpack() {
        float dur = 0.5f;
        float[] buf = new float[(int) (SR * dur)];
        for (int i = 0; i < buf.length; i++) {
            float t = i / (float) SR;
            float env = Math.min(1f, t * 8f) * Math.min(1f, (dur - t) * 8f);
            float noise = (float) (Math.random() * 2 - 1);
            float tone = (float) Math.sin(2 * Math.PI * 170 * t);
            buf[i] = (noise * 0.5f + tone * 0.3f) * 0.5f * env;
        }
        return build("jetpack", buf);
    }

    public static Sound boss() {
        float dur = 0.6f;
        float[] buf = new float[(int) (SR * dur)];
        for (int i = 0; i < buf.length; i++) {
            float t = i / (float) SR;
            float env = (float) Math.min(1f, t * 6f) * (float) Math.exp(-2.2f * t);
            float a = (float) Math.sin(2 * Math.PI * 90 * t);
            float b = (float) Math.sin(2 * Math.PI * 96 * t);
            float noise = (float) (Math.random() * 2 - 1) * 0.2f;
            buf[i] = (a * 0.5f + b * 0.5f + noise) * 0.55f * env;
        }
        return build("boss", buf);
    }

    public static Sound gameOver() {
        float[] a = tone(0.18f, 600f, 0.4f, Wave.SQUARE);
        float[] b = tone(0.18f, 460f, 0.4f, Wave.SQUARE);
        float[] c = tone(0.26f, 300f, 0.4f, Wave.SQUARE);
        return build("gameover", concat(a, b, c));
    }

    // --- synthesis primitives --------------------------------------------------

    private enum Wave { SINE, SQUARE }

    private static float osc(Wave w, double phase) {
        double s = Math.sin(phase);
        return w == Wave.SQUARE ? (s >= 0 ? 1f : -1f) : (float) s;
    }

    private static float[] tone(float dur, float freq, float amp, Wave w) {
        return sweep(dur, freq, freq, amp, w, 5f);
    }

    /** Frequency sweep from {@code f0} to {@code f1} with exponential decay. */
    private static float[] sweep(float dur, float f0, float f1, float amp, Wave w, float decay) {
        int n = (int) (SR * dur);
        float[] buf = new float[n];
        double phase = 0;
        for (int i = 0; i < n; i++) {
            float t = i / (float) n;
            float freq = f0 + (f1 - f0) * t;
            phase += 2 * Math.PI * freq / SR;
            float env = (float) Math.exp(-decay * (i / (float) SR));
            buf[i] = osc(w, phase) * amp * env;
        }
        return buf;
    }

    private static float[] concat(float[]... parts) {
        int n = 0;
        for (float[] p : parts) n += p.length;
        float[] out = new float[n];
        int o = 0;
        for (float[] p : parts) {
            System.arraycopy(p, 0, out, o, p.length);
            o += p.length;
        }
        return out;
    }

    // --- WAV packaging ---------------------------------------------------------

    private static Sound build(String name, float[] samples) {
        try {
            byte[] wav = toWav(samples);
            File dir = new File(System.getProperty("java.io.tmpdir"), "starjump-sfx");
            dir.mkdirs();
            File f = new File(dir, name + ".wav");
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(wav);
            }
            return Gdx.audio.newSound(Gdx.files.absolute(f.getAbsolutePath()));
        } catch (Throwable t) {
            Gdx.app.error("SfxSynth", "Failed to synthesize '" + name + "'", t);
            return null;
        }
    }

    private static byte[] toWav(float[] samples) {
        int dataLen = samples.length * 2;
        ByteBuffer bb = ByteBuffer.allocate(44 + dataLen).order(ByteOrder.LITTLE_ENDIAN);
        bb.put("RIFF".getBytes(StandardCharsets.US_ASCII));
        bb.putInt(36 + dataLen);
        bb.put("WAVE".getBytes(StandardCharsets.US_ASCII));
        bb.put("fmt ".getBytes(StandardCharsets.US_ASCII));
        bb.putInt(16);                 // fmt chunk size
        bb.putShort((short) 1);        // PCM
        bb.putShort((short) 1);        // mono
        bb.putInt(SR);                 // sample rate
        bb.putInt(SR * 2);             // byte rate (mono, 16-bit)
        bb.putShort((short) 2);        // block align
        bb.putShort((short) 16);       // bits per sample
        bb.put("data".getBytes(StandardCharsets.US_ASCII));
        bb.putInt(dataLen);
        for (float s : samples) {
            int v = Math.round(Math.max(-1f, Math.min(1f, s)) * 32767f);
            bb.putShort((short) v);
        }
        return bb.array();
    }
}
