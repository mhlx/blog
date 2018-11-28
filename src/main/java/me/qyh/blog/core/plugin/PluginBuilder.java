package me.qyh.blog.core.plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;

import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.Validators;

public class PluginBuilder {

	/**
	 * 
	 * @param name
	 *            插件名称
	 * @param projectRoot
	 *            项目根目录 例如 c:/workspace/project/blog
	 * @param plugin
	 *            插件根目录
	 * @throws Exception
	 */
	public static void build(String name, Path projectRoot, Path plugin) throws Exception {
		ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(null);
		Resource[] resources = resolver.getResources("classpath:me/qyh/blog/plugin/" + name + "/**");
		Path root = plugin.resolve(name);
		FileUtils.deleteQuietly(root);
		if (!Validators.isEmpty(resources)) {
			Path classRoot = root.resolve("WEB-INF/classes/me/qyh/blog/plugin/" + name);

			FileUtils.forceMkdir(classRoot);

			Path _root = resolver.getResource("classpath:").getFile().toPath().resolve("me/qyh/blog/plugin/" + name);

			for (Resource res : resources) {
				Path source = res.getFile().toPath();
				if (FileUtils.isRegularFile(source)) {
					Path dest = classRoot.resolve(_root.relativize(source));
					FileUtils.copy(source, dest);
				}
			}
		}

		Path _root = projectRoot.resolve("src/main/webapp");

		copyDir(_root, root, "WEB-INF/templates/plugin/" + name);
		copyDir(_root, root, "static/plugin/" + name);

		System.out.println("插件" + name + "构建完成");

	}

	private static void copyDir(Path source, Path dest, String resolvePath) throws IOException {
		Path resolve = source.resolve(resolvePath);
		if (!FileUtils.exists(resolve)) {
			return;
		}
		Path _resolve = dest.resolve(resolvePath);

		Files.walk(resolve).forEach(p -> {

			Path _dest = _resolve.resolve(resolve.relativize(p));

			if (FileUtils.isRegularFile(p)) {
				try {
					FileUtils.copy(p, _dest);
				} catch (IOException e) {
					throw new SystemException(e.getMessage(), e);
				}
			}

		});
	}

	public static void main(String[] args) throws Exception {
		build("imagevideolazyload", Paths.get("F:\\workspace\\blog"), Paths.get("f:/plugin"));
	}

}
