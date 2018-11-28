package me.qyh.blog.web.controller.front;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import me.qyh.blog.core.config.Constants;
import me.qyh.blog.core.config.UrlHelper;
import me.qyh.blog.core.entity.Lock;
import me.qyh.blog.core.entity.LockKey;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.LockManager;
import me.qyh.blog.web.LockHelper;
import me.qyh.blog.web.Webs;
import me.qyh.blog.web.security.CaptchaValidator;
import me.qyh.blog.web.security.IgnoreSpaceLock;

@Controller
public class LockController {

	@Autowired
	private UrlHelper urlHelper;
	@Autowired
	private CaptchaValidator captchaValidator;
	@Autowired
	private LockManager lockManager;

	@IgnoreSpaceLock
	@PostMapping({ "space/{alias}/unlock", "/unlock" })
	public String unlock(HttpServletRequest request, RedirectAttributes ra, @RequestParam("lockId") String lockId,
			@RequestParam(required = false, name = "redirectUrl") String redirectUrl) {
		Optional<Lock> op = lockManager.findLock(lockId);
		if (!op.isPresent()) {
			return "redirect:" + urlHelper.getUrl();
		}
		Lock lock = op.get();
		LockKey key;
		try {
			captchaValidator.doValidate(request);
			key = lock.getKeyFromRequest(request);
			lock.tryOpen(key);
		} catch (LogicException e) {
			ra.addFlashAttribute(Constants.ERROR, e.getLogicMessage());
			return "redirect:" + Webs.getSpaceUrls(request).getUnlockUrl(lock, redirectUrl);
		}
		LockHelper.addKey(request, key);
		return "redirect:" + (redirectUrl == null ? urlHelper.getUrl() : redirectUrl);
	}

	@IgnoreSpaceLock
	@PostMapping(value = { "space/{alias}/api/lock/{lockId}/key", "api/lock/{lockId}/key" })
	@ResponseBody
	public ResponseEntity<Void> unlock(HttpServletRequest request, @PathVariable("lockId") String lockId)
			throws LogicException {
		Optional<Lock> op = lockManager.findLock(lockId);
		if (!op.isPresent()) {
			return ResponseEntity.notFound().build();
		}
		captchaValidator.doValidate(request);
		Lock lock = op.get();
		LockKey key = lock.getKeyFromRequest(request);
		lock.tryOpen(key);
		LockHelper.addKey(request, key);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
}
