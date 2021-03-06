package cn.ac.iie.configs;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConfLoading {
    private final static Logger LOG = LoggerFactory.getLogger(ConfLoading.class);

    private static Gson gson = new Gson();

    public static void helpInfo(Class _class) {
        Field[] fields = _class.getDeclaredFields();

        LOG.info("config items:");
        for (Field f : fields) {
            String info = "{" + f.getName() + "}(required:{"
                    + (f.getAnnotation(FieldMeta.class).isOptional() ? "no" : "yes") + "})--{"
                    + f.getAnnotation(FieldMeta.class).desc() + "}";
            LOG.info(info);
        }
    }

    public static void init(Class _class, String confFile) throws FileNotFoundException, IOException,
            NumberFormatException, IllegalArgumentException, IllegalAccessException {
        LOG.info("Starting to read config file.");

        Properties prop = new Properties();
        prop.load(new InputStreamReader(new FileInputStream(confFile), "UTF-8"));

        Field[] fields = _class.getDeclaredFields();
        Map<String, Field> nameToFieldMap = new HashMap<String, Field>();

        for (Field f : fields) {
            nameToFieldMap.put(f.getName().toLowerCase(), f);
        }

        for (Map.Entry<Object, Object> en : prop.entrySet()) {
            if (nameToFieldMap.containsKey(en.getKey().toString().toLowerCase())) {
                LOG.info("{}={}", en.getKey(), en.getValue());
                Field f = nameToFieldMap.get(en.getKey().toString().toLowerCase());
                f.setAccessible(true);
                if (f.getType() == int.class) {
                    f.setInt(null, Integer.parseInt(en.getValue().toString()));
                } else if (f.getType() == String.class) {
                    f.set(null, en.getValue().toString());
                } else if (f.getType() == long.class) {
                    f.set(null, Long.parseLong(en.getValue().toString()));
                } else if (f.getType() == double.class) {
                    f.set(null, Double.parseDouble(en.getValue().toString()));
                } else if (f.getType() == float.class) {
                    f.set(null, Float.parseFloat(en.getValue().toString()));
                } else if (f.getType() == boolean.class) {
                    f.set(null, Boolean.parseBoolean(en.getValue().toString()));
                } else if (f.getType() == List.class) {
                    f.set(null, gson.fromJson(en.getValue().toString(), List.class));
                } else if (f.getType() == Map.class) {
                    f.set(null, gson.fromJson(en.getValue().toString(), Map.class));
                } else {
                    throw new RuntimeException("Unknow datatype exception :" + f.getType());
                }
            } else {
                LOG.info("Undefined config item:{}={}", en.getKey(), en.getValue());
            }
        }

        for (Map.Entry<Object, Object> en : prop.entrySet()) {
            nameToFieldMap.remove(en.getKey().toString().toLowerCase());
        }

        if (nameToFieldMap.size() > 0) {
            for (Map.Entry<String, Field> en : nameToFieldMap.entrySet()) {
                en.getValue().setAccessible(true);
                FieldMeta fm = (FieldMeta) en.getValue().getAnnotation(FieldMeta.class);
                if (fm.isOptional() == false) {
                    LOG.error("Config item:" + en.getValue().getName() + " is required.");
                    throw new RuntimeException("Config item:" + en.getValue().getName() + " is required.");
                }
                LOG.info("{}={} (default value)", en.getValue().getName(), en.getValue().get(null));
            }
        }
    }
}
