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
package tabulo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tabulo.JsonConverter;
import tabulo.model.Description;
import tabulo.model.DescriptionWrapper;
import tabulo.model.WebSocketMessage;
import tabulo.repository.DescriptionRepository;

@Service
@Transactional
public class DescriptionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DescriptionService.class);

	/** SQL for getting descriptions with user info. */
	private static final String SQL_SELECT_DESCRIPTION = "select d.id, d.board_id, "
			+ "d.create_user, d.update_user, "
			+ "cu.user_name as create_name, uu.user_name as update_name, "
			+ "d.x, d.y, d.width, d.height, d.raw, d.html from description d "
			+ "left join login_user cu on d.create_user = cu.user_id "
			+ "left join login_user uu on d.update_user = uu.user_id "
			+ "where d.board_id = ?";

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	DescriptionRepository descRepository;

	private JsonConverter<WebSocketMessage> jsonConverter = new JsonConverter<WebSocketMessage>();

	/**
	 * レコードを全件取得する。
	 * @return
	 */
	public List<Description> findAllDescriptionData() {
		return descRepository.findAll();
	}

	public Optional<Description> findById(Integer id) {
		return descRepository.findById(id);
	}

	public Description save(Description target) {
		return descRepository.save(target);
	}

	public void deleteById(Integer id) {
		descRepository.deleteById(id);
	}

	public void deleteByBoard(Integer id) {
		descRepository.deleteByBoardId(id);
	}

	public List<DescriptionWrapper> createDescriptionWrappers(Integer boardId) {
		Object[] params = new Object[]{boardId};
		List<Map<String, Object>> list = jdbcTemplate.queryForList(SQL_SELECT_DESCRIPTION, params);
		List<DescriptionWrapper> descDataList = new ArrayList<DescriptionWrapper>();
		for (Map<String, Object> descMap : list) {
			DescriptionWrapper w = new DescriptionWrapper();
			w.setId((Integer) descMap.get("id"));
			w.setBoardId((Integer) descMap.get("board_id"));
			w.setCreate_user((Integer) descMap.get("create_user"));
			w.setUpdate_user((Integer) descMap.get("update_user"));

			w.setCreate_name((String) descMap.get("create_name"));
			w.setUpdate_name((String) descMap.get("update_name"));
			w.setX((Float) descMap.get("x"));
			w.setY((Float) descMap.get("y"));
			w.setWidth((Float) descMap.get("width"));
			w.setHeight((Float) descMap.get("height"));
			w.setRaw((String) descMap.get("raw"));
			w.setHtml((String) descMap.get("html"));
			descDataList.add(w);
		}
		return descDataList;
	}
}
