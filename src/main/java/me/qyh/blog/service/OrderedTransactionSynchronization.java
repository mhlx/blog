package me.qyh.blog.service;

import org.springframework.core.Ordered;
import org.springframework.transaction.support.TransactionSynchronization;

public interface OrderedTransactionSynchronization extends TransactionSynchronization, Ordered{

}
