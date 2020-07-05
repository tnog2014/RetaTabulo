package tabulo.controller;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import org.springframework.web.util.NestedServletException;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import tabulo.ApplicationException;
import tabulo.ControllerUtil;
import tabulo.CustomUser;
import tabulo.MessageUtil;
import tabulo.constant.ErrorCode;
import tabulo.model.LoginUser;
import tabulo.model.UserTeam;
import tabulo.service.UserService;
import tabulo.service.UserTeamService;

// TODO: ユーザーIDを直接指定している個所をgetUserIdOfに変更する。
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserControllerTest {

	@Autowired
	MessageUtil messageUtil;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@Autowired
	private UserService userService;

	@Autowired
	private UserTeamService userTeamService;

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
	// ユーザー一覧表示
	//================================================

	@Test
	public void test_全体管理者はユーザー一覧を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};

		this.mockMvc.perform(get("/user"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("user_list"));
	}

	@Test
	public void test_チーム管理者はユーザー一覧を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};

		this.mockMvc.perform(get("/user"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("user_list"));
	}

	@Test
	public void test_一般ユーザーはユーザー一覧を表示できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_N");
			}
		};

		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_OPEN_USER_LIST, "user1_N");
		this.mockMvc.perform(get("/user"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	// 作成画面表示

	@Test
	public void test_全体管理者はユーザー作成画面を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		List<LoginUser> users = userService.findAll();
		System.out.println(users.get(0).getId());
		System.out.println(users.get(0).getUserName());
		this.mockMvc.perform(get("/user/create"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("user_create"));

	}

	// getリクエストでviewを指定し、httpステータスでリクエストの成否を判定
	@Test
	public void test_チーム管理者はユーザー作成画面を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		this.mockMvc.perform(get("/user/create"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("user_create"));
	}

	@Test
	public void test_一般ユーザーはユーザー作成画面を表示できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_N");
			}
		};

		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_OPEN_USER_CREATE_VIEW, "2");
		this.mockMvc.perform(get("/user/create"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	//================================================
	// 編集画面表示
	//================================================

	@Test
	public void test_全体管理者が自分自身のユーザー編集画面を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};

		this.mockMvc.perform(get("/user/edit/" + getUserIdOf("admin")))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("user_edit"));

	}

	@Test
	public void test_全体管理者が自分自身のユーザー編集画面を表示できる_ユーザー一覧画面から() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};

		this.mockMvc.perform(get("/user/edit/1?from=user"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("user_edit"));

	}

	@Test
	public void test_全体管理者は存在しないユーザーの編集画面を表示できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};

		String message = messageUtil.decode(
				ErrorCode.ERR_CANNOT_OPEN_USER_EDIT_VIEW, "admin", "123");

		this.mockMvc.perform(get("/user/edit/123"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	private void assertApplicationExceptionMessage(Exception e, String message) {
		assertEquals(NestedServletException.class, e.getClass());
		Throwable ee = e.getCause();
		assertEquals(ApplicationException.class, ee.getClass());
		assertEquals(message, ee.getMessage());
	}

	@Test
	public void test_チーム管理者が管理対象のユーザー編集画面を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};

		this.mockMvc.perform(get("/user/edit/2"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("user_edit"));

	}

	@Test
	public void test_チーム管理者が自分自身のユーザー編集画面を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};

		this.mockMvc.perform(get("/user/edit/4"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("user_edit"));
	}

	@Test
	public void test_チーム管理者が管理対象外のユーザー編集画面を表示できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};

		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_OPEN_USER_EDIT_VIEW, "user1_1",
				"3");
		this.mockMvc.perform(get("/user/edit/3"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	@Test
	public void test_一般ユーザーは他ユーザー編集画面を表示できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_N");
			}
		};

		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_OPEN_USER_EDIT_VIEW, "user1_N",
				"3");
		this.mockMvc.perform(get("/user/edit/3"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	//================================================
	// 編集実行
	//================================================

	@Test
	public void test_全体管理者は自分自身のユーザー編集を実行できる_メイン画面から() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "1");
		params.add("userName", "admin");
		params.add("nickname", "NICKNAME2");
		params.add("password", "PASSWORD");
		params.add("cnfPassword", "PASSWORD");
		params.add("valid", "1");
		params.add("level", "1");
		params.add("assignedTeams", "");
		params.add("unassignedTeams", "");
		params.add("assignedAdminTeams", "");
		params.add("unassignedAdminTeams", "");

		this.mockMvc.perform(post("/user/edit").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/main"));

		List<LoginUser> users = userService.findAll();

		boolean exists = false;
		for (LoginUser user : users) {
			if ("admin".equals(user.getUserName())) {
				assertEquals("NICKNAME2", user.getNickname());
				// パスワードは暗号化されているため確認しない。
				assertEquals("1", user.getValid());
				assertEquals("1", user.getLevel());
				exists = true;
			}
		}

	}

	@Test
	public void test_全体管理者がパスワード変更を実行できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			};

		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "1");
		params.add("userName", "admin");
		params.add("nickname", "NICKNAME2");
		params.add("changePassword", "true");
		params.add("oldPassword", "password");
		params.add("password", "password2");
		params.add("cnfPassword", "password2");
		params.add("valid", "1");
		params.add("level", "1");
		params.add("assignedTeams", "");
		params.add("unassignedTeams", "");
		params.add("assignedAdminTeams", "");
		params.add("unassignedAdminTeams", "");

		this.mockMvc.perform(post("/user/edit").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/main"));

		List<LoginUser> users = userService.findAll();

		boolean exists = false;
		for (LoginUser user : users) {
			if ("admin".equals(user.getUserName())) {
				assertEquals("NICKNAME2", user.getNickname());
				// パスワードは暗号化されているため確認しない。
				assertEquals("1", user.getValid());
				assertEquals("1", user.getLevel());
				exists = true;
			}
		}

	}

	@Test
	public void test_全体管理者がパスワード変更時に確認パスワード不一致() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			};

		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "1");
		params.add("userName", "admin");
		params.add("nickname", "NICKNAME2");
		params.add("changePassword", "true");
		params.add("oldPassword", "password");
		params.add("password", "password2");
		params.add("cnfPassword", "passwordNG");
		params.add("valid", "1");
		params.add("level", "1");
		params.add("assignedTeams", "");
		params.add("unassignedTeams", "");
		params.add("assignedAdminTeams", "");
		params.add("unassignedAdminTeams", "");

		this.mockMvc.perform(post("/user/edit").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/user/edit/1"));

		// TODO: エラーをアサート
	}

	@Test
	public void test_全体管理者がパスワード変更時にニックネームが空のエラー() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			};

		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "1");
		params.add("userName", "admin");
		params.add("nickname", "");
		params.add("valid", "1");
		params.add("level", "1");
		params.add("assignedTeams", "");
		params.add("unassignedTeams", "");
		params.add("assignedAdminTeams", "");
		params.add("unassignedAdminTeams", "");

		this.mockMvc.perform(post("/user/edit").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/user/edit/1"));

		// TODO: エラーをアサート
	}

	@Test
	public void test_全体管理者がユーザー編集時にユーザー名重複エラー() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "2");
		params.add("userName", "user2_N");
		params.add("nickname", "NICKNAME");
		params.add("password", "PASSWORD");
		params.add("cnfPassword", "PASSWORD");
		params.add("valid", "1");
		params.add("level", "1");
		params.add("assignedTeams", "");
		params.add("unassignedTeams", "");
		params.add("assignedAdminTeams", "");
		params.add("unassignedAdminTeams", "");

		this.mockMvc.perform(post("/user/edit").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/user/edit/2"));

		// TODO: 重複エラーをアサート

	}

	@Test
	public void test_全体管理者が存在しないユーザー情報の編集はできない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "123");
		params.add("userName", "");
		params.add("nickname", "NICKNAME");
		params.add("valid", "1");
		params.add("level", "1");
		params.add("assignedTeams", "");
		params.add("unassignedTeams", "");
		params.add("assignedAdminTeams", "");
		params.add("unassignedAdminTeams", "");

		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_EDIT_USER, "admin", "123");
		this.mockMvc.perform(post("/user/edit").params(params))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	@Test
	public void test_チーム管理者は管理対象チームに属するユーザー編集を実行できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "2");
		params.add("userName", "user1_N");
		params.add("nickname", "NICKNAME2");
		params.add("oldPassword", "PASSWORD");
		params.add("password", "PASSWORD");
		params.add("cnfPassword", "PASSWORD");
		params.add("valid", "0");
		params.add("level", "0");
		params.add("assignedTeams", "");
		params.add("unassignedTeams", "");
		params.add("assignedAdminTeams", "");
		params.add("unassignedAdminTeams", "");

		this.mockMvc.perform(post("/user/edit").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/main"));

		List<LoginUser> users = userService.findAll();

		boolean exists = false;
		for (LoginUser user : users) {
			if ("user1_N".equals(user.getUserName())) {
				assertEquals("NICKNAME2", user.getNickname());
				// パスワードは暗号化されているため確認しない。
				assertEquals("0", user.getValid());
				assertEquals("0", user.getLevel());
				exists = true;
			}
		}
		assertTrue(exists);
	}

	@Test
	public void test_チーム管理者は管理対象チームに属するユーザー編集を実行できる_チーム追加エラー() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "2");
		params.add("userName", "user1_N");
		params.add("nickname", "NICKNAME2");
		params.add("oldPassword", "PASSWORD");
		params.add("password", "PASSWORD");
		params.add("cnfPassword", "PASSWORD");
		params.add("valid", "0");
		params.add("level", "0");
		params.add("assignedTeams", "cb_2");
		params.add("unassignedTeams", "");
		params.add("assignedAdminTeams", "");
		params.add("unassignedAdminTeams", "");

		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_GIVE_REF_RIGHT, "2");
		this.mockMvc.perform(post("/user/edit").params(params))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	@Test
	public void test_チーム管理者は管理対象チームに属するユーザー編集を実行できる_チーム削除エラー() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "2");
		params.add("userName", "user1_N");
		params.add("nickname", "NICKNAME2");
		params.add("oldPassword", "PASSWORD");
		params.add("password", "PASSWORD");
		params.add("cnfPassword", "PASSWORD");
		params.add("valid", "0");
		params.add("level", "0");
		params.add("assignedTeams", "");
		params.add("unassignedTeams", "cb_2");
		params.add("assignedAdminTeams", "");
		params.add("unassignedAdminTeams", "");

		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_DEPRIVE_REF_RIGHT, "2");
		this.mockMvc.perform(post("/user/edit").params(params))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	@Test
	public void test_全体管理者はユーザー編集実行時に存在しないチームの管理権限を付与できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "2");
		params.add("userName", "user1_N");
		params.add("nickname", "NICKNAME2");
		params.add("oldPassword", "PASSWORD");
		params.add("password", "PASSWORD");
		params.add("cnfPassword", "PASSWORD");
		params.add("valid", "0");
		params.add("level", "0");
		params.add("assignedTeams", "");
		params.add("unassignedTeams", "");
		params.add("assignedAdminTeams", "cbadm_123");
		params.add("unassignedAdminTeams", "");

		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_GIVE_ADMIN_AUTHORITY, "123");
		this.mockMvc.perform(post("/user/edit").params(params))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	@Test
	public void test_全体管理者はユーザー編集実行時に存在しないチームの管理権限を除去できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "2");
		params.add("userName", "user1_N");
		params.add("nickname", "NICKNAME2");
		params.add("oldPassword", "PASSWORD");
		params.add("password", "PASSWORD");
		params.add("cnfPassword", "PASSWORD");
		params.add("valid", "0");
		params.add("level", "0");
		params.add("assignedTeams", "");
		params.add("unassignedTeams", "");
		params.add("assignedAdminTeams", "");
		params.add("unassignedAdminTeams", "cbadm_123");

		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_DEPRIVE_ADMIN_AUTHORITY, "123");
		this.mockMvc.perform(post("/user/edit").params(params))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	@Test
	public void test_チーム管理者は管理対象チームに属さないユーザー編集を実行できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "3");
		params.add("userName", "user2_N");
		params.add("nickname", "NICKNAME2");
		params.add("oldPassword", "PASSWORD");
		params.add("password", "PASSWORD");
		params.add("cnfPassword", "PASSWORD");
		params.add("valid", "0");
		params.add("level", "0");
		params.add("assignedTeams", "");
		params.add("unassignedTeams", "");
		params.add("assignedAdminTeams", "");
		params.add("unassignedAdminTeams", "");

		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_EDIT_USER, "user1_1", "3");
		this.mockMvc.perform(post("/user/edit").params(params))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	@Test
	public void test_チーム管理者はユーザーを全体管理者にできない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "2");
		params.add("userName", "user1_N");
		params.add("nickname", "NICKNAME2");
		params.add("valid", "0");
		params.add("level", "1");
		params.add("assignedTeams", "");
		params.add("unassignedTeams", "");
		params.add("assignedAdminTeams", "");
		params.add("unassignedAdminTeams", "");

		String message = messageUtil.decode(ErrorCode.ERR_ONLY_FULL_ADMIN_CAN_EDIT_FULL_ADMIN,
				"user1_N");

		this.mockMvc.perform(post("/user/edit").params(params))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	@Test
	public void test_全体管理者は自分自身のユーザー編集を実行できる_ユーザー一覧画面から() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "1");
		params.add("userName", "admin");
		params.add("nickname", "NICKNAME2");
		params.add("password", "PASSWORD");
		params.add("cnfPassword", "PASSWORD");
		params.add("valid", "1");
		params.add("level", "1");
		params.add("assignedTeams", "");
		params.add("unassignedTeams", "");
		params.add("assignedAdminTeams", "");
		params.add("unassignedAdminTeams", "");
		params.add("from", "user");

		this.mockMvc.perform(post("/user/edit").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/user"));

		List<LoginUser> users = userService.findAll();

		boolean exists = false;
		for (LoginUser user : users) {
			if ("admin".equals(user.getUserName())) {
				assertEquals("NICKNAME2", user.getNickname());
				// パスワードは暗号化されているため確認しない。
				assertEquals("1", user.getValid());
				assertEquals("1", user.getLevel());
				exists = true;
			}
		}
	}

	@Test
	public void test_全体管理者は一般ユーザー編集を実行できる_ユーザー一覧画面から_チーム設定あり() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "4");
		params.add("userName", "user1_1");
		params.add("nickname", "NICKNAME2");
		params.add("valid", "1");
		params.add("level", "0");
		params.add("assignedTeams", "cb_2");
		params.add("unassignedTeams", "cb_1");
		params.add("assignedAdminTeams", "cbadm_2");
		params.add("unassignedAdminTeams", "cbadm_1");
		params.add("from", "user");

		// 前提条件の確認
		{
			// チーム１に対するユーザー・チーム関係
			Optional<UserTeam> ut1 = userTeamService.findByUserIdTeamId(4, 1);
			assertTrue(ut1.isPresent());
			UserTeam ut1g = ut1.get();
			assertEquals("1", ut1g.getAuth());

			// チーム２に対するユーザー・チーム関係
			Optional<UserTeam> ut2 = userTeamService.findByUserIdTeamId(4, 2);
			assertFalse(ut2.isPresent());
		}

		this.mockMvc.perform(post("/user/edit").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/user"));

		List<LoginUser> users = userService.findAll();

		boolean exists = false;
		for (LoginUser user : users) {
			if ("user1_1".equals(user.getUserName())) {
				assertEquals("NICKNAME2", user.getNickname());
				// パスワードは暗号化されているため確認しない。
				assertEquals("1", user.getValid());
				assertEquals("0", user.getLevel());
				exists = true;
			}
		}
		assertTrue(exists);

		{
			// チーム１に対するユーザー・チーム関係
			Optional<UserTeam> ut1 = userTeamService.findByUserIdTeamId(4, 1);
			assertFalse(ut1.isPresent());

			// チーム２に対するユーザー・チーム関係
			Optional<UserTeam> ut2 = userTeamService.findByUserIdTeamId(4, 2);
			assertTrue(ut2.isPresent());
			UserTeam ut2g = ut2.get();
			assertEquals("1", ut2g.getAuth());
		}
	}

	@Test
	public void test_一般ユーザーは他ユーザー編集を実行きない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "3");
		params.add("userName", "user2_N");
		params.add("nickname", "NICKNAME");
		params.add("password", "PASSWORD");
		params.add("cnfPassword", "PASSWORD");
		params.add("valid", "1");
		params.add("level", "");
		params.add("assignedTeams", "");
		params.add("unassignedTeams", "");
		params.add("assignedAdminTeams", "");
		params.add("unassignedAdminTeams", "");
		params.add("from", "user");

		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_EDIT_USER, "user1_1", "3");
		this.mockMvc.perform(post("/user/edit").params(params))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	//================================================
	// 作成実行
	//================================================

	@Test
	public void test_全体管理者は全体管理者を作成可能() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("userName", "USERNAME");
		params.add("nickname", "NICKNAME");
		params.add("password", "PASSWORD");
		params.add("cnfPassword", "PASSWORD");
		params.add("valid", "1");
		params.add("level", "1");
		params.add("assignedTeams", "cb_1");
		params.add("unassignedTeams", "");
		params.add("assignedAdminTeams", "cbadm_1");
		params.add("unassignedAdminTeams", "");

		this.mockMvc.perform(post("/user/create").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/user"));

		List<LoginUser> users = userService.findAll();

		boolean exists = false;
		for (LoginUser user : users) {
			if ("USERNAME".equals(user.getUserName())) {
				assertEquals("NICKNAME", user.getNickname());
				// パスワードは暗号化されているため確認しない。
				assertEquals("1", user.getValid());
				assertEquals("1", user.getLevel());
				exists = true;
			}
		}
		assertTrue(exists);
	}

	@Test
	public void test_チーム管理者は一般ユーザーを作成可能() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("userName", "USERNAME");
		params.add("nickname", "NICKNAME");
		params.add("password", "PASSWORD");
		params.add("cnfPassword", "PASSWORD");
		params.add("valid", "1");
		params.add("level", "0");
		params.add("assignedTeams", "cb_1");
		params.add("unassignedTeams", "");
		params.add("assignedAdminTeams", "");
		params.add("unassignedAdminTeams", "");

		this.mockMvc.perform(post("/user/create").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/user"));

		List<LoginUser> users = userService.findAll();

		boolean exists = false;
		for (LoginUser user : users) {
			if ("USERNAME".equals(user.getUserName())) {
				assertEquals("NICKNAME", user.getNickname());
				// パスワードは暗号化されているため確認しない。
				assertEquals("1", user.getValid());
				assertEquals("0", user.getLevel());
				exists = true;
			}
		}
		assertTrue(exists);
	}

	@Test
	public void test_チーム管理者はチーム管理者を作成できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("userName", "USERNAME");
		params.add("nickname", "NICKNAME");
		params.add("password", "PASSWORD");
		params.add("cnfPassword", "PASSWORD");
		params.add("valid", "1");
		params.add("level", "0");
		params.add("assignedTeams", "cb_1");
		params.add("unassignedTeams", "");
		params.add("assignedAdminTeams", "cb_1");
		params.add("unassignedAdminTeams", "");

		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_CHANGE_TEAM_ASSIGNMENTS, "cb_1",
				"");
		this.mockMvc.perform(post("/user/create").params(params))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	@Test
	public void test_全体管理者がユーザー作成時にチェックエラー発生() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("userName", ""); // 未入力エラー
		params.add("nickname", "NICKNAME");
		params.add("password", "PASSWORD");
		params.add("cnfPassword", "PASSWORD");
		params.add("valid", "1");
		params.add("level", "1");
		params.add("assignedTeams", "");
		params.add("unassignedTeams", "");
		params.add("assignedAdminTeams", "");
		params.add("unassignedAdminTeams", "");

		this.mockMvc.perform(post("/user/create").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/user/create"));
	}

	@Test
	public void test_全体管理者がユーザー作成時にユーザー名重複エラー発生() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("userName", "admin"); // ユーザー名重複エラー
		params.add("nickname", "NICKNAME");
		params.add("password", "PASSWORD");
		params.add("cnfPassword", "PASSWORD");
		params.add("valid", "1");
		params.add("level", "1");
		params.add("assignedTeams", "");
		params.add("unassignedTeams", "");
		params.add("assignedAdminTeams", "");
		params.add("unassignedAdminTeams", "");

		// TODO: エラー情報をアサートしたい。

		this.mockMvc.perform(post("/user/create").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/user/create"));
	}

	@Test
	public void test_チーム管理者は全体管理者を作成できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("userName", "USERNAME");
		params.add("nickname", "NICKNAME");
		params.add("password", "PASSWORD");
		params.add("cnfPassword", "PASSWORD");
		params.add("valid", "1");
		params.add("level", "1");
		params.add("assignedTeams", "");
		params.add("unassignedTeams", "");
		params.add("assignedAdminTeams", "");
		params.add("unassignedAdminTeams", "");

		String message = messageUtil.decode(ErrorCode.ERR_NON_FULL_ADMIN_CANNOT_CREATE_FULL_ADMIN,
				"USERNAME");
		this.mockMvc.perform(post("/user/create").params(params))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	//================================================
	// ユーザー削除
	//================================================

	@Test
	public void test_全体管理者はチーム管理者を削除できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		this.mockMvc.perform(get("/user/delete/4"))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/user"));
	}

	@Test
	public void test_チーム管理者は全体管理者を削除できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};

		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_DELETE_USER_NOT_UNDER_CONTROL,
				"1");
		this.mockMvc.perform(get("/user/delete/1"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	@Test
	public void test_チーム管理者は管理対象外ユーザーを削除できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
				CustomUser user = new CustomUser("user1_1", "password", list, 4);
				result = user;
			}
		};

		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_DELETE_USER_NOT_UNDER_CONTROL,
				"3");
		this.mockMvc.perform(get("/user/delete/3"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	@Test
	public void test_チーム管理者は管理対象ユーザーを削除できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		this.mockMvc.perform(get("/user/delete/2"))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/user"));

	}

}