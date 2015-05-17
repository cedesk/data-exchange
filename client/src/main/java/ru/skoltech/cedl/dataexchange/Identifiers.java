package ru.skoltech.cedl.dataexchange;

import java.util.regex.Pattern;

/**
 * Created by dknoll on 16/05/15.
 */
public class Identifiers {
    private static final Pattern NAME_RULE = Pattern.compile("[a-zA-Z]*");

    private static final String NAME_RULE_DESCRIPTION = "Names must be at least of 1 character and contain only alphanumeric characters!";

    public static boolean validateNodeName(String nodeName) {
        if (nodeName.length() < 1) return false;
        return NAME_RULE.matcher(nodeName).matches();
    }

    public static String getNameValidationDescription() {
        return NAME_RULE_DESCRIPTION;
    }

}
