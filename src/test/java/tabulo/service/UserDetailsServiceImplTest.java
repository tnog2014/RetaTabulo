package tabulo.service;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import mockit.Deencapsulation;
import tabulo.MessageUtil;
import tabulo.constant.ErrorCode;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserDetailsServiceImplTest {

	@Autowired
	private WebApplicationContext webApplicationContext;
	private MockMvc mockMvc;
	//
	//	@Autowired
	//	private UserService userService;
	//
	//	@Autowired
	//	private TeamService teamService;

	@Autowired
	private UserDetailsServiceImpl service;

	@Autowired
	private MessageUtil messageUtil;

	@Before
	public void setUp() {
		System.out.println("★セットアップ");
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		//		List<Map<String, Object>> actualData = this.jdbcTemplate.queryForList("select * from login_user");
		//		System.out.println("★" + actualData);
		clearTables();
	}

	private String[][] CLEAR_SQLS = new String[][]{
			{"login_user", "delete from login_user"},
			{"description", "delete from description"},
			{"board", "delete from board"},
			{"team", "delete from team"},
			{"userteam", "delete from userteam"},
			{"login_user", "insert into login_user "
					+ "(user_id,user_name, password, nickname, valid, level)"
					+ " values (1, 'admin', '$2a$10$2a6LgH1pZj3s0ZMAqJafW.Cd8mceQgBK.nR8FHA6yK8tM09SBJjUm', 'ADMIN', 1, 1)"},
			{"login_user", "insert into login_user "
					+ "(user_id,user_name, password, nickname, valid, level)"
					+ " values (2, 'user1_N', 'password', 'USER1_N', 1, 0)"},
			{"login_user", "insert into login_user "
					+ "(user_id,user_name, password, nickname, valid, level)"
					+ " values (3, 'user2_N', 'password', 'USER2_N', 1, 0)"},
			{"login_user", "insert into login_user "
					+ "(user_id,user_name, password, nickname, valid, level)"
					+ " values (4, 'user1_1', 'password', 'USER1_1', 1, 0)"},
			// チーム１に対して参照権限がある
			{"userteam", "insert into userteam "
					+ "(user_id,team_id, auth) values (2, 1, '')"},
			// チーム１に対して管理権限がある
			{"userteam", "insert into userteam "
					+ "(user_id,team_id, auth) values (4, 1, '1')"},
			{"team", "insert into team "
					+ "(id, name, description) values (1, 'チーム１', 'チーム１説明')"},
			{"team", "insert into team "
					+ "(id, name, description) values (2, 'チーム２', 'チーム２説明')"},

	};

	public void clearTables() {
		for (String[] sql : CLEAR_SQLS) {
			int result = jdbcTemplate.update(sql[1]);
			System.out.println("DELETE SQL実行:[" + sql[0] + "]:" + result);
		}
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	public void test_loadUserByUsername_全体管理者_正常() throws Exception {
		UserDetails details = service.loadUserByUsername("admin");
		assertEquals("admin", details.getUsername());
		assertEquals("$2a$10$2a6LgH1pZj3s0ZMAqJafW.Cd8mceQgBK.nR8FHA6yK8tM09SBJjUm", details
				.getPassword());
		Collection<? extends GrantedAuthority> grantList = details.getAuthorities();

		assertEquals(1, grantList.size());
		assertEquals("ADMIN", grantList.iterator().next().getAuthority());
	}

	@Test
	public void test_loadUserByUsername_一般ユーザー_正常() throws Exception {
		UserDetails details = service.loadUserByUsername("user1_N");
		assertEquals("user1_N", details.getUsername());
		assertEquals("password", details.getPassword());
		Collection<? extends GrantedAuthority> grantList = details.getAuthorities();
		assertEquals(1, grantList.size());
	}

	@Test
	public void test_loadUserByUsername_存在しないユーザー() throws Exception {
		Deencapsulation.setField(service, "messageUtil", messageUtil);
		try {
			UserDetails details = service.loadUserByUsername("userNG");
			fail("想定する例外が発生しない");
		} catch (UsernameNotFoundException e) {
			assertEquals(UsernameNotFoundException.class, e.getClass());
			assertEquals(messageUtil.decode(ErrorCode.ERR_USER_NOT_FOUND, "userNG"),
					e.getMessage());
		}

	}

}
