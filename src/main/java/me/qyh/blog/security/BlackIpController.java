package me.qyh.blog.security;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<String> index() {
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
