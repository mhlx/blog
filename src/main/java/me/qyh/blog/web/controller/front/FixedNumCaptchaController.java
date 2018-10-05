package me.qyh.blog.web.controller.front;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.WebUtils;

import com.github.cage.Cage;
import com.github.cage.GCage;
import com.github.cage.token.RandomTokenGenerator;

import me.qyh.blog.core.config.UrlHelper;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.util.StringUtils;
import me.qyh.blog.web.security.CaptchaValidator;

@Controller
public class FixedNumCaptchaController implements InitializingBean, CaptchaValidator {

	private static final String CAPTHCHA_ID = "captchaId";

	private static final Random random = new Random(System.nanoTime());
	private static final Message INVALID_CAPTCHA_MESSAGE = new Message("validateCode.error", "验证码错误");

	@Value("${captcha.num:4}")
	private int num;
	@Value("${captcha.delta:0}")
	private int delta;
	@Value("${captcha.maxNum:1000}")
	private int captchMum;
	@Autowired
	private UrlHelper urlHelper;

	private Cage cage;

	private final Map<String, String> fifoMap = new LinkedHashMap<>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, String> eldest) {
			return size() > captchMum;
		}

	};

	@ResponseBody
	@GetMapping(value = "captcha", produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] draw(HttpServletRequest request, HttpServletResponse resp) {
		String capText = cage.getTokenGenerator().next();
		String uuid = StringUtils.uuid();
		Cookie cookie = WebUtils.getCookie(request, CAPTHCHA_ID);
		synchronized (this) {
			if (cookie != null) {
				fifoMap.remove(cookie.getValue());
			}
			fifoMap.put(uuid, capText);
		}
		urlHelper.getCookieHelper().setCookie(CAPTHCHA_ID, uuid, -1, request, resp);
		BufferedImage bi = cage.drawImage(capText);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(bi, "jpg", baos);
		} catch (IOException e) {
			throw new SystemException(e.getMessage(), e);
		}
		return baos.toByteArray();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (num > 0 && delta >= 0) {
			cage = new Cage(null, null, null, null, Cage.DEFAULT_COMPRESS_RATIO,
					new RandomTokenGenerator(random, num, delta), random);
		} else {
			cage = new GCage();
		}
	}

	@Override
	public void doValidate(HttpServletRequest request) throws LogicException {
		String captcha = request.getParameter("validateCode");
		if (captcha == null) {
			throw new LogicException(INVALID_CAPTCHA_MESSAGE);
		}
		Cookie cookie = WebUtils.getCookie(request, CAPTHCHA_ID);
		if (cookie == null) {
			throw new LogicException(INVALID_CAPTCHA_MESSAGE);
		}
		String text;
		synchronized (this) {
			text = fifoMap.remove(cookie.getValue());
		}
		if (text == null || !text.equals(captcha)) {
			throw new LogicException(INVALID_CAPTCHA_MESSAGE);
		}
	}

}
