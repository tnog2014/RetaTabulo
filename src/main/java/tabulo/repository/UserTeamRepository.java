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
package tabulo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tabulo.model.UserTeam;
import tabulo.model.UserTeamPK;

@Repository
public interface UserTeamRepository extends JpaRepository<UserTeam, UserTeamPK> {

	List<UserTeam> findByUserId(Integer userId);

	List<UserTeam> findByTeamId(Integer teamId);

	void deleteByTeamId(Integer teamId);

	void deleteByUserId(Integer userId);

}
