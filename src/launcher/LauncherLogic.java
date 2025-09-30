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

    private static final String FABRIC_JSON = "{\"inheritsFrom\":\"1.21.9\",\"releaseTime\":\"2025-09-30T15:05:46+0000\",\"mainClass\":\"net.fabricmc.loader.impl.launch.knot.KnotClient\",\"libraries\":[{\"sha1\":\"dc19ecb3f7889b7860697215cae99c0f9b6f6b4b\",\"sha256\":\"876eab6a83daecad5ca67eb9fcabb063c97b5aeb8cf1fca7a989ecde17522051\",\"size\":126113,\"name\":\"org.ow2.asm:asm:9.8\",\"sha512\":\"cbd250b9c698a48a835e655f5f5262952cc6dd1a434ec0bc3429a9de41f2ce08fcd3c4f569daa7d50321ca6ad1d32e131e4199aa4fe54bce9e9691b37e45060e\",\"url\":\"https://maven.fabricmc.net/\",\"md5\":\"f5adf3bfc54fb3d2cd8e3a1f275084bc\"},{\"sha1\":\"b9747a320844b6cb1eacd90d8ecfd260a16c01d3\",\"sha256\":\"e640732fbcd3c6271925a504f125e38384688f4dfbbf92c8622dfcee0d09edb9\",\"size\":35151,\"name\":\"org.ow2.asm:asm-analysis:9.8\",\"sha512\":\"0268e6dc2cc4965180ca1b62372e3c5fc280d6dc09cfeace2ac4e43468025e8a78813e4e93beafc0352e67498c70616cb4368313aaab532025fa98146c736117\",\"url\":\"https://maven.fabricmc.net/\",\"md5\":\"3d63508405f5610fc2ea673ff5471553\"},{\"sha1\":\"36e4d212970388e5bd2c5180292012502df461bb\",\"sha256\":\"3301a1c1cb4c59fcc5292648dac1d7c5aed4c0f067dfbe88873b8cdfe77404f4\",\"size\":73498,\"name\":\"org.ow2.asm:asm-commons:9.8\",\"sha512\":\"d2add10e25416b701bd84651b42161e090df2f32940de5e06e0e2a41c6106734db2fe5136f661d8a8af55e80dc958bc7b385a1004f0ebe550828dfa1e9d70d41\",\"url\":\"https://maven.fabricmc.net/\",\"md5\":\"c8c3d9ccf240144e74d94ff658b024c9\"},{\"sha1\":\"018419ca5b77a2f81097c741e7872e6ab8d2f40d\",\"sha256\":\"14b7880cb7c85eed101e2710432fc3ffb83275532a6a894dc4c4095d49ad59f1\",\"size\":51934,\"name\":\"org.ow2.asm:asm-tree:9.8\",\"sha512\":\"4493f573d9f0cfc8837db9be25a8b61a825a06aafc0e02f0363875584ff184a5a14600e53793c09866300859e44f153faffd0e050de4a7fba1a63b5fb010a9a7\",\"url\":\"https://maven.fabricmc.net/\",\"md5\":\"4ab1aaec43c77a2d9b56e6d6d496f705\"},{\"sha1\":\"395f1c1f035258511f27bc9b2583d76e4b143f59\",\"sha256\":\"8ba0460ecb28fd0e2980e5f3ef3433a513a457bc077f81a53bdc75b587a08d15\",\"size\":94559,\"name\":\"org.ow2.asm:asm-util:9.8\",\"sha512\":\"b68048e199c49d2f90b2990c6993f1fcddccd34fb9d91154ef327d874aa5ff8609db5fbd63e23141020cdeda8fb753e97a61c2152e1b4e8f20003a5390e7e1d9\",\"url\":\"https://maven.fabricmc.net/\",\"md5\":\"62498ef324bdab15f407d703f7d78d19\"},{\"sha1\":\"3e535042688d1265447e52ad86950b7d9678a5fa\",\"sha256\":\"bd13b372996ed6c2ea76a31b496b779562b2cd20cb1f8197e9fa91d6a5f4649c\",\"size\":1504156,\"name\":\"net.fabricmc:sponge-mixin:0.16.3+mixin.0.8.7\",\"sha512\":\"525186f69ed9f06f5f9b1afd714fab9bd89cc2b453f3b052561729332cd0e6ef312d728eaf8c2cb68d3f06cb5da844862931de36373e4e14a8bb3793af266b1a\",\"url\":\"https://maven.fabricmc.net/\",\"md5\":\"aa4a43d5b0e9abf4fcb57520ffeb0019\"},{\"name\":\"net.fabricmc:intermediary:1.21.9\",\"url\":\"https://maven.fabricmc.net/\"},{\"name\":\"net.fabricmc:fabric-loader:0.17.2\",\"url\":\"https://maven.fabricmc.net/\"}],\"arguments\":{\"jvm\":[\"-DFabricMcEmu= net.minecraft.client.main.Main \"],\"game\":[]},\"id\":\"fabric-loader-0.17.2-1.21.9\",\"time\":\"2025-09-30T15:05:46+0000\",\"type\":\"release\"}";

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