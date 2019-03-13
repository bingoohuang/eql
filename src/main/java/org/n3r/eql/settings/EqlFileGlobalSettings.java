package org.n3r.eql.settings;

import lombok.extern.slf4j.Slf4j;
import org.n3r.eql.cache.EqlCacheSettings;
import org.n3r.eql.codedesc.CodeDescSettings;
import org.n3r.eql.util.KeyValue;

@Slf4j
public class EqlFileGlobalSettings {
    public static void process(String sqlClassPath, String globalSettings) {
        KeyValue globalSettingKeyValue = KeyValue.parse(globalSettings);

        if (globalSettingKeyValue.keyStartsWith("cacheModel")) {
            KeyValue cacheModelSetting = globalSettingKeyValue.removeKeyPrefix("cacheModel");
            EqlCacheSettings.processCacheModel(sqlClassPath, cacheModelSetting);
        } else if (globalSettingKeyValue.keyStartsWith("desc")) {
            KeyValue cacheModelSetting = globalSettingKeyValue.removeKeyPrefix("desc");
            CodeDescSettings.processSetting(cacheModelSetting);
        } else {
            log.warn("unrecognized global settings {} ", globalSettings);
        }
    }


}
