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
import java.util.List;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import tabulo.constant.ValidationMessageKey;

public class UserForm implements Serializable {

	private Integer id;

	@NotEmpty(
			message = ValidationMessageKey.VALIDATION_REQUIRED)
	@Size(
			min = ValidationMessageKey.USER_NAME_MIN,
			max = ValidationMessageKey.USER_NAME_MAX,
			message = ValidationMessageKey.VALIDATION_RANGE)
	@Pattern(
			regexp = ValidationMessageKey.USER_NAME_REGEX,
			message = ValidationMessageKey.VALIDATION_USERNAME_VALID)
	private String userName;

	private boolean changePassword;

	@NotEmpty(
			message = ValidationMessageKey.VALIDATION_REQUIRED)
	@Size(
			min = ValidationMessageKey.USER_PASSWORD_MIN,
			max = ValidationMessageKey.USER_PASSWORD_MAX,
			message = ValidationMessageKey.VALIDATION_RANGE)
	@Pattern(
			regexp = ValidationMessageKey.REGEX_ASCII_CHARS,
			message = ValidationMessageKey.VALIDATION_ONLY_ASCII)
	private String password;

	@NotEmpty(
			message = ValidationMessageKey.VALIDATION_REQUIRED)
	@Size(
			min = ValidationMessageKey.USER_PASSWORD_MIN,
			max = ValidationMessageKey.USER_PASSWORD_MAX,
			message = ValidationMessageKey.VALIDATION_RANGE)
	@Pattern(
			regexp = ValidationMessageKey.REGEX_ASCII_CHARS,
			message = ValidationMessageKey.VALIDATION_ONLY_ASCII)
	private String oldPassword;

	@NotEmpty(
			message = ValidationMessageKey.VALIDATION_REQUIRED)
	@Size(
			min = ValidationMessageKey.USER_PASSWORD_MIN,
			max = ValidationMessageKey.USER_PASSWORD_MAX,
			message = ValidationMessageKey.VALIDATION_RANGE)
	@Pattern(
			regexp = ValidationMessageKey.REGEX_ASCII_CHARS,
			message = ValidationMessageKey.VALIDATION_ONLY_ASCII)
	private String cnfPassword;

	@NotEmpty(
			message = ValidationMessageKey.VALIDATION_REQUIRED)

	@Size(
			min = ValidationMessageKey.USER_NICKNAME_MIN,
			max = ValidationMessageKey.USER_NICKNAME_MAX,
			message = ValidationMessageKey.VALIDATION_RANGE)
	private String nickname;

	@Pattern(
			regexp = "[01]",
			message = ValidationMessageKey.VALIDATION_ONLY_0_1)
	private String valid;

	@Pattern(
			regexp = "[01]",
			message = ValidationMessageKey.VALIDATION_ONLY_0_1)

	private String level;

	private String currentAssignedTeams;

	private String assignedTeams;

	private String unassignedTeams;

	private String currentAdminTeams;

	private String assignedAdminTeams;

	private String unassignedAdminTeams;

	private List<ExtendedTeamForm> teamForms;

	/**
	 * the previous page.
	 * TODO: I am not sure whether this method is good when one needs to control the returning page.
	 */
	private String from;

	public String getAssignedTeams() {
		return assignedTeams;
	}

	public void setAssignedTeams(String assignedTeams) {
		this.assignedTeams = assignedTeams;
	}

	public String getUnassignedTeams() {
		return unassignedTeams;
	}

	public void setUnassignedTeams(String unassignedTeams) {
		this.unassignedTeams = unassignedTeams;
	}

	public String getCurrentAssignedTeams() {
		return currentAssignedTeams;
	}

	public void setCurrentAssignedTeams(String currentAssignedTeams) {
		this.currentAssignedTeams = currentAssignedTeams;
	}

	public String getCurrentAdminTeams() {
		return currentAdminTeams;
	}

	public void setCurrentAdminTeams(String currentAdminTeams) {
		this.currentAdminTeams = currentAdminTeams;
	}

	public String getAssignedAdminTeams() {
		return assignedAdminTeams;
	}

	public void setAssignedAdminTeams(String assignedAdminTeams) {
		this.assignedAdminTeams = assignedAdminTeams;
	}

	public String getUnassignedAdminTeams() {
		return unassignedAdminTeams;
	}

	public void setUnassignedAdminTeams(String unassignedAdminTeams) {
		this.unassignedAdminTeams = unassignedAdminTeams;
	}

	@AssertTrue(
			message = ValidationMessageKey.VALIDATION_PASSWORDS_NOT_MATCH
	//,groups = Group1.class
	)
	public boolean isCnfPasswordMatch() {
		if (!changePassword) {
			return true;
		}
		return password != null && password.equals(cnfPassword);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean isChangePassword() {
		return changePassword;
	}

	public void setChangePassword(boolean changePassword) {
		this.changePassword = changePassword;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public String getCnfPassword() {
		return cnfPassword;
	}

	public void setCnfPassword(String cnfPassword) {
		this.cnfPassword = cnfPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getValid() {
		return valid;
	}

	public void setValid(String valid) {
		this.valid = valid;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public List<ExtendedTeamForm> getTeamForms() {
		return teamForms;
	}

	public void setTeamForms(List<ExtendedTeamForm> teamForms) {
		this.teamForms = teamForms;
	}

}
