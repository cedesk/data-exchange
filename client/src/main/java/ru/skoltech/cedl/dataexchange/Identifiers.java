/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange;

import java.util.regex.Pattern;

/**
 * Created by dknoll on 16/05/15.
 */
public class Identifiers {
    private static final Pattern NODE_NAME_RULE = Pattern.compile("^[a-zA-Z\\-]{1,}$");

    private static final Pattern PARAMETER_NAME_RULE = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9\\(\\)\\.\\-_\\\\ ]{1,}$");

    private static final Pattern USER_NAME_RULE = Pattern.compile("^[a-z]{1,}[\\.\\-_]?[a-z]{1,}$");

    private static final Pattern PROJECT_NAME_RULE = Pattern.compile("^[a-zA-Z]{2,}[\\-]?[a-zA-Z0-9]{1,}$");

    private static final String NODE_NAME_RULE_DESCRIPTION = "Names must be at least of 1 character and contain only alphabetic characters, minus '-'!";

    private static final String PARAMETER_NAME_RULE_DESCRIPTION = "Names must be at least of 1 character and contain only alphanumeric characters, minus '-', underscore '_', space ' '!";

    private static final String USER_NAME_RULE_DESCRIPTION = "User names must be at least of 1 character and contain only alphanumeric lowercase characters and minus '-', underscore '_' and dot '.' inside!";

    private static final String PROJECT_NAME_RULE_DESCRIPTION = "Project names must be at least of 4 character and contain only alphanumeric characters and minus '-' inside!";

    public static boolean validateNodeName(String nodeName) {
        if (nodeName.length() < 1) return false;
        return NODE_NAME_RULE.matcher(nodeName).matches();
    }

    public static boolean validateParameterName(String parameterName) {
        if (parameterName.length() < 1) return false;
        return PARAMETER_NAME_RULE.matcher(parameterName).matches();
    }

    public static boolean validateUserName(String userName) {
        if (userName.length() < 2) return false;
        return USER_NAME_RULE.matcher(userName).matches();
    }

    public static boolean validateProjectName(String projectName) {
        if (projectName.length() < 5) return false;
        return PROJECT_NAME_RULE.matcher(projectName).matches();
    }

    public static String getNodeNameValidationDescription() {
        return NODE_NAME_RULE_DESCRIPTION;
    }

    public static String getParameterNameValidationDescription() {
        return PARAMETER_NAME_RULE_DESCRIPTION;
    }

    public static String getUserNameValidationDescription() {
        return USER_NAME_RULE_DESCRIPTION;
    }

    public static String getProjectNameValidationDescription() {
        return PROJECT_NAME_RULE_DESCRIPTION;
    }
}
