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

public final class ValidationMessageKey {

	public static final String VALIDATION_ONLY_0_1 = "{validation.only_0_1}";

	public static final String VALIDATION_ONLY_ASCII = "{validation.only_ascii}";

	public static final String VALIDATION_REQUIRED = "{validation.required}";

	public static final String VALIDATION_RANGE = "{validation.range}";

	public static final String VALIDATION_RANGE_MAX = "{validation.range_max}";

	public static final String VALIDATION_USERNAME_VALID = "{validation.username_valid}";

	public static final String VALIDATION_PASSWORDS_NOT_MATCH = "{validation.passwords_not_match}";

	public static final String REGEX_ASCII_CHARS = //
			"^[!\"#$%&'()*+,./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ"
					+ "\\\\\\[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~-]+$";

	public static final int USER_NAME_MAX = 16;

	public static final int USER_NAME_MIN = 4;

	public static final int USER_PASSWORD_MAX = 20;

	public static final int USER_PASSWORD_MIN = 8;

	public static final int USER_NICKNAME_MAX = 20;

	public static final int USER_NICKNAME_MIN = 1;

	public static final String USER_NAME_REGEX = "^[a-zA-Z0-9_.-]+$";

	public static final int TEAM_DESCRIPTION_MAX = 100;

	public static final int TEAM_NAME_MAX = 30;

	public static final int TEAM_NAME_MIN = 3;

	public static final int BOARD_DESCRIPTION_MAX = 100;

	public static final int BOARD_NAME_MAX = 30;

	public static final int BOARD_NAME_MIN = 3;

	private ValidationMessageKey() {

	}

}
