package launcher.util;

import java.nio.file.Path;

public class Library {
    public final String name;
    public final String group;
    public final String artifact;
    public final String version;
    public final String mavenUrl;
    public final String downloadUrl;
    public final Path localPath;
    public final String classifier;
    public final String extensionClassifier;

    public Library(String name, String mavenUrl, Path mcDir) {
        this.name = name;
        String baseUrl = (mavenUrl == null || mavenUrl.isEmpty()) ? "https://libraries.minecraft.net" : mavenUrl;
        if (baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        this.mavenUrl = baseUrl;

        String[] parts = name.split(":");
        if (parts.length >= 3) {
            this.group = parts[0];
            this.artifact = parts[1];
            this.version = parts[2];

            if (parts.length == 4) {
                this.classifier = parts[3];
                this.extensionClassifier = "";
            } else if (parts.length == 5) {
                this.classifier = parts[3];
                this.extensionClassifier = parts[4];
            } else {
                this.classifier = "";
                this.extensionClassifier = "";
            }

            String finalClassifier = (parts.length > 3) ? parts[parts.length - 1] : "";

            String jarName = finalClassifier.isEmpty() ? artifact + "-" + version + ".jar"
                    : artifact + "-" + version + "-" + finalClassifier + ".jar";

            String path = String.format("%s/%s/%s/%s", group.replace('.', '/'), artifact, version, jarName);
            this.localPath = mcDir.resolve("libraries").resolve(path);
            this.downloadUrl = baseUrl + "/" + path;
        } else {
            throw new IllegalArgumentException("Ungültiges Library-Format: " + name);
        }
    }

    public boolean isNativeJar() {
        return !classifier.isEmpty() && classifier.startsWith("natives-");
    }

    public boolean isLinuxNative() {
        return extensionClassifier.startsWith("linux-") ||
                (isNativeJar() && classifier.contains("linux"));
    }

    public boolean isOSNative() {
        if (!isNativeJar() && !isLinuxNative()) return false;

        String currentOSName = OSUtil.getOSName();
        String currentOSArch = OSUtil.getOSArchitecture();

        String relevantClassifier = !classifier.isEmpty() ? classifier : extensionClassifier;

        if (!relevantClassifier.contains(currentOSName)) return false;

        if (relevantClassifier.contains("x86") && !currentOSArch.equals("x86")) return false;
        if (relevantClassifier.contains("arm64") && !currentOSArch.equals("arm64")) return false;

        if (currentOSName.equals("windows") &&
                !relevantClassifier.contains("x86") &&
                !relevantClassifier.contains("arm")) {
            return currentOSArch.equals("x64");
        }

        return true;
    }

    public String getKey() {
        return group + ":" + artifact;
    }
}