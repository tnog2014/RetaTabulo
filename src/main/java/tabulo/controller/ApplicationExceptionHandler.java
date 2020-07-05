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

import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import tabulo.ApplicationException;
import tabulo.MessageUtil;
import tabulo.PropertyAccessor;
import tabulo.constant.CommonConst;
import tabulo.constant.ErrorCode;

@ControllerAdvice
public class ApplicationExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationExceptionHandler.class);

	private static final String SERVICE_UNAVAILABLE = "page.error.message.service_unavailable";

	private static final String NOT_FOUND = "page.error.message.file_not_found";

	@Autowired
	MessageUtil messageUtil;

	@Autowired
	PropertyAccessor propertyAccessor;

	@ExceptionHandler({ApplicationException.class})
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ModelAndView handleApplicationException(ApplicationException e, WebRequest req) {
		LOGGER.debug("handleApplicationException start");
		String debugMessage = "";
		ErrorCode code = e.getCode();
		if (code == null) {
			LOGGER.error("[CHECK]" + e.getMessage());
		} else {
			String message = messageUtil.decodeLogMessage(code, e.getParameters());
			debugMessage = message;
			LOGGER.error("[CHECK]" + message);
		}

		ModelAndView mav = new ModelAndView();

		String errorMessage = messageUtil.getMessage(SERVICE_UNAVAILABLE);
		Locale locale = LocaleContextHolder.getLocale();
		mav.addObject(CommonConst.LOCALE_KEY, locale.getLanguage());
		mav.addObject("errorMessage", errorMessage);

		if (propertyAccessor.isDebug()) {
			mav.addObject("debugMessage", debugMessage);
		}

		String timestamp = (new Date()).toString();
		mav.addObject("timestamp", timestamp);

		mav.setViewName("error");
		LOGGER.debug("handleApplicationException end");
		return mav;
	}
}
