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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import tabulo.constant.CommonConst;

public class LocaleCheckInterceptor extends HandlerInterceptorAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(LocaleCheckInterceptor.class);

	@Autowired
	private PropertyAccessor accessor;

	@Override
	public final boolean preHandle(
			HttpServletRequest request,
			HttpServletResponse response,
			Object handler) throws Exception {

		if (!(handler instanceof ResourceHttpRequestHandler)) {
			// パラメータlangが定義ファイルで指定された値でなければエラーとする。
			String query = request.getQueryString();

			if (query != null) {
				Map<String, String> map = new HashMap<String, String>();
				String[] items = null;
				if (query.contains("&")) {
					items = query.split("&");
				} else {
					items = new String[]{query};
				}
				for (String item : items) {
					String[] kv = split(item);
					map.put(kv[0], kv[1]);
				}
				if (map.containsKey(CommonConst.LOCALE_KEY)) {
					String value = map.get(CommonConst.LOCALE_KEY);
					if (!isValidLocale(value)) {
						LOGGER.error("不正な言語コードが指定されました:[" + value + "]");
						response.sendRedirect("/login");
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Check if the locale string is valid.
	 *
	 * @param locale locale
	 * @return true if valid, false otherwise.
	 */
	public boolean isValidLocale(String locale) {
		return Arrays.asList(accessor.getLocales().split(",")).contains(locale);
	}

	public static String[] split(String item) {
		int index = item.indexOf("=");
		String[] ret = null;
		if (index > -1) {
			ret = new String[]{item.substring(0, index), item.substring(index + 1)};
		} else {
			ret = new String[]{item, null};
		}
		return ret;

	}
}
