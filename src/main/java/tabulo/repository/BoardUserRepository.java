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

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import tabulo.model.BoardUser;

@Repository
public interface BoardUserRepository extends JpaRepository<BoardUser, Integer> {

	String SQL_DELETE_NOT_UPDATED_ROWS = //
			"DELETE FROM board_user AS p "
					+ "WHERE p.last_update_date <= :date";

	String SQL_SELECT_BY_USER_ID_AND_SUB_ID = //
			"SELECT * FROM board_user AS p "
					+ "WHERE p.user_id = :userId and p.sub_id = :subId";

	String SQL_DELETE_BY_USER_ID_AND_SUB_ID = //
			"DELETE FROM board_user AS p "
					+ "WHERE p.user_id = :userId and p.sub_id = :subId";

	List<BoardUser> findByBoardId(Integer boardId);

	@Modifying
	@Query(
			value = SQL_DELETE_BY_USER_ID_AND_SUB_ID,
			nativeQuery = true)
	void deleteByUserIdSubId(Integer userId, String subId);

	@Query(
			value = SQL_SELECT_BY_USER_ID_AND_SUB_ID,
			nativeQuery = true)
	Optional<BoardUser> findByUserIdSubId(Integer userId, String subId);

	@Modifying
	@Query(
			value = SQL_DELETE_NOT_UPDATED_ROWS,
			nativeQuery = true)
	void deleteNotUpdatedRows(Date date);

}
