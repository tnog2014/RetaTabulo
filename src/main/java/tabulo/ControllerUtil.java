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
package tabulo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.zip.Checksum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;

import tabulo.constant.CommonConst;
import tabulo.form.BoardForm;
import tabulo.form.TeamForm;
import tabulo.model.Board;
import tabulo.model.Team;

public final class ControllerUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerUtil.class);

	/** encoding for taking a checksum. */
	private static final String CHECKSUM_ENCODING = "UTF8";

	private static final Comparator<NameHolder> COMP_BY_NAME = new Comparator<NameHolder>() {

		@Override
		public int compare(NameHolder o1, NameHolder o2) {
			return o1.getName().compareTo(o2.getName());
		}

	};

	private ControllerUtil() {

	}

	public static CustomUser getLoginUser() {
		Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CustomUser principal = null;
		if (obj instanceof CustomUser) {
			principal = (CustomUser) obj;
		}
		return principal;
	}

	public static String getLoginUserName() {
		return getLoginUser().getUsername();
	}

	public static boolean hasRole(CustomUser user, String role) {
		for (GrantedAuthority ga : user.getAuthorities()) {
			if (role.equals(ga.getAuthority())) {
				return true;
			}
		}
		return false;
	}

	public static BoardForm boardToForm(Board b) {
		BoardForm bf = new BoardForm();
		bf.setId(b.getId());
		bf.setTeamId(b.getTeam().getId());
		bf.setName(b.getName());
		bf.setDescription(b.getDescription());
		return bf;
	}

	public static TeamForm teamToForm(Team team) {
		TeamForm form = new TeamForm();
		form.setId(team.getId());
		form.setName(team.getName());
		form.setDescription(team.getDescription());
		return form;
	}

	public static List<TeamForm> teamsToForms(List<Team> teams) {
		List<TeamForm> teamForms = new ArrayList<TeamForm>();
		for (Team team : teams) {
			TeamForm form = teamToForm(team);
			teamForms.add(form);
		}
		return teamForms;
	}

	public static void sort(List<? extends NameHolder> boardForms) {
		Collections.sort(boardForms, COMP_BY_NAME);
	}

	public static void preProcess(Model model) {
		Locale locale = LocaleContextHolder.getLocale();
		model.addAttribute(CommonConst.LOCALE_KEY, locale.getLanguage());
	}

	public static String getCRC32HexString(String paramString) throws Exception {
		byte[] bytes = paramString.getBytes(CHECKSUM_ENCODING);
		Checksum checksum = new java.util.zip.CRC32();
		checksum.update(bytes, 0, bytes.length);
		long checksumValue = checksum.getValue();
		return Long.toHexString(checksumValue).toUpperCase();
	};
}
