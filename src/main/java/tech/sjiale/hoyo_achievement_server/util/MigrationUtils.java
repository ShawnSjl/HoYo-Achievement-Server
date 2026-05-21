package tech.sjiale.hoyo_achievement_server.util;

import com.baomidou.mybatisplus.core.toolkit.BeanUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MigrationUtils {

    public static boolean hasNullFieldExcept(Object obj, String... ignoreFields)
            throws IllegalAccessException {

        Set<String> ignoreSet = new HashSet<>(Arrays.asList(ignoreFields));

        for (Field field : obj.getClass().getDeclaredFields()) {

            if (ignoreSet.contains(field.getName())) {
                continue;
            }

            field.setAccessible(true);

            if (field.get(obj) == null) {
                return true;
            }
        }

        return false;
    }
}
