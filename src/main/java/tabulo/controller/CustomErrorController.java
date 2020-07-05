/**
 * Copyright(c) 2020 tnog2014. All rights reserved.
 *
 * This file is part of RetaTabulo.
 *
 * RetaTabulo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RetaTabulo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RetaTabulo.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package tabulo.controller;

import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;

import tabulo.ControllerUtil;
import tabulo.MessageUtil;

/**
 * Web アプリケーション全体のエラーコントローラー.
 * ErrorController インターフェースの実装クラス。
 */
@Controller
@RequestMapping("/error")
public class CustomErrorController implements ErrorController {

	private static final int STATUS_FILE_NOT_FOUND = 404;

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomErrorController.class);

	private static final String SERVICE_UNAVAILABLE = "page.error.message.service_unavailable";

	private static final String NOT_FOUND = "page.error.message.file_not_found";

	@Autowired
	MessageUtil messageUtil;

	/**
	 * エラーページのパスを返す。
	 *
	 * @return エラーページのパス
	 */
	@Override
	public String getErrorPath() {
		return "/error";
	}

	/**
	  * エラー情報を抽出する。
	  *
	  * @param req リクエスト情報
	  * @return エラー情報
	  */
	private static Map<String, Object> getErrorAttributes(HttpServletRequest req) {
		ServletWebRequest swr = new ServletWebRequest(req);
		DefaultErrorAttributes dea = new DefaultErrorAttributes(true);
		return dea.getErrorAttributes(swr, true);
	}

	/**
	 * レスポンス用のHTTPステータスを決める。
	 *
	 * @param req リクエスト情報
	 * @return レスポンス用HTTPステータス
	 */
	private static HttpStatus getHttpStatus(HttpServletRequest req) {
		// 404 以外は500にする
		Object statusCode = req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		if (statusCode != null && statusCode.toString().equals("404")) {
			status = HttpStatus.NOT_FOUND;
		}
		return status;
	}

	@RequestMapping
	public void error(
			HttpServletRequest req,
			ModelAndView mav,
			Model model) {
		LOGGER.debug("error start");
		ControllerUtil.preProcess(model);

		// Gea error attributes.
		Map<String, Object> attr = getErrorAttributes(req);

		// Remove a stack trace because it is not used and too long for logging.
		attr.remove("trace");
		LOGGER.info("An error occured:" + attr);

		// HTTP ステータスを決める
		HttpStatus status = getHttpStatus(req);

		String code = SERVICE_UNAVAILABLE;
		if (status.value() == STATUS_FILE_NOT_FOUND) {
			code = NOT_FOUND;
		}
		String errorMessage = messageUtil.getMessage(code);
		model.addAttribute("errorMessage", errorMessage);

		model.addAttribute("timestamp", attr.get("timestamp"));
		mav.setStatus(status);
		mav.setViewName("error");
		LOGGER.debug("error end");
	}
}
