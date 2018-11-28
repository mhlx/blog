package me.qyh.blog.web.controller.console.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.qyh.blog.core.entity.Lock;
import me.qyh.blog.core.service.LockManager;
import me.qyh.blog.web.controller.console.BaseMgrController;

@RestController
@RequestMapping("api/console")
public class LockConsole extends BaseMgrController {

	@Autowired
	private LockManager lockManager;

	@GetMapping("locks")
	public List<Lock> allLock() {
		return lockManager.allLock();
	}

}
