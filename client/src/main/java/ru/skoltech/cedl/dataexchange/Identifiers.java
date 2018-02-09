/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.skoltech.cedl.dataexchange;

import java.util.regex.Pattern;

/**
 * Created by dknoll on 16/05/15.
 */
public class Identifiers {
    private static final Pattern NODE_NAME_RULE = Pattern.compile("^[a-zA-Z\\-]{1,}$");

    private static final Pattern PARAMETER_NAME_RULE = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9\\(\\)\\.\\-_\\\\ ]{1,}$");

    private static final Pattern USER_NAME_RULE = Pattern.compile("(?=^.{1,255}$)^[a-zA-Z][a-zA-Z0-9._-]*[a-zA-Z0-9]+$");

    private static final Pattern PROJECT_NAME_RULE = Pattern.compile("^[a-zA-Z]{2,}[\\-]?[a-zA-Z0-9]{1,}$");

    private static final String NODE_NAME_RULE_DESCRIPTION = "Names must be at least of 1 character and contain only alphabetic characters, minus '-'!";

    private static final String PARAMETER_NAME_RULE_DESCRIPTION = "Names must be at least of 1 character and contain only alphanumeric characters, minus '-', underscore '_', space ' '!";

    private static final String USER_NAME_RULE_DESCRIPTION = "User names must be at least of 1 but not more than 255 characters and contain only alphanumeric lowercase characters and minus '-', underscore '_' and dot '.' inside!";

    private static final String PROJECT_NAME_RULE_DESCRIPTION = "Project names must be at least of 4 character and contain only alphanumeric characters and minus '-' inside!";

    public static String getNodeNameValidationDescription() {
        return NODE_NAME_RULE_DESCRIPTION;
    }

    public static String getParameterNameValidationDescription() {
        return PARAMETER_NAME_RULE_DESCRIPTION;
    }

    public static String getProjectNameValidationDescription() {
        return PROJECT_NAME_RULE_DESCRIPTION;
    }

    public static String getUserNameValidationDescription() {
        return USER_NAME_RULE_DESCRIPTION;
    }

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
}
