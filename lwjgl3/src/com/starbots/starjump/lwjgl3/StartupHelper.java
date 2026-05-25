package com.starbots.starjump.lwjgl3;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;

/**
 * Relaunches the JVM with {@code -XstartOnFirstThread} on macOS, which GLFW
 * (and therefore the LWJGL3 backend) requires. A no-op on Windows/Linux and
 * when the flag is already present. Adapted from the standard libGDX helper,
 * using {@code ProcessHandle} (Java 9+) instead of LWJGL JNI calls.
 */
public final class StartupHelper {

    private static final String JVM_RESTARTED_ARG = "jvmIsRestarted";

    private StartupHelper() {}

    public static boolean startNewJvmIfRequired() {
        return startNewJvmIfRequired(true);
    }

    public static boolean startNewJvmIfRequired(boolean redirectOutput) {
        String osName = System.getProperty("os.name").toLowerCase();
        if (!osName.contains("mac")) {
            return false;
        }

        long pid = ProcessHandle.current().pid();
        if ("1".equals(System.getenv("JAVA_STARTED_ON_FIRST_THREAD_" + pid))) {
            return false; // already on the first thread
        }
        if ("true".equals(System.getProperty(JVM_RESTARTED_ARG))) {
            System.err.println("Could not start with -XstartOnFirstThread; "
                    + "the window may fail to open. Try running with that JVM flag manually.");
            return false;
        }

        String separator = System.getProperty("file.separator");
        String javaExec = System.getProperty("java.home") + separator + "bin" + separator + "java";
        if (!new File(javaExec).exists()) {
            System.err.println("Cannot locate the java executable; skipping JVM relaunch.");
            return false;
        }

        ArrayList<String> args = new ArrayList<>();
        args.add(javaExec);
        args.add("-XstartOnFirstThread");
        args.add("-D" + JVM_RESTARTED_ARG + "=true");
        args.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
        args.add("-cp");
        args.add(System.getProperty("java.class.path"));

        String mainClass = System.getenv("JAVA_MAIN_CLASS_" + pid);
        if (mainClass == null) {
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            if (trace.length > 0) {
                mainClass = trace[trace.length - 1].getClassName();
            } else {
                System.err.println("Could not determine the main class; skipping JVM relaunch.");
                return false;
            }
        }
        args.add(mainClass);

        try {
            ProcessBuilder builder = new ProcessBuilder(args);
            if (redirectOutput) {
                builder.inheritIO();
                Process process = builder.start();
                process.waitFor();
            } else {
                builder.start();
            }
        } catch (Exception e) {
            System.err.println("Failed to relaunch the JVM on the first thread:");
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
