package launcher.util;

public class OSUtil {

    private OSUtil() {}

    public static String getOSName() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return "windows";
        if (os.contains("mac")) return "osx";
        if (os.contains("nix") || os.contains("nux") || os.contains("aix")) return "linux";
        return os;
    }

    public static String getOSArchitecture() {
        String arch = System.getProperty("os.arch").toLowerCase();
        if (arch.contains("64")) return "x64";
        if (arch.contains("86")) return "x86";
        if (arch.contains("arm")) return "arm64";
        return arch;
    }
}