package me.qyh.blog.web.template.expression;

import me.qyh.blog.utils.FileUtils;

/**
 * @author wwwqyhme
 */
final class Formats {

    public String formatByte(long bytes) {
        return FileUtils.humanReadableByteCountBin(bytes);
    }
}
