package me.qyh.blog.plugin.sqlexport;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.zaxxer.hikari.HikariDataSource;

import me.qyh.blog.core.context.Environment;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.security.AttemptLogger;
import me.qyh.blog.core.security.EnsureLogin;
import me.qyh.blog.core.security.GoogleAuthenticator;
import me.qyh.blog.core.util.FileUtils;

@EnsureLogin
@Controller
public class SqlDumpController {

	private final List<SqlDump> dumps;
	private final HikariDataSource dataSource;
	private final GoogleAuthenticator ga;
	private final AttemptLogger attemptLogger;

	SqlDumpController(List<SqlDump> dumps, HikariDataSource dataSource, GoogleAuthenticator ga,
			AttemptLogger attemptLogger) {
		super();
		this.dumps = dumps;
		this.dataSource = dataSource;
		this.ga = ga;
		this.attemptLogger = attemptLogger;
	}

	@GetMapping("mgr/sqlExport")
	public String dump() {
		return "plugin/sqlexport/index";
	}

	@PostMapping("mgr/sqlExport")
	public String dump(HttpServletRequest request, HttpServletResponse response, RedirectAttributes ra)
			throws LogicException {
		String ip = Environment.getIP();
		if (attemptLogger.log(ip)) {
			ra.addFlashAttribute("error", new Message("sql.export.forbidden", "尝试次数过多，请稍后再尝试导出"));
			return "redirect:/mgr/sqlExport";
		}
		if (!ga.checkCode(request.getParameter("code"))) {
			ra.addFlashAttribute("error", new Message("otp.verifyFail", "动态口令校验失败"));
			return "redirect:/mgr/sqlExport";
		}
		attemptLogger.remove(ip);
		String productName;
		try (Connection conn = dataSource.getConnection()) {
			productName = conn.getMetaData().getDatabaseProductName();
		} catch (SQLException e) {
			throw new SystemException(e.getMessage(), e);
		}

		for (SqlDump dump : dumps) {
			if (dump.matchProductName(productName)) {
				try {
					dump.doDump(dataSource, is -> {
						response.setHeader("Content-Disposition",
								"attachment;filename=" + productName.toLowerCase() + ".sql");
						response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
						try {
							FileUtils.write(is, response.getOutputStream());
						} catch (IOException e) {
							// throw new SystemException(e.getMessage(), e);
						}
					});
					return null;
				} catch (Exception e) {
					throw new SystemException(e.getMessage(), e);
				}
			}
		}
		ra.addFlashAttribute("error", new Message("sql.export.unsupport", "不支持该数据库的导出"));
		return "redirect:/mgr/sqlExport";
	}

}
