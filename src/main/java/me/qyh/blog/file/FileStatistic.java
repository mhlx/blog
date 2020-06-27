package me.qyh.blog.file;

import java.util.List;

public class FileStatistic {

    private final long size;
    private final long dirCount;
    private final long fileCount;
    private final List<FileTypeStatistic> typeStatistics;

    FileStatistic(long size, long dirCount, long fileCount, List<FileTypeStatistic> typeStatistics) {
        super();
        this.size = size;
        this.dirCount = dirCount;
        this.fileCount = fileCount;
        this.typeStatistics = typeStatistics;
    }

    public long getSize() {
        return size;
    }

    public long getDirCount() {
        return dirCount;
    }

    public long getFileCount() {
        return fileCount;
    }

    public List<FileTypeStatistic> getTypeStatistics() {
        return typeStatistics;
    }

}
