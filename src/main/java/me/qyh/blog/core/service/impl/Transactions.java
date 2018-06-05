/*
 * Copyright 2016 qyh.me
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
package me.qyh.blog.core.service.impl;

import java.util.Objects;
import java.util.function.Consumer;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.RuntimeLogicException;
import me.qyh.blog.core.service.LogicConsumer;
import me.qyh.blog.core.service.LogicFunction;

/**
 * 用来在新事务中执行一些操作或者处理一些事务<b>事务提交</b>之后的事情
 * <p>
 * <b>事务提交后的操作必须在事务中使用，eg</b>
 * 
 * <pre>
 * TransactionStatus status = begin();
 * Transactions.afterCommit(() -&gt; System.out.println("commit"));
 * commit(status);
 * </pre>
 * 
 * 或者配合新事务使用
 * 
 * <pre>
 * Transactions.executeInReadOnlyTransaction(manager, status -&gt; {
 * 	Transactions.afterCommit(() -&gt; System.out.println("commit"));
 * })
 * </pre>
 * </p>
 * 
 * @see TransactionSynchronizationManager
 * @author Administrator
 *
 */
public final class Transactions {

	private Transactions() {
		super();
	}

	/**
	 * 事务提交成功后回调
	 * 
	 * @param callback
	 */
	public static void afterCommit(Callback callback) {
		Objects.requireNonNull(callback);
		afterCompletion(status -> {
			if (status == TransactionSynchronization.STATUS_COMMITTED) {
				callback.callback();
			}
		});
	}

	/**
	 * 事务回滚后回调
	 * 
	 * @param callback
	 */
	public static void afterRollback(Callback callback) {
		Objects.requireNonNull(callback);
		afterCompletion(status -> {
			if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
				callback.callback();
			}
		});
	}

	/**
	 * 事务完成之后回调
	 * 
	 * @see TransactionSynchronization#STATUS_COMMITTED
	 * @see TransactionSynchronization#STATUS_ROLLED_BACK
	 * @see TransactionSynchronization#STATUS_UNKNOWN
	 * @param consumer
	 * 
	 */
	public static void afterCompletion(Consumer<Integer> consumer) {
		Objects.requireNonNull(consumer);
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {

			@Override
			public void afterCompletion(int status) {
				consumer.accept(status);
			}

		});
	}

	/**
	 * 在 <b><i>新的</i></b> 只读事务中执行操作，并且返回一个结果
	 * 
	 * @param platformTransactionManager
	 * @param function
	 * @return
	 */
	public static <T> T executeInReadOnlyTransaction(PlatformTransactionManager platformTransactionManager,
			LogicFunction<T, TransactionStatus> function) {
		DefaultTransactionDefinition dtd = new DefaultTransactionDefinition();
		dtd.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		dtd.setReadOnly(true);
		return executeInTransaction(platformTransactionManager, dtd, function);
	}

	/**
	 * 在 <b><i>新的</i></b> 只读事务中执行操作
	 * 
	 * @param platformTransactionManager
	 * @param consumer
	 */
	public static void executeInReadOnlyTransaction(PlatformTransactionManager platformTransactionManager,
			Consumer<TransactionStatus> consumer) {
		DefaultTransactionDefinition dtd = new DefaultTransactionDefinition();
		dtd.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		dtd.setReadOnly(true);
		executeInTransaction(platformTransactionManager, dtd, status -> {
			consumer.accept(status);
			return null;
		});
	}

	/**
	 * 在 <b><i>新的</i></b> 默认事务中执行操作，并且返回一个结果
	 * 
	 * @param platformTransactionManager
	 * @see DefaultTransactionDefinition
	 * @return
	 */
	public static <T> T executeInTransaction(PlatformTransactionManager platformTransactionManager,
			LogicFunction<T, TransactionStatus> function) {
		DefaultTransactionDefinition dtd = new DefaultTransactionDefinition();
		dtd.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		return executeInTransaction(platformTransactionManager, dtd, function);
	}

	/**
	 * 在 <b><i>新的</i></b> 默认事务中执行操作
	 * 
	 * @param platformTransactionManager
	 * @param consumer
	 * @see DefaultTransactionDefinition
	 * @return
	 */
	public static void executeInTransaction(PlatformTransactionManager platformTransactionManager,
			LogicConsumer<TransactionStatus> consumer) {
		DefaultTransactionDefinition dtd = new DefaultTransactionDefinition();
		dtd.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		executeInTransaction(platformTransactionManager, dtd, status -> {
			consumer.accept(status);
			return null;
		});
	}

	private static <T> T executeInTransaction(PlatformTransactionManager platformTransactionManager,
			TransactionDefinition definition, LogicFunction<T, TransactionStatus> function) {
		TransactionTemplate template = new TransactionTemplate(platformTransactionManager, definition);
		return template.execute(status -> {
			try {
				return function.apply(status);
			} catch (LogicException e) {
				throw new RuntimeLogicException(e);
			}
		});
	}

	@FunctionalInterface
	public interface Callback {
		void callback();
	}
}
