package me.qyh.blog.security;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.cage.Cage;
import com.github.cage.token.RandomTokenGenerator;

import me.qyh.blog.BlogProperties;
import me.qyh.blog.Message;
import me.qyh.blog.exception.BadRequestException;
import me.qyh.blog.exception.ResourceNotFoundException;

@RestController
@RequestMapping("api")
public class CaptchaController implements CaptchaValidator {

	private static final int MAX_SIZE = 1000;
	private final Map<String, String> captchaMap = Collections.synchronizedMap(new LinkedHashMap<String, String>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, String> eldest) {
			return size() > MAX_SIZE;
		}

	});
	private final BlogProperties blogProperties;
	private final Cage cage;

	public CaptchaController(BlogProperties blogProperties) {
		Random random = new Random(System.nanoTime());
		this.cage = new Cage(null, null, null, null, Cage.DEFAULT_COMPRESS_RATIO,
				new RandomTokenGenerator(random, blogProperties.getCaptchaNum(), 0), random);
		this.blogProperties = blogProperties;
	}

	@PostMapping("captcha")
	public ResponseEntity<String> create() {
		String id = UUID.randomUUID().toString().replace("-", "");
		captchaMap.put(id, "");
		return ResponseEntity.created(blogProperties.buildUrl("api/captchas/" + id)).body(id);
	}

	@GetMapping(value = "captchas/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] getCaptcha(@PathVariable("id") String id) {
		String capText;
		synchronized (captchaMap) {
			capText = captchaMap.get(id);
			if (capText == null) {
				throw new ResourceNotFoundException("captcha.notExists", "验证码不存在");
			}
			if (capText.isEmpty()) {
				capText = cage.getTokenGenerator().next();
				captchaMap.put(id, capText);
			}
		}
		BufferedImage bi = cage.drawImage(capText);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(bi, "jpg", baos);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return baos.toByteArray();
	}

	@Override
	public void validate(String key, String code) {
		if (key == null || code == null || !code.equals(captchaMap.remove(key))) {
			throw new BadRequestException(new Message("captcha.invalid", "验证码错误"));
		}
	}

}
