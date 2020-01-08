package io.github.indicode.fabric.tinyconfig.gui;

import io.github.indicode.fabric.tinyconfig.api.ConfigObject;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;

import java.util.HashMap;
import java.util.Map;

public class ClothEntryBuilder {
    private static Map<Class, ConfigEntryProvider<?, AbstractConfigListEntry>> BUILDERS = new HashMap<>();

    public static <T> void registerBuilder(Class<T> dataType, ConfigEntryProvider<T, AbstractConfigListEntry> getter) {
        BUILDERS.put(dataType, getter);
    }

    public static <T> ConfigEntryProvider<T, AbstractConfigListEntry> getEntryProvider(Class<T> dataType) {
        if (!BUILDERS.containsKey(dataType)) {
            for (Map.Entry<Class, ConfigEntryProvider<?, AbstractConfigListEntry>> entry : BUILDERS.entrySet()) {
                if (dataType.isAssignableFrom(entry.getKey())) {
                    return (ConfigEntryProvider<T, AbstractConfigListEntry>) entry.getValue();
                }
            }
            return null;
        } else {
            return (ConfigEntryProvider<T, AbstractConfigListEntry>) BUILDERS.get(dataType);
        }
    }

    public static AbstractConfigListEntry getGuiEntry(String id, ConfigObject.Entry entry, ConfigEntryBuilder entryBuilder) {
        ConfigEntryProvider entryProvider = getEntryProvider(entry.get().getClass());
        AbstractConfigListEntry lentry = entryProvider.provide(id, entry, entryBuilder);
        return lentry;
    }

    private static final String TPRE = "tinyconfig.";

    private static AbstractConfigListEntry buildEntry(String key, ConfigObject.Entry entry, ConfigEntryBuilder entryBuilder, String prefix, ConfigBuilder config) {
        Object data = entry.get();
        if (data instanceof ConfigObject) {
            ConfigObject object = (ConfigObject) data;
            if (object.isCategory) {
                String catName = entry.getTranslation() == null ? prefix + "." + key : entry.getTranslation();
                ConfigCategory cat = config.getOrCreateCategory(catName);
                for (Map.Entry<String, ConfigObject.Entry> subEntry : object.entrySet()) {
                    AbstractConfigListEntry guiEntry = buildEntry(subEntry.getKey(), subEntry.getValue(), entryBuilder, prefix + "." + key, config);
                    if (guiEntry != null) cat.addEntry(guiEntry);
                }
                return null;
            } else {
                String dropName = entry.getTranslation() == null ? TPRE + prefix + "." + key : entry.getTranslation();
                SubCategoryBuilder dropdown = entryBuilder.startSubCategory(dropName);
                for (Map.Entry<String, ConfigObject.Entry> subEntry : object.entrySet()) {
                    AbstractConfigListEntry guiEntry = buildEntry(subEntry.getKey(), subEntry.getValue(), entryBuilder, prefix + "." + key, config);
                    if (guiEntry != null) dropdown.add(guiEntry);
                }
                return dropdown.build();
            }
        } else {
            AbstractConfigListEntry guiEntry = getGuiEntry(TPRE + prefix + "." + key, entry, entryBuilder);
            return guiEntry;
        }
    }

    public static ConfigBuilder getConfigBuilder(ConfigObject config, String modid) {
        ConfigBuilder configBuilder = ConfigBuilder.create();
        ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
        for (Map.Entry<String, ConfigObject.Entry> entry : config.entrySet()) {
            AbstractConfigListEntry guiEntry = buildEntry(entry.getKey(), entry.getValue(), entryBuilder, modid, configBuilder);
            if (guiEntry != null) configBuilder.getOrCreateCategory(entry.getValue().getTranslation() == null ? TPRE + modid + ".general" : entry.getValue().getTranslation()).addEntry(guiEntry);
        }
        return configBuilder;
    }

    static {
        registerBuilder(String.class, (id, entry, entryBuilder) -> entryBuilder.startStrField(id, entry.get()).setTooltip(entry.getComment()).setDefaultValue(entry.get()).build());
        registerBuilder(Number.class, (id, entry, entryBuilder) -> entryBuilder.startDoubleField(id, entry.get().doubleValue()).setTooltip(entry.getComment()).setDefaultValue(entry.get().doubleValue()).build());
        registerBuilder(Integer.class, (id, entry, entryBuilder) -> entryBuilder.startIntField(id, entry.get()).setTooltip(entry.getComment()).setDefaultValue(entry.get()).build());
        registerBuilder(Float.class, (id, entry, entryBuilder) -> entryBuilder.startFloatField(id, entry.get()).setTooltip(entry.getComment()).setDefaultValue(entry.get()).build());
        registerBuilder(Double.class, (id, entry, entryBuilder) -> entryBuilder.startDoubleField(id, entry.get()).setTooltip(entry.getComment()).setDefaultValue(entry.get()).build());
        registerBuilder(Long.class, (id, entry, entryBuilder) -> entryBuilder.startLongField(id, entry.get()).setTooltip(entry.getComment()).setDefaultValue(entry.get()).build());
    }
}
