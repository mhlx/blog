package me.qyh.blog.security;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.cage.Cage;
import com.github.cage.token.RandomTokenGenerator;

import me.qyh.blog.BlogProperties;
import me.qyh.blog.exception.InvalidCaptchaException;
import me.qyh.blog.utils.StringUtils;

@RequestMapping("captcha")
public class CageCaptchaController implements CaptchaValidator {

	private static final String CAPTCHA_SESSION = "captcha";
	private static final String CAPTCHA_PARAMETER = CAPTCHA_SESSION;

	private final Cage cage;

	public CageCaptchaController(BlogProperties blogProperties) {
		Random random = new Random(System.nanoTime());
		this.cage = new Cage(null, null, null, null, Cage.DEFAULT_COMPRESS_RATIO,
				new RandomTokenGenerator(random, blogProperties.getCaptchaNum(), 0), random);
	}

	@ResponseBody
	@GetMapping(produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] draw(HttpServletRequest request) {
		String capText = cage.getTokenGenerator().next();
		BufferedImage bi = cage.drawImage(capText);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(bi, "jpg", baos);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		request.getSession().setAttribute(CAPTCHA_SESSION, capText);
		return baos.toByteArray();
	}

	@Override
	public void validate(HttpServletRequest request) {
		if (!isValidCaptchaRequest(request)) {
			throw new InvalidCaptchaException();
		}
	}

	private boolean isValidCaptchaRequest(HttpServletRequest request) {
		String captcha = request.getParameter(CAPTCHA_PARAMETER);
		if (StringUtils.isNullOrBlank(captcha)) {
			return false;
		}
		HttpSession session = request.getSession(false);
		if (session == null) {
			return false;
		}
		if (Objects.equals(captcha, session.getAttribute(CAPTCHA_SESSION))) {
			session.removeAttribute(CAPTCHA_SESSION);
			return true;
		}
		return false;
	}
}
