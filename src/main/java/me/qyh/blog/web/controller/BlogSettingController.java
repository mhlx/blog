package me.qyh.blog.web.controller;

import me.qyh.blog.entity.BlogConfig;
import me.qyh.blog.security.Authenticated;
import me.qyh.blog.service.BlogConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("api/setting")
@Authenticated
public class BlogSettingController {

    private final BlogConfigService configService;

    public BlogSettingController(BlogConfigService configService) {
        super();
        this.configService = configService;
    }

    @GetMapping
    public BlogConfig index() {
        return configService.getConfig();
    }

    @PutMapping
    public ResponseEntity<Object> save(@RequestBody @Valid BlogConfig config,
                                       @RequestParam("oldPassword") String oldPassword) {
        configService.update(config, oldPassword);
        return ResponseEntity.noContent().build();
    }
}
