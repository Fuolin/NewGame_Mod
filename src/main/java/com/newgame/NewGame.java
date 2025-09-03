package com.newgame;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

@Mod(NewGame.MOD_ID)
public class NewGame {
    public static final String MOD_ID = "newgame";

    //logger
    public static final Logger LOGGER =  LoggerFactory.getLogger(MOD_ID);

    public NewGame(ModContainer container){
        // 构建配置规范
        ModConfigSpec.Builder configBuilder = new ModConfigSpec.Builder();
        Config.init(configBuilder);// 初始化配置定义
        ModConfigSpec startupSpec = configBuilder.build();// 构建 Spec

        // 注册配置
        container.registerConfig(
                ModConfig.Type.STARTUP,
                startupSpec,
                Path.of(MOD_ID).resolve("delete.toml").toString()
        );
        Config.createREADME();
    }

    public static String getRootPath() {
        return FMLPaths.GAMEDIR.get().toString();
    }

    public static Path getRePlacePath(){
        return FMLPaths.CONFIGDIR.get().resolve(MOD_ID).resolve("replace");
    }

    public static String getNewGamePath(){
        return FMLPaths.CONFIGDIR.get().resolve(MOD_ID).toString();
    }

    public static String getModsPath(){
        return FMLPaths.MODSDIR.get().toString();
    }
}