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

import org.apache.commons.lang3.SystemUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents the deployment package of the application for different platforms.
 * <p>
 * Created by D.Knoll on 28.11.2015.
 */
public class ApplicationPackage implements Comparable<ApplicationPackage> {

    private static final String DIST_PACKAGE_FILE_NAME_START = "cedesk-";

    private String url;

    private String filename;

    private String version;

    private boolean isRelease;

    public ApplicationPackage(String url, String fileName) {
        this.url = url;
        this.filename = fileName;
    }

    public static String getExtension() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return "exe";
        } else if (SystemUtils.IS_OS_MAC) {
            return "dmg";
        } else if (SystemUtils.IS_OS_LINUX) {
            return "deb";
        }
        return "NONE";
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isRelease() {
        return isRelease;
    }

    public void setRelease(boolean release) {
        isRelease = release;
    }

    public void setBaseUrl(String baseUrl) {
        this.url = baseUrl + filename;
    }

    public static ApplicationPackage fromFileName(String baseUrl, String fileName) {
        ApplicationPackage applicationPackage = new ApplicationPackage(baseUrl + fileName, fileName);
        Pattern pattern = Pattern.compile(DIST_PACKAGE_FILE_NAME_START + "([^_]*)_.*");
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.matches()) {
            String versionName = matcher.group(1);
            applicationPackage.version = versionName;
            applicationPackage.isRelease = isRelease(versionName);
        } else {
            throw new IllegalArgumentException(fileName + " is not a parseable application package");
        }
        return applicationPackage;
    }

    public static boolean isRelease(String versionName) {
        return !versionName.endsWith("snapshot") || versionName.endsWith("Snapshot") || versionName.endsWith("SNAPSHOT");
    }

    @Override
    public int compareTo(ApplicationPackage o) {
        int releaseCompare = Boolean.compare(this.isRelease, o.isRelease);
        if (releaseCompare != 0) {
            return releaseCompare;
        }
        return Utils.compareVersions(this.version, o.version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApplicationPackage that = (ApplicationPackage) o;

        if (isRelease != that.isRelease) return false;
        if (!url.equals(that.url)) return false;
        return version.equals(that.version);
    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + version.hashCode();
        result = 31 * result + (isRelease ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ApplicationPackage{");
        sb.append("url='").append(url).append('\'');
        sb.append(", fileName='").append(filename).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append(", isRelease=").append(isRelease);
        sb.append('}');
        return sb.toString();
    }
}
