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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import tabulo.ApplicationException;
import tabulo.ControllerUtil;
import tabulo.CustomUser;
import tabulo.MessageUtil;
import tabulo.constant.CommonConst;
import tabulo.constant.ErrorCode;
import tabulo.constant.MappingPath;
import tabulo.constant.MessageCode;
import tabulo.constant.ViewName;
import tabulo.form.ExtendedTeamForm;
import tabulo.form.UserForm;
import tabulo.model.LoginUser;
import tabulo.model.Team;
import tabulo.model.UserTeam;
import tabulo.service.TeamService;
import tabulo.service.UserService;
import tabulo.service.UserTeamService;
import tabulo.session.SessionCommon;

@Controller
public class UserController {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

	private static final String VIEW_MAIN = "main";

	private static final String VIEW_USER = "user";

	private static final String FROM = "from";

	private static final String FORM_MODEL_KEY = "userForm";

	private static final String ERRORS_MODEL_KEY = BindingResult.MODEL_KEY_PREFIX + FORM_MODEL_KEY;

	private static final String KEY_USERS = "users";

	private static final String KEY_MESSAGE = "message";

	private static final String KEY_PASSWORD = "password";

	private static final String KEY_CNF_PASSWORD = "cnfPassword";

	private static final String KEY_OLD_PASSWORD = "oldPassword";

	private static final String KEY_PASSWORD_NOT_MATCH = "passwordNotMatch";

	private static final String KEY_PREFIX_CB = "cb_";

	private static final String KEY_PREFIX_CBADM = "cbadm_";

	private static final String KEY_NO_OLD_PASSWORD = "noOldPassword";

	private static final List<String> ERRORS_TO_IGNORE_WHEN_NOT_CHANGING_PASSWORD = Arrays
			.asList(new String[]{
					KEY_PASSWORD,
					KEY_CNF_PASSWORD,
					KEY_OLD_PASSWORD});

	private static final List<String> ERRORS_TO_IGNORE_WHEN_NOT_TYPING_OLD_PASSWORD = Arrays
			.asList(new String[]{
					KEY_OLD_PASSWORD
			});

	@Autowired
	UserService userService;

	@Autowired
	UserDetailsService userDetailsService;

	@Autowired
	UserTeamService userTeamService;

	@Autowired
	TeamService teamService;

	@Autowired
	AuthorizationUtil authUtil;

	@Autowired
	SessionCommon session;

	/** Utility for messages. */
	@Autowired
	MessageUtil messageUtil;

	private PasswordEncoder encoder = new BCryptPasswordEncoder();

	@RequestMapping(
			path = MappingPath.USER_LIST)
	private String showUserListView(Model model) throws ApplicationException {
		LOGGER.debug("showUserListView start");
		ControllerUtil.preProcess(model);
		// ----- nav -----
		CustomUser loginUser = ControllerUtil.getLoginUser();
		model.addAttribute(CommonConst.COMMON_FORM, session);
		boolean admin = ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN);
		boolean teamAdmin = authUtil.isTeamLeader(loginUser);
		model.addAttribute(CommonConst.KEY_ADMIN, admin);
		model.addAttribute(CommonConst.KEY_TEAM_ADMIN, teamAdmin);
		// ---------------

		if (!admin && !teamAdmin) {
			throw new ApplicationException(
					ErrorCode.ERR_CANNOT_OPEN_USER_LIST, loginUser.getUsername());
		}

		List<LoginUser> usersUnderControl = authUtil.getUsersUnderControl(loginUser);

		List<UserForm> forms = new ArrayList<UserForm>();
		for (LoginUser b : usersUnderControl) {
			UserForm bf = new UserForm();
			bf.setId(b.getId());
			bf.setUserName(b.getUserName());
			bf.setNickname(b.getNickname());
			bf.setValid(b.getValid());
			forms.add(bf);
		}
		model.addAttribute(KEY_USERS, forms);
		LOGGER.debug("showUserListView end");
		return ViewName.USER_LIST;
	}

	@RequestMapping(
			path = MappingPath.USER_CREATE,
			method = RequestMethod.GET)
	private String showUserCreateView(Model model) throws ApplicationException {
		LOGGER.debug("showUserCreateView start");
		ControllerUtil.preProcess(model);
		// ----- nav -----
		CustomUser loginUser = ControllerUtil.getLoginUser();
		model.addAttribute(CommonConst.COMMON_FORM, session);
		boolean admin = ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN);
		boolean teamAdmin = authUtil.isTeamLeader(loginUser);
		model.addAttribute(CommonConst.KEY_ADMIN, admin);
		model.addAttribute(CommonConst.KEY_TEAM_ADMIN, teamAdmin);
		// ---------------

		if (!admin && !teamAdmin) {
			throw new ApplicationException(
					ErrorCode.ERR_CANNOT_OPEN_USER_CREATE_VIEW,
					loginUser.getUserId());
		}

		UserForm bf = (UserForm) model.asMap().get(FORM_MODEL_KEY);
		if (bf == null) {
			bf = new UserForm();
		}
		model.addAttribute(FORM_MODEL_KEY, bf);

		List<Team> teamsUnderControl = authUtil.getTeamsUnderControl(loginUser);
		List<ExtendedTeamForm> teamForms = convertToExtendedTeamForms(teamsUnderControl);
		bf.setTeamForms(teamForms);
		LOGGER.debug("showUserCreateView end");
		return ViewName.USER_CREATE;
	}

	@RequestMapping(
			path = MappingPath.USER_CREATE,
			method = RequestMethod.POST)
	private String createUser(
			//@ModelAttribute @Validated(UserForm.All.class) UserForm bf,
			@ModelAttribute @Validated UserForm bf,
			BindingResult error,
			RedirectAttributes redirectAttributes) throws ApplicationException {
		LOGGER.debug("createUser start");
		LOGGER.debug("userForm:" + bf);
		LOGGER.debug("error:" + error);
		LOGGER.debug("assignedTeams:" + bf.getAssignedTeams());

		CustomUser loginUser = ControllerUtil.getLoginUser();

		Locale locale = LocaleContextHolder.getLocale();
		redirectAttributes.addFlashAttribute(CommonConst.LOCALE_KEY, locale.getLanguage());
		redirectAttributes.addFlashAttribute(FORM_MODEL_KEY, bf);
		redirectAttributes.addFlashAttribute(ERRORS_MODEL_KEY, error);
		redirectAttributes.addFlashAttribute(CommonConst.COMMON_FORM, session);

		// Setting the full admin flag
		if (!ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN)) {
			// Only full admins can set the full admin flag = "1"(full admin).
			if ("1".equals(bf.getLevel())) {
				throw new ApplicationException(
						ErrorCode.ERR_NON_FULL_ADMIN_CANNOT_CREATE_FULL_ADMIN,
						bf.getUserName());
			}
		}
		List<Team> teamsUnderControl = authUtil.getTeamsUnderControl(loginUser);
		List<ExtendedTeamForm> teamForms = convertToExtendedTeamForms(teamsUnderControl);
		bf.setTeamForms(teamForms);

		if (error.hasErrors()) {
			if (hasErrorsNotRelatedTo(error, ERRORS_TO_IGNORE_WHEN_NOT_TYPING_OLD_PASSWORD)) {
				// Back to the user create view because of errors not caused by a blank old password.
				return MappingPath.REDIRECT_USER_CREATE;
			}
		}

		// Duplication check for userName
		List<LoginUser> found = userService.findByUserName(bf.getUserName());
		if (found.size() > 0) {
			LOGGER.debug("UserId is duplicated:[" + bf.getUserName() + "]");
			redirectAttributes.addFlashAttribute(KEY_MESSAGE,
					messageUtil.getMessage(MessageCode.MESSAGE_USER_NAME_DUPLICATED));
			return MappingPath.REDIRECT_USER_CREATE;
		}

		LoginUser user = new LoginUser();
		user.setUserName(bf.getUserName());
		String password = bf.getPassword();
		user.setPassword(encoder.encode(password));
		user.setNickname(bf.getNickname());
		user.setValid(bf.getValid());
		user.setLevel(bf.getLevel());
		LoginUser updatedUser = userService.insert(user);

		// Update a user-team table
		String assignedTeams = bf.getAssignedTeams();
		String assignedAdminTeams = bf.getAssignedAdminTeams();
		updateUserTeamTable(updatedUser, assignedTeams, "", assignedAdminTeams, "");
		redirectAttributes.getFlashAttributes().clear();
		LOGGER.debug("createUser end");
		return MappingPath.REDIRECT_USER;
	}

	void updateAuthentification() throws ApplicationException {
		CustomUser loginUser = ControllerUtil.getLoginUser();
		UserDetails obj = userService.loadUserById(loginUser.getUserId());
		Authentication newAuth = new UsernamePasswordAuthenticationToken(
				obj, obj.getPassword(), obj.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(newAuth);
	}

	/**
	 * Update a User-Team table.
	 *
	 * Update a user-team table based on assign/unassing list for reference/admin authorization.
	 * They are expressed int the following form: "cb_###,cb_###,cb_###,..."
	 *
	 * @param userToBeUpdated User to be updated
	 * @param assignedTeams a teamId list to which one give a reference right
	 * @param unassignedTeams a teamId list of which one deprive a reference right
	 * @param assignedAdminTeams a teamId list to which one give an admin right
	 * @param unassignedAdminTeams a teamId list of which one deprive an admin right
	 * @throws ApplicationException exception
	 */
	// TODO: The variable names are not appropriate.
	private void updateUserTeamTable(
			LoginUser userToBeUpdated,
			String assignedTeams,
			String unassignedTeams,
			String assignedAdminTeams,
			String unassignedAdminTeams) throws ApplicationException {
		LOGGER.debug("updateUserTeamTable start");
		LOGGER.debug("assignedTeams:" + assignedTeams);
		LOGGER.debug("unassignedTeams:" + unassignedTeams);
		LOGGER.debug("assignedAdminTeams:" + assignedAdminTeams);
		LOGGER.debug("unassignedAdminTeams:" + unassignedAdminTeams);

		CustomUser loginUser = ControllerUtil.getLoginUser();

		// Setting a full admin right
		if (!ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN)) {
			if (assignedAdminTeams.length() > 0 || unassignedAdminTeams.length() > 0) {
				throw new ApplicationException(
						ErrorCode.ERR_CANNOT_CHANGE_TEAM_ASSIGNMENTS,
						assignedAdminTeams, unassignedAdminTeams);
			}
		}

		// Get teams under the control
		List<Integer> teamIdsUnderControl = authUtil.getTeamIdsUnderControl(loginUser);

		// 1. Give a reference right (insert a record into a user-team table)
		// 2. Give a admin right (update the record)
		// (The order of processes is important.)
		if (assignedTeams.length() > 0) {
			String[] items = assignedTeams.replace(KEY_PREFIX_CB, "").split(",");
			for (String item : items) {
				Integer teamId = Integer.parseInt(item);
				if (!teamIdsUnderControl.contains(teamId)) {
					throw new ApplicationException(
							ErrorCode.ERR_CANNOT_GIVE_REF_RIGHT, teamId);
				}
				LOGGER.debug("give a reference right to a team:[" + item + "]");
				UserTeam ut = new UserTeam();
				ut.setUserId(userToBeUpdated.getId());
				ut.setTeamId(teamId);
				userTeamService.insert(ut);
			}
		}

		if (assignedAdminTeams.length() > 0) {
			String[] items = assignedAdminTeams.replace(KEY_PREFIX_CBADM, "").split(",");
			for (String item : items) {
				LOGGER.debug("give an admin right to a team:[" + item + "]");
				Optional<UserTeam> ut = userTeamService.findByUserIdTeamId(userToBeUpdated.getId(),
						Integer.parseInt(item));
				if (ut.isPresent()) {
					ut.get().setAuth("1");
					userTeamService.update(ut.get());
				} else {
					throw new ApplicationException(
							ErrorCode.ERR_CANNOT_GIVE_ADMIN_AUTHORITY, item);
				}
			}
		}

		// 1. Deprive an admin right (update the record)
		// 2. Deprive a reference right (delete a record from a user-team table)
		// (The order of processes is important.)
		if (unassignedAdminTeams.length() > 0) {
			String[] items = unassignedAdminTeams.replace(KEY_PREFIX_CBADM, "").split(",");
			for (String item : items) {
				LOGGER.debug(
						"Deprive an admin right: userId/teamId ["
								+ userToBeUpdated.getId() + "][" + item + "]");
				Optional<UserTeam> ut = userTeamService.findByUserIdTeamId(userToBeUpdated.getId(),
						Integer.parseInt(item));
				if (ut.isPresent()) {
					ut.get().setAuth("");
					userTeamService.update(ut.get());
				} else {
					throw new ApplicationException(
							ErrorCode.ERR_CANNOT_DEPRIVE_ADMIN_AUTHORITY, item);
				}
			}
		}

		if (unassignedTeams.length() > 0) {
			String[] items = unassignedTeams.replace(KEY_PREFIX_CB, "").split(",");
			for (String item : items) {
				Integer teamId = Integer.parseInt(item);
				if (!teamIdsUnderControl.contains(teamId)) {
					throw new ApplicationException(
							ErrorCode.ERR_CANNOT_DEPRIVE_REF_RIGHT, teamId);
				}
				LOGGER.debug("Deprive a reference right: teamId [" + item + "]");
				userTeamService.deleteByUserIdTeamId(userToBeUpdated.getId(), teamId);
			}
		}
		LOGGER.debug("updateUserTeamTable end");
	}

	@RequestMapping(
			path = MappingPath.USER_SHOW + MappingPath.REGEX_USER_ID,
			method = RequestMethod.GET)
	private String openUserRefView(
			Model model,
			@PathVariable int userId,
			@RequestParam(
					name = FROM,
					required = false) String from)
			throws ApplicationException {
		return openUserCommonView(model, userId, from, true);
	}

	@RequestMapping(
			path = MappingPath.USER_EDIT
					+ MappingPath.REGEX_USER_ID,
			method = RequestMethod.GET)
	private String showUserEditView(
			Model model,
			@PathVariable int userId,
			@RequestParam(
					name = FROM,
					required = false) String from)
			throws ApplicationException {
		return openUserCommonView(model, userId, from, false);
	}

	private String openUserCommonView(
			Model model,
			int userId,
			String from,
			boolean readonly) throws ApplicationException {
		LOGGER.debug("openUserCommonView start:[" + userId + "]");
		LOGGER.debug("from:[" + from + "]");
		ControllerUtil.preProcess(model);
		// ----- nav -----
		CustomUser loginUser = ControllerUtil.getLoginUser();
		model.addAttribute(CommonConst.COMMON_FORM, session);
		boolean admin = ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN);
		boolean teamAdmin = authUtil.isTeamLeader(loginUser);
		model.addAttribute(CommonConst.KEY_ADMIN, admin);
		model.addAttribute(CommonConst.KEY_TEAM_ADMIN, teamAdmin);
		// ---------------

		if (!authUtil.canLoginUserModifyOtherUser(loginUser, userId)) {
			throw new ApplicationException(
					ErrorCode.ERR_CANNOT_OPEN_USER_EDIT_VIEW,
					loginUser.getUsername(),
					userId);
		}

		// Setting a readonly mode
		model.addAttribute(CommonConst.KEY_READONLY, readonly);

		// One (admins or team admins) can set a new password of another user without typing an old password
		model.addAttribute(KEY_NO_OLD_PASSWORD,
				loginUser.getUserId().intValue() != userId);

		UserForm bf = (UserForm) model.asMap().get(FORM_MODEL_KEY);
		if (bf == null) {
			Optional<LoginUser> found = userService.findById(userId);
			if (found.isPresent()) {
				LoginUser user = found.get();
				bf = new UserForm();
				bf.setId(user.getId());
				bf.setUserName(user.getUserName());
				bf.setOldPassword("");
				bf.setPassword("");
				bf.setNickname(user.getNickname());
				bf.setValid(user.getValid());
				bf.setLevel(user.getLevel());

				// If one came from the main view, one get back there after the process.
				// If one came from the user list view, one get back there after the process.
				String fromToSet = VIEW_MAIN;
				if (VIEW_USER.equals(from)) {
					fromToSet = VIEW_USER;
				}
				bf.setFrom(fromToSet);

				// Create a team list
				List<Team> teamsUnderControl = authUtil.getTeamsUnderControl(loginUser);
				List<ExtendedTeamForm> teamForms = convertToExtendedTeamForms(teamsUnderControl);

				bf.setTeamForms(teamForms);

				String currentAssignedTeams = createAssignedTeams(userId);
				LOGGER.debug("currentAssignedTeams=[" + currentAssignedTeams + "]");
				bf.setCurrentAssignedTeams(currentAssignedTeams);

				String currentAdminTeams = createAdminTeams(userId);
				LOGGER.debug("currentAdminTeams=[" + currentAdminTeams + "]");
				bf.setCurrentAdminTeams(currentAdminTeams);

			} else {
				throw new ApplicationException(
						ErrorCode.ERR_EDIT_USER_NOT_FOUND, userId);
			}
		}
		model.addAttribute(FORM_MODEL_KEY, bf);
		LOGGER.debug("openUserCommonView end");
		return ViewName.USER_EDIT;
	}

	private ExtendedTeamForm convertToExtendedTeamForm(Team team) {
		ExtendedTeamForm tf = new ExtendedTeamForm();
		tf.setId(team.getId());
		tf.setName(team.getName());
		tf.setDescription(team.getDescription());
		return tf;
	}

	private List<ExtendedTeamForm> convertToExtendedTeamForms(List<Team> teams) {
		List<ExtendedTeamForm> teamForms = new ArrayList<ExtendedTeamForm>();
		for (Team team : teams) {
			teamForms.add(convertToExtendedTeamForm(team));
		}
		return teamForms;
	}

	/**
	 * Create a team id list for all the teams, that the given user belongs to.
	 *
	 * @param userIdToEdit
	 * @return a team id list
	 */
	private String createAssignedTeams(Integer userIdToEdit) {
		List<UserTeam> userTeam = userTeamService.findByUserId(userIdToEdit);
		StringBuffer sb = new StringBuffer();
		for (UserTeam ut : userTeam) {
			sb.append(KEY_PREFIX_CB).append(ut.getTeamId()).append(",");
		}

		if (sb.length() > 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		return sb.toString();
	}

	/**
	 * Create a team id list for all the teams, of which the given user has an admin right.
	 *
	 * @param userIdToEdit
	 * @return a team id list
	 */
	private String createAdminTeams(Integer userIdToEdit) {
		List<UserTeam> userTeam = userTeamService.findByUserId(userIdToEdit);
		List<String> items = new ArrayList<String>();
		for (UserTeam ut : userTeam) {
			if ("1".equals(ut.getAuth())) {
				items.add(KEY_PREFIX_CBADM + ut.getTeamId());
			}
		}
		String ret = String.join(",", items);
		return ret;
	}

	private boolean hasErrorsNotRelatedTo(BindingResult error, List<String> targets) {
		for (FieldError e : error.getFieldErrors()) {
			LOGGER.debug("Default message:" + e.getDefaultMessage());
			if (!targets.contains(e.getField())) {
				return true;
			}
		}
		return false;
	}

	@RequestMapping(
			path = MappingPath.USER_EDIT_INNER,
			method = RequestMethod.POST)
	private String editUser(
			//@ModelAttribute @Validated(UserForm.All.class) UserForm bf,
			@ModelAttribute @Validated UserForm bf,
			BindingResult error,
			RedirectAttributes redirectAttributes) throws ApplicationException {
		LOGGER.debug("editUser start:" + bf);
		LOGGER.debug("error:" + error);
		LOGGER.debug("assignedTeams:" + bf.getAssignedTeams());

		CustomUser loginUser = ControllerUtil.getLoginUser();
		if (!authUtil.canLoginUserModifyOtherUser(loginUser, bf.getId())) {
			throw new ApplicationException(
					ErrorCode.ERR_CANNOT_EDIT_USER, loginUser.getUsername(),
					bf.getId());
		}

		Locale locale = LocaleContextHolder.getLocale();
		redirectAttributes.addFlashAttribute(CommonConst.LOCALE_KEY, locale.getLanguage());
		redirectAttributes.addFlashAttribute(FORM_MODEL_KEY, bf);
		redirectAttributes.addFlashAttribute(ERRORS_MODEL_KEY, error);
		redirectAttributes.addFlashAttribute(CommonConst.COMMON_FORM, session);

		// Setting a full admin flag
		redirectAttributes.addFlashAttribute(CommonConst.KEY_ADMIN,
				ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN));

		// Setting a readonly flag
		redirectAttributes.addFlashAttribute(CommonConst.KEY_READONLY, false);

		// One (admins or team admins) can set a new password of another user without typing an old password
		boolean changeOwnPassword = (loginUser.getUserId().intValue() == bf.getId().intValue());
		redirectAttributes.addFlashAttribute(KEY_NO_OLD_PASSWORD, !changeOwnPassword);

		// Create a team list in case of showing the edit view again.
		List<Team> teamsUnderControl = authUtil.getTeamsUnderControl(loginUser);
		List<ExtendedTeamForm> teamForms = convertToExtendedTeamForms(teamsUnderControl);
		bf.setTeamForms(teamForms);

		boolean flagErrorBack = false;
		// When not changing the password, ignore errors related to passwords.
		LOGGER.debug("isChangePassword:" + bf.isChangePassword());
		if (bf.isChangePassword()) {

			if (changeOwnPassword) {
				// Need the old password when changing one's own password
				if (error.hasErrors()) {
					flagErrorBack = true;
				}
			} else {
				// No need for the old password when changing the password of others
				if (hasErrorsNotRelatedTo(error, ERRORS_TO_IGNORE_WHEN_NOT_TYPING_OLD_PASSWORD)) {
					flagErrorBack = true;
				}
			}
		} else {
			if (hasErrorsNotRelatedTo(error, ERRORS_TO_IGNORE_WHEN_NOT_CHANGING_PASSWORD)) {
				flagErrorBack = true;
			}
		}

		Optional<LoginUser> targetUser = userService.findById(bf.getId());
		if (!targetUser.isPresent()) {
			throw new ApplicationException(
					ErrorCode.ERR_EDIT_USER_NOT_FOUND, bf.getId());
		}

		String currentUserName = targetUser.get().getUserName();
		LOGGER.debug("Current user name:[" + currentUserName + "]");

		// UserName duplication check
		List<LoginUser> usersFound = userService.findByUserName(bf.getUserName());
		if (!currentUserName.equals(bf.getUserName()) && usersFound.size() > 0) {
			LOGGER.info("UserName duplication error occurred:[" + bf.getUserName() + "]");
			redirectAttributes.addFlashAttribute(KEY_MESSAGE,
					messageUtil.getMessage(MessageCode.MESSAGE_USER_NAME_DUPLICATED));
			flagErrorBack = true;
		}

		// If check error occurs, go to the user edit view.
		if (flagErrorBack) {
			return MappingPath.REDIRECT_USER_EDIT + bf.getId();
		}

		// Setting a full admin right
		if (!ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN)) {
			// Only full admins can set the full admin flag = "1"(full admin).
			if ("1".equals(bf.getLevel())) {
				throw new ApplicationException(
						ErrorCode.ERR_ONLY_FULL_ADMIN_CAN_EDIT_FULL_ADMIN,
						bf.getUserName());
			}
		}

		redirectAttributes.getFlashAttributes().clear();
		LoginUser user = targetUser.get();

		// パスワード確認
		if (bf.isChangePassword()) {
			String oldPassword = bf.getOldPassword();
			// 自分自身の以外の場合、旧パスワードの入力が不要
			if (changeOwnPassword && !encoder.matches(oldPassword, user.getPassword())) {
				// パスワード不正
				LOGGER.info("Changes the password [NG]: " + bf.getId() + "/" + bf.getUserName());
				redirectAttributes.addFlashAttribute(KEY_PASSWORD_NOT_MATCH, "1");
				return MappingPath.REDIRECT_USER_EDIT + bf.getId();
			} else {
				LOGGER.info("Changes the password [OK]: " + bf.getId() + "/" + bf.getUserName());
				user.setPassword(encoder.encode(bf.getPassword()));
			}
		}

		if (!user.getUserName().equals(bf.getUserName())) {
			// if the name of a login user changes,
			// update the user name in the authentification info and that in the session object.
			if (loginUser.getUserId().equals(user.getId())) {
				updateAuthentification();
				session.setLoginUserName(bf.getUserName());
			}
			user.setUserName(bf.getUserName());
		}

		user.setNickname(bf.getNickname());

		// Do not update the level if the value is null.
		if (bf.getLevel() != null) {
			// When "" -> "1",
			// remove all the records related to the target user in the user-team table.
			// (Full admins are allowed to modify all the teams.)
			if (!"1".equals(user.getLevel()) && "1".equals(bf.getLevel())) {
				LOGGER.debug(String.format(
						"delete all the records related to the target user[%s] in the user-team table.",
						"" + user.getId()));
				userTeamService.deleteByUserId(user.getId());
			}
			user.setLevel(bf.getLevel());
		}
		if (bf.getValid() != null) {
			user.setValid(bf.getValid());
		}

		// ユーザー更新
		LoginUser updatedUser = userService.update(user);

		// ユーザー・チームテーブル更新
		updateUserTeamTable(updatedUser,
				bf.getAssignedTeams(),
				bf.getUnassignedTeams(),
				bf.getAssignedAdminTeams(),
				bf.getUnassignedAdminTeams());

		String next = null;
		if (VIEW_USER.equals(bf.getFrom())) {
			next = MappingPath.REDIRECT_USER;
		} else {
			next = MappingPath.REDIRECT_MAIN;
		}
		LOGGER.debug("ユーザー更新画面からの遷移先:[" + next + "]");
		redirectAttributes.getFlashAttributes().clear();
		return next;
	}

	@RequestMapping(
			path = MappingPath.USER_DELETE + MappingPath.REGEX_USER_ID)
	private String deleteUser(
			Model model,
			@PathVariable int userId) throws ApplicationException {
		LOGGER.debug("deleteUser start:" + userId);
		CustomUser loginUser = ControllerUtil.getLoginUser();
		List<Integer> userIdsUnderControl = authUtil.getUserIdsUnderControl(loginUser);
		if (!userIdsUnderControl.contains(userId)) {
			throw new ApplicationException(
					ErrorCode.ERR_CANNOT_DELETE_USER_NOT_UNDER_CONTROL, userId);
		}
		userService.deleteById(userId);
		LOGGER.debug("deleteUser end");
		return MappingPath.REDIRECT_USER;
	}

}
