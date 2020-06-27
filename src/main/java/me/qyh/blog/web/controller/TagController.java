package me.qyh.blog.web.controller;

import me.qyh.blog.entity.Tag;
import me.qyh.blog.security.Authenticated;
import me.qyh.blog.service.TagService;
import me.qyh.blog.web.template.TemplateDataMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Authenticated
@RestController
@RequestMapping("api")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        super();
        this.tagService = tagService;
    }

    @TemplateDataMapping("tags")
    public List<Tag> getTags() {
        return tagService.getAllTags();
    }

    @DeleteMapping("tags/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") int id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("tags/{id}")
    public ResponseEntity<?> update(@PathVariable("id") int id, @RequestBody @Valid Tag tag) {
        tag.setId(id);
        tagService.updateTag(tag);
        return ResponseEntity.noContent().build();
    }
}
