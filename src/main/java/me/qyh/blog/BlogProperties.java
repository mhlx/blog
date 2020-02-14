package me.qyh.blog;

import java.time.Duration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import me.qyh.blog.security.TOTPAuthenticator;

@ConfigurationProperties(prefix = "blog.core")
@Validated
public class BlogProperties {

	private boolean fileEnable;
	private boolean totpEnable;
	private String ipHeader;// used to get ip if has a proxy
	private String totpSecret;
	@Min(1)
	@Max(17)
	private int totpWindow = 3;
	@Min(1)
	private int loginLimitTimes = 10;
	@Min(1)
	private int loginLimitSec = 60;
	@Min(1)
	private int commentLimitTimes = 10;
	@Min(1)
	private int commentLimitSec = 60;
	@Min(1)
	private int captchaNum = 4;
	@URL(message = "无效的markdown转化服务地址")
	private String markdownServiceUrl;
	@Min(1)
	private int commentEmailNotifySecond = 600;// comment email notify every 600 second
	@Size(min = 1)
	private String commentEmailTemplateLocation;

	private boolean rebuildIndexWhenStartup;

	private Bucket commentBucket;
	private Bucket loginBucket;

	public boolean isFileEnable() {
		return fileEnable;
	}

	public void setFileEnable(boolean fileEnable) {
		this.fileEnable = fileEnable;
	}

	public boolean isTotpEnable() {
		return totpEnable;
	}

	public void setTotpEnable(boolean totpEnable) {
		this.totpEnable = totpEnable;
	}

	public String getIpHeader() {
		return ipHeader;
	}

	public void setIpHeader(String ipHeader) {
		this.ipHeader = ipHeader;
	}

	public String getTotpSecret() {
		return totpSecret;
	}

	public void setTotpSecret(String totpSecret) {
		TOTPAuthenticator.validateToken(totpSecret);
		this.totpSecret = totpSecret;
	}

	public int getTotpWindow() {
		return totpWindow;
	}

	public void setTotpWindow(int totpWindow) {
		this.totpWindow = totpWindow;
	}

	public int getLoginLimitTimes() {
		return loginLimitTimes;
	}

	public void setLoginLimitTimes(int loginLimitTimes) {
		this.loginLimitTimes = loginLimitTimes;
	}

	public int getLoginLimitSec() {
		return loginLimitSec;
	}

	public void setLoginLimitSec(int loginLimitSec) {
		this.loginLimitSec = loginLimitSec;
	}

	public int getCaptchaNum() {
		return captchaNum;
	}

	public void setCaptchaNum(int captchaNum) {
		this.captchaNum = captchaNum;
	}

	public String getMarkdownServiceUrl() {
		return markdownServiceUrl;
	}

	public void setMarkdownServiceUrl(String markdownServiceUrl) {
		this.markdownServiceUrl = markdownServiceUrl;
	}

	public int getCommentLimitTimes() {
		return commentLimitTimes;
	}

	public void setCommentLimitTimes(int commentLimitTimes) {
		this.commentLimitTimes = commentLimitTimes;
	}

	public int getCommentLimitSec() {
		return commentLimitSec;
	}

	public void setCommentLimitSec(int commentLimitSec) {
		this.commentLimitSec = commentLimitSec;
	}

	public int getCommentEmailNotifySecond() {
		return commentEmailNotifySecond;
	}

	public void setCommentEmailNotifySecond(int commentEmailNotifySecond) {
		this.commentEmailNotifySecond = commentEmailNotifySecond;
	}

	public String getCommentEmailTemplateLocation() {
		return commentEmailTemplateLocation;
	}

	public void setCommentEmailTemplateLocation(String commentEmailTemplateLocation) {
		this.commentEmailTemplateLocation = commentEmailTemplateLocation;
	}

	public boolean isRebuildIndexWhenStartup() {
		return rebuildIndexWhenStartup;
	}

	public void setRebuildIndexWhenStartup(boolean rebuildIndexWhenStartup) {
		this.rebuildIndexWhenStartup = rebuildIndexWhenStartup;
	}

	public Bucket getCommentBucket() {
		if (commentBucket == null) {
			synchronized (this) {
				if (commentBucket == null) {
					commentBucket = Bucket4j.builder()
							.addLimit(Bandwidth.classic(this.commentLimitTimes, Refill
									.intervally(this.commentLimitTimes, Duration.ofSeconds(this.commentLimitSec))))
							.build();
				}
			}
		}
		return commentBucket;
	}

	public Bucket getLoginBucket() {
		if (loginBucket == null) {
			synchronized (this) {
				if (loginBucket == null) {
					loginBucket = Bucket4j.builder()
							.addLimit(Bandwidth.classic(this.loginLimitTimes,
									Refill.intervally(this.loginLimitTimes, Duration.ofSeconds(this.loginLimitSec))))
							.build();
				}
			}
		}
		return loginBucket;
	}
}
