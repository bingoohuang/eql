package org.n3r.eql.settings;


import org.n3r.eql.cache.EqlCacheSettings;
import org.n3r.eql.codedesc.CodeDescSettings;
import org.n3r.eql.util.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EqlFileGlobalSettings {
    static Logger logger = LoggerFactory.getLogger(EqlFileGlobalSettings.class);

    public static void process(String sqlClassPath, String globalSettings) {
        KeyValue globalSettingKeyValue = KeyValue.parse(globalSettings);

        if (globalSettingKeyValue.keyStartsWith("cacheModel")) {
            KeyValue cacheModelSetting = globalSettingKeyValue.removeKeyPrefix("cacheModel");
            EqlCacheSettings.processCacheModel(sqlClassPath, cacheModelSetting);
        } else if (globalSettingKeyValue.keyStartsWith("desc")) {
            KeyValue cacheModelSetting = globalSettingKeyValue.removeKeyPrefix("desc");
            CodeDescSettings.processSetting(sqlClassPath, cacheModelSetting);
        } else {
            logger.warn("unrecognized global settings {} ", globalSettings);
        }
    }


}
