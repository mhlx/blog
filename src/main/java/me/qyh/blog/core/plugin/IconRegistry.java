package me.qyh.blog.core.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IconRegistry {

	private IconRegistry() {
		super();
	}

	private static final IconRegistry ins = new IconRegistry();
	public List<Icon> icons = Collections.synchronizedList(new ArrayList<>());

	public void addIcon(Icon icon) {
		icons.add(icon);
	}

	public List<Icon> getIcons() {
		return List.copyOf(icons);
	}

	public static IconRegistry getInstance() {
		return ins;
	}

}
