package com.bgsoftware.common.shopsbridge;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.util.Locale;
import java.util.Optional;

public enum ShopsProvider {

    CMI("CMI", ShopsBridge_CMI.class),

    ECONOMYSHOPGUI("EconomyShopGUI", ShopsBridge_EconomyShopGUI.class) {
        @Override
        protected boolean isPluginEnabled() {
            return Bukkit.getPluginManager().isPluginEnabled("EconomyShopGUI") ||
                    Bukkit.getPluginManager().isPluginEnabled("EconomyShopGUI-Premium");
        }
    },

    ESSENTIALS("Essentials", null) {
        @Override
        protected IShopsBridge createInstanceInternal(Plugin plugin) {
            // Determine which version of Essentials we have installed.
            Plugin essentials = plugin.getServer().getPluginManager().getPlugin("Essentials");
            if (essentials.getDescription().getVersion().startsWith("2.15")) {
                return new ShopsBridge_Essentials2_15(plugin);
            } else {
                return new ShopsBridge_Essentials2_16(plugin);
            }
        }
    },

    EXCELLENTSHOP("ExcellentShop", null) {
        @Override
        protected IShopsBridge createInstanceInternal(Plugin plugin) throws Exception {
            Class<?> excellentShopClass;
            if(isClassLoaded("su.nightexpress.nexshop.api.shop.product.typing.PhysicalTyping")) {
                excellentShopClass = Class.forName("com.bgsoftware.common.shopsbridge.ShopsBridge_ExcellentShop4_13");
            } else if(isClassLoaded("su.nightexpress.nexshop.product.price.AbstractProductPricer")) {
                excellentShopClass = Class.forName("com.bgsoftware.common.shopsbridge.ShopsBridge_ExcellentShop4_11");
            } else if (isClassLoaded("su.nightexpress.nexshop.api.shop.product.VirtualProduct")) {
                excellentShopClass = Class.forName("com.bgsoftware.common.shopsbridge.ShopsBridge_ExcellentShop4_8");
            } else {
                excellentShopClass = Class.forName("com.bgsoftware.common.shopsbridge.ShopsBridge_ExcellentShop4_4");
            }

            Constructor<?> constructor = excellentShopClass.getConstructor(Plugin.class);
            return (IShopsBridge) constructor.newInstance(plugin);
        }
    },

    GUISHOP("GUIShop", ShopsBridge_GUIShop.class),

    NEWTSHOP("newtShop", ShopsBridge_newtShop.class),

    NEXTGENS("NextGens", null) {
        @Override
        protected IShopsBridge createInstanceInternal(Plugin plugin) throws Exception {
            Class<?> nextGensClass = Class.forName("com.bgsoftware.common.shopsbridge.ShopsBridge_NextGens");
            Constructor<?> constructor = nextGensClass.getConstructor(Plugin.class);
            return (IShopsBridge) constructor.newInstance(plugin);
        }
    },

    QUANTUMSHOP("QuantumShop", ShopsBridge_QuantumShop.class),

    SHOPGUIPLUS("ShopGUIPlus", null) {
        @Override
        protected IShopsBridge createInstanceInternal(Plugin plugin) {
            // Determine which version of ShopGUIPlus we have installed.
            Plugin shopGUIPlus = plugin.getServer().getPluginManager().getPlugin("ShopGUIPlus");
            if (shopGUIPlus.getDescription().getVersion().startsWith("1.2")) {
                return new ShopsBridge_ShopGUIPlus1_20(plugin);
            } else if (isClassLoaded("net.brcdev.shopgui.shop.item.ShopItem")) {
                return new ShopsBridge_ShopGUIPlus1_80(plugin);
            } else {
                return new ShopsBridge_ShopGUIPlus1_43(plugin);
            }
        }
    },

    ZSHOP("zShop", null) {
        @Override
        protected IShopsBridge createInstanceInternal(Plugin plugin) {
            // Determine which version of zShop we have installed.
            if (isClassLoaded("fr.maxlego08.zshop.api.ShopManager")) {
                return new ShopsBridge_zShop3(plugin);
            } else {
                return new ShopsBridge_zShop(plugin);
            }
        }
    };

    private final String pluginName;
    private final Class<?> clazz;

    ShopsProvider(String formattedPluginName, Class<?> clazz) {
        this.pluginName = formattedPluginName;
        this.clazz = clazz;
    }

    public final Optional<IShopsBridge> createInstance(Plugin plugin) {
        if (isPluginEnabled()) {
            try {
                return Optional.of(createInstanceInternal(plugin));
            } catch (Exception error) {
                plugin.getLogger().info("An error occurred while loading shops-bridge hook for " + getPluginName());
                error.printStackTrace();
            }
        }

        return Optional.empty();
    }

    public final String getPluginName() {
        return pluginName;
    }

    protected IShopsBridge createInstanceInternal(Plugin plugin) throws Exception {
        Constructor<?> constructor = clazz.getConstructor(Plugin.class);
        return (IShopsBridge) constructor.newInstance(plugin);
    }

    protected boolean isPluginEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled(this.pluginName);
    }

    public static Optional<ShopsProvider> getShopsProvider(String pluginName) {
        try {
            return Optional.of(ShopsProvider.valueOf(pluginName.toUpperCase(Locale.ENGLISH)));
        } catch (Exception error) {
            return Optional.empty();
        }
    }

    public static Optional<ShopsProvider> findAvailableProvider() {
        for (ShopsProvider shopsProvider : values()) {
            if (shopsProvider.isPluginEnabled()) {
                return Optional.of(shopsProvider);
            }
        }

        return Optional.empty();
    }

    private static boolean isClassLoaded(String clazz) {
        try {
            Class.forName(clazz);
            return true;
        } catch (ClassNotFoundException error) {
            return false;
        }
    }

}
