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
package tabulo.constant;

public final class MappingPath {

	public static final String LOGIN = "/login";

	public static final String MAIN = "/main";

	public static final String USER_LIST = "/user";

	public static final String USER_SHOW = "/user/";

	public static final String USER_CREATE = "/user/create";

	public static final String USER_EDIT = "/user/edit/";

	public static final String USER_EDIT_INNER = "/user/edit";

	public static final String USER_DELETE = "/user/delete/";

	public static final String TEAM_LIST = "/team";

	public static final String TEAM_SHOW = "/team/";

	public static final String TEAM_CREATE = "/team/create";

	public static final String TEAM_EDIT = "/team/edit/";

	public static final String TEAM_EDIT_INNER = "/team/edit";

	public static final String TEAM_DELETE = "/team/delete/";

	public static final String BOARD_LIST = "/board";

	public static final String BOARD_SHOW = "/board/";

	public static final String BOARD_OPEN = "/board/open/";

	public static final String BOARD_CREATE = "/board/create";

	public static final String BOARD_EDIT = "/board/edit/";

	public static final String BOARD_EDIT_INNER = "/board/edit";

	public static final String BOARD_DELETE = "/board/delete/";

	public static final String REDIRECT_MAIN = "redirect:/main";

	public static final String REDIRECT_USER = "redirect:/user";

	public static final String REDIRECT_USER_EDIT = "redirect:/user/edit/";

	public static final String REDIRECT_USER_CREATE = "redirect:/user/create";

	public static final String REDIRECT_TEAM = "redirect:/team";

	public static final String REDIRECT_TEAM_EDIT = "redirect:/team/edit/";

	public static final String REDIRECT_TEAM_CREATE = "redirect:/team/create";

	public static final String REDIRECT_BOARD = "redirect:/board";

	public static final String REDIRECT_BOARD_EDIT = "redirect:/board/edit/";

	public static final String REDIRECT_BOARD_CREATE = "redirect:/board/create";

	/** Regex for allowed boardId identifiers. */
	public static final String REGEX_BOARD_ID = "{boardId:[0-9]+}";

	/** Regex for allowed teamId identifiers. */
	public static final String REGEX_TEAM_ID = "{teamId:[0-9]+}";

	/** Regex for allowed userId identifiers. */
	public static final String REGEX_USER_ID = "{userId:[0-9]+}";

	private MappingPath() {

	}
}
