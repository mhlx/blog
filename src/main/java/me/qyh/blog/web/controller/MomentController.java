package me.qyh.blog.web.controller;

import me.qyh.blog.BlogProperties;
import me.qyh.blog.entity.Moment;
import me.qyh.blog.exception.ResourceNotFoundException;
import me.qyh.blog.security.Authenticated;
import me.qyh.blog.service.MomentService;
import me.qyh.blog.utils.WebUtils;
import me.qyh.blog.vo.MomentArchive;
import me.qyh.blog.vo.MomentArchiveQueryParam;
import me.qyh.blog.vo.MomentStatistic;
import me.qyh.blog.vo.PageResult;
import me.qyh.blog.web.template.TemplateDataMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Authenticated
@RestController
@RequestMapping("api")
public class MomentController {

    private final MomentService momentService;
    private final BlogProperties blogProperties;

    public MomentController(MomentService momentService, BlogProperties blogProperties) {
        super();
        this.momentService = momentService;
        this.blogProperties = blogProperties;
    }

    @TemplateDataMapping("moments/{id}")
    public Moment get(@PathVariable("id") int id) {
        return momentService.getMoment(id)
                .orElseThrow(() -> new ResourceNotFoundException("moment.notExists", "动态不存在"));
    }

    @TemplateDataMapping("momentArchives")
    public PageResult<MomentArchive> queryMomentArchives(@Valid MomentArchiveQueryParam param) {
        param.setQueryPrivate(true);
        param.setQueryPasswordProtected(true);
        return momentService.queryMomentArchive(param);
    }

    @TemplateDataMapping("moments/{id}/next")
    public Moment nextMoment(@PathVariable("id") int id) {
        return momentService.next(id)
                .orElseThrow(() -> new ResourceNotFoundException("moment.next.notExists", "下一条动态不存在"));
    }

    @TemplateDataMapping("moments/{id}/previous")
    public Moment prevMoment(@PathVariable("id") int id) {
        return momentService.prev(id)
                .orElseThrow(() -> new ResourceNotFoundException("moment.next.notExists", "上一条动态不存在"));
    }

    @Authenticated(required = false)
    @PatchMapping("moments/{id}/hit")
    public ResponseEntity<?> hitMoment(@PathVariable("id") int id, HttpServletRequest request) {
        if (WebUtils.isSpider(request)) {
            return ResponseEntity.noContent().build();
        }
        momentService.hit(id);
        return ResponseEntity.noContent().build();
    }

    @TemplateDataMapping("momentStatistic")
    public MomentStatistic statistic() {
        return momentService.getMomentStatistic();
    }

    @PostMapping("moment")
    public ResponseEntity<Integer> create(@Valid @RequestBody Moment moment) {
        int id = momentService.saveMoment(moment);
        return ResponseEntity.created(blogProperties.buildUrl("api/editableMoments/" + id)).body(id);
    }

    @PutMapping("moments/{id}")
    public ResponseEntity<?> updateMoment(@Valid @RequestBody Moment moment) {
        momentService.updateMoment(moment);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("moments/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") int id) {
        momentService.deleteMoment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("editableMoments/{id}")
    public Moment getEditableMoment(@PathVariable("id") int id) {
        return momentService.getMomentForEdit(id)
                .orElseThrow(() -> new ResourceNotFoundException("moment.notExists", "动态不存在"));
    }

}
