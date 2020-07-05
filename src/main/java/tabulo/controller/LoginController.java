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

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import tabulo.PropertyAccessor;
import tabulo.constant.CommonConst;
import tabulo.constant.MappingPath;
import tabulo.constant.ViewName;
import tabulo.service.UserService;

@Controller
public class LoginController {

	private static final String PREFIX_LANG = "lang.";

	private static final String KEY_LOCALE_DATA = "localeData";

	private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

	private static final String[] BASE_KEYS = new String[]{
			"message.login.please",
			"common.user",
			"common.password",
			"common.login"
	};

	@Autowired
	MessageSource messageSource;

	@Autowired
	PropertyAccessor propertyAccessor;

	@Autowired
	UserService userService;

	private String localeData = null;

	private String wrap(String item, String c) {
		return c + item + c;
	}

	@RequestMapping(
			path = MappingPath.LOGIN)
	public String showLoginForm(Model model) {
		LOGGER.debug("showLoginForm start");
		if (this.localeData == null) {
			this.localeData = getLocaleData();
		}
		model.addAttribute(KEY_LOCALE_DATA, this.localeData);
		Locale locale = LocaleContextHolder.getLocale();
		if (locale != null) {
			model.addAttribute(CommonConst.LOCALE_KEY, locale.getLanguage());
		}
		LOGGER.debug("showLoginForm end");
		return ViewName.LOGIN;
	}

	private String getLocaleData() {
		LOGGER.debug("getLocaleData start");
		String[] locales = propertyAccessor.getLocales().split(",");
		String[] keys = new String[BASE_KEYS.length + locales.length];
		System.arraycopy(BASE_KEYS, 0, keys, 0, BASE_KEYS.length);
		for (int i = 0; i < locales.length; i++) {
			keys[BASE_KEYS.length + i] = PREFIX_LANG + locales[i];
		}
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		for (int i = 0; i < locales.length; i++) {
			String locale = locales[i];
			sb.append(wrap(locale, "'")).append(":");
			sb.append("[");
			for (int k = 0; k < keys.length; k++) {
				String a = messageSource.getMessage(keys[k], new Object[]{},
						Locale.forLanguageTag(locale));
				sb.append(wrap(a, "'"));
				if (k < keys.length - 1) {
					sb.append(",");
				}
			}
			sb.append("]");
			if (i < locales.length - 1) {
				sb.append(",");
			}
		}
		sb.append("}");
		String ret = sb.toString();
		LOGGER.debug("getLocaleData end: " + ret);
		return ret;
	}

}
