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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.core.JsonProcessingException;

import tabulo.ApplicationException;
import tabulo.ControllerUtil;
import tabulo.CustomUser;
import tabulo.constant.CommonConst;
import tabulo.constant.ErrorCode;
import tabulo.constant.MappingPath;
import tabulo.model.Board;
import tabulo.model.DescriptionWrapper;
import tabulo.service.BoardService;
import tabulo.service.DescriptionService;
import tabulo.template.ThymeleafText;

@Controller
public class DownloadController {

	private static final String DOWNLOAD_CSV = "/download/csv/";

	private static final String DOWNLOAD_HTML = "/download/html/";

	private static final String TEMPLATE_KEY_DESCS = "descs";

	private static final String TEMPLATE_KEY_BOARD_NAME = "boardName";

	private static final String COMMA = ",";

	private static final String DESC_TEMPLATE_IN_HTML = "{id:'%s', create_name: %s, update_name: %s, x: %s, y: %s, width: %s, height: %s, html:'%s'}";

	private static final String PAREN_OPEN = "[";

	private static final String PAREN_CLOSE = "]";

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	DownloadHelper downloadHelper;

	@Autowired
	DescriptionService descService;

	@Autowired
	BoardService boardService;

	@Autowired
	AuthorizationUtil authUtil;

	@Autowired
	ThymeleafText thymeleafText;

	/**
	 * Create a CSV.
	 * @param id a board id
	 * @return csv(String)
	 * @throws JsonProcessingException exception
	 */
	public String getCsvText(Integer id) throws JsonProcessingException {
		List<DescriptionWrapper> descs = descService.createDescriptionWrappers(id);
		StringBuffer sb = new StringBuffer();
		for (DescriptionWrapper desc : descs) {
			sb.append(desc.toTSV());
		}
		return sb.toString();
	}

	/**
	 * Create a HTML.
	 *
	 * @param boardId a board id
	 * @param boardName a board name
	 * @return html(String)
	 * @throws JsonProcessingException exception
	 */
	public String getHtml(Integer boardId, String boardName)
			throws JsonProcessingException {
		List<DescriptionWrapper> descs = descService.createDescriptionWrappers(boardId);
		StringBuffer sb = new StringBuffer();
		sb.append(PAREN_OPEN).append(CommonConst.CRLF);
		for (int i = 0; i < descs.size(); i++) {
			DescriptionWrapper desc = descs.get(i);
			sb.append(desc.toJSONForHTML());
			if (i < descs.size() - 1) {
				sb.append(COMMA)
						.append(CommonConst.CRLF);
			}
		}
		sb.append(PAREN_CLOSE).append(CommonConst.CRLF);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put(TEMPLATE_KEY_BOARD_NAME, boardName);
		params.put(TEMPLATE_KEY_DESCS, sb.toString());
		String text = thymeleafText.process("board.html", params);
		return text;
	}

	/**
	 * Download a CSV file.
	 *
	 * @param model a model
	 * @param boardId a board id
	 * @return
	 * @throws ApplicationException
	 */
	@RequestMapping(
			path = DOWNLOAD_CSV + MappingPath.REGEX_BOARD_ID,
			method = RequestMethod.GET)
	public ResponseEntity<byte[]> downloadCSV(
			Model model,
			@PathVariable int boardId)
			throws ApplicationException {

		CustomUser loginUser = ControllerUtil.getLoginUser();
		// ボードIDの存在チェックと権限チェックを行う。
		if (!authUtil.isBoardAvailable(loginUser, boardId, false)) {
			throw new ApplicationException(
					ErrorCode.ERR_CANNOT_DOWNLOAD_CSV, boardId);
		}

		String boardName0 = getBoardName(boardId);
		String boardName = convertToSafeName(boardName0);
		HttpHeaders headers = new HttpHeaders();
		try {
			downloadHelper.addContentDisposition(headers, "RT_" + boardName + ".csv");
			return new ResponseEntity<>(getCsvText(boardId).getBytes("MS932"), headers,
					HttpStatus.OK);
		} catch (Exception e) {
			throw new ApplicationException(e, ErrorCode.ERR_FAILURE_IN_SENDING_A_CSV_FILE, boardId);
		}
	}

	/**
	 * Down load a HTML file.
	 * @param model a model
	 * @param boardId a boardId
	 * @return
	 * @throws ApplicationException
	 */
	@RequestMapping(
			path = DOWNLOAD_HTML + MappingPath.REGEX_BOARD_ID,
			method = RequestMethod.GET)
	public ResponseEntity<byte[]> downloadHtml(
			Model model,
			@PathVariable int boardId)
			throws ApplicationException {
		// 権限チェック
		CustomUser loginUser = ControllerUtil.getLoginUser();
		// Check the accessibility to the board.
		if (!authUtil.isBoardAvailable(loginUser, boardId, false)) {
			throw new ApplicationException(
					ErrorCode.ERR_CANNOT_DOWNLOAD_HTML, boardId);
		}

		String boardName0 = getBoardName(boardId);
		String boardName = convertToSafeName(boardName0);
		HttpHeaders headers = new HttpHeaders();
		try {
			downloadHelper.addContentDisposition(headers, "RT_" + boardName + ".html");
			return new ResponseEntity<>(getHtml(boardId, boardName).getBytes("UTF8"), headers,
					HttpStatus.OK);
		} catch (Exception e) {
			throw new ApplicationException(e, ErrorCode.ERR_FAILURE_IN_SENDING_A_HTML_FILE,
					boardId);
		}
	}

	// TODO: convertToSafeName is not completed (how to convert a board name to a valid file name).
	private String convertToSafeName(String name) {
		String ret = name;
		ret = ret.trim();
		ret = ret.replaceAll("\\s+", "_");
		ret = ret.replaceAll("[/／\\\\]", "_");
		return ret;

	}

	private String getBoardName(Integer boardId) throws ApplicationException {
		Optional<Board> optional = boardService.findById(boardId);
		String boardName = null;
		if (optional.isPresent()) {
			boardName = optional.get().getName();
		} else {
			throw new ApplicationException(ErrorCode.ERR_BOARD_NOT_FOUND, boardId);
		}
		return boardName;
	}
}
