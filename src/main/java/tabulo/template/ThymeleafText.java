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
package tabulo.template;

import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class ThymeleafText {

	private TemplateEngine templateEngine = new ThymeleafConfig().textTemplateEngine();

	/**
	 * Create a text from a template and given parameters.
	 *
	 * @param templateName a template name
	 * @param params a map of embedding parameters
	 * @return a merged text
	 */
	public String process(final String templateName, final Map<String, Object> params) {
		final Context ctx = new Context(Locale.getDefault());
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			ctx.setVariable(entry.getKey(), entry.getValue());
		}
		String text = this.templateEngine.process(templateName, ctx);
		return text;
	}
}
