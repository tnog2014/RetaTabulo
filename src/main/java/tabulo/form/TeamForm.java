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

public class TeamForm implements NameHolder, Serializable {

	private Integer id;

	@NotBlank(
			message = ValidationMessageKey.VALIDATION_REQUIRED)
	@Size(
			min = ValidationMessageKey.TEAM_NAME_MIN,
			max = ValidationMessageKey.TEAM_NAME_MAX,
			message = ValidationMessageKey.VALIDATION_RANGE)
	private String name;

	@Size(
			max = ValidationMessageKey.TEAM_DESCRIPTION_MAX,
			message = ValidationMessageKey.VALIDATION_RANGE_MAX)
	private String description;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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
