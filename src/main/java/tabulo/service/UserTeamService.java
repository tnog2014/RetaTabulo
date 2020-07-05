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

import tabulo.model.UserTeam;
import tabulo.model.UserTeamPK;
import tabulo.repository.UserTeamRepository;

@Service
@Transactional
public class UserTeamService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserTeamService.class);

	@Autowired
	UserTeamRepository userTeamRepository;

	/**
	 * レコードを全件取得する。
	 * @return
	 */
	public List<UserTeam> findAll() {
		return userTeamRepository.findAll();
	}

	public UserTeam insert(UserTeam entity) {
		return userTeamRepository.save(entity);
	}

	public List<UserTeam> findByUserId(Integer id) {
		return userTeamRepository.findByUserId(id);
	}

	public List<UserTeam> findByTeamId(Integer id) {
		return userTeamRepository.findByTeamId(id);
	}

	public Optional<UserTeam> findByUserIdTeamId(
			Integer userId,
			Integer teamId) {
		UserTeamPK pk = new UserTeamPK();
		pk.setTeamId(teamId);
		pk.setUserId(userId);
		return userTeamRepository.findById(pk);
	}

	public void deleteByUserIdTeamId(Integer userId, Integer teamId) {
		UserTeamPK pk = new UserTeamPK();
		pk.setTeamId(teamId);
		pk.setUserId(userId);
		userTeamRepository.deleteById(pk);
	}

	public UserTeam update(UserTeam entity) {
		return userTeamRepository.save(entity);
	}

	public void deleteByTeamId(Integer teamId) {
		userTeamRepository.deleteByTeamId(teamId);
	}

	public void deleteByUserId(Integer userId) {
		userTeamRepository.deleteByUserId(userId);
	}

}
