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
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tabulo.CustomUser;
import tabulo.MessageUtil;
import tabulo.constant.CommonConst;
import tabulo.constant.ErrorCode;
import tabulo.model.LoginUser;
import tabulo.repository.UserRepository;

@Service
@Transactional
public class UserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

	@Autowired
	UserRepository userRepository;

	@Autowired
	private MessageUtil messageUtil;

	/**
	 * レコードを全件取得する。
	 * @return
	 */
	public List<LoginUser> findAll() {
		return userRepository.findAll(new Sort(Direction.ASC, "userName"));
	}

	public LoginUser insert(LoginUser entity) {
		return userRepository.save(entity);
	}

	public void deleteById(Integer id) {
		userRepository.deleteById(id);
	}

	public Optional<LoginUser> findById(Integer id) {
		return userRepository.findById(id);
	}

	public List<LoginUser> findByUserName(String name) {
		return userRepository.findByUserName(name);
	}

	public LoginUser update(LoginUser entity) {
		return userRepository.save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	public UserDetails loadUserById(Integer userId) throws UsernameNotFoundException {
		Optional<LoginUser> optional = null;
		String errorMessage = messageUtil.decode(ErrorCode.ERR_USER_NOT_FOUND, userId);
		try {
			optional = findById(userId);
		} catch (Exception e) {
			// The trials to log in with a wrong username must be recorded at the INFO level.
			LOGGER.info(errorMessage);
			throw new UsernameNotFoundException(errorMessage);
		}
		if (!optional.isPresent()) {
			throw new UsernameNotFoundException(errorMessage);
		}
		CustomUser userDetails = convertToCustomUser(optional.get());
		return userDetails;
	}

	public CustomUser convertToCustomUser(LoginUser user) {
		List<GrantedAuthority> grantList = new ArrayList<GrantedAuthority>();
		String granted = CommonConst.ROLE_USER;
		if ("1".equals(user.getLevel())) {
			granted = CommonConst.ROLE_ADMIN;
		}
		GrantedAuthority authority = new SimpleGrantedAuthority(granted);
		grantList.add(authority);

		// Return a CustomUser object as UserDetails
		// so that one can get extended user infomation from SecurityContext.
		CustomUser userDetails = new CustomUser(user.getUserName(), user.getPassword(), grantList,
				user.getId());
		userDetails.setEnabled("1".equals(user.getValid()));
		return userDetails;
	}
}
