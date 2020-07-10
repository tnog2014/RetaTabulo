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
package tabulo.model;

import tabulo.constant.CommonConst;

public class DescriptionWrapper {

	private Integer id;

	private Integer boardId;

	private Integer create_user;

	private Integer update_user;

	private String create_name;

	private String update_name;

	private Float x;

	private Float y;

	private Float width;

	private Float height;

	private String raw;

	private String html;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Float getX() {
		return x;
	}

	public void setX(Float x) {
		this.x = x;
	}

	public Float getY() {
		return y;
	}

	public void setY(Float y) {
		this.y = y;
	}

	public String getRaw() {
		return raw;
	}

	public void setRaw(String raw) {
		this.raw = raw;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public Integer getBoardId() {
		return boardId;
	}

	public void setBoardId(Integer boardId) {
		this.boardId = boardId;
	}

	public Float getWidth() {
		return width;
	}

	public void setWidth(Float width) {
		this.width = width;
	}

	public Float getHeight() {
		return height;
	}

	public void setHeight(Float height) {
		this.height = height;
	}

	public Integer getCreate_user() {
		return create_user;
	}

	public void setCreate_user(Integer create_user) {
		this.create_user = create_user;
	}

	public Integer getUpdate_user() {
		return update_user;
	}

	public void setUpdate_user(Integer update_user) {
		this.update_user = update_user;
	}

	public String getCreate_name() {
		return create_name;
	}

	public void setCreate_name(String create_name) {
		this.create_name = create_name;
	}

	public String getUpdate_name() {
		return update_name;
	}

	public void setUpdate_name(String update_name) {
		this.update_name = update_name;
	}

	public String toJSON() {
		return toJSON(true);
	}

	public String toJSONForHTML() {
		return toJSON(false);
	}

	public String toJSON(boolean withRaw) {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append("id:").append(getId()).append(", ");
		String createName = quote(getCreate_name());
		String updateName = quote(getUpdate_name());
		sb.append("create_name:").append(createName).append(", ");
		sb.append("update_name:").append(updateName).append(", ");
		sb.append("x:").append(getX()).append(", ");
		sb.append("y:").append(getY()).append(", ");
		sb.append("width:").append(getWidth()).append(", ");
		sb.append("height:").append(getHeight()).append(", ");
		if (withRaw) {
			String raw = escape(getRaw());
			sb.append("raw:'").append(raw).append("', ");
		}
		String html = escape(getHtml());
		sb.append("html:'").append(html).append("'");
		sb.append("}");
		return sb.toString();
	}

	public String toTSV() {
		StringBuffer sb = new StringBuffer();
		sb.append(getBoardId()).append(CommonConst.TAB);
		sb.append(getId()).append(CommonConst.TAB);
		sb.append(nullToVacant(getCreate_name())).append(CommonConst.TAB);
		sb.append(nullToVacant(getUpdate_name())).append(CommonConst.TAB);
		sb.append(getX()).append(CommonConst.TAB);
		sb.append(getY()).append(CommonConst.TAB);
		sb.append(nullToVacant(getWidth())).append(CommonConst.TAB);
		sb.append(nullToVacant(getHeight())).append(CommonConst.TAB);
		sb.append(escape(getRaw())).append(CommonConst.TAB);
		sb.append(escape(getHtml())).append(CommonConst.CRLF);
		return sb.toString();
	}

	private String nullToVacant(Object obj) {
		if (obj != null) {
			return obj.toString();
		} else {
			return "";
		}
	}

	public static String escape(String input) {
		String ret = input;
		if (ret != null) {
			ret = ret.replaceAll("'", "\\\\'");
		}
		return ret;
	}

	private String quote(String value) {
		if (value != null) {
			value = "'" + value + "'";
		} else {
			value = "null";
		}
		return value;
	}
}
