package me.qyh.blog.web.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.qyh.blog.entity.Tag;
import me.qyh.blog.security.Authenticated;
import me.qyh.blog.service.TagService;

@Authenticated
@RestController
@RequestMapping("api")
public class TagController {

	private final TagService tagService;

	public TagController(TagService tagService) {
		super();
		this.tagService = tagService;
	}

	@GetMapping("tags")
	public List<Tag> getTags(Model model) {
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
