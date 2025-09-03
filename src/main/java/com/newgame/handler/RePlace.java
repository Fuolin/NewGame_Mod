package com.newgame.handler;

import com.newgame.NewGame;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static com.newgame.NewGame.getRePlacePath;

public class RePlace {
    public static void place(String root) {
        Path replacePath = getRePlacePath();
        Path rootPath = Paths.get(root).toAbsolutePath().normalize();

        // 检查replace目录是否存在
        if (!Files.exists(replacePath)) {
            NewGame.LOGGER.error("Replace directory does not exist: {}", replacePath);
            return;
        }

        // 检查根目录是否存在
        if (!Files.exists(rootPath)) {
            NewGame.LOGGER.error("Root directory does not exist: {}", rootPath);
            return;
        }

        try {
            // 遍历replace目录下的所有文件和文件夹
            Files.walkFileTree(replacePath, new SimpleFileVisitor<>() {
                @Override
                public @NotNull FileVisitResult preVisitDirectory(@NotNull Path dir, @NotNull BasicFileAttributes attrs) throws IOException {
                    // 计算相对于replace目录的相对路径
                    Path relativePath = replacePath.relativize(dir);
                    // 构建目标路径
                    Path targetDir = rootPath.resolve(relativePath).normalize();

                    // 安全检查
                    if (Safe.safe(targetDir)) return FileVisitResult.SKIP_SUBTREE;


                    // 如果目标目录不存在，则创建
                    if (!Files.exists(targetDir)) {
                        Files.createDirectories(targetDir);
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                    // 计算相对于replace目录的相对路径
                    Path relativePath = replacePath.relativize(file);
                    // 构建目标路径
                    Path targetFile = rootPath.resolve(relativePath).normalize();

                    // 安全检查
                    if (Safe.safe(targetFile)) return FileVisitResult.CONTINUE;


                    // 确保目标文件的父目录存在
                    if (!Files.exists(targetFile.getParent())) {
                        Files.createDirectories(targetFile.getParent());
                    }

                    // 复制文件，替换已存在的文件
                    Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    NewGame.LOGGER.error("Replaced: {}", targetFile);

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public @NotNull FileVisitResult visitFileFailed(@NotNull Path file, @NotNull IOException exc) {
                    NewGame.LOGGER.error("Failed to access file: {}: {}", file, exc.getMessage());
                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (IOException e) {
            NewGame.LOGGER.error("Error during file replacement: {}", e.getMessage());
        }
    }
}