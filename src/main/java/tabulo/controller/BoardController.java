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

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
import tabulo.JsonConverter;
import tabulo.MessageUtil;
import tabulo.PropertyAccessor;
import tabulo.constant.CommonConst;
import tabulo.constant.ErrorCode;
import tabulo.constant.MappingPath;
import tabulo.constant.MessageCode;
import tabulo.constant.ViewName;
import tabulo.form.BoardForm;
import tabulo.form.TeamForm;
import tabulo.model.Board;
import tabulo.model.BoardRegistrationMessage;
import tabulo.model.BoardUser;
import tabulo.model.Description;
import tabulo.model.DescriptionWrapper;
import tabulo.model.Team;
import tabulo.model.UserTeam;
import tabulo.model.WebSocketMessage;
import tabulo.service.BoardService;
import tabulo.service.BoardUserService;
import tabulo.service.DescriptionService;
import tabulo.service.TeamService;
import tabulo.service.UserTeamService;
import tabulo.session.SessionCommon;

/**
 * Controller for boards.
 */
@Controller
public class BoardController {

	/** logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(BoardController.class);

	// Keys used in a board
	/** map key for boardName. */
	private static final String KEY_BOARD_NAME = "boardName";

	/** map key for descs. */
	private static final String KEY_DESCS = "descs";

	/** map key for boardId. */
	private static final String KEY_BOARD_ID = "boardId";

	private static final String KEY_USER_LIST = "userList";

	private static final String KEY_SEND_BEACON_INTERVAL = "sendBeaconInterval";

	private static final String KEY_CONNECTION_CHECK_INTERVAL = "connectionCheckInterval";

	private static final String KEY_BOARDS = "boards";

	private static final String KEY_SELECTED_TEAM = "selectedTeam";

	private static final String KEY_TEAMS = "teams";

	private static final String KEY_MESSAGE = "message";

	// Keys used for a description object:
	private static final String KEY_X = "x";
	private static final String KEY_Y = "y";
	private static final String KEY_WIDTH = "width";
	private static final String KEY_HEIGHT = "height";
	private static final String KEY_RAW = "raw";
	private static final String KEY_HTML = "html";

	// Endpoints used for WebSocket.
	private static final String TOPIC_BOARD = "/topic/board/";
	private static final String PROCESS = "/process";
	private static final String TOPIC_REGISTRATION = "/topic/registration/";
	private static final String REGISTRATION = "/registration";

	/** Key for a board form. */
	private static final String FORM_MODEL_KEY = "boardForm";

	/** Key for a error model. */
	private static final String ERRORS_MODEL_KEY = BindingResult.MODEL_KEY_PREFIX + FORM_MODEL_KEY;

	// Keys for WebSocket message types
	/** WebSocketMessage type "open". */
	private static final String WS_MESSAGE_TYPE_OPEN = "open";
	/** WebSocketMessage type "close". */
	private static final String WS_MESSAGE_TYPE_CLOSE = "close";
	/** WebSocketMessage type "create". */
	private static final String WS_MESSAGE_TYPE_CREATE = "create";
	/** WebSocketMessage type "update". */
	private static final String WS_MESSAGE_TYPE_UPDATE = "update";
	/** WebSocketMessage type "remove". */
	private static final String WS_MESSAGE_TYPE_REMOVE = "remove";
	/** WebSocketMessage type "move". */
	private static final String WS_MESSAGE_TYPE_MOVE = "move";
	/** WebSocketMessage type "resize". */
	private static final String WS_MESSAGE_TYPE_RESIZE = "resize";

	/**
	 * String identifying login user.
	 *
	 * This service allows the users to login with their username.
	 *
	 *  */
	private static final int SESSION_DISPLAY_NAME_LENGTH = 4;

	/**
	 * Property accessor.
	 */
	@Autowired
	PropertyAccessor propertyAccessor;

	/**
	 * Board service.
	 */
	@Autowired
	BoardService boardService;

	/** BoardUser service. */
	@Autowired
	BoardUserService boardUserService;

	/** Team service. */
	@Autowired
	TeamService teamService;

	/** UserTeam service. */
	@Autowired
	UserTeamService userTeamService;

	/** Desc service. */
	@Autowired
	DescriptionService descService;

	/** Utility for authorization. */
	@Autowired
	AuthorizationUtil authUtil;

	/** Utility for messages. */
	@Autowired
	MessageUtil messageUtil;

	/**
	 * Simple messaging template.
	 */
	@Autowired
	SimpMessagingTemplate simpMessagingTemplate;

	/**
	 * Session object for common use.
	 */
	@Autowired
	SessionCommon session;

	/**
	 * JSON converter.
	 */
	private JsonConverter<WebSocketMessage> jsonConverter = new JsonConverter<WebSocketMessage>();

	/**
	 * Open the main view.
	 *
	 * @param model Model
	 * @return a view name
	 * @throws ApplicationException exception
	 */
	@RequestMapping(
			path = MappingPath.MAIN,
			method = RequestMethod.GET)
	private String openMainView(Model model) throws ApplicationException {
		LOGGER.debug("openMainView start");
		ControllerUtil.preProcess(model);

		CustomUser loginUser = ControllerUtil.getLoginUser();
		Integer loginUserId = loginUser.getUserId();
		model.addAttribute(CommonConst.COMMON_FORM, session);

		// a variable meaning whether the login user is admin for any team.
		boolean teamAdmin = false;
		List<Team> teams = null;
		if (ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN)) {
			model.addAttribute(CommonConst.KEY_ADMIN, true);
			// Set all the teams for full admins.
			teams = teamService.findAll();
		} else {
			model.addAttribute(CommonConst.KEY_ADMIN, false);
			// Get all the teams that the login User belongs to.
			List<UserTeam> userTeams = userTeamService.findByUserId(loginUserId);
			teams = new ArrayList<Team>();
			for (UserTeam userTeam : userTeams) {
				Optional<Team> team = teamService.findById(userTeam.getTeamId());
				if (team.isPresent()) {
					if ("1".equals(userTeam.getAuth())) {
						teamAdmin = true;
					}
					teams.add(team.get());
				} else {
					throw new ApplicationException(
							ErrorCode.ERR_TEAM_NOT_FOUND,
							userTeam.getTeamId());
				}
			}
		}

		model.addAttribute(CommonConst.KEY_TEAM_ADMIN, teamAdmin);

		List<TeamForm> teamForms = new ArrayList<TeamForm>();

		List<BoardForm> boardForms = new ArrayList<BoardForm>();

		for (Team team : teams) {
			List<Board> boardsInTeam = team.getBoards();
			for (Board board : boardsInTeam) {
				BoardForm boardForm = new BoardForm();
				boardForm.setId(board.getId());
				boardForm.setTeamId(board.getTeam().getId());
				boardForm.setName(board.getName());
				boardForm.setDescription(board.getDescription());
				boardForms.add(boardForm);
			}

			// Sort boardForms alphabetically according to board names
			ControllerUtil.sort(boardForms);

			teamForms.add(ControllerUtil.teamToForm(team));
		}

		// Sort teamForms alphabetically according to board names
		ControllerUtil.sort(teamForms);
		String teamId = session.getSelectedTeam();
		if (teamId != null) {
			model.addAttribute(KEY_SELECTED_TEAM, teamId);
		}
		model.addAttribute(KEY_TEAMS, teamForms);
		model.addAttribute(KEY_BOARDS, boardForms);

		LOGGER.debug("openMainView end");
		return ViewName.MAIN;
	}

	/**
	 * Process the action from the login view.
	 *
	 * The request is redirect to the main view.
	 * (Post-Redirect-Get Pattern)
	 *
	 * @param model Model
	 * @return a view name
	 * @throws ApplicationException exception
	 */
	@RequestMapping(
			path = MappingPath.MAIN,
			method = RequestMethod.POST)
	private String processMain(Model model) throws ApplicationException {
		LOGGER.debug("processMain start");
		ControllerUtil.preProcess(model);

		CustomUser loginUser = ControllerUtil.getLoginUser();
		Integer loginUserId = loginUser.getUserId();
		session.setLoginUserId(loginUserId.toString());
		session.setLoginUserName(loginUser.getUsername());
		model.addAttribute(CommonConst.COMMON_FORM, session);

		LOGGER.debug("processMain end");
		return MappingPath.REDIRECT_MAIN;
	}

	/**
	 * Show all the boards that the login user has the right to manage.
	 *
	 * @param model Model
	 * @return a view name
	 * @throws ApplicationException exception
	 */
	@RequestMapping(
			path = MappingPath.BOARD_LIST)
	private String openBoardList(Model model)
			throws ApplicationException {
		LOGGER.debug("openBoardList start");
		ControllerUtil.preProcess(model);

		// ----- nav -----
		CustomUser loginUser = ControllerUtil.getLoginUser();
		model.addAttribute(CommonConst.COMMON_FORM, session);
		boolean admin = ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN);
		boolean teamAdmin = authUtil.isTeamLeader(loginUser);
		model.addAttribute(CommonConst.KEY_ADMIN, admin);
		model.addAttribute(CommonConst.KEY_TEAM_ADMIN, teamAdmin);
		// ---------------

		// Get all the teams under the control
		List<Team> teamsUnderControl = authUtil.getTeamsUnderControl(loginUser);

		// Get a teamId-teamName map
		Map<Integer, String> teamIdNameMap = getTeamIdNameMap(teamsUnderControl);

		// Only full admins or team admins are allowed to open.
		if (!admin && !teamAdmin) {
			throw new ApplicationException(
					ErrorCode.ERR_NON_ADMIN_CANNOT_OPEN_BOARD_LIST_VIEW,
					loginUser.getUserId());
		}
		// Get all the boards under the control
		List<Board> boardsUnderControl = new ArrayList<Board>();
		for (Team team : teamsUnderControl) {
			boardsUnderControl.addAll(team.getBoards());
		}

		List<BoardForm> boardFormsUnderControl = new ArrayList<BoardForm>();
		for (Board board : boardsUnderControl) {
			BoardForm boardForm = ControllerUtil.boardToForm(board);
			boardForm.setTeamName(teamIdNameMap.get(boardForm.getTeamId()));
			boardFormsUnderControl.add(boardForm);
		}
		model.addAttribute(KEY_BOARDS, boardFormsUnderControl);
		LOGGER.debug("openBoardList end");
		return ViewName.BOARD_LIST;
	}

	/**
	 * Get a teamId-teamName map.
	 *
	 * @param teams
	 * @return a teamId-teamName map.
	 */
	private Map<Integer, String> getTeamIdNameMap(List<Team> teams) {
		Map<Integer, String> ret = new HashMap<Integer, String>();
		for (Team team : teams) {
			ret.put(team.getId(), team.getName());
		}
		return ret;
	}

	/**
	 * Open a user create view.
	 *
	 * @param model Model
	 * @return a view name
	 * @throws ApplicationException exception
	 */
	@RequestMapping(
			path = MappingPath.BOARD_CREATE,
			method = RequestMethod.GET)
	private String openBoardCreateView(Model model) throws ApplicationException {
		LOGGER.debug("openBoardCreateView start");
		ControllerUtil.preProcess(model);

		// ----- nav -----
		CustomUser loginUser = ControllerUtil.getLoginUser();
		model.addAttribute(CommonConst.COMMON_FORM, session);
		boolean admin = ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN);
		boolean teamAdmin = authUtil.isTeamLeader(loginUser);
		model.addAttribute(CommonConst.KEY_ADMIN, admin);
		model.addAttribute(CommonConst.KEY_TEAM_ADMIN, teamAdmin);
		// ---------------

		BoardForm boardForm = (BoardForm) model.asMap().get(FORM_MODEL_KEY);
		if (boardForm == null) {
			boardForm = new BoardForm();
		}
		model.addAttribute(FORM_MODEL_KEY, boardForm);

		// Get the teams under the control to be shown in the pulldown.
		List<TeamForm> teamFormsUnderControl = authUtil.getTeamFormsUnderControl(loginUser);
		// Only full admins and team admins are allowed to open.
		if (!admin && !teamAdmin) {
			throw new ApplicationException(
					ErrorCode.ERR_NON_ADMIN_CANNOT_CREATE_BOARD,
					loginUser.getUserId());
		}

		model.addAttribute(KEY_TEAMS, teamFormsUnderControl);
		LOGGER.debug("openBoardCreateView end");
		return ViewName.BOARD_CREATE;
	}

	/**
	 * Internal process for creating a board.
	 *
	 * One can create a board belonging to the teams under the control.
	 * Throws an exception if this method is called by non-admin for the team.
	 *
	 * @param boardForm a board form
	 * @param error an error object
	 * @param redirectAttributes attributes available in the next view
	 * @return a view name
	 * @throws ApplicationException exception
	 */
	@RequestMapping(
			path = MappingPath.BOARD_CREATE,
			method = RequestMethod.POST)
	private String createBoard(
			//@ModelAttribute @Validated(GroupOrder.class) BoardForm boardForm,
			@ModelAttribute @Validated BoardForm boardForm,
			BindingResult error,
			RedirectAttributes redirectAttributes) throws ApplicationException {
		LOGGER.debug("createBoard" + boardForm + "," + error);

		CustomUser loginUser = ControllerUtil.getLoginUser();

		Locale locale = LocaleContextHolder.getLocale();
		redirectAttributes.addFlashAttribute(CommonConst.LOCALE_KEY, locale.getLanguage());
		redirectAttributes.addFlashAttribute(FORM_MODEL_KEY, boardForm);
		redirectAttributes.addFlashAttribute(ERRORS_MODEL_KEY, error);
		redirectAttributes.addFlashAttribute(CommonConst.COMMON_FORM, session);

		// Get the teams under the control to be shown in the pulldown.
		List<TeamForm> teamFormsUnderControl = authUtil.getTeamFormsUnderControl(loginUser);
		redirectAttributes.addFlashAttribute(KEY_TEAMS, teamFormsUnderControl);

		if (error.hasErrors()) {
			return MappingPath.REDIRECT_BOARD_CREATE;
		}

		Integer teamId = boardForm.getTeamId();

		// Check whether the login user has authen of the given team.
		if (!authUtil.isTeamAvailable(loginUser, teamId, true)) {
			throw new ApplicationException(
					ErrorCode.ERR_CANNOT_CREATE_BOARD_BELONGING_TO_TEAM, teamId);
		}

		// Duplication check for board names.
		List<Board> boards = boardService.findByName(boardForm.getName());
		if (boards.size() > 0) {
			redirectAttributes.addFlashAttribute(KEY_MESSAGE,
					messageUtil.getMessage(MessageCode.MESSAGE_BOARD_NAME_DUPLICATED));
			return MappingPath.REDIRECT_BOARD_CREATE;
		}

		Board board = new Board();
		Optional<Team> found = teamService.findById(teamId);
		if (!found.isPresent()) {
			throw new ApplicationException(ErrorCode.ERR_TEAM_NOT_FOUND, teamId);
		}
		board.setTeam(found.get());
		board.setName(boardForm.getName());
		board.setDescription(boardForm.getDescription());
		boardService.insert(board);

		LOGGER.debug("createBoard end");
		redirectAttributes.getFlashAttributes().clear();
		return MappingPath.REDIRECT_BOARD;
	}

	/**
	 * Show a board edit view if the login user has the right to do that.
	 * Otherwise, an exception will be thrown.
	 *
	 * @param model Model
	 * @param boardId a board id
	 * @return a view name
	 * @throws ApplicationException
	 */
	@RequestMapping(
			path = MappingPath.BOARD_EDIT + MappingPath.REGEX_BOARD_ID,
			method = RequestMethod.GET)
	private String openBoardEditView(Model model, @PathVariable int boardId)
			throws ApplicationException {
		return openCommonView(model, boardId, false);
	}

	/**
	 * Show a board edit view if the login user has the right to do that.
	 * Otherwise, an exception will be thrown.
	 *
	 * @param model Model
	 * @param boardId a board Id to edit
	 * @return a view name
	 * @throws ApplicationException exception
	 */
	@RequestMapping(
			path = MappingPath.BOARD_SHOW + MappingPath.REGEX_BOARD_ID,
			method = RequestMethod.GET)
	private String openBoardRefView(
			Model model,
			@PathVariable int boardId)
			throws ApplicationException {
		return openCommonView(model, boardId, true);
	}

	/**
	 * Open a edit view in a readonly mode or an edit mode.
	 *
	 * @param model Model
	 * @param boardId boardId
	 * @param readonly set true if one want to show in a readonly mode, set false otherwise.
	 * @return a view name
	 * @throws ApplicationException
	 */
	private String openCommonView(
			Model model,
			int boardId,
			boolean readonly)
			throws ApplicationException {
		LOGGER.debug("openCommonView start:[" + boardId + "][" + readonly + "]");
		ControllerUtil.preProcess(model);
		// ----- nav -----
		CustomUser loginUser = ControllerUtil.getLoginUser();
		model.addAttribute(CommonConst.COMMON_FORM, session);
		boolean admin = ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN);
		boolean teamAdmin = authUtil.isTeamLeader(loginUser);
		model.addAttribute(CommonConst.KEY_ADMIN, admin);
		model.addAttribute(CommonConst.KEY_TEAM_ADMIN, teamAdmin);
		// ---------------

		if (!authUtil.isBoardAvailable(loginUser, boardId, true)) {
			throw new ApplicationException(
					ErrorCode.ERR_CANNOT_EDIT_BOARD, boardId);
		}

		BoardForm boardForm = (BoardForm) model.asMap().get(FORM_MODEL_KEY);
		if (boardForm == null) {
			Optional<Board> found = boardService.findById(boardId);
			if (found.isPresent()) {
				Board board = found.get();
				boardForm = ControllerUtil.boardToForm(board);

				// Get the teams under the control to be shown in a pulldown.
				List<TeamForm> teamFormsUnderControl = authUtil.getTeamFormsUnderControl(loginUser);
				model.addAttribute(KEY_TEAMS, teamFormsUnderControl);
			} else {
				throw new ApplicationException(
						ErrorCode.ERR_BOARD_NOT_FOUND, boardId);
			}
		}
		model.addAttribute(FORM_MODEL_KEY, boardForm);
		model.addAttribute(CommonConst.KEY_READONLY, readonly);
		LOGGER.debug("openCommonView end");
		return ViewName.BOARD_EDIT;
	}

	@RequestMapping(
			path = MappingPath.BOARD_EDIT_INNER,
			method = RequestMethod.POST)
	private String editBoard(
			//@ModelAttribute @Validated(GroupOrder.class) BoardForm boardForm,
			@ModelAttribute @Validated BoardForm boardForm,
			BindingResult error,
			RedirectAttributes redirectAttributes) throws ApplicationException {
		LOGGER.debug("editBoard:" + boardForm + "," + error);

		CustomUser loginUser = ControllerUtil.getLoginUser();

		// 編集対象ボードに対する管理権限チェック
		if (!authUtil.isBoardAvailable(loginUser, boardForm.getId(), true)) {
			throw new ApplicationException(
					ErrorCode.ERR_CANNOT_EDIT_BOARD, boardForm.getId());
		}

		Locale locale = LocaleContextHolder.getLocale();
		redirectAttributes.addFlashAttribute(CommonConst.LOCALE_KEY, locale.getLanguage());
		redirectAttributes.addFlashAttribute(FORM_MODEL_KEY, boardForm);
		redirectAttributes.addFlashAttribute(ERRORS_MODEL_KEY, error);
		redirectAttributes.addFlashAttribute(CommonConst.COMMON_FORM, session);

		// ログインユーザーが管理権限をもつチームのみプルダウンに表示する。
		List<TeamForm> teamFormsUnderControl = authUtil.getTeamFormsUnderControl(loginUser);
		redirectAttributes.addFlashAttribute(KEY_TEAMS, teamFormsUnderControl);

		if (error.hasErrors()) {
			return MappingPath.REDIRECT_BOARD_EDIT + boardForm.getId();
		}
		Integer teamId = boardForm.getTeamId();

		// ボードが存在が前提
		Optional<Board> foundBoardById = boardService.findById(boardForm.getId());

		// ログインユーザーが指定したチームの管理権限をもつかチェックする。
		// 全体権限を持つユーザーはチーム権限チェック不要、そうでない場合には個別チームに対する権限チェックを行う。
		if (!authUtil.isTeamAvailable(loginUser, teamId, true)) {
			throw new ApplicationException(
					ErrorCode.ERR_CANNOT_EDIT_TEAM_NOT_UNDER_CONTROL, teamId);
		}

		Optional<Team> teamFound = teamService.findById(teamId);
		if (!teamFound.isPresent()) {
			throw new ApplicationException(ErrorCode.ERR_TEAM_NOT_FOUND, teamId);
		}

		// ボード名の重複チェック
		List<Board> boardsFoundByName = boardService.findByName(boardForm.getName());
		// 名前が変更されていて、かつ、変更先の名前が存在する場合はボード名重複と判定する。
		if (!foundBoardById.get().getName().equals(boardForm.getName())
				&& boardsFoundByName.size() > 0) {
			redirectAttributes.addFlashAttribute(KEY_MESSAGE,
					messageUtil.getMessage(MessageCode.MESSAGE_BOARD_NAME_DUPLICATED));
			return MappingPath.REDIRECT_BOARD_EDIT + boardForm.getId();
		}

		Board board = new Board();
		board.setTeam(teamFound.get());
		board.setId(boardForm.getId());
		board.setName(boardForm.getName());
		board.setDescription(boardForm.getDescription());
		boardService.update(board);

		redirectAttributes.getFlashAttributes().clear();
		LOGGER.debug("editBoard end");
		return MappingPath.REDIRECT_BOARD;
	}

	@RequestMapping(
			path = MappingPath.BOARD_DELETE + MappingPath.REGEX_BOARD_ID)
	private String deleteBoard(Model model, @PathVariable int boardId)
			throws ApplicationException {
		LOGGER.debug("deleteBoard start:[" + boardId + "]");

		CustomUser loginUser = ControllerUtil.getLoginUser();

		// 編集対象ボードに対する管理権限チェック
		if (!authUtil.isBoardAvailable(loginUser, boardId, true)) {
			throw new ApplicationException(
					ErrorCode.ERR_CANNOT_DELETE_BOARD, boardId);
		}

		// Delete all the descriptions on the board.
		LOGGER.info(String.format(
				"Delete all the description on the board:[%s]", boardId));
		descService.deleteByBoard(boardId);

		LOGGER.info(String.format("Delete the board:[%s]", boardId));
		boardService.deleteById(boardId);
		LOGGER.debug("deleteBoard end");
		return MappingPath.REDIRECT_BOARD;
	}

	@RequestMapping(
			path = MappingPath.BOARD_OPEN + MappingPath.REGEX_BOARD_ID,
			method = RequestMethod.GET)
	public String openBoardView(
			Model model,
			@RequestParam(
					name = "st",
					required = false) String selectedTeam,
			@PathVariable Integer boardId) throws ApplicationException {
		LOGGER.debug("openBoardView start:[" + boardId + "]");
		ControllerUtil.preProcess(model);
		// ----- nav -----
		CustomUser loginUser = ControllerUtil.getLoginUser();
		model.addAttribute(CommonConst.COMMON_FORM, session);
		boolean admin = ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN);
		boolean teamAdmin = authUtil.isTeamLeader(loginUser);
		model.addAttribute(CommonConst.KEY_ADMIN, admin);
		model.addAttribute(CommonConst.KEY_TEAM_ADMIN, teamAdmin);
		// ---------------

		model.addAttribute(KEY_CONNECTION_CHECK_INTERVAL,
				propertyAccessor.getConnectionCheckInterval());
		model.addAttribute(KEY_SEND_BEACON_INTERVAL,
				propertyAccessor.getSendBeaconInterval());

		// ボードIDの存在チェックと権限チェックを行う。
		if (!authUtil.isBoardAvailable(loginUser, boardId, false)) {
			throw new ApplicationException(
					ErrorCode.ERR_CANNOT_OPEN_BOARD_VIEW, boardId);
		}

		Optional<Board> found = boardService.findById(boardId);
		model.addAttribute(KEY_BOARD_NAME, found.get().getName());

		// ボードユーザーリスト
		String userList = createBoardUserArray(boardId);

		// 記述リスト
		String descriptions = createDecsArray(boardId);
		model.addAttribute(KEY_BOARD_ID, boardId);
		model.addAttribute(KEY_DESCS, descriptions);
		model.addAttribute(KEY_USER_LIST, userList);

		// Set the selected teamId into SessionCommon.
		if (selectedTeam != null) {
			session.setSelectedTeam(selectedTeam);
		}

		LOGGER.debug("openBoardView end");
		return ViewName.BOARD;
	}

	/**
	 * Create a json string of description objects for embedding into the board template.
	 *
	 * @param boardId
	 * @return a json string of the description objects on a board
	 */
	private String createDecsArray(Integer boardId) {
		LOGGER.debug("createDecsArray start:" + boardId);
		List<DescriptionWrapper> descDataList = descService.createDescriptionWrappers(boardId);
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (int i = 0; i < descDataList.size(); i++) {
			DescriptionWrapper desc = descDataList.get(i);
			sb.append(desc.toJSON());
			if (i < descDataList.size() - 1) {
				sb.append(",").append(CommonConst.CRLF);
			}
		}
		sb.append("]");
		LOGGER.debug("createDecsArray end");
		return sb.toString();
	}

	private String createBoardUserArray(Integer boardId) {
		List<BoardUser> boardUsers = boardUserService.findByBoardId(boardId);
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (int i = 0; i < boardUsers.size(); i++) {
			BoardUser boardUser = boardUsers.get(i);
			sb.append("{");
			sb.append("name:'").append(boardUser.getUserName()).append("', ");
			sb.append("unite:'").append(boardUser.getSubId()).append("'");
			sb.append("}");
			if (i < boardUsers.size() - 1) {
				sb.append(",").append(CommonConst.CRLF);
			}
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * ボード画面の表示時にボード利用者テーブルから対象ボードの利用者リストを取得し、
	 * ボード画面表示時にuserListとしてjavascript配列に設定する。
	 *
	 * 登録の変更があった場合には、最新の利用者リストを配信する。
	 * @param sha
	 * @param message
	 * @param principal
	 * @return
	 * @throws Exception
	 */
	// TODO: 処理効率が悪い可能性がある。
	@MessageMapping(REGISTRATION)
	public void userRegistration(
			SimpMessageHeaderAccessor sha,
			BoardRegistrationMessage message,
			Principal principal) throws Exception {
		LOGGER.debug("userRegistration:" + message);
		String sessionId = sha.getSessionId();
		String hash = ControllerUtil.getCRC32HexString(
				sessionId).substring(0, SESSION_DISPLAY_NAME_LENGTH);
		LOGGER.debug("The first 2 bytes of CRC32 hash value:" + hash + "]");

		// Throw an exception if the board id is not set.
		if (message.getBoardId() == null) {
			throw new ApplicationException(ErrorCode.ERR_NO_BOARD_ID_FOR_REGISTRATION);
		}

		// Check the accessibility to the board.
		checkBoardAvailability(principal, message.getBoardId());

		// Remove the records corresponding to the login users who has not
		// sent connection notifications for a certain period.
		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		LOGGER.debug("Remove the login users from the board :["
				+ propertyAccessor.getThresholdPeriodInSeconds() + "] seconds");
		cal.add(Calendar.SECOND, -propertyAccessor.getThresholdPeriodInSeconds());
		boardUserService.deleteNotUpdatedRows(cal.getTime());

		// Insert a new record into the Board-User table if not registered.
		if (WS_MESSAGE_TYPE_OPEN.equals(message.getType())) {
			LOGGER.debug("A board user entered");
			Optional<BoardUser> found = boardUserService.findByUserIdSubId(message.getUserId(),
					hash);

			if (!found.isPresent()) {
				BoardUser boardUser = new BoardUser();
				boardUser.setUserName(message.getUserName());
				boardUser.setUserId(message.getUserId());
				boardUser.setSubId(hash);
				boardUser.setBoardId(message.getBoardId());
				boardUser.setLastUpdateData(new Date());
				boardUserService.insert(boardUser);
			} else {
				LOGGER.debug(String.format("Update a board user table:[%s]", found.get()));
				found.get().setLastUpdateData(new Date());
				boardUserService.update(found.get());
			}
		}
		if (WS_MESSAGE_TYPE_CLOSE.equals(message.getType())) {
			LOGGER.debug("A board user left");
			boardUserService.deleteByUserIdSubId(message.getUserId(), hash);
		}

		// Create a message for publish.
		// Publish the newest state of a board user table.
		BoardRegistrationMessage outMessage = new BoardRegistrationMessage();
		List<BoardUser> boardUsers = boardUserService.findByBoardId(message.getBoardId());
		Collections.sort(boardUsers, new Comparator<BoardUser>() {

			@Override
			public int compare(BoardUser o1, BoardUser o2) {
				return o1.getUserName().compareTo(o2.getUserName());
			}

		});
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < boardUsers.size(); i++) {
			BoardUser boardUser = boardUsers.get(i);
			sb.append(boardUser.getUserName() + " (" + boardUser.getSubId() + ")");
			if (i < boardUsers.size() - 1) {
				sb.append(CommonConst.TAB);
			}
		}
		outMessage.setType("list");
		String list = sb.toString();
		LOGGER.debug(String.format("Publish the board state:[%s]", list));
		outMessage.setList(list);

		// TODO: 対象ボードに対するユーザリスト更新が一定の時間（外部定義できるようにする）
		// 行われていない場合のみリストを送信する。
		// すなわち、生存確認を頻繁に受信し、DBを更新したとしても、配信の頻度はあげたくない。
		// TODO: 前回のリストに比べて異なっている場合には即時配信する。これは、誰かがボードに入ったり、出たりした場合に発生。
		simpMessagingTemplate.convertAndSend(TOPIC_REGISTRATION + message.getBoardId(), outMessage);
	}

	/**
	 * Check whether the login user has the authority to access a board.
	 *
	 * @param principal principal
	 * @param boardId boardId
	 * @throws ApplicationException
	 */
	CustomUser checkBoardAvailability(Principal principal, Integer boardId)
			throws ApplicationException {
		UsernamePasswordAuthenticationToken token = null;
		if (principal instanceof UsernamePasswordAuthenticationToken) {
			token = (UsernamePasswordAuthenticationToken) principal;
		} else {
			throw new ApplicationException(
					ErrorCode.ERR_UNEXPECTED_PRINCIPAL, principal);
		}
		CustomUser loginUser = null;
		Object obj = token.getPrincipal();
		if (obj instanceof CustomUser) {
			loginUser = (CustomUser) obj;
		} else {
			throw new ApplicationException(
					ErrorCode.ERR_UNEXPECTED_USER_OBJ, obj.getClass());
		}

		// Check whether a board id is given.
		if (boardId == null) {
			throw new ApplicationException(ErrorCode.ERR_BOARD_ID_WAS_NULL);
		}

		if (!authUtil.isBoardAvailable(loginUser, boardId, false)) {
			throw new ApplicationException(
					ErrorCode.ERR_CANNOT_ACCESS_BOARD, boardId);
		}
		return loginUser;
	}

	/**
	 *
	 * @param principal Principal
	 * @param message WebSocketMessage
	 * @throws Exception exception
	 */
	@MessageMapping(PROCESS)
	public void processMessage(Principal principal, WebSocketMessage message)
			throws Exception {
		LOGGER.debug("processMessage start:" + message.getType());

		// Check the access auth.
		CustomUser user = checkBoardAvailability(principal, message.getBoardId());

		switch (message.getType()) {
		case WS_MESSAGE_TYPE_MOVE:
			processMove(message);
			break;
		case WS_MESSAGE_TYPE_RESIZE:
			processResize(message);
			break;
		case WS_MESSAGE_TYPE_CREATE:
			processCreate(message, user);
			break;
		case WS_MESSAGE_TYPE_UPDATE:
			processUpdate(message, user);
			break;
		case WS_MESSAGE_TYPE_REMOVE:
			processRemove(message);
			break;
		default:
			throw new ApplicationException(ErrorCode.ERR_UNEXPECTED_MESSAGE_TYPE,
					message.getType());
		}
		LOGGER.debug("processMessage end:[" + message.getId() + "]");
		simpMessagingTemplate.convertAndSend(TOPIC_BOARD + message.getBoardId(), message);
	}

	private void processRemove(WebSocketMessage message) throws ApplicationException {
		LOGGER.debug("processRemove start:[" + message.getId() + "]");
		if (message.getId() == null) {
			throw new ApplicationException(ErrorCode.ERR_NO_ID_FOR_DELETING_DESCRPTION);
		}
		Integer id = Integer.parseInt(message.getId());
		descService.deleteById(id);
		LOGGER.debug("processRemove end" + id);
	}

	private Double objectToDouble(Object obj) {
		Double ret = null;
		if (obj != null) {
			ret = Double.parseDouble(obj.toString());
		}
		return ret;
	}

	private Integer objectToInteger(Object obj) {
		Integer ret = null;
		if (obj != null) {
			ret = Integer.parseInt(obj.toString());
		}
		return ret;
	}

	private void processUpdate(WebSocketMessage message, CustomUser user)
			throws ApplicationException {
		LOGGER.debug("processUpdate start:[" + message.getId() + "]");
		if (message.getId() == null) {
			throw new ApplicationException(ErrorCode.ERR_NO_ID_FOR_UPDATING_DESCRPTION);
		}
		Map<String, Object> map = null;
		try {
			map = jsonConverter.convertToMap(message.getData());
		} catch (IOException ioe) {
			throw new ApplicationException(
					ErrorCode.ERR_FAILURE_JSON_TO_MAP, message.getData());
		}
		Integer id = Integer.parseInt(message.getId());
		Optional<Description> found = descService.findById(id);
		if (found.isPresent()) {
			Description target = found.get();
			target.setUpdate_user(user.getUserId());
			target.setRaw(convertRaw(map.get(KEY_RAW)));
			target.setHtml(toString(map.get(KEY_HTML)));
			descService.save(target);
			map.put("update_name", user.getUsername());
			String data;
			try {
				data = jsonConverter.convertToJson(map);
			} catch (IOException e) {
				throw new ApplicationException(
						ErrorCode.ERR_FAILURE_MAP_TO_JSON, map.toString());
			}
			message.setData(data);
		} else {
			LOGGER.error(
					messageUtil.decode(ErrorCode.ERR_NO_RECORD_FOUND, id));
		}
		LOGGER.debug("processUpdate end:" + id);
	}

	private void processCreate(WebSocketMessage message, CustomUser user)
			throws ApplicationException {
		LOGGER.debug("processCreate start:" + message.getData() + "]");
		Map<String, Object> map = null;
		try {
			map = jsonConverter.convertToMap(message.getData());
		} catch (IOException e) {
			throw new ApplicationException(
					ErrorCode.ERR_FAILURE_JSON_TO_MAP, message.getData());
		}
		Description desc = new Description();
		desc.setBoardId(message.getBoardId());
		desc.setCreate_user(user.getUserId());
		desc.setX(objectToDouble(map.get(KEY_X)));
		desc.setY(objectToDouble(map.get(KEY_Y)));
		desc.setWidth(objectToDouble(map.get(KEY_WIDTH)));
		desc.setHeight(objectToDouble(map.get(KEY_HEIGHT)));
		desc.setHtml(toString(map.get(KEY_HTML)));
		desc.setRaw(convertRaw(map.get(KEY_RAW)));
		Description result = descService.save(desc);
		map.put("create_name", user.getUsername());
		String data;
		try {
			data = jsonConverter.convertToJson(map);
		} catch (IOException e) {
			throw new ApplicationException(
					ErrorCode.ERR_FAILURE_MAP_TO_JSON, map.toString());
		}
		message.setId(Integer.toString(result.getId()));
		message.setData(data);
		LOGGER.debug("processCreate end: " + result.getId());
	}

	private void processMove(WebSocketMessage message) throws ApplicationException {
		LOGGER.debug("processMove start:[" + message.getId() + "][" + message.getData() + "]");
		if (message.getId() == null) {
			throw new ApplicationException(ErrorCode.ERR_NO_ID_FOR_MOVING_DESCRPTION);
		}
		Map<String, Object> map = null;
		try {
			map = jsonConverter.convertToMap(message.getData());
		} catch (IOException ioe) {
			throw new ApplicationException(
					ErrorCode.ERR_FAILURE_JSON_TO_MAP, message.getData());
		}
		Integer id = objectToInteger(message.getId());
		Optional<Description> found = descService.findById(id);
		if (found.isPresent()) {
			Description target = found.get();
			target.setX(objectToDouble(map.get(KEY_X)));
			target.setY(objectToDouble(map.get(KEY_Y)));
			descService.save(target);
		} else {
			LOGGER.error(
					messageUtil.decode(ErrorCode.ERR_NO_RECORD_FOUND, id));
		}
		LOGGER.debug("processMove end:" + id);
	}

	private void processResize(WebSocketMessage message) throws ApplicationException {
		LOGGER.debug("processResize start:[" + message.getId() + "][" + message.getData() + "]");
		if (message.getId() == null) {
			throw new ApplicationException(ErrorCode.ERR_NO_ID_FOR_MOVING_DESCRPTION);
		}
		Map<String, Object> map = null;
		try {
			map = jsonConverter.convertToMap(message.getData());
		} catch (IOException ioe) {
			throw new ApplicationException(
					ErrorCode.ERR_FAILURE_JSON_TO_MAP, message.getData());
		}
		Integer id = objectToInteger(message.getId());
		Optional<Description> found = descService.findById(id);
		if (found.isPresent()) {
			Description target = found.get();
			target.setWidth(objectToDouble(map.get(KEY_WIDTH)));
			target.setHeight(objectToDouble(map.get(KEY_HEIGHT)));
			descService.save(target);
		} else {
			LOGGER.error(
					messageUtil.decode(ErrorCode.ERR_NO_RECORD_FOUND, id));
		}
		LOGGER.debug("processResize end:" + id);
	}

	private String convertRaw(Object obj) {
		String strRaw = "";
		if (obj != null) {
			strRaw = toString(obj);
			strRaw = strRaw.replaceAll("\r\n", "\\\\n");
			strRaw = strRaw.replaceAll("\n", "\\\\n");
		}
		return strRaw;
	}

	private String toString(Object obj) {
		return obj != null ? obj.toString() : null;
	}
}
