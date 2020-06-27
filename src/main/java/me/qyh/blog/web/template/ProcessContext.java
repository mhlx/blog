package me.qyh.blog.web.template;

import org.springframework.transaction.TransactionStatus;

import java.util.Optional;

/**
 * for template process only !!!
 *
 * @author wwwqyhme
 */
public class ProcessContext {

    private static final ThreadLocal<TransactionStatus> transactionStatusLocal = new ThreadLocal<>();
    private static final ThreadLocal<TemplateDataRequest> dataRequestStatusLocal = new ThreadLocal<>();

    public static void setTransactionStatus(TransactionStatus status) {
        transactionStatusLocal.set(status);
    }

    public static Optional<TransactionStatus> getTransactionStatus() {
        return Optional.ofNullable(transactionStatusLocal.get());
    }

    public static void remove() {
        transactionStatusLocal.remove();
    }

    public static TemplateDataRequest getTemplateDataRequest() {
        return dataRequestStatusLocal.get();
    }

    public static void setTemplateDataRequest(TemplateDataRequest request) {
        dataRequestStatusLocal.set(request);
    }
}
