package me.qyh.blog.web.template;

import java.util.Optional;

import org.springframework.transaction.TransactionStatus;

/**
 * for template process only !!!
 * 
 * @author wwwqyhme
 *
 */
public class ProcessContext {

	private static final ThreadLocal<TransactionStatus> transactionStatusLocal = new ThreadLocal<>();

	public static void setTransactionStatus(TransactionStatus status) {
		transactionStatusLocal.set(status);
	}

	public static Optional<TransactionStatus> getTransactionStatus() {
		return Optional.ofNullable(transactionStatusLocal.get());
	}

	public static void remove() {
		transactionStatusLocal.remove();
	}

}
