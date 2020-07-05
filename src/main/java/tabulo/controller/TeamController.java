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

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
import tabulo.form.TeamForm;
import tabulo.model.Board;
import tabulo.model.Team;
import tabulo.service.BoardService;
import tabulo.service.DescriptionService;
import tabulo.service.TeamService;
import tabulo.service.UserTeamService;
import tabulo.session.SessionCommon;

@Controller
public class TeamController {

	public static final String FORM_MODEL_KEY = "teamForm";

	public static final String ERRORS_MODEL_KEY = BindingResult.MODEL_KEY_PREFIX + FORM_MODEL_KEY;

	private static final Logger LOGGER = LoggerFactory.getLogger(TeamController.class);

	@Autowired
	TeamService teamService;

	@Autowired
	BoardService boardService;

	@Autowired
	UserTeamService userTeamService;

	@Autowired
	DescriptionService descService;

	@Autowired
	AuthorizationUtil authUtil;

	@Autowired
	SessionCommon session;

	/** Utility for messages. */
	@Autowired
	MessageUtil messageUtil;

	/**
	 * Open a team list view.
	 *
	 * 全体管理権限がある場合にはすべてのチームを表示する。
	 * そうでない場合には、管理権限をもつチームを表示する。
	 *
	 * @param model
	 * @return a view name
	 * @throws ApplicationException
	 */
	@RequestMapping(
			path = MappingPath.TEAM_LIST)
	private String openTeamListView(Model model) throws ApplicationException {
		LOGGER.debug("openTeamListView start");
		ControllerUtil.preProcess(model);
		// ----- nav -----
		CustomUser loginUser = ControllerUtil.getLoginUser();
		model.addAttribute(CommonConst.COMMON_FORM, session);
		boolean admin = ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN);
		boolean teamAdmin = authUtil.isTeamLeader(loginUser);
		model.addAttribute(CommonConst.KEY_ADMIN, admin);
		model.addAttribute(CommonConst.KEY_TEAM_ADMIN, teamAdmin);
		// ---------------

		// 管理権限をもつチームを取得
		List<Team> teams = getControllableTeams(loginUser);

		// 管理権限を持つチームが存在しない場合、エラーとする。
		if (!admin && teams.size() == 0) {
			throw new ApplicationException(
					ErrorCode.ERR_CANNOT_SHOW_TEAM_LIST, loginUser);
		}

		List<TeamForm> teamForms = ControllerUtil.teamsToForms(teams);
		model.addAttribute("teams", teamForms);
		LOGGER.debug("openTeamListView end");
		return ViewName.TEAM_LIST;
	}

	/**
	 * Open a team create view.
	 *
	 * @param model
	 * @return
	 * @throws ApplicationException
	 */
	@RequestMapping(
			path = MappingPath.TEAM_CREATE,
			method = RequestMethod.GET)
	private String openTeamCreateView(Model model) throws ApplicationException {
		LOGGER.debug("openTeamCreateView start");
		ControllerUtil.preProcess(model);
		// ----- nav -----
		CustomUser loginUser = ControllerUtil.getLoginUser();
		model.addAttribute(CommonConst.COMMON_FORM, session);
		boolean admin = ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN);
		boolean teamAdmin = authUtil.isTeamLeader(loginUser);
		model.addAttribute(CommonConst.KEY_ADMIN, admin);
		model.addAttribute(CommonConst.KEY_TEAM_ADMIN, teamAdmin);
		// ---------------

		TeamForm form = (TeamForm) model.asMap().get(FORM_MODEL_KEY);
		if (form == null) {
			form = new TeamForm();
		}

		// 全体管理者のみ作成可
		if (!admin) {
			throw new ApplicationException(ErrorCode.ERR_CANNOT_OPEN_TEAM_CREATE_VIEW);
		}

		model.addAttribute(FORM_MODEL_KEY, form);
		LOGGER.debug("openTeamCreateView end");
		return ViewName.TEAM_CREATE;
	}

	private List<Team> getControllableTeams(CustomUser loginUser) {
		List<Team> teams = null;
		if (ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN)) {
			teams = teamService.findAll();
		} else {
			teams = authUtil.getTeams(loginUser, true);
		}
		return teams;
	}

	/**
	 * Create a team.
	 *
	 * Only full admin can create a team.
	 *
	 * @param bf
	 * @param error
	 * @param redirectAttributes
	 * @return a view name
	 * @throws ApplicationException
	 */
	@RequestMapping(
			path = MappingPath.TEAM_CREATE,
			method = RequestMethod.POST)
	private String createTeam(
			//@ModelAttribute @Validated(GroupOrder.class) TeamForm bf,
			@ModelAttribute @Validated TeamForm bf,
			BindingResult error,
			RedirectAttributes redirectAttributes) throws ApplicationException {
		LOGGER.debug("createTeam start:" + bf.getName() + "," + bf.getDescription());
		LOGGER.debug("error:" + error);
		Locale locale = LocaleContextHolder.getLocale();
		redirectAttributes.addFlashAttribute(CommonConst.LOCALE_KEY, locale.getLanguage());
		redirectAttributes.addFlashAttribute(FORM_MODEL_KEY, bf);
		redirectAttributes.addFlashAttribute(ERRORS_MODEL_KEY, error);

		CustomUser loginUser = ControllerUtil.getLoginUser();

		// 全体管理者のみ作成可
		if (!ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN)) {
			throw new ApplicationException(ErrorCode.ERR_NON_FULL_ADMIN_CANNOT_CREATE_TEAM);
		}

		if (error.hasErrors()) {
			return MappingPath.REDIRECT_TEAM_CREATE;
		}

		List<Team> found = teamService.findByName(bf.getName());
		if (found.size() > 0) {
			LOGGER.info("チーム名重複のためエラー:[" + bf.getName() + "]");
			redirectAttributes.addFlashAttribute("message",
					messageUtil.getMessage(MessageCode.MESSAGE_TEAM_NAME_DUPLICATED));
			return MappingPath.REDIRECT_TEAM_CREATE;
		}

		Team board = new Team();
		board.setName(bf.getName());
		board.setDescription(bf.getDescription());
		teamService.insert(board);

		redirectAttributes.getFlashAttributes().clear();
		LOGGER.debug("createTeam end");
		return MappingPath.REDIRECT_TEAM;
	}

	/**
	 * Open a team edit view.
	 *
	 * @param model
	 * @param teamId
	 * @return a view name
	 * @throws ApplicationException
	 */
	@RequestMapping(
			path = MappingPath.TEAM_EDIT + MappingPath.REGEX_TEAM_ID,
			method = RequestMethod.GET)
	private String openTeamEditView(
			Model model,
			@PathVariable int teamId) throws ApplicationException {
		return openCommonTeamView(model, teamId, false);
	}

	/**
	 * Open a team info view.
	 *
	 * @param model
	 * @param teamId
	 * @return a view name
	 * @throws ApplicationException
	 */
	@RequestMapping(
			path = MappingPath.TEAM_SHOW + MappingPath.REGEX_TEAM_ID,
			method = RequestMethod.GET)
	private String openTeamRefView(
			Model model,
			@PathVariable int teamId) throws ApplicationException {
		return openCommonTeamView(model, teamId, true);
	}

	private String openCommonTeamView(
			Model model,
			int teamId,
			boolean readonly) throws ApplicationException {
		LOGGER.debug("openCommonTeamView start");
		ControllerUtil.preProcess(model);
		// ----- nav -----
		CustomUser loginUser = ControllerUtil.getLoginUser();
		model.addAttribute(CommonConst.COMMON_FORM, session);
		boolean admin = ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN);
		boolean teamAdmin = authUtil.isTeamLeader(loginUser);
		model.addAttribute(CommonConst.KEY_ADMIN, admin);
		model.addAttribute(CommonConst.KEY_TEAM_ADMIN, teamAdmin);
		// ---------------

		TeamForm form = (TeamForm) model.asMap().get(FORM_MODEL_KEY);
		if (form == null) {
			Optional<Team> found = teamService.findById(teamId);
			if (!found.isPresent()) {
				throw new ApplicationException(
						ErrorCode.ERR_NO_TEAM_TO_BE_EDITED, teamId);
			}
			Team board = found.get();
			form = ControllerUtil.teamToForm(board);
		}
		model.addAttribute(FORM_MODEL_KEY, form);
		model.addAttribute(CommonConst.KEY_READONLY, readonly);

		List<Integer> teamIdsUnderControl = authUtil.getTeamIdsUnderControl(loginUser);
		if (!teamIdsUnderControl.contains(teamId)) {
			throw new ApplicationException(
					ErrorCode.ERR_CANNOT_EDIT_TEAM_NOT_UNDER_CONTROL, teamId);
		}
		LOGGER.debug("openCommonTeamView end");
		return ViewName.TEAM_EDIT;
	}

	@RequestMapping(
			path = MappingPath.TEAM_EDIT_INNER,
			method = RequestMethod.POST)
	private String editTeam(
			//@ModelAttribute @Validated(GroupOrder.class) TeamForm bf,
			@ModelAttribute @Validated TeamForm bf,
			BindingResult error,
			RedirectAttributes redirectAttributes) throws ApplicationException {
		LOGGER.debug("editTeam start:" + bf.getName() + "," + bf.getDescription());
		LOGGER.debug("error:" + error);

		CustomUser loginUser = ControllerUtil.getLoginUser();

		Locale locale = LocaleContextHolder.getLocale();
		redirectAttributes.addFlashAttribute(CommonConst.LOCALE_KEY, locale.getLanguage());
		redirectAttributes.addFlashAttribute(FORM_MODEL_KEY, bf);
		redirectAttributes.addFlashAttribute(ERRORS_MODEL_KEY, error);
		Integer teamId = bf.getId();
		if (error.hasErrors()) {
			return MappingPath.REDIRECT_TEAM_EDIT + teamId;
		}

		List<Integer> teamIdsUnderControl = authUtil.getTeamIdsUnderControl(loginUser);
		if (!teamIdsUnderControl.contains(teamId)) {
			throw new ApplicationException(
					ErrorCode.ERR_CANNOT_EDIT_TEAM_NOT_UNDER_CONTROL, teamId);
		}

		Optional<Team> team = teamService.findById(teamId);
		if (!team.isPresent()) {
			throw new ApplicationException(ErrorCode.ERR_TEAM_NOT_FOUND, teamId);
		}

		List<Team> found = teamService.findByName(bf.getName());
		// Check duplication when changing the board name.
		if (!bf.getName().equals(team.get().getName()) && found.size() > 0) {
			LOGGER.info("Duplication error for team names:[" + bf.getName() + "]");
			redirectAttributes.addFlashAttribute("message",
					messageUtil.getMessage(MessageCode.MESSAGE_TEAM_NAME_DUPLICATED));
			return MappingPath.REDIRECT_TEAM_EDIT + teamId;
		}

		Team board = new Team();
		board.setId(bf.getId());
		board.setName(bf.getName());
		board.setDescription(bf.getDescription());
		teamService.update(board);

		redirectAttributes.getFlashAttributes().clear();
		LOGGER.debug("editTeam end");
		return MappingPath.REDIRECT_TEAM;
	}

	@RequestMapping(
			path = MappingPath.TEAM_DELETE + MappingPath.REGEX_TEAM_ID)
	private String deleteTeam(
			Model model,
			@PathVariable int teamId) throws ApplicationException {
		LOGGER.debug("deleteTeam start:" + teamId);

		CustomUser loginUser = ControllerUtil.getLoginUser();
		if (!ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN)) {
			throw new ApplicationException(ErrorCode.ERR_NON_FULL_ADMIN_CANNOT_DELETE_TEAM, teamId);
		}

		Optional<Team> optional = teamService.findById(teamId);
		if (!optional.isPresent()) {
			throw new ApplicationException(ErrorCode.ERR_TEAM_NOT_FOUND, teamId);
		}
		Team teamToDelete = optional.get();
		List<Board> boardsToDelete = boardService.findByTeam(teamToDelete);

		// Delete all the records related to the team to delete in the User-Team table.
		userTeamService.deleteByTeamId(teamId);

		// Delete all the boards belonging to the team to delete.
		for (Board b : boardsToDelete) {
			// Delete all the description on the board
			LOGGER.info(String.format(
					"Delete all the description on the board:[%s]", b.toString()));
			descService.deleteByBoard(b.getId());

			LOGGER.info(String.format("Delete the board:[%s]", b.toString()));
			boardService.deleteById(b.getId());
		}

		LOGGER.info("Delete the team:[" + teamToDelete + "]");
		teamService.deleteById(teamId);

		LOGGER.debug("deleteTeam end");
		return MappingPath.REDIRECT_TEAM;
	}

}
