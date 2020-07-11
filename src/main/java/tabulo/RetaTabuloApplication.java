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
package tabulo;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import tabulo.model.LoginUser;
import tabulo.service.UserService;

/**
 * SpringBootApplication class.
 *
 * This class must have a public constructor.
 *
 * @author tnog2014
 */
@SpringBootApplication
public class RetaTabuloApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(RetaTabuloApplication.class);

	private static final int DEFAULT_ADMIN_NAME_SUFFIX_LENGTH = 4;

	private static final int DEFAULT_ADMIN_PASSWORD_LENGTH = 12;

	private static final String DEFUALT_ADMIN_USER_NAME = "admin_%s";

	private static final String DEFAULT_ADMIN_NICKNAME = "Administrator";

	private static final String LOGIN_INFO_FILE = "UserName: %s\r\nPassword: %s\r\n";

	@Autowired
	UserService userService;

	@Autowired
	URIEncodeConverter conv;

	public static void main(String[] args) throws ApplicationException {
		ConfigurableApplicationContext ctx = SpringApplication.run(
				RetaTabuloApplication.class, args);
		RetaTabuloApplication app = ctx.getBean(RetaTabuloApplication.class);
		app.execStartup(args);
	}

	public void execStartup(String[] args) throws ApplicationException {
		createAdminUser();
		// Convert descriptions of anold format into a new format.
		conv.convert();
	}

	/**
	 * Create a new admin user.
	 *
	 * If the user table is vacant, create a new admin user with a random password.
	 * The user name and the password is written in the following file:
	 *
	 *   LoginInfo_yyyyMMdd_HHmmss.
	 *
	 */
	private void createAdminUser() {
		List<LoginUser> users = userService.findAll();
		if (users.size() == 0) {
			LOGGER.info("====================================================");
			LOGGER.info("No user found. Create a new admin user.");
			String userNameSuffix = RandomStringUtils.randomAlphanumeric(
					DEFAULT_ADMIN_NAME_SUFFIX_LENGTH);
			String userName = String.format(DEFUALT_ADMIN_USER_NAME, userNameSuffix);
			String password = RandomStringUtils.randomAlphanumeric(
					DEFAULT_ADMIN_PASSWORD_LENGTH);

			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			String encodedPassword = encoder.encode(password);
			String contents = String.format(LOGIN_INFO_FILE, userName, password);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
			String fileName = String.format("LoginInfo_%s", sdf.format(new Date()));
			File file = new File(fileName);
			try {
				FileUtils.writeStringToFile(file, contents, "UTF8");
				LOGGER.info(String.format("Create a LoginInfo file: %s", file.getAbsolutePath()));

				LoginUser user = new LoginUser();
				user.setUserName(userName);
				user.setLevel("1");
				user.setNickname(DEFAULT_ADMIN_NICKNAME);
				user.setValid("1");
				user.setPassword(encodedPassword);

				//Insert a new admin user
				userService.insert(user);

				LOGGER.info("====================================================");
			} catch (IOException e) {
				LOGGER.error("Failure in creating a LoginInfo file", e);
				LOGGER.info("====================================================");
			}
		} else {
			LOGGER.info("Do not create any admin user.");
		}

	}
}
