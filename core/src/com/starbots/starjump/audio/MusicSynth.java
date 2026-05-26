package com.starbots.starjump.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/** Generates two small loopable chiptune tracks at startup. */
public final class MusicSynth {
    private MusicSynth() {}

    private static final int SR = 44100;

    public static Music regular() {
        return build("regular-loop", renderRegular());
    }

    public static Music boss() {
        return build("boss-loop", renderBoss());
    }

    private static float[] renderRegular() {
        float duration = 8f;
        float bpm = 120f;
        int steps = 32;
        float stepDur = duration / steps;
        float[] arp = notes(new int[] {61, 64, 68, 73, 68, 64, 61, 56,
                                       59, 63, 66, 71, 66, 63, 59, 54});
        float[] bass = notes(new int[] {37, 37, 44, 44, 35, 35, 42, 42});
        float[] out = new float[(int) (duration * SR)];

        for (int i = 0; i < out.length; i++) {
            float t = i / (float) SR;
            int step = Math.min(steps - 1, (int) (t / stepDur));
            float local = (t - step * stepDur) / stepDur;
            float beat = t * bpm / 60f;

            float leadEnv = pluck(local, 0.65f);
            float bassEnv = pluck((t % (stepDur * 4f)) / (stepDur * 4f), 0.95f);
            float sparkle = triangle(arp[step % arp.length], t) * leadEnv * 0.22f;
            float sub = square(bass[(step / 4) % bass.length] * 0.5f, t) * bassEnv * 0.15f;
            float pulse = sine(arp[(step + 5) % arp.length] * 2f, t) * 0.04f;
            float kick = Math.max(0f, 1f - (beat % 1f) * 8f);
            out[i] = softClip(sparkle + sub + pulse + sine(70f, t) * kick * 0.08f);
        }
        return fadeLoop(out);
    }

    private static float[] renderBoss() {
        float duration = 8f;
        int steps = 32;
        float stepDur = duration / steps;
        float[] lead = notes(new int[] {49, 48, 49, 55, 48, 46, 48, 43});
        float[] bass = notes(new int[] {25, 25, 31, 25, 24, 24, 30, 24});
        float[] out = new float[(int) (duration * SR)];

        for (int i = 0; i < out.length; i++) {
            float t = i / (float) SR;
            int step = Math.min(steps - 1, (int) (t / stepDur));
            float local = (t - step * stepDur) / stepDur;
            float grit = ((hash(step * 17 + (int) (local * 12f)) & 255) / 255f) * 2f - 1f;

            float bassEnv = pluck(local, 0.92f);
            float leadEnv = pluck(local, 0.45f);
            float low = square(bass[(step / 4) % bass.length], t) * bassEnv * 0.23f;
            float alarm = saw(lead[step % lead.length], t) * leadEnv * 0.14f;
            float drone = sine(49f, t) * 0.08f + sine(51.5f, t) * 0.07f;
            float noiseHit = local < 0.12f ? grit * (1f - local / 0.12f) * 0.08f : 0f;
            out[i] = softClip(low + alarm + drone + noiseHit);
        }
        return fadeLoop(out);
    }

    private static float[] notes(int[] midi) {
        float[] f = new float[midi.length];
        for (int i = 0; i < midi.length; i++) {
            f[i] = (float) (440.0 * Math.pow(2.0, (midi[i] - 69) / 12.0));
        }
        return f;
    }

    private static float sine(float freq, float t) {
        return (float) Math.sin(2 * Math.PI * freq * t);
    }

    private static float square(float freq, float t) {
        return sine(freq, t) >= 0f ? 1f : -1f;
    }

    private static float triangle(float freq, float t) {
        float p = (t * freq) % 1f;
        return 4f * Math.abs(p - 0.5f) - 1f;
    }

    private static float saw(float freq, float t) {
        return ((t * freq) % 1f) * 2f - 1f;
    }

    private static float pluck(float x, float sustain) {
        float attack = Math.min(1f, x / 0.06f);
        float decay = (float) Math.exp(-4.5f * Math.max(0f, x - 0.06f));
        return attack * (sustain * decay);
    }

    private static int hash(int x) {
        x ^= x << 13;
        x ^= x >>> 17;
        x ^= x << 5;
        return x;
    }

    private static float softClip(float x) {
        return Math.max(-0.95f, Math.min(0.95f, x));
    }

    private static float[] fadeLoop(float[] buf) {
        int fade = Math.min(SR / 12, buf.length / 4);
        for (int i = 0; i < fade; i++) {
            float a = i / (float) fade;
            buf[i] *= a;
            buf[buf.length - 1 - i] *= a;
        }
        return buf;
    }

    private static Music build(String name, float[] samples) {
        try {
            byte[] wav = toWav(samples);
            File dir = new File(System.getProperty("java.io.tmpdir"), "starjump-music");
            dir.mkdirs();
            File f = new File(dir, name + ".wav");
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(wav);
            }
            return Gdx.audio.newMusic(Gdx.files.absolute(f.getAbsolutePath()));
        } catch (Throwable t) {
            Gdx.app.error("MusicSynth", "Failed to synthesize '" + name + "'", t);
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
        bb.putInt(16);
        bb.putShort((short) 1);
        bb.putShort((short) 1);
        bb.putInt(SR);
        bb.putInt(SR * 2);
        bb.putShort((short) 2);
        bb.putShort((short) 16);
        bb.put("data".getBytes(StandardCharsets.US_ASCII));
        bb.putInt(dataLen);
        for (float s : samples) {
            int v = Math.round(Math.max(-1f, Math.min(1f, s)) * 32767f);
            bb.putShort((short) v);
        }
        return bb.array();
    }
}
