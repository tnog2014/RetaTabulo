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
package tabulo.form;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import tabulo.NameHolder;
import tabulo.constant.ValidationMessageKey;

public class BoardForm implements NameHolder, Serializable {

	private Integer id;

	private Integer teamId;

	@NotBlank(
			message = ValidationMessageKey.VALIDATION_REQUIRED)
	@Size(
			min = ValidationMessageKey.BOARD_NAME_MIN,
			max = ValidationMessageKey.BOARD_NAME_MAX,
			message = ValidationMessageKey.VALIDATION_RANGE)
	private String name;

	@Size(
			max = ValidationMessageKey.BOARD_DESCRIPTION_MAX,
			message = ValidationMessageKey.VALIDATION_RANGE_MAX)
	private String description;

	private String teamName;

	@Override
	public String toString() {
		return id + "/" + name + "/" + teamId + "/" + teamName;
	}

	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getTeamId() {
		return teamId;
	}

	public void setTeamId(Integer teamId) {
		this.teamId = teamId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
