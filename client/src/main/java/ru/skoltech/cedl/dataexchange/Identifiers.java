package ru.skoltech.cedl.dataexchange;

import java.util.regex.Pattern;

/**
 * Created by dknoll on 16/05/15.
 */
public class Identifiers {
    private static final Pattern NAME_RULE = Pattern.compile("^[a-zA-Z]{1,}$");

    private static final Pattern USER_NAME_RULE = Pattern.compile("^[a-zA-Z]{1,}[\\.\\-_]?[a-zA-Z]{1,}$");

    private static final String NAME_RULE_DESCRIPTION = "Names must be at least of 1 character and contain only alphanumeric characters!";

    private static final String USER_NAME_RULE_DESCRIPTION = "User names must be at least of 1 character and contain only alphanumeric characters!";

    public static boolean validateNodeName(String nodeName) {
        if (nodeName.length() < 1) return false;
        return NAME_RULE.matcher(nodeName).matches();
    }

    public static boolean validateUserName(String userName) {
        if (userName.length() < 2) return false;
        return USER_NAME_RULE.matcher(userName).matches();
    }

    public static String getNameValidationDescription() {
        return NAME_RULE_DESCRIPTION;
    }

    public static String getUserNameValidationDescription() {
        return USER_NAME_RULE_DESCRIPTION;
    }

}
