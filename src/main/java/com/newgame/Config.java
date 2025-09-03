package com.newgame;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.newgame.NewGame.getRePlacePath;

public class Config {
    private static ModConfigSpec.ConfigValue<List<? extends String>> delListConfig;

    public static void init(ModConfigSpec.Builder builder){
        delListConfig = builder
                .comment("!!!DONT DELETE IMPORTANT FILES!!!")
                .comment("If a path ends with a separator, all contents within the directory will be deleted while the directory itself is retained.eg\"config/example/\"")
                .comment("If the path does not end with a separator, the entire directory and its contents will be deleted.eg\"resourcepacks\"")
                .comment("If the path ultimately points to a file, the file will be deleted directly.eg\"shaderpacks/ExampleShaderPack.zip\"")
                .comment("!!!DONT DELETE IMPORTANT FILES!!!")
                .comment("If you wanna replace files ,pleas read \"README.txt\"")
                .defineListAllowEmpty("FilesNeedDelete",List.of(),() -> "/Path to Files Which Need be Deleted",obj -> obj instanceof String);
    }

    public static List<? extends String> getDelPathList() {
        return delListConfig.get();
    }

    public static void createREADME() {

        Path replacePath = getRePlacePath();

        if (!Files.exists(replacePath)) {
            try {
                Files.createDirectories(replacePath);
            } catch (IOException e) {
                NewGame.LOGGER.error(e.toString());
            }
        }

        String path = replacePath.getParent().resolve("README.txt").toString();

        String content = """
                Save files and folders in the "replace" folder. After clicking the "NewGame" button,
                the files and folders in the root directory will be replaced, while maintaining the consistent path structure.
                For example:
                If the file "example/example.txt" exists under "replace/",
                it will be copied and used to replace "example/example.txt" in the root directory.
                
                If the target path ends with a file, only the file will be replaced;
                if it ends with a folder, the folder and all files within it will be replaced.
                "mods/" and "config/replace/" are baned
                """;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            // 写入内容
            writer.write(content);
        } catch (IOException e) {
            NewGame.LOGGER.error(e.toString());
        }
    }
}
