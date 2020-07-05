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
package tabulo;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;

import tabulo.constant.ErrorCode;

@Controller
public class MessageUtil {

	@Autowired
	MessageSource messageSource;

	@Autowired
	PropertyAccessor propertyAccessor;

	/**
	 * Get a message corresponding to the given code.
	 * @param code code
	 * @return a message
	 */
	public String getMessage(String code) {
		Locale locale = LocaleContextHolder.getLocale();
		return getMessage(code, locale);
	}

	/**
	 * Get a message corresponding to the given code.
	 * @param code code
	 * @param locale locale
	 * @return a message
	 */
	public String getMessage(String code, Locale locale) {
		String ret = null;
		try {
			ret = messageSource.getMessage(code, new Object[]{}, locale);
		} catch (Exception e) {
			ret = "%" + code + "%";
		}
		return ret;
	}

	public String decode(ErrorCode code, Object... parameters) {
		Locale locale = LocaleContextHolder.getLocale();
		return decode(code, locale, parameters);
	}

	public String decode(ErrorCode code, Locale locale, Object... parameters) {
		String message = null;
		if (parameters != null) {
			message = String.format(getMessage(code.toString(), locale), parameters);
		} else {
			message = getMessage(code.toString(), locale);
		}
		return message;
	}

	public String decodeLogMessage(ErrorCode code, Object... parameters) {
		String logOutputLocale = propertyAccessor.getLogOutputLocale();
		if (logOutputLocale == null) {
			logOutputLocale = "en";
		}
		Locale locale = Locale.forLanguageTag(logOutputLocale);
		return decode(code, locale, parameters);
	}

}
