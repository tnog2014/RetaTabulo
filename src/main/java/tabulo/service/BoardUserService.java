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

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tabulo.model.BoardUser;
import tabulo.repository.BoardUserRepository;

@Service
@Transactional
public class BoardUserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BoardUserService.class);

	@Autowired
	BoardUserRepository boardUserRepository;

	/**
	 * レコードを全件取得する。
	 * @return
	 */
	public List<BoardUser> findByBoardId(Integer boardId) {
		return boardUserRepository.findByBoardId(boardId);
	}

	public BoardUser insert(BoardUser entity) {
		return boardUserRepository.save(entity);
	}

	public void deleteById(Integer id) {
		boardUserRepository.deleteById(id);
	}

	public void deleteByUserIdSubId(Integer userId, String subId) {
		boardUserRepository.deleteByUserIdSubId(userId, subId);
	}

	public Optional<BoardUser> findByUserIdSubId(Integer userId, String subId) {
		return boardUserRepository.findByUserIdSubId(userId, subId);
	}

	public BoardUser update(BoardUser boardUser) {
		return boardUserRepository.save(boardUser);
	}

	public void deleteNotUpdatedRows(Date date) {
		boardUserRepository.deleteNotUpdatedRows(date);
	}

}
