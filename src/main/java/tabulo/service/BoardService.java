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

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tabulo.model.Board;
import tabulo.model.Team;
import tabulo.repository.BoardRepository;

@Service
@Transactional
public class BoardService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BoardService.class);

	@Autowired
	BoardRepository boardRepository;

	/**
	 * レコードを全件取得する。
	 * @return
	 */
	public List<Board> findAll() {
		return boardRepository.findAll(new Sort(Direction.ASC, "name"));
	}

	public Board insert(Board entity) {
		return boardRepository.save(entity);
	}

	public void deleteById(Integer id) {
		boardRepository.deleteById(id);
	}

	public Optional<Board> findById(Integer id) {
		return boardRepository.findById(id);
	}

	public Board update(Board entity) {
		return boardRepository.save(entity);
	}

	public List<Board> findByName(String name) {
		return boardRepository.findByName(name);
	}

	public List<Board> findByTeam(Team team) {
		return boardRepository.findByTeam(team);
	}

}
