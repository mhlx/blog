package me.qyh.blog.security;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
@Authenticated
public class BlackIpController {

	private final BlackIpService blackIpService;

	public BlackIpController(BlackIpService blackIpService) {
		super();
		this.blackIpService = blackIpService;
	}

	@GetMapping("blackips")
	public List<String> index(Model model) {
		return blackIpService.getAllBlackIps();
	}

	@PostMapping("blackip")
	public ResponseEntity<?> save(@RequestParam("ip") String ip) {
		blackIpService.saveBlackIp(ip);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("blackip")
	public ResponseEntity<?> delete(@RequestParam("ip") String ip) {
		blackIpService.deleteBlackIp(ip);
		return ResponseEntity.noContent().build();
	}
}
