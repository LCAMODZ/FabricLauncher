package launcher;


import launcher.util.Library;
import launcher.util.OSUtil;
import launcher.util.RuleChecker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LauncherLogic {

    private static final String FABRIC_JSON = "{\"inheritsFrom\":\"1.21.4\",\"releaseTime\":\"2025-09-28T17:22:18+0000\",\"mainClass\":\"net.fabricmc.loader.impl.launch.knot.KnotClient\",\"libraries\":[{\"name\":\"org.ow2.asm:asm:9.8\",\"url\":\"https://maven.fabricmc.net/\"},{\"name\":\"org.ow2.asm:asm-analysis:9.8\",\"url\":\"https://maven.fabricmc.net/\"},{\"name\":\"org.ow2.asm:asm-commons:9.8\",\"url\":\"https://maven.fabricmc.net/\"},{\"name\":\"org.ow2.asm:asm-tree:9.8\",\"url\":\"https://maven.fabricmc.net/\"},{\"name\":\"org.ow2.asm:asm-util:9.8\",\"url\":\"https://maven.fabricmc.net/\"},{\"name\":\"net.fabricmc:sponge-mixin:0.16.3+mixin.0.8.7\",\"url\":\"https://maven.fabricmc.net/\"},{\"name\":\"net.fabricmc:intermediary:1.21.4\",\"url\":\"https://maven.fabricmc.net/\"},{\"name\":\"net.fabricmc:fabric-loader:0.17.2\",\"url\":\"https://maven.fabricmc.net/\"}],\"arguments\":{\"jvm\":[\"-DFabricMcEmu= net.minecraft.client.main.Main \"],\"game\":[]},\"id\":\"fabric-loader-0.17.2-1.21.4\",\"time\":\"2025-09-28T17:22:18+0000\",\"type\":\"release\"}";

    private final Consumer<String> logger;
    private final Consumer<Integer> progressUpdater;
    private final Path mcDir;
    private final String versionId;
    private final String assetsId;
    private boolean finished = false;

    public LauncherLogic(Consumer<String> logger, Consumer<Integer> progressUpdater, Path mcDir, String versionId, String assetsId) {
        this.logger = logger;
        this.progressUpdater = progressUpdater;
        this.mcDir = mcDir;
        this.versionId = versionId;
        this.assetsId = assetsId;
    }

    public boolean isFinished() {
        return finished;
    }

    public void launch() throws Exception {
        logger.accept("=== Fabric Launcher für Minecraft " + versionId + " ===");
        logger.accept("Minecraft Verzeichnis: " + mcDir);

        Path versionJarPath = mcDir.resolve("versions").resolve(versionId).resolve(versionId + ".jar");
        if (!Files.exists(versionJarPath)) {
            throw new IOException("Minecraft JAR nicht gefunden: " + versionJarPath +
                    "\nBitte starte Minecraft " + versionId + " einmal über den offiziellen Launcher!");
        }

        logger.accept("\n[1/4] Lade Minecraft Libraries...");
        String minecraftJson = loadMinecraftJson();
        List<Library> minecraftLibs = parseMinecraftLibraries(minecraftJson);
        logger.accept("  ✓ " + minecraftLibs.size() + " Minecraft Libraries gefunden");

        logger.accept("\n[2/4] Lade Fabric Libraries...");
        List<Library> fabricLibs = parseFabricLibraries(FABRIC_JSON);
        logger.accept("  ✓ " + fabricLibs.size() + " Fabric Libraries gefunden");

        logger.accept("\n[3/4] Löse Konflikte, synthetisiere und lade Libraries...");
        List<Library> allLibraries = resolveLibraries(minecraftLibs, fabricLibs);
        downloadLibraries(allLibraries);

        logger.accept("\n[4/4] Erstelle Klassenpfad und starte Minecraft...");
        String classpath = buildClasspath(allLibraries, versionJarPath);
        startMinecraft(classpath, versionJarPath);

        finished = true;
    }

    private String loadMinecraftJson() throws IOException {
        Path versionJsonPath = mcDir.resolve("versions").resolve(versionId).resolve(versionId + ".json");
        if (!Files.exists(versionJsonPath)) {
            throw new IOException("Minecraft JSON nicht gefunden: " + versionJsonPath);
        }
        return Files.readString(versionJsonPath);
    }

    private List<Library> parseMinecraftLibraries(String json) {
        List<Library> libraries = new ArrayList<>();
        String osName = OSUtil.getOSName();

        int startIndex = json.indexOf("\"libraries\"");
        if (startIndex == -1) return libraries;

        int depth = 0;
        int arrayStart = json.indexOf("[", startIndex);
        int pos = arrayStart;

        while (pos < json.length()) {
            char c = json.charAt(pos);
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) break;
            }
            pos++;
        }

        String librariesSection = json.substring(arrayStart, pos + 1);
        Pattern entryPattern = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
        Matcher entryMatcher = entryPattern.matcher(librariesSection);

        int lastEnd = 0;
        while (entryMatcher.find()) {
            String name = entryMatcher.group(1);
            int namePos = entryMatcher.start();
            int objStart = librariesSection.lastIndexOf("{", namePos);
            int objEnd = findMatchingBrace(librariesSection, objStart);

            if (objStart >= lastEnd && objEnd > objStart) {
                String libObj = librariesSection.substring(objStart, objEnd + 1);
                lastEnd = objEnd;

                String url = "https://libraries.minecraft.net/";
                Pattern urlPattern = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"");
                Matcher urlMatcher = urlPattern.matcher(libObj);
                if (urlMatcher.find()) url = urlMatcher.group(1);

                try {
                    Library lib = new Library(name, url, mcDir);

                    boolean hasRules = libObj.contains("\"rules\"");

                    if (hasRules) {
                        if (!RuleChecker.checkLibraryRules(libObj)) continue;
                    } else if (lib.isNativeJar() || lib.isLinuxNative()) {
                        if (!lib.isOSNative()) {
                            continue;
                        }
                    }

                    libraries.add(lib);
                } catch (Exception ignored) {}
            }
        }
        return libraries;
    }

    private int findMatchingBrace(String str, int openPos) {
        int depth = 1;
        for (int i = openPos + 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return str.length() - 1;
    }

    private List<Library> parseFabricLibraries(String json) {
        List<Library> libraries = new ArrayList<>();
        Pattern libPattern = Pattern.compile("\\{[^}]*\"name\":\"([^\"]+)\"[^}]*\\}");
        Matcher matcher = libPattern.matcher(json);

        while (matcher.find()) {
            String libBlock = matcher.group(0);
            String name = extractFromBlock(libBlock, "name");
            String url = extractFromBlock(libBlock, "url");
            if (name != null && !name.isEmpty()) {
                try { libraries.add(new Library(name, url, mcDir)); } catch (Exception ignored) { }
            }
        }
        return libraries;
    }

    private String extractFromBlock(String block, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\":\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(block);
        return matcher.find() ? matcher.group(1) : "";
    }

    private List<Library> resolveLibraries(List<Library> minecraftLibs, List<Library> fabricLibs) {
        Map<String, Library> libraryMap = new LinkedHashMap<>();
        int nativeCount = 0;
        int conflicts = 0;

        for (Library lib : minecraftLibs) {
            libraryMap.put(lib.name, lib);
            if (lib.isNativeJar() || !lib.classifier.isEmpty()) nativeCount++;
        }

        for (Library lib : fabricLibs) {
            String artifactKey = lib.getKey();

            Iterator<Map.Entry<String, Library>> iterator = libraryMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Library existingLib = iterator.next().getValue();

                if (existingLib.getKey().equals(artifactKey) && existingLib.classifier.isEmpty()) {
                    logger.accept("  Konflikt gelöst: " + existingLib.name + " entfernt (ersetzt durch Fabric-Version " + lib.version + ")");
                    iterator.remove();
                    conflicts++;
                }
            }

            libraryMap.put(lib.name, lib);
        }

        if (conflicts > 0) logger.accept("  ✓ " + conflicts + " Libraries ersetzt/aufgelöst");
        logger.accept("  ✓ " + nativeCount + " Native-Komponenten im Map (vor Download)");

        return new ArrayList<>(libraryMap.values());
    }

    private void downloadLibraries(List<Library> libraries) throws IOException {
        int total = libraries.size();
        int current = 0;
        int downloaded = 0;
        int skipped = 0;

        for (Library lib : libraries) {
            current++;
            int progress = (int) ((current / (double) total) * 100);
            progressUpdater.accept(progress);

            if (!Files.exists(lib.localPath)) {
                logger.accept("  Lade: " + lib.name);
                Files.createDirectories(lib.localPath.getParent());
                try (InputStream in = new URL(lib.downloadUrl).openStream()) {
                    Files.copy(in, lib.localPath, StandardCopyOption.REPLACE_EXISTING);
                    downloaded++;
                } catch (IOException e) {
                    logger.accept("    ⚠ Fehler beim Download: " + lib.name + " (" + e.getMessage() + ")");
                    skipped++;
                }
            } else {
                skipped++;
            }

            if (!lib.classifier.isEmpty() && Files.exists(lib.localPath)) {
                logger.accept("  Entpacke Native-Komponente: " + lib.name);
                unpackNatives(lib);
            }
        }

        progressUpdater.accept(100);
        logger.accept("  ✓ " + downloaded + " Libraries heruntergeladen, " + skipped + " übersprungen");
    }

    private void unpackNatives(Library lib) throws IOException {
        Path nativesDir = getNativePath();
        try (JarFile jar = new JarFile(lib.localPath.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory() && !entry.getName().startsWith("META-INF/")) {
                    Path fileName = Paths.get(entry.getName()).getFileName();
                    Path outPath = nativesDir.resolve(fileName.toString());

                    if (fileName.toString().endsWith(".dll") || fileName.toString().endsWith(".so")) {
                        logger.accept("    Entpacke DLL/SO nach: " + outPath.toAbsolutePath());
                    }

                    Files.createDirectories(outPath.getParent());
                    try (InputStream in = jar.getInputStream(entry)) {
                        Files.copy(in, outPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }

    private String buildClasspath(List<Library> libraries, Path versionJarPath) {
        StringBuilder sb = new StringBuilder();
        int included = 0;
        for (Library lib : libraries) {
            if (Files.exists(lib.localPath)) {

                if (lib.classifier.isEmpty()) {
                    sb.append(lib.localPath.toAbsolutePath()).append(File.pathSeparator);
                    included++;
                } else if (lib.isOSNative() && lib.isNativeJar()) {
                    sb.append(lib.localPath.toAbsolutePath()).append(File.pathSeparator);
                    included++;
                }
            }
        }
        logger.accept("  ✓ " + included + " Libraries im Klassenpfad");
        return versionJarPath.toAbsolutePath() + File.pathSeparator +
               (sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "");
    }

    private void startMinecraft(String classpath, Path versionJarPath) throws IOException, InterruptedException {
        Path nativesPath = getNativePath();

        List<String> command = new ArrayList<>();
        command.add(findJavaExecutable());
        command.add("-Xmx2048M");
        command.add("-Xms512M");
        command.add("-Djava.library.path=" + nativesPath.toAbsolutePath());
        command.add("-cp");
        command.add(classpath);

        command.add("-DFabricMcEmu=net.minecraft.client.main.Main");
        command.add("net.fabricmc.loader.impl.launch.knot.KnotClient");

        command.add("--username");
        command.add("Player");
        command.add("--version");
        command.add("fabric-loader-0.17.2-1.21.4");
        command.add("--gameDir");
        command.add(mcDir.toString());
        command.add("--assetsDir");
        command.add(mcDir.resolve("assets").toString());
        command.add("--assetIndex");
        command.add(assetsId);
        command.add("--uuid");
        command.add("00000000-0000-0000-0000-000000000000");
        command.add("--accessToken");
        command.add("0");
        command.add("--userType");
        command.add("legacy");
        command.add("--versionType");
        command.add("release");

        logger.accept("\n=== Minecraft wird gestartet ===\n");

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(mcDir.toFile());
        pb.inheritIO();
        Process process = pb.start();

        int exitCode = process.waitFor();

        if (exitCode != 0) logger.accept("\n!!! Minecraft wurde mit Fehlercode " + exitCode + " beendet");
    }

    private String findJavaExecutable() {
        return Paths.get(System.getProperty("java.home"), "bin", "java").toString();
    }

    private Path getNativePath() throws IOException {
        Path nativesDir = mcDir.resolve("versions").resolve(versionId).resolve("natives");
        if (Files.exists(nativesDir)) {
            try (var walk = Files.walk(nativesDir)) {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        }
        Files.createDirectories(nativesDir);
        return nativesDir;
    }
}