/*
 This file is part of AdGuard Content Blocker (https://github.com/AdguardTeam/ContentBlocker).
 Copyright © 2018 AdGuard Content Blocker. All rights reserved.

 AdGuard Content Blocker is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by the
 Free Software Foundation, either version 3 of the License, or (at your option)
 any later version.

 AdGuard Content Blocker is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 AdGuard Content Blocker.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.adguard.android.commons;

import android.content.Context;
import android.content.res.Resources;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import com.adguard.android.contentblocker.R;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Class for obtaining resources
 */
public class RawResources {

    private final static Logger LOG = LoggerFactory.getLogger(RawResources.class);

    private static Properties properties;

    private static String createTablesScript;
    private static String dropTablesScript;
    private static String insertFiltersScript;
    private static String insertFiltersLocalizationScript;
    private static String enableDefaultFiltersScript;

    /**
     * Gets check filters versions url
     *
     * @param context Context
     * @return URL for checking filter version
     */
    public static String getCheckFilterVersionsUrl(Context context) {
        if (properties == null) {
            if (loadProperties(context) == null) {
                return null;
            }
        }

        return properties.getProperty("check.filter.versions.url");
    }

    /**
     * Gets filter get url
     *
     * @param context Current context
     * @return Url for getting filter rules
     */
    public static String getFilterUrl(Context context) {
        if (properties == null) {
            if (loadProperties(context) == null) {
                return null;
            }
        }

        return properties.getProperty("get.filter.url");
    }

    /**
     * @param context Current context
     * @return create tables script
     */
    public static String getCreateTablesScript(Context context) {
        if (createTablesScript == null) {
            createTablesScript = getResourceAsString(context, R.raw.create_tables);
        }

        return createTablesScript;
    }

    /**
     * @param context Current context
     * @return insert filters script string
     */
    public static String getInsertFiltersScript(Context context) {
        if (insertFiltersScript == null) {
            insertFiltersScript = getResourceAsString(context, R.raw.insert_filters);
        }

        return insertFiltersScript;
    }

    /**
     * @param context Current context
     * @return insert filters localization script string
     */
    public static String getInsertFiltersLocalizationScript(Context context) {
        if (insertFiltersLocalizationScript == null) {
            insertFiltersLocalizationScript = getResourceAsString(context, R.raw.insert_filters_localization);
        }

        return insertFiltersLocalizationScript;
    }

    /**
     * Gets update script used to update schema to a new version
     *
     * @param context    App context
     * @param oldVersion Old schema version
     * @param newVersion New schema version
     * @return Update script
     */
    public static String getUpdateScript(Context context, int oldVersion, int newVersion) {
        String updateScriptName = "update_" + oldVersion + "_" + newVersion;
        int id = context.getResources().getIdentifier(updateScriptName, "raw", context.getPackageName());

        if (id <= 0) {
            return null;
        }

        return getResourceAsString(context, id);
    }

    /**
     * @param context Current context
     * @return drop tables script
     */
    public static String getDropTablesScript(Context context) {
        if (dropTablesScript == null) {
            dropTablesScript = getResourceAsString(context, R.raw.drop_tables);
        }

        return dropTablesScript;
    }

    /**
     * @param context Current context
     * @return enable default filters script string
     */
    public static String getEnableDefaultFiltersScript(Context context) {
        if (enableDefaultFiltersScript == null) {

            List<String> languages = getInputLanguages(context);
            String defaultLanguage = cleanUpLanguageCode(Locale.getDefault().getLanguage());
            if (!languages.contains(defaultLanguage)) {
                languages.add(defaultLanguage);
            }

            enableDefaultFiltersScript = getResourceAsString(context, R.raw.enable_default_filters).replace("{0}", StringUtils.join(languages, ","));
        }

        return enableDefaultFiltersScript;
    }

    /**
     * @param context Current context
     * @return select filters script string
     */
    public static String getSelectFiltersScript(Context context) {
        return getResourceAsString(context, R.raw.select_filters).replace("{0}", Locale.getDefault().getLanguage());
    }

    /**
     * Gets resource as string
     *
     * @param context    Context
     * @param resourceId resource file id
     * @return String
     */
    private static String getResourceAsString(Context context, int resourceId) {
        try {
            return IOUtils.toString(context.getResources().openRawResource(resourceId));
        } catch (Exception ex) {
            throw new RuntimeException("Error getting resource " + resourceId, ex);
        }
    }

    private static Properties loadProperties(Context context) {
        try {
            InputStream rawResource = context.getResources().openRawResource(R.raw.application);
            properties = new Properties();
            properties.load(rawResource);

            return properties;
        } catch (Resources.NotFoundException e) {
            LOG.error("Did not find raw resource", e);
        } catch (IOException e) {
            LOG.error("Failed to open properties file");
        }

        return null;
    }

    /**
     * Gets input languages
     *
     * @param context Application context
     * @return List of input languages
     */
    private static List<String> getInputLanguages(Context context) {
        List<String> languages = new ArrayList<>();

        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            List<InputMethodInfo> ims = imm.getEnabledInputMethodList();

            for (InputMethodInfo method : ims) {
                List<InputMethodSubtype> subMethods = imm.getEnabledInputMethodSubtypeList(method, true);
                for (InputMethodSubtype subMethod : subMethods) {
                    if ("keyboard".equals(subMethod.getMode())) {
                        String currentLocale = subMethod.getLocale();
                        String language = cleanUpLanguageCode(new Locale(currentLocale).getLanguage());
                        if (!languages.contains(language)) {
                            languages.add(language);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LOG.warn("Cannot get user input languages\r\n", ex);
        }

        return languages;
    }

    /**
     * Cleans up language code, leaves only first part of it
     *
     * @param language Language code (like en_US)
     * @return language (like en)
     */
    private static String cleanUpLanguageCode(String language) {
        String languageCode = StringUtils.substringBefore(language, "_");
        return StringUtils.lowerCase(languageCode);
    }
}
