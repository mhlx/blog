/*
 * Copyright 2018 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
