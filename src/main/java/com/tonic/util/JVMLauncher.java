package com.tonic.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JVMLauncher {

    public static Process launchInNewJVM(String mainClass, String classpath, String[] programArgs) throws IOException {
        List<String> command = new ArrayList<>();

        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        command.add(javaBin);
        command.add("-XX:+DisableAttachMechanism");
        command.add("-Drunelite.launcher.blacklistedDlls=RTSSHooks.dll,RTSSHooks64.dll,NahimicOSD.dll,NahimicMSIOSD.dll,Nahimic2OSD.dll,Nahimic2DevProps.dll,k_fps32.dll,k_fps64.dll,SS2DevProps.dll,SS2OSD.dll,GTIII-OSD64-GL.dll,GTIII-OSD64-VK.dll,GTIII-OSD64.dll");

        // Parse custom JVM args from programArgs and build filtered args list
        String customXms = null;
        String customXmx = null;
        List<String> filteredArgs = new ArrayList<>();

        for (int i = 0; i < programArgs.length; i++) {
            String arg = programArgs[i];
            boolean skipArg = false;

            // Handle --Xms=VALUE or -Xms=VALUE
            if (arg.startsWith("--Xms=") || arg.startsWith("-Xms=")) {
                customXms = arg.split("=")[1];
                skipArg = true;
            }
            // Handle --XmsVALUE or -XmsVALUE (e.g., --Xms2g)
            else if (arg.startsWith("--Xms") && arg.length() > 5) {
                customXms = arg.substring(5);
                skipArg = true;
            } else if (arg.startsWith("-Xms") && arg.length() > 4) {
                customXms = arg.substring(4);
                skipArg = true;
            }
            // Handle --Xms VALUE or -Xms VALUE (space-separated)
            else if (arg.equals("--Xms") || arg.equals("-Xms")) {
                if (i + 1 < programArgs.length) {
                    customXms = programArgs[i + 1];
                    i++; // Skip next arg (the value)
                }
                skipArg = true;
            }

            // Handle --Xmx=VALUE or -Xmx=VALUE
            else if (arg.startsWith("--Xmx=") || arg.startsWith("-Xmx=")) {
                customXmx = arg.split("=")[1];
                skipArg = true;
            }
            // Handle --XmxVALUE or -XmxVALUE (e.g., --Xmx2g)
            else if (arg.startsWith("--Xmx") && arg.length() > 5) {
                customXmx = arg.substring(5);
                skipArg = true;
            } else if (arg.startsWith("-Xmx") && arg.length() > 4) {
                customXmx = arg.substring(4);
                skipArg = true;
            }
            // Handle --Xmx VALUE or -Xmx VALUE (space-separated)
            else if (arg.equals("--Xmx") || arg.equals("-Xmx")) {
                if (i + 1 < programArgs.length) {
                    customXmx = programArgs[i + 1];
                    i++; // Skip next arg (the value)
                }
                skipArg = true;
            }

            // Only add non-JVM args to filtered list
            if (!skipArg) {
                filteredArgs.add(arg);
            }
        }

        // Apply JVM settings with automatic GC optimization
        if (customXms != null) {
            command.add("-Xms" + customXms);
        } else if (customXmx != null && isLargeHeap(customXmx)) {
            // Auto-set Xms to half of Xmx for large heaps (reduces early GC)
            command.add("-Xms" + getHalfHeap(customXmx));
        }

        if (customXmx != null) {
            command.add("-Xmx" + customXmx);
            // Auto-apply optimal GC based on heap size
            if (isLargeHeap(customXmx)) {
                // >1GB: Use G1GC with optimized settings
                command.add("-XX:+UseG1GC");
                command.add("-XX:MaxGCPauseMillis=50");
                command.add("-XX:G1HeapRegionSize=16M");
            } else {
                // <=1GB: Use SerialGC (low resource mode)
                command.add("-XX:+UseSerialGC");
            }
        } else {
            // Default: 768MB max, SerialGC (low resource mode)
            command.add("-Xmx768m");
            command.add("-XX:+UseSerialGC");
        }

        command.add("-Xss2m");
        command.add("-XX:CompileThreshold=1500");
        command.add("-XX:+UseStringDeduplication");

        if (classpath != null && !classpath.isEmpty()) {
            command.add("-cp");
            command.add(classpath);
        } else {
            command.add("-cp");
            command.add(System.getProperty("java.class.path"));
        }

        command.add(mainClass);

        // Add only non-JVM program args (JVM args already applied above)
        command.addAll(filteredArgs);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();

        return pb.start();
    }

    /**
     * Check if heap size is > 1GB (use G1GC for large heaps)
     */
    private static boolean isLargeHeap(String heapSize) {
        if (heapSize == null) return false;
        heapSize = heapSize.toLowerCase();

        if (heapSize.endsWith("g")) {
            return true; // Any value in GB is large
        }

        if (heapSize.endsWith("m")) {
            try {
                int mb = Integer.parseInt(heapSize.replace("m", ""));
                return mb > 1024; // > 1GB
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return false;
    }

    /**
     * Get half of heap size for initial heap (prevents early GC thrashing)
     */
    private static String getHalfHeap(String heapSize) {
        if (heapSize == null) return "512m";
        heapSize = heapSize.toLowerCase();

        if (heapSize.endsWith("g")) {
            try {
                int gb = Integer.parseInt(heapSize.replace("g", ""));
                int halfGb = gb / 2;
                return halfGb > 0 ? halfGb + "g" : "512m";
            } catch (NumberFormatException e) {
                return "512m";
            }
        }

        if (heapSize.endsWith("m")) {
            try {
                int mb = Integer.parseInt(heapSize.replace("m", ""));
                int halfMb = mb / 2;
                return halfMb + "m";
            } catch (NumberFormatException e) {
                return "512m";
            }
        }

        return "512m";
    }
}