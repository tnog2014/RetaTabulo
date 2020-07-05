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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import tabulo.CustomUser;
import tabulo.MessageUtil;
import tabulo.constant.ErrorCode;
import tabulo.dao.LoginUserDao;
import tabulo.model.LoginUser;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

	@Autowired
	private LoginUserDao userDao;

	@Autowired
	private UserService userService;

	@Autowired
	private MessageUtil messageUtil;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {

		LoginUser user = null;
		String errorMessage = messageUtil.decode(ErrorCode.ERR_USER_NOT_FOUND, userName);
		try {
			user = userDao.findUser(userName);
		} catch (Exception e) {
			// The trials to log in with a wrong username must be recorded at the INFO level.
			LOGGER.info(errorMessage);
			throw new UsernameNotFoundException(errorMessage);
		}
		if (user == null) {
			throw new UsernameNotFoundException(errorMessage);
		}

		CustomUser userDetails = userService.convertToCustomUser(user);
		return userDetails;
	}

}
