package me.qyh.blog.web.controller.front;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("heather")
public class HeatherController {

	@GetMapping
	public String heather() {
		return "heather";
	}

}
