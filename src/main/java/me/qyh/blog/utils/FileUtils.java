package me.qyh.blog.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtils {

    private static final char SPLITER = '/';
    private static final char SPLITER2 = '\\';
    private static final char[] ILLEGAL_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>',
            '|', '\"', ':', '\u0000'};

    private FileUtils() {
        super();
    }

    public static String getFileExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    public static String getFileExtension(String fullName) {
        return getFileExtension(new File(fullName));
    }

    public static String getFileExtension(Path path) {
        return getFileExtension(path.toFile());
    }

    public static void deleteQuietly(Path path) {
        if (!Files.exists(path)) {
            return;
        }
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception ignored) {
        }
    }

    public static void forceMkdir(Path path) throws IOException {
        if (path == null) {
            return;
        }
        if (Files.exists(path) && Files.isDirectory(path)) {
            return;
        }
        synchronized (FileUtils.class) {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            } else {
                if (!Files.isDirectory(path)) {
                    throw new IOException("目标位置" + path + "已经存在文件，但不是文件夹");
                }
            }
        }
    }

    public static void move(Path source, Path target) throws IOException {
        forceMkdir(target.getParent());
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 删除连续的 '/'，开头和结尾的'/'
     *
     * <p>
     * cleanPath("\\\\////123/\\\\////456//\\\\////789.txt") =
     * '123/456/789.txt';<br>
     * cleanPath("") = ""; <br>
     * cleanPath(" ") = " "; <br>
     * cleanPath(null) = "";
     * </p>
     *
     * @param path 路径
     * @return 新路径
     */
    public static String cleanPath(String path) {
        if (StringUtils.isNullOrBlank(path)) {
            return "";
        }
        char[] chars = path.toCharArray();
        char prev = chars[0];
        char last = SPLITER;
        if (chars.length == 1) {
            if (prev == SPLITER || prev == SPLITER2) {
                return "";
            }
            return Character.toString(prev);
        }
        if (prev == SPLITER2) {
            prev = SPLITER;
        }
        StringBuilder sb = new StringBuilder();
        if (prev != SPLITER) {
            sb.append(prev);
        }
        for (int i = 1; i < chars.length; i++) {
            char ch = chars[i];
            if (ch == SPLITER || ch == SPLITER2) {
                if (prev == SPLITER) {
                    continue;
                }
                prev = SPLITER;
                if (i < chars.length - 1) {
                    sb.append(SPLITER);
                    last = SPLITER;
                }
            } else {
                prev = ch;
                sb.append(ch);
                last = ch;
            }
        }
        if (last == SPLITER) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public static boolean validPath(String path) {
        try {
            Paths.get(path);
            return true;
        } catch (InvalidPathException e) {
            return false;
        }
    }

    /**
     * 提供一个简单的文件名校验，最終是否允许以能否成功创建文件为准
     * <p>
     * <b>不会对文件系统的保留文件名做校验，例如windows下的CON文件</b>
     * </p>
     *
     * @param name 包含后缀名的文件名
     * @return 是否合法
     */
    public static boolean maybeValidateFilename(String name) {
        if (StringUtils.isNullOrBlank(name)) {
            return false;
        }

        if (!validPath(name)) {
            return false;
        }

        if (name.chars().anyMatch(FileUtils::isIllegalChar)) {
            return false;
        }

        if (name.endsWith(".")) {
            return false;
        }

        // 一些文件系統的保留名称不做校验 例如windows下的CON
        return !name.endsWith(" ");
    }

    public static boolean isSub(Path dest, Path parent) {
        return dest.equals(parent) || dest.startsWith(parent.normalize());
    }

    public static String getNameWithoutExtension(String fullname) {
        String fileName = new File(fullname).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }

    private static boolean isIllegalChar(int ch) {
        for (char illegal : ILLEGAL_CHARACTERS) {
            if (illegal == ch) {
                return true;
            }
        }
        return false;
    }

    public static void createFile(Path path) {
        synchronized (FileUtils.class) {
            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(path.getParent());
                    Files.createFile(path);
                } catch (IOException e) {
                    throw new RuntimeException("创建文件：" + path + "失败:" + e.getMessage(), e);
                }
            } else {
                if (!Files.isRegularFile(path)) {
                    throw new RuntimeException("目标位置" + path + "已经存在文件，但不是文件");
                }
            }
        }
    }

    public static long lastModified(Path p) {
        try {
            return Files.getLastModifiedTime(p).toMillis();
        } catch (IOException e) {
            return -1;
        }
    }

    public static long size(Path p) {
        try {
            return Files.size(p);
        } catch (IOException e) {
            return -1;
        }
    }

    public static String humanReadableByteCountBin(long bytes) {
        long b = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        return b < 1024L ? bytes + " B"
                : b <= 0xfffccccccccccccL >> 40 ? String.format("%.1f KiB", bytes / 0x1p10)
                : b <= 0xfffccccccccccccL >> 30 ? String.format("%.1f MiB", bytes / 0x1p20)
                : b <= 0xfffccccccccccccL >> 20 ? String.format("%.1f GiB", bytes / 0x1p30)
                : b <= 0xfffccccccccccccL >> 10 ? String.format("%.1f TiB", bytes / 0x1p40)
                : b <= 0xfffccccccccccccL
                ? String.format("%.1f PiB", (bytes >> 10) / 0x1p40)
                : String.format("%.1f EiB", (bytes >> 20) / 0x1p40);
    }

}
