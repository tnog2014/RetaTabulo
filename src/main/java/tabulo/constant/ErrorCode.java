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

public enum ErrorCode {

	//------------------------------------
	// 画面表示時の権限エラー
	//------------------------------------
	ERR_CANNOT_OPEN_USER_LIST,
	ERR_CANNOT_OPEN_USER_CREATE_VIEW,
	ERR_CANNOT_OPEN_USER_EDIT_VIEW,
	ERR_NON_ADMIN_CANNOT_OPEN_BOARD_LIST_VIEW,
	ERR_NON_ADMIN_CANNOT_CREATE_BOARD,
	ERR_CANNOT_DOWNLOAD_HTML,
	ERR_CANNOT_DOWNLOAD_CSV,
	ERR_CANNOT_OPEN_BOARD_VIEW,
	ERR_CANNOT_SHOW_TEAM_LIST,
	ERR_CANNOT_OPEN_TEAM_CREATE_VIEW,

	//------------------------------------
	// DB関連のエラー
	//------------------------------------
	ERR_USER_NOT_FOUND,
	ERR_EDIT_USER_NOT_FOUND,
	ERR_NO_TEAM_TO_BE_EDITED,
	ERR_TEAM_NOT_FOUND,
	ERR_NO_RECORD_FOUND,
	ERR_BOARD_NOT_FOUND,

	//------------------------------------
	// 送信された値が不正
	//------------------------------------
	ERR_FAILURE_JSON_TO_MAP,
	ERR_FAILURE_MAP_TO_JSON,
	ERR_NO_ID_FOR_MOVING_DESCRPTION,
	ERR_NO_ID_FOR_RESIZING_DESCRPTION,
	ERR_NO_ID_FOR_UPDATING_DESCRPTION,
	ERR_NO_ID_FOR_DELETING_DESCRPTION,
	ERR_UNEXPECTED_MESSAGE_TYPE,
	ERR_BOARD_ID_WAS_NULL,
	ERR_NO_BOARD_ID_FOR_REGISTRATION,

	//------------------------------------
	// 処理実行時の権限エラー
	//------------------------------------
	ERR_NON_FULL_ADMIN_CANNOT_CREATE_FULL_ADMIN,
	ERR_CANNOT_CHANGE_TEAM_ASSIGNMENTS,
	ERR_CANNOT_GIVE_REF_RIGHT,
	ERR_CANNOT_GIVE_ADMIN_AUTHORITY,
	ERR_CANNOT_DEPRIVE_ADMIN_AUTHORITY,
	ERR_CANNOT_DEPRIVE_REF_RIGHT,
	ERR_CANNOT_EDIT_USER,
	ERR_ONLY_FULL_ADMIN_CAN_EDIT_FULL_ADMIN,
	ERR_CANNOT_DELETE_USER_NOT_UNDER_CONTROL,
	ERR_NON_FULL_ADMIN_CANNOT_DELETE_TEAM,
	ERR_CANNOT_EDIT_TEAM_NOT_UNDER_CONTROL,
	ERR_NON_FULL_ADMIN_CANNOT_CREATE_TEAM,
	ERR_CANNOT_EDIT_BOARD,
	ERR_CANNOT_DELETE_BOARD,
	ERR_CANNOT_CREATE_BOARD_BELONGING_TO_TEAM,
	ERR_CANNOT_ACCESS_BOARD,

	//------------------------------------
	// その他のエラー
	//------------------------------------
	ERR_UNEXPECTED_USER_OBJ,
	ERR_UNEXPECTED_PRINCIPAL,
	ERR_FAILURE_IN_SENDING_A_CSV_FILE,
	ERR_FAILURE_IN_SENDING_A_HTML_FILE,

}
