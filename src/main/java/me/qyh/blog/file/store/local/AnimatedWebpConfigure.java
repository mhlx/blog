package me.qyh.blog.file.store.local;

import java.util.concurrent.Semaphore;

import me.qyh.blog.file.store.AnimatedWebpConfig;

public class AnimatedWebpConfigure extends AnimatedWebpConfig {

	private final Semaphore semaphore;

	public AnimatedWebpConfigure(int semaphoreNum) {
		super();
		this.semaphore = new Semaphore(semaphoreNum);
	}

	public AnimatedWebpConfig newAnimatedWebpConfig() {
		return new AnimatedWebpConfig(this);
	}

	public Semaphore getSemaphore() {
		return semaphore;
	}

}
