package me.qyh.blog.security;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class BlackIpService {

	private final BlackIpMapper blackIpMapper;
	private final StampedLock lock = new StampedLock();
	private final Set<String> ips = new HashSet<>();
	private final TransactionTemplate writeTemplate;

	public BlackIpService(BlackIpMapper blackIpMapper, TransactionTemplate writeTemplate) {
		super();
		this.blackIpMapper = blackIpMapper;
		this.writeTemplate = writeTemplate;
		init();
	}

	@Transactional(readOnly = true)
	public List<String> getAllBlackIps() {
		return blackIpMapper.selectAll();
	}

	public void deleteBlackIp(String ip) {
		long stamp = lock.writeLock();
		try {
			if (!ips.contains(ip)) {
				return;
			}
			writeTemplate.executeWithoutResult(status -> {
				blackIpMapper.delete(ip);
				TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

					@Override
					public void afterCommit() {
						ips.remove(ip);
					}
				});
			});
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	public void saveBlackIp(String ip) {
		long stamp = lock.writeLock();
		try {
			if (ips.contains(ip)) {
				return;
			}
			writeTemplate.executeWithoutResult(status -> {
				blackIpMapper.insert(ip);
				TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

					@Override
					public void afterCommit() {
						ips.add(ip);
					}
				});
			});
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	public Map<String, Boolean> isBlackIps(Set<String> ips) {
		long stamp = lock.tryOptimisticRead();
		try {
			retryHoldingLock: for (;; stamp = lock.readLock()) {
				if (stamp == 0L)
					continue retryHoldingLock;
				Map<String, Boolean> result = ips.stream()
						.collect(Collectors.toMap(ip -> ip, ip -> this.ips.contains(ip)));
				if (!lock.validate(stamp))
					continue retryHoldingLock;
				return result;
			}
		} finally {
			if (StampedLock.isReadLockStamp(stamp)) {
				lock.unlockRead(stamp);
			}
		}
	}

	public boolean isBlackIp(String ip) {
		if (ip == null)
			return false;
		long stamp = lock.tryOptimisticRead();
		try {
			retryHoldingLock: for (;; stamp = lock.readLock()) {
				if (stamp == 0L)
					continue retryHoldingLock;
				boolean contains = ips.contains(ip);
				if (!lock.validate(stamp))
					continue retryHoldingLock;
				return contains;
			}
		} finally {
			if (StampedLock.isReadLockStamp(stamp)) {
				lock.unlockRead(stamp);
			}
		}
	}

	private void init() {
		blackIpMapper.selectAll().forEach(ips::add);
	}

}
