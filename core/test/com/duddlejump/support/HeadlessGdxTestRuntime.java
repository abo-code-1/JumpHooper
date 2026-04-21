package com.duddlejump.support;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.backends.headless.mock.graphics.MockGraphics;
import com.badlogic.gdx.graphics.GL20;
import com.duddlejump.Config;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public final class HeadlessGdxTestRuntime {

    private static HeadlessApplication application;
    private static MockGraphics graphics;
    private static TestInput input;

    private HeadlessGdxTestRuntime() {
    }

    public static void boot() {
        if (application != null) {
            input.reset();
            return;
        }

        application = new HeadlessApplication(new ApplicationAdapter() {
        }, new HeadlessApplicationConfiguration());

        graphics = new MockGraphics();
        graphics.setWindowedMode(Config.VIEWPORT_WIDTH, Config.VIEWPORT_HEIGHT);

        GL20 gl20 = createNoOpGl20();
        graphics.setGL20(gl20);

        input = new TestInput();

        Gdx.graphics = graphics;
        Gdx.gl = gl20;
        Gdx.gl20 = gl20;
        Gdx.gl30 = null;
        Gdx.input = input.asInput();
    }

    public static void shutdown() {
        if (application == null) {
            return;
        }

        application.exit();
        application = null;
        graphics = null;
        input = null;

        Gdx.graphics = null;
        Gdx.gl = null;
        Gdx.gl20 = null;
        Gdx.gl30 = null;
        Gdx.input = null;
    }

    public static TestInput input() {
        return input;
    }

    public static void renderFrames(com.badlogic.gdx.Game game, int frameCount, float delta) {
        for (int i = 0; i < frameCount; i++) {
            Screen screen = game.getScreen();
            if (screen == null) {
                throw new IllegalStateException("Game has no active screen");
            }
            screen.render(delta);
            input.endFrame();
        }
    }

    private static GL20 createNoOpGl20() {
        AtomicInteger nextId = new AtomicInteger(1);
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                String name = method.getName();
                Class<?> returnType = method.getReturnType();

                switch (name) {
                    case "glCreateShader":
                    case "glCreateProgram":
                    case "glGenTexture":
                        return nextId.getAndIncrement();
                    case "glGetError":
                        return GL20.GL_NO_ERROR;
                    case "glCheckFramebufferStatus":
                        return GL20.GL_FRAMEBUFFER_COMPLETE;
                    case "glGetString":
                    case "glGetShaderInfoLog":
                    case "glGetProgramInfoLog":
                        return "";
                    case "glGetShaderiv":
                    case "glGetProgramiv":
                    case "glGetIntegerv":
                    case "glGetTexParameteriv":
                    case "glGetVertexAttribiv":
                        writeIntResult(name, args);
                        return null;
                    case "glGenTextures":
                    case "glGenBuffers":
                    case "glGenFramebuffers":
                    case "glGenRenderbuffers":
                        fillGeneratedIds(args, nextId);
                        return null;
                    default:
                        return defaultValue(returnType);
                }
            }
        };

        return (GL20) Proxy.newProxyInstance(
            GL20.class.getClassLoader(),
            new Class<?>[] {GL20.class},
            handler
        );
    }

    private static void writeIntResult(String methodName, Object[] args) {
        if (args == null || args.length < 3) {
            return;
        }

        int pname = args[1] instanceof Integer ? (Integer) args[1] : 0;
        int value = 0;

        if ("glGetShaderiv".equals(methodName)) {
            if (pname == GL20.GL_COMPILE_STATUS) {
                value = GL20.GL_TRUE;
            } else if (pname == GL20.GL_INFO_LOG_LENGTH) {
                value = 0;
            }
        } else if ("glGetProgramiv".equals(methodName)) {
            if (pname == GL20.GL_LINK_STATUS || pname == GL20.GL_VALIDATE_STATUS) {
                value = GL20.GL_TRUE;
            } else if (pname == GL20.GL_INFO_LOG_LENGTH) {
                value = 0;
            }
        } else {
            value = 1;
        }

        Object target = args[2];
        if (target instanceof IntBuffer) {
            ((IntBuffer) target).put(0, value);
        } else if (target instanceof int[]) {
            int[] values = (int[]) target;
            int offset = args.length > 3 && args[3] instanceof Integer ? (Integer) args[3] : 0;
            if (offset < values.length) {
                values[offset] = value;
            }
        }
    }

    private static void fillGeneratedIds(Object[] args, AtomicInteger nextId) {
        if (args == null || args.length < 2 || !(args[0] instanceof Integer)) {
            return;
        }

        int count = (Integer) args[0];
        Object target = args[1];

        if (target instanceof IntBuffer) {
            IntBuffer buffer = (IntBuffer) target;
            for (int i = 0; i < count && i < buffer.capacity(); i++) {
                buffer.put(i, nextId.getAndIncrement());
            }
        } else if (target instanceof int[]) {
            int[] ids = (int[]) target;
            int offset = args.length > 2 && args[2] instanceof Integer ? (Integer) args[2] : 0;
            for (int i = 0; i < count && offset + i < ids.length; i++) {
                ids[offset + i] = nextId.getAndIncrement();
            }
        }
    }

    private static Object defaultValue(Class<?> type) {
        if (type == Void.TYPE) {
            return null;
        }
        if (type == Boolean.TYPE) {
            return false;
        }
        if (type == Integer.TYPE) {
            return 0;
        }
        if (type == Long.TYPE) {
            return 0L;
        }
        if (type == Float.TYPE) {
            return 0f;
        }
        if (type == Double.TYPE) {
            return 0d;
        }
        if (type == Short.TYPE) {
            return (short) 0;
        }
        if (type == Byte.TYPE) {
            return (byte) 0;
        }
        if (type == Character.TYPE) {
            return '\0';
        }
        if (Buffer.class.isAssignableFrom(type)) {
            return null;
        }
        return null;
    }

    public static final class TestInput implements InvocationHandler {

        private final Set<Integer> pressedKeys = new HashSet<>();
        private final Set<Integer> justPressedKeys = new HashSet<>();
        private boolean justTouched;
        private final Input proxy;

        private TestInput() {
            proxy = (Input) Proxy.newProxyInstance(
                Input.class.getClassLoader(),
                new Class<?>[] {Input.class},
                this
            );
        }

        public Input asInput() {
            return proxy;
        }

        public void pressKey(int keycode) {
            pressedKeys.add(keycode);
            justPressedKeys.add(keycode);
        }

        public void releaseKey(int keycode) {
            pressedKeys.remove(keycode);
        }

        public void tap() {
            justTouched = true;
        }

        public void reset() {
            pressedKeys.clear();
            justPressedKeys.clear();
            justTouched = false;
        }

        public void endFrame() {
            justPressedKeys.clear();
            justTouched = false;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "isKeyPressed":
                    return pressedKeys.contains((Integer) args[0]);
                case "isKeyJustPressed":
                    return justPressedKeys.contains((Integer) args[0]);
                case "justTouched":
                    return justTouched;
                case "getX":
                case "getY":
                case "getDeltaX":
                case "getDeltaY":
                case "getMaxPointers":
                    return 0;
                case "getPressure":
                case "getPitch":
                case "getRoll":
                case "getAzimuth":
                    return 0f;
                case "getRotation":
                    return 0;
                case "getRotationMatrix":
                    if (args != null && args.length == 1 && args[0] instanceof float[]) {
                        float[] matrix = (float[]) args[0];
                        for (int i = 0; i < matrix.length; i++) {
                            matrix[i] = 0f;
                        }
                    }
                    return null;
                default:
                    return defaultValue(method.getReturnType());
            }
        }
    }
}
