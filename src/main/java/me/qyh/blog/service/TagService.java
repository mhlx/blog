package me.qyh.blog.service;

import me.qyh.blog.entity.Tag;
import me.qyh.blog.event.TagDeleteEvent;
import me.qyh.blog.exception.ResourceNotFoundException;
import me.qyh.blog.mapper.TagMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TagService {

    private final TagMapper tagMapper;
    private final ApplicationEventPublisher publisher;

    public TagService(TagMapper tagMapper, ApplicationEventPublisher publisher) {
        super();
        this.tagMapper = tagMapper;
        this.publisher = publisher;
    }

    @Transactional(readOnly = true)
    public List<Tag> getAllTags() {
        return tagMapper.selectAll();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteTag(final int id) {
        Optional<Tag> opTag = tagMapper.selectById(id);
        if (opTag.isEmpty()) {
            return;
        }
        tagMapper.deleteById(id);
        publisher.publishEvent(new TagDeleteEvent(this, opTag.get()));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateTag(final Tag tag) {
        Optional<Tag> opTag = tagMapper.selectById(tag.getId());
        if (opTag.isEmpty()) {
            throw new ResourceNotFoundException("tag.notExists", "标签不存在");
        }
        Tag old = opTag.get();
        if (old.getName().equals(tag.getName())) {
            return;
        }
        old.setName(tag.getName());
        old.setModifyTime(LocalDateTime.now());
        tagMapper.update(old);
    }
}
