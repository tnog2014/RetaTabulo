package tabulo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

// TODO: ユーザーIDを直接指定している個所をgetUserIdOfに変更する。
@RunWith(SpringRunner.class)
@SpringBootTest
public class LoginControllerTest {

	@Autowired
	private WebApplicationContext webApplicationContext;
	private MockMvc mockMvc;

	//	@Autowired
	//	private UserService userService;
	//
	//	@Autowired
	//	private TeamService teamService;
	//
	//	@Autowired
	//	private BoardService boardService;

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
			{"board", "insert into board "
					+ "(id, team_id, name, description) values (1, 1, 'ボード１', 'ボード１説明')"},
			{"board", "insert into board "
					+ "(id, team_id, name, description) values (2, 2, 'ボード２', 'ボード２説明')"},

	};

	public void clearTables() {
		for (String[] sql : CLEAR_SQLS) {
			int result = jdbcTemplate.update(sql[1]);
			System.out.println("DELETE SQL実行:[" + sql[0] + "]:" + result);
		}
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	//	@Autowired
	//	private UserController target;
	//
	//	@Mocked("getLoginUser")
	//	ControllerUtil moc;
	//
	//	/**
	//	 * 管理者権限を含む権限リストを作成する。
	//	 * @return
	//	 */
	//	private List<GrantedAuthority> getAdminAuthList() {
	//		List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
	//		GrantedAuthority ga = new GrantedAuthority() {
	//
	//			@Override
	//			public String getAuthority() {
	//				return "ADMIN";
	//			}
	//		};
	//		list.add(ga);
	//		return list;
	//	}
	//
	//	private Integer getUserIdOf(String name) {
	//		List<LoginUser> list = userService.findByUserName(name);
	//		return list.get(0).getId();
	//	}
	//
	//	private CustomUser getStubUser(String name) {
	//		List<LoginUser> users = userService.findByUserName(name);
	//		List<GrantedAuthority> list = null;
	//		if ("1".equals(users.get(0).getLevel())) {
	//			list = getAdminAuthList();
	//		} else {
	//			list = new ArrayList<GrantedAuthority>();
	//		}
	//		CustomUser user = new CustomUser(name, "password", list, getUserIdOf(name));
	//		return user;
	//	}

	//================================================
	// ログイン画面表示
	//================================================

	@Test
	public void test_ログインを表示できる() throws Exception {
		this.mockMvc.perform(get("/login"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("Login"));
		// TODO: 表示内容のアサートを追加
	}

}