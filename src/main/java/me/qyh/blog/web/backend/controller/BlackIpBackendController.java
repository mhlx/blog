package me.qyh.blog.web.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import me.qyh.blog.security.BlackIpService;

@Controller
@RequestMapping("console")
public class BlackIpBackendController {

	private final BlackIpService blackIpService;

	public BlackIpBackendController(BlackIpService blackIpService) {
		super();
		this.blackIpService = blackIpService;
	}

	@GetMapping("blackips")
	public String index(Model model) {
		model.addAttribute("blackips", blackIpService.getAllBlackIps());
		return "console/blackip/index";
	}

	@PostMapping("blackip/save")
	@ResponseBody
	public void save(@RequestParam("ip") String ip) {
		blackIpService.saveBlackIp(ip);
	}

	@PostMapping("blackip/delete")
	@ResponseBody
	public void delete(@RequestParam("ip") String ip) {
		blackIpService.deleteBlackIp(ip);
	}
}
