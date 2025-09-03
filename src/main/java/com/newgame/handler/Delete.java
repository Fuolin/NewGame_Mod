package com.newgame.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public class Delete {
    public static void del(String path, String root ) throws IOException {
        //判断路径是否为空
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("path is null");
        }
        if (root == null || root.isEmpty()) {
            throw new IllegalArgumentException("root path is null");
        }

        //获取完整的路径
        Path rootPath = Path.of(root);
        Path fullPath = rootPath.resolve(path);

        if(Safe.safe(fullPath)) return;

        //判断删除方式
        boolean endsWithSeparator = path.endsWith("/") || path.endsWith(File.separator);

        // 检查路径是否存在
        if (!Files.exists(fullPath)) {
            throw new IOException("path not found:" + fullPath);
        }

        if (Files.isDirectory(fullPath)){
            // 路径以分隔符结尾，删除目录内所有内容但保留目录本身
            deleteDirectoryContent(fullPath);

            if (!endsWithSeparator){
                // 路径不以分隔符结尾，删除整个目录及其内容
                Files.delete(fullPath);
            }
        }else {
            // 如果是文件，直接删除
            Files.delete(fullPath);
        }
    }

    private static void deleteDirectoryContent(Path directory) throws IOException{
        // 如果是文件，直接删除
        if (Files.isRegularFile(directory)) {
            Files.delete(directory);
            return;
        }

        // 如果是目录，删除其所有内容（保留自身）
        if (Files.isDirectory(directory)) {
            try(Stream<Path> stream = Files.walk(directory)){
                stream.filter(p -> !p.equals(directory)) // 排除顶层目录本身
                        .sorted(Comparator.reverseOrder()) // 逆序确保删除顺序正确
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                // 包装为未检查异常传递给外层
                                throw new RuntimeException("file cannot be delete:" + p, e);
                            }
                        });
            } catch (RuntimeException e) {
                // 还原为IOException抛出
                if (e.getCause() instanceof IOException) {
                    throw (IOException) e.getCause();
                }
                throw e;
            }
        }else {
            // 既不是文件也不是目录（如特殊文件）
            throw new IOException("file is not supported" + directory);
        }
    }
}