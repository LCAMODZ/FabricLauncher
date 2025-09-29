package launcher.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuleChecker {

    private RuleChecker() {}

    public static boolean checkLibraryRules(String libObj) {
        Pattern rulesPattern = Pattern.compile("\"rules\"\\s*:\\s*\\[([^\\]]+)\\]");
        Matcher rulesMatcher = rulesPattern.matcher(libObj);
        if (!rulesMatcher.find()) return true;

        String rulesContent = rulesMatcher.group(1);
        Pattern rulePattern = Pattern.compile("\\{[^}]+\\}");
        Matcher ruleMatcher = rulePattern.matcher(rulesContent);

        String currentOSName = OSUtil.getOSName();
        String currentOSArch = OSUtil.getOSArchitecture();

        boolean defaultAllowed = true;

        while (ruleMatcher.find()) {
            String rule = ruleMatcher.group(0);
            Pattern actionPattern = Pattern.compile("\"action\"\\s*:\\s*\"([^\"]+)\"");
            Matcher actionMatcher = actionPattern.matcher(rule);
            String action = actionMatcher.find() ? actionMatcher.group(1) : "allow";

            if (!rule.contains("\"os\"")) {
                defaultAllowed = action.equals("allow");
                continue;
            }

            Pattern osNamePattern = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
            Matcher osNameMatcher = osNamePattern.matcher(rule);

            if (osNameMatcher.find()) {
                String ruleName = osNameMatcher.group(1).toLowerCase();

                boolean osMatches = currentOSName.equals(ruleName);

                boolean archMatches = true;
                if (rule.contains("\"arch\"")) {
                    Pattern archPattern = Pattern.compile("\"arch\"\\s*:\\s*\"([^\"]+)\"");
                    Matcher archMatcher = archPattern.matcher(rule);
                    if (archMatcher.find()) {
                        String ruleArch = archMatcher.group(1).toLowerCase();
                        if (!currentOSArch.equals(ruleArch)) {
                            archMatches = false;
                        }
                    }
                }

                if (osMatches && archMatches) {
                    return action.equals("allow");
                }
            }
        }

        return defaultAllowed;
    }
}