package tabulo.controller;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import tabulo.ControllerUtil;
import tabulo.CustomUser;
import tabulo.MessageUtil;
import tabulo.constant.ErrorCode;
import tabulo.model.LoginUser;
import tabulo.model.Team;
import tabulo.service.TeamService;
import tabulo.service.UserService;
import tabulo.service.UserTeamService;

// TODO: ユーザーIDを直接指定している個所をgetUserIdOfに変更する。
@RunWith(SpringRunner.class)
@SpringBootTest
public class TeamControllerTest {

	@Autowired
	private WebApplicationContext webApplicationContext;
	private MockMvc mockMvc;

	@Autowired
	private UserService userService;

	@Autowired
	private TeamService teamService;

	@Autowired
	private UserTeamService userTeamService;

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

	@Autowired
	private UserController target;

	@Mocked("getLoginUser")
	ControllerUtil moc;

	/**
	 * 管理者権限を含む権限リストを作成する。
	 * @return
	 */
	private List<GrantedAuthority> getAdminAuthList() {
		List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
		GrantedAuthority ga = new GrantedAuthority() {

			@Override
			public String getAuthority() {
				return "ADMIN";
			}
		};
		list.add(ga);
		return list;
	}

	private Integer getUserIdOf(String name) {
		List<LoginUser> list = userService.findByUserName(name);
		return list.get(0).getId();
	}

	private CustomUser getStubUser(String name) {
		List<LoginUser> users = userService.findByUserName(name);
		List<GrantedAuthority> list = null;
		if ("1".equals(users.get(0).getLevel())) {
			list = getAdminAuthList();
		} else {
			list = new ArrayList<GrantedAuthority>();
		}
		CustomUser user = new CustomUser(name, "password", list, getUserIdOf(name));
		return user;
	}

	//================================================
	// チーム一覧表示
	//================================================

	@Test
	public void test_全体管理者はチーム一覧を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};

		this.mockMvc.perform(get("/team"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("team_list"));
	}

	@Test
	public void test_チーム管理者はチーム一覧を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};

		this.mockMvc.perform(get("/team"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("team_list"));
	}

	@Test
	public void test_一般ユーザーはチーム一覧を表示できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_N");
			}
		};
		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_SHOW_TEAM_LIST,
				"user1_N/2/false");
		this.mockMvc.perform(get("/team"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}
	//================================================
	// 作成画面表示
	//================================================

	@Test
	public void test_全体管理者はチーム作成画面を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		this.mockMvc.perform(get("/team/create"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("team_create"));
		// チーム名未入力エラーをアサートする。
	}

	@Test
	public void test_チーム管理者はチーム作成画面を表示できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};

		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_OPEN_TEAM_CREATE_VIEW);
		this.mockMvc.perform(get("/team/create"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	@Test
	public void test_一般ユーザーはチーム作成画面を表示できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_N");
			}
		};

		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_OPEN_TEAM_CREATE_VIEW);
		this.mockMvc.perform(get("/team/create"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	//================================================
	// 作成実行
	//================================================

	@Test
	public void test_全体管理者はチームを作成可能() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("name", "新しいチーム１");
		params.add("description", "新しいチーム１の説明");

		this.mockMvc.perform(post("/team/create").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/team"));

		List<Team> teams = teamService.findAll();

		boolean exists = false;
		for (Team team : teams) {
			if ("新しいチーム１".equals(team.getName())) {
				assertEquals(Integer.valueOf(3), team.getId());
				assertEquals("新しいチーム１の説明", team.getDescription());
				exists = true;
			}
		}
		assertTrue(exists);
	}

	@Test
	public void test_全体管理者がチーム作成時にチーム名未入力エラー() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("name", "");
		params.add("description", "新しいチーム１の説明");
		this.mockMvc.perform(post("/team/create").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/team/create"));
	}

	@Test
	public void test_全体管理者がチーム作成時にチーム名重複エラー() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("name", "チーム１");
		params.add("description", "新しいチーム１の説明");
		this.mockMvc.perform(post("/team/create").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/team/create"));
		// TODO: 重複エラーをアサートする。
	}

	@Test
	public void test_チーム管理者はチームを作成できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("name", "チーム１");
		params.add("description", "新しいチーム１の説明");

		String message = messageUtil.decode(ErrorCode.ERR_NON_FULL_ADMIN_CANNOT_CREATE_TEAM);
		this.mockMvc.perform(post("/team/create").params(params))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));

	}

	//================================================
	// 編集画面表示
	//================================================

	@Test
	public void test_全体管理者がチーム編集画面を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};

		this.mockMvc.perform(get("/team/edit/1"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("team_edit"));

	}

	@Test
	public void test_チーム管理者は管理下のチーム編集画面を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};

		this.mockMvc.perform(get("/team/edit/1"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("team_edit"));
	}

	@Test
	public void test_チーム管理者は管理外のチーム編集画面を表示できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};

		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_EDIT_TEAM_NOT_UNDER_CONTROL, "2");
		this.mockMvc.perform(get("/team/edit/2"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));

	}

	@Test
	public void test_一般ユーザーはチーム編集画面を表示できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_N");
			}
		};

		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_EDIT_TEAM_NOT_UNDER_CONTROL, "1");
		this.mockMvc.perform(get("/team/edit/1"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	@Test
	public void test_全体管理者は存在しないチームのチーム編集画面を表示できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};

		String message = messageUtil.decode(ErrorCode.ERR_NO_TEAM_TO_BE_EDITED, "123");
		this.mockMvc.perform(get("/team/edit/123"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	//================================================
	// 編集実行
	//================================================

	@Test
	public void test_全体管理者はチームを編集可能() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "1");
		params.add("name", "新しいチーム１（変更）");
		params.add("description", "新しいチーム１の説明（変更）");

		this.mockMvc.perform(post("/team/edit").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/team"));

		List<Team> teams = teamService.findAll();

		boolean exists = false;
		for (Team team : teams) {
			if ("新しいチーム１（変更）".equals(team.getName())) {
				assertEquals(Integer.valueOf(1), team.getId());
				assertEquals("新しいチーム１の説明（変更）", team.getDescription());
				exists = true;
			}
		}
		assertTrue(exists);
	}

	@Test
	public void test_全体管理者がチーム編集時にチーム名未入力エラー() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "1");
		params.add("name", "");
		params.add("description", "新しいチーム１の説明（変更）");

		this.mockMvc.perform(post("/team/edit").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/team/edit/1"));
		// TODO: 名前未入力エラーのアサート
	}

	@Test
	public void test_全体管理者がチーム編集時にチーム名重複エラー() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "1");
		params.add("name", "チーム２");
		params.add("description", "新しいチーム１の説明（変更）");

		this.mockMvc.perform(post("/team/edit").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/team/edit/1"));
		// TODO: 名前の重複エラーのアサート
	}

	@Test
	public void test_チーム管理者は管理下のチームを編集可能() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "1");
		params.add("name", "新しいチーム１（変更）");
		params.add("description", "新しいチーム１の説明（変更）");

		this.mockMvc.perform(post("/team/edit").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/team"));

		List<Team> teams = teamService.findAll();

		boolean exists = false;
		for (Team team : teams) {
			if ("新しいチーム１（変更）".equals(team.getName())) {
				assertEquals(Integer.valueOf(1), team.getId());
				assertEquals("新しいチーム１の説明（変更）", team.getDescription());
				exists = true;
			}
		}
		assertTrue(exists);
	}

	@Test
	public void test_チーム管理者は管理外のチームの編集を実行できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "2");
		params.add("name", "新しいチーム１（変更）");
		params.add("description", "新しいチーム１の説明（変更）");

		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_EDIT_TEAM_NOT_UNDER_CONTROL, "2");
		this.mockMvc.perform(post("/team/edit").params(params))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	//================================================
	// チーム削除
	//================================================

	@Test
	public void test_全体管理者はチームを削除できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		this.mockMvc.perform(get("/team/delete/2"))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/team"));
	}

	@Test
	public void test_チーム管理者はチームを削除できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};

		String message = messageUtil.decode(ErrorCode.ERR_NON_FULL_ADMIN_CANNOT_DELETE_TEAM, "1");
		this.mockMvc.perform(get("/team/delete/1"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}
}