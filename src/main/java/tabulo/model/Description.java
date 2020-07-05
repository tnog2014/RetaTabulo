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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(
		name = "description")
public class Description {
	@Id
	@GeneratedValue(
			strategy = GenerationType.IDENTITY)
	private Integer id;

	private Integer boardId;

	private Integer create_user;

	private Integer update_user;

	private Double x;

	private Double y;

	private Double width;

	private Double height;

	private String raw;

	private String html;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
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

	public Double getWidth() {
		return width;
	}

	public void setWidth(Double width) {
		this.width = width;
	}

	public Double getHeight() {
		return height;
	}

	public void setHeight(Double height) {
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

	public String toJSON() {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append("id:").append(getId()).append(", ");
		sb.append("create_user:'").append(getCreate_user()).append("', ");
		sb.append("update_user:'").append(getUpdate_user()).append("', ");
		sb.append("x:").append(getX()).append(", ");
		sb.append("y:").append(getY()).append(", ");
		sb.append("width:").append(getWidth()).append(", ");
		sb.append("height:").append(getHeight()).append(", ");
		String raw = getRaw();
		if (raw != null) {
			raw = raw.replaceAll("[\r\n]", "");
		}
		sb.append("raw:'").append(raw).append("', ");
		sb.append("html:'").append(getHtml()).append("'");
		sb.append("}");
		return sb.toString();
	}
}
