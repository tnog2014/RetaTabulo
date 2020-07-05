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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import tabulo.ApplicationException;
import tabulo.ControllerUtil;
import tabulo.CustomUser;
import tabulo.constant.CommonConst;
import tabulo.constant.ErrorCode;
import tabulo.form.TeamForm;
import tabulo.model.Board;
import tabulo.model.LoginUser;
import tabulo.model.Team;
import tabulo.model.UserTeam;
import tabulo.service.BoardService;
import tabulo.service.TeamService;
import tabulo.service.UserService;
import tabulo.service.UserTeamService;

// TODO: 内部でServiceクラスをAutowiredしているクラスの呼び出し方はどうするのが良いか？暫定的にControllerにしているが。
@Controller
public class AuthorizationUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationUtil.class);

	@Autowired
	BoardService boardService;

	@Autowired
	UserService userService;

	@Autowired
	TeamService teamService;

	@Autowired
	UserTeamService userTeamService;

	public boolean isTeamAvailable(
			CustomUser loginUser,
			Integer teamId,
			boolean admin) {
		LOGGER.debug("チーム権限チェック:[" + loginUser.getUsername() + "][" + teamId + "],管理権限要求[" + admin
				+ "]");
		if (ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN)) {
			LOGGER.debug("全体管理権限あり -> true");
			return true;
		}
		Integer userId = loginUser.getUserId();

		Optional<UserTeam> found = userTeamService.findByUserIdTeamId(userId, teamId);
		if (!found.isPresent()) {
			return false;
		}
		if (admin && !"1".equals(found.get().getAuth())) {
			return false;
		}
		if (admin) {
			LOGGER.debug("対象チームに対する管理権限あり -> true");
		} else {
			LOGGER.debug("対象チームに対するアクセス権限あり -> true");
		}
		return true;
	}

	public boolean isTeamLeader(CustomUser loginUser) {
		List<Team> teamsUnderControl = getTeamsUnderControl(loginUser);
		return teamsUnderControl.size() > 0;
	}

	public boolean isAdminOrTeamLeader(CustomUser loginUser) {
		LOGGER.debug("isAdminOrTeamLeader:start");
		boolean ret = false;
		if (ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN)) {
			ret = true;
		} else {
			ret = isTeamLeader(loginUser);
		}
		LOGGER.debug("isAdminOrTeamLeader:end:ret=[" + ret + "]");
		return ret;
	}

	public boolean isBoardAvailable(
			CustomUser loginUser,
			Integer boardId,
			boolean admin)
			throws ApplicationException {
		LOGGER.debug("ボード権限チェック:[" + loginUser.getUsername() + "][" + boardId + "]");
		Optional<Board> board = boardService.findById(boardId);
		if (!board.isPresent()) {
			throw new ApplicationException(ErrorCode.ERR_BOARD_NOT_FOUND, boardId);
		}

		if (ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN)) {
			LOGGER.debug("全体管理権限あり -> true");
			return true;
		}

		Integer teamId = board.get().getTeam().getId();

		return isTeamAvailable(loginUser, teamId, admin);
	}

	public List<TeamForm> getTeamFormsUnderControl(CustomUser loginUser) {
		List<Team> teamsUnderControl = getTeamsUnderControl(loginUser);
		return ControllerUtil.teamsToForms(teamsUnderControl);
	}

	public List<Team> getTeamsUnderControl(CustomUser loginUser) {
		List<Team> ret = null;
		if (ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN)) {
			ret = teamService.findAll();
		} else {
			// 全体管理者でない場合は管理権限を持つチームを返却リストに追加
			List<UserTeam> userTeams = userTeamService.findByUserId(loginUser.getUserId());
			ret = new ArrayList<Team>();
			for (UserTeam ut : userTeams) {
				if ("1".equals(ut.getAuth())) {
					Optional<Team> team = teamService.findById(ut.getTeamId());
					if (team.isPresent()) {
						ret.add(team.get());
					}
				}
			}
		}
		return ret;
	}

	public List<Integer> getTeamIdsUnderControl(CustomUser loginUser) {
		List<Team> teams = getTeamsUnderControl(loginUser);
		List<Integer> ret = new ArrayList<Integer>();
		for (Team t : teams) {
			ret.add(t.getId());
		}
		return ret;
	}

	/**
	 * ログインユーザーの管理下のユーザーリストを取得する。
	 * ・ログインユーザーが全体管理者の場合は、全ユーザー。
	 * ・ログインユーザーがチーム管理者の場合、
	 * 　管理対象チームに属するユーザーのうち、
	 * 　他のチームの管理権権を持たないユーザー。
	 * ・ログインユーザーが一般ユーザーの場合、自分自身のみ。
	 *
	 * @param loginUser
	 * @return
	 */
	public List<LoginUser> getUsersUnderControl(CustomUser loginUser) {
		LOGGER.debug("getUsersUnderControl start:userId=[" + loginUser.getUserId() + "]");
		List<LoginUser> users = null;
		if (ControllerUtil.hasRole(loginUser, CommonConst.ROLE_ADMIN)) {
			users = userService.findAll();
		} else {

			// 管理権限を持つチームの取得
			List<Integer> teamIds = getTeamIdsUnderControl(loginUser);

			// 何らかのチームの管理権限を持つユーザーのリスト
			Set<Integer> userIdsWhoHasAuthOverSomeTeams = new HashSet<Integer>();

			List<Integer> userIds1 = new ArrayList<Integer>();

			List<UserTeam> allUserTeams = userTeamService.findAll();
			for (UserTeam ut : allUserTeams) {
				if ("1".equals(ut.getAuth())) {
					userIdsWhoHasAuthOverSomeTeams.add(ut.getUserId());
				}
				// ログインユーザーの管理下のチームに対して参照権限をもつユーザーを集める。
				if (teamIds.contains(ut.getTeamId())) {
					userIds1.add(ut.getUserId());
				}
			}

			// 参照権限を持つユーザーリストから、管理権限保有者を除去
			List<LoginUser> tmpUsers = userService.findAll();
			users = new ArrayList<LoginUser>();
			for (LoginUser user : tmpUsers) {
				boolean isAdmin = "1".equals(user.getLevel());
				Integer userId = user.getId();
				// 自分自身のIDは追加する。
				if (userId.equals(loginUser.getUserId())) {
					users.add(user);
				} else {
					// Add the users satisfying the following conditions:
					// 1) not full-admin
					// 2) not team admin.
					// 3) with the reference right for at least one of teams under the control.
					if (!isAdmin && !userIdsWhoHasAuthOverSomeTeams.contains(userId)
							&& userIds1.contains(userId)) {
						users.add(user);
					}
				}
			}
		}
		LOGGER.debug("getUsersUnderControl end:[" + users + "]");
		return users;
	}

	public List<Integer> getUserIdsUnderControl(
			CustomUser loginUser) {
		List<LoginUser> users = getUsersUnderControl(loginUser);
		List<Integer> ret = new ArrayList<Integer>();
		for (LoginUser t : users) {
			ret.add(t.getId());
		}
		return ret;
	}

	public List<Team> getTeams(
			CustomUser loginUser,
			boolean onlyAdmin) {
		List<UserTeam> userTeams = userTeamService.findByUserId(loginUser.getUserId());
		List<Team> teams = new ArrayList<Team>();
		for (UserTeam ut : userTeams) {
			// 管理権限を持つチームの場合のみ処理を行う。
			if (onlyAdmin && !"1".equals(ut.getAuth())) {
				continue;
			}
			Optional<Team> team = teamService.findById(ut.getTeamId());
			if (team.isPresent()) {
				teams.add(team.get());
			} else {
				throw new RuntimeException("想定するチームIDが存在しません[" + ut.getTeamId() + "]");
			}
		}
		return teams;
	}

	public boolean canLoginUserModifyOtherUser(
			CustomUser loginUser,
			Integer targetUserId) {
		LOGGER.debug(
				"canLoginUserModifyOtherUser:" + loginUser.getUsername() + "->" + targetUserId);

		List<Integer> userIdsUnderControl = getUserIdsUnderControl(loginUser);
		boolean ret = userIdsUnderControl.contains(targetUserId);
		LOGGER.debug("canLoginUserModifyOtherUser: result=" + ret);
		return ret;
	}

}
