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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tabulo.model.Team;
import tabulo.repository.TeamRepository;

@Service
@Transactional
public class TeamService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TeamService.class);

	@Autowired
	TeamRepository teamRepository;

	/**
	 * レコードを全件取得する。
	 * @return
	 */
	public List<Team> findAll() {
		return teamRepository.findAll();
	}

	public Team insert(Team entity) {
		return teamRepository.save(entity);
	}

	public void deleteById(Integer id) {
		teamRepository.deleteById(id);
	}

	public Optional<Team> findById(Integer id) {
		return teamRepository.findById(id);
	}

	public List<Team> findByName(String name) {
		return teamRepository.findByName(name);
	}

	public Team update(Team entity) {
		return teamRepository.save(entity);
	}

}
