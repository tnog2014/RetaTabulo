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
package tabulo.dao;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import tabulo.model.LoginUser;

@Repository
public class LoginUserDao {

	@Autowired
	EntityManager em;

	/**
	 *
	 * @param userName
	 * @return
	 */
	public LoginUser findUser(String userName) {
		String query = "SELECT * FROM login_user WHERE user_name = :userName";
		return (LoginUser) em.createNativeQuery(query, LoginUser.class)
				.setParameter("userName", userName)
				.getSingleResult();
	}

}
