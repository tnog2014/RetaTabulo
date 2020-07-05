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
import tabulo.model.Board;
import tabulo.model.LoginUser;
import tabulo.service.BoardService;
import tabulo.service.UserService;

// TODO: ユーザーIDを直接指定している個所をgetUserIdOfに変更する。
@RunWith(SpringRunner.class)
@SpringBootTest
public class BoardControllerTest {

	@Mocked("getLoginUser")
	ControllerUtil moc;

	@Autowired
	private WebApplicationContext webApplicationContext;
	private MockMvc mockMvc;

	@Autowired
	private UserService userService;

	@Autowired
	private BoardService boardService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private MessageUtil messageUtil;

	//================================================
	// ボード画面表示
	//================================================

	private String descs = "var boardId = 1;\r\n"
			+ "var descs = [{id:1, create_name:null, update_name:null, x:101.0, y:201.0, width:301.0, height:401.0, raw:'RAW1', html:'HTML1'},\r\n"
			+ "{id:2, create_name:null, update_name:null, x:102.0, y:202.0, width:302.0, height:402.0, raw:'RAW2', html:'HTML2'},\r\n"
			+ "{id:3, create_name:null, update_name:null, x:103.0, y:203.0, width:303.0, height:403.0, raw:'null', html:'HTML3'}];\r\n";

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
			{"description", "insert into description "
					+ "(id, board_id, x, y, width, height, raw, html) values (1, 1, 101.0, 201.0, 301.0, 401.0, 'RAW1', 'HTML1')"},
			{"description", "insert into description "
					+ "(id, board_id, x, y, width, height, raw, html) values (2, 1, 102.0, 202.0, 302.0, 402.0, 'RAW2', 'HTML2')"},
			{"description", "insert into description "
					+ "(id, board_id, x, y, width, height, raw, html) values (3, 1, 103.0, 203.0, 303.0, 403.0, null, 'HTML3')"},

	};

	@Before
	public void setUp() {
		System.out.println("★セットアップ");
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		//		List<Map<String, Object>> actualData = this.jdbcTemplate.queryForList("select * from login_user");
		//		System.out.println("★" + actualData);
		clearTables();
	}

	public void clearTables() {
		for (String[] sql : CLEAR_SQLS) {
			int result = jdbcTemplate.update(sql[1]);
			System.out.println("DELETE SQL実行:[" + sql[0] + "]:" + result);
		}
	}

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
	// メイン画面表示
	//================================================

	@Test
	public void test_全体管理者はメイン画面を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};

		this.mockMvc.perform(get("/main"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("main"));
	}

	@Test
	public void test_チーム管理者はメイン画面を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};

		this.mockMvc.perform(get("/main"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("main"));
	}

	@Test
	public void test_一般ユーザーはメイン画面を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};

		this.mockMvc.perform(get("/main"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("main"));
	}

	@Test
	public void test_全体管理者はメイン画面を表示できる_post() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};

		this.mockMvc.perform(post("/main"))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/main"));
	}

	//================================================
	// ボード一覧表示
	//================================================

	@Test
	public void test_全体管理者はボード一覧を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};

		this.mockMvc.perform(get("/board"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("board_list"));
	}

	@Test
	public void test_チーム管理者はボード一覧を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};

		this.mockMvc.perform(get("/board"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("board_list"));
	}

	@Test
	public void test_一般ユーザーはチーム一覧を表示できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_N");
			}
		};
		String message = messageUtil.decode(ErrorCode.ERR_NON_ADMIN_CANNOT_OPEN_BOARD_LIST_VIEW,
				"2");
		this.mockMvc.perform(get("/board"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	//================================================
	// 作成画面表示
	//================================================

	@Test
	public void test_全体管理者はボード作成画面を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		this.mockMvc.perform(get("/board/create"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("board_create"));
	}

	@Test
	public void test_チーム管理者はボード作成画面を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		this.mockMvc.perform(get("/board/create"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("board_create"));
	}

	@Test
	public void test_一般ユーザーはボード作成画面を表示できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_N");
			}
		};
		String message = messageUtil.decode(ErrorCode.ERR_NON_ADMIN_CANNOT_CREATE_BOARD, "2");
		this.mockMvc.perform(get("/board/create"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	//================================================
	// 作成実行
	//================================================

	@Test
	public void test_全体管理者はボードを作成可能() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("teamId", "1");
		params.add("name", "新しいボード１");
		params.add("description", "新しいボード１の説明");

		this.mockMvc.perform(post("/board/create").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/board"));

		List<Board> boards = boardService.findAll();

		boolean exists = false;
		for (Board board : boards) {
			if ("新しいボード１".equals(board.getName())) {
				assertEquals(Integer.valueOf(3), board.getId());
				assertEquals("新しいボード１の説明", board.getDescription());
				//assertEquals("新しいボード１の説明", board.getTeam().getName());
				exists = true;
			}
		}
		assertTrue(exists);
	}

	@Test
	public void test_全体管理者がボード作成時にボード名未入力エラー() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("teamId", "1");
		params.add("name", "");
		params.add("description", "新しいボード１の説明");
		this.mockMvc.perform(post("/board/create").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/board/create"));
		// TODO: チェックエラーをアサート
	}

	@Test
	public void test_全体管理者がボード作成時にボード名重複エラー() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("teamId", "1");
		params.add("name", "ボード１");
		params.add("description", "新しいボード１の説明");
		this.mockMvc.perform(post("/board/create").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/board/create"));

		// TODO: 重複エラーをアサートする。
	}

	//here
	@Test
	public void test_チーム管理者は管理下のチームに属するボードを作成可能() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("teamId", "1");
		params.add("name", "新しいボード１");
		params.add("description", "新しいボード１の説明");

		this.mockMvc.perform(post("/board/create").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/board"));

		List<Board> boards = boardService.findAll();

		boolean exists = false;
		for (Board board : boards) {
			if ("新しいボード１".equals(board.getName())) {
				assertEquals(Integer.valueOf(4), board.getId());
				assertEquals("新しいボード１の説明", board.getDescription());
				//assertEquals("新しいボード１の説明", board.getTeam().getName());
				exists = true;
			}
		}
		assertTrue(exists);
	}

	@Test
	public void test_チーム管理者は管理外のチームに属するボードを作成できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("teamId", "2");
		params.add("name", "新しいボード１");
		params.add("description", "新しいボード１の説明");
		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_CREATE_BOARD_BELONGING_TO_TEAM,
				"2");

		this.mockMvc.perform(post("/board/create").params(params))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	//================================================
	// 編集画面表示
	//================================================

	@Test
	public void test_全体管理者がボード編集画面を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};

		this.mockMvc.perform(get("/board/edit/1"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("board_edit"));
	}

	@Test
	public void test_全体管理者は存在しないボード編集画面を表示できない() throws Exception {
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

	@Test
	public void test_チーム管理者は管理下のボード編集画面を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};

		this.mockMvc.perform(get("/board/edit/1"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("board_edit"));
	}

	@Test
	public void test_チーム管理者は管理外のボード編集画面を表示できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_EDIT_TEAM_NOT_UNDER_CONTROL,
				"2");
		this.mockMvc.perform(get("/team/edit/2"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	@Test
	public void test_一般ユーザーはボード編集画面を表示できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_N");
			}
		};
		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_EDIT_BOARD, "1");
		this.mockMvc.perform(get("/board/edit/1"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	//================================================
	// 編集実行
	//================================================

	@Test
	public void test_全体管理者はボードを編集可能() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "1");
		params.add("teamId", "1");
		params.add("name", "新しいボード１（変更）");
		params.add("description", "新しいボード１の説明（変更）");

		this.mockMvc.perform(post("/board/edit").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/board"));

		List<Board> teams = boardService.findAll();

		boolean exists = false;
		for (Board team : teams) {
			if ("新しいボード１（変更）".equals(team.getName())) {
				assertEquals(Integer.valueOf(1), team.getId());
				assertEquals("新しいボード１の説明（変更）", team.getDescription());
				exists = true;
			}
		}
		assertTrue(exists);
	}

	@Test
	public void test_全体管理者がボード編集時にボード名未入力エラー() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "1");
		params.add("teamId", "1");
		params.add("name", "");
		params.add("description", "新しいボード１の説明（変更）");

		this.mockMvc.perform(post("/board/edit").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/board/edit/1"));
		// TODO: 名前未入力エラーのアサート
	}

	@Test
	public void test_全体管理者がボード編集時にボード名重複エラー() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "1");
		params.add("teamId", "1");
		params.add("name", "ボード２");
		params.add("description", "新しいチーム１の説明（変更）");

		this.mockMvc.perform(post("/board/edit").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/board/edit/1"));

		// TODO: 名前の重複エラーのアサート
	}

	@Test
	public void test_チーム管理者は管理下のボードを編集可能() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "1");
		params.add("teamId", "1");
		params.add("name", "ボード１（変更）");
		params.add("description", "ボード１説明（変更）");

		this.mockMvc.perform(post("/board/edit").params(params))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/board"));

		List<Board> boards = boardService.findAll();

		boolean exists = false;
		for (Board board : boards) {
			if ("ボード１（変更）".equals(board.getName())) {
				assertEquals(Integer.valueOf(1), board.getId());
				// TODO: チームをアサートしたい
				assertEquals("ボード１説明（変更）", board.getDescription());
				exists = true;
			}
		}
		assertTrue(exists);
	}

	@Test
	public void test_チーム管理者は管理外のボードを編集できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "2");
		params.add("teamId", "2");
		params.add("name", "ボード２（変更）");
		params.add("description", "ボード２説明（変更）");
		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_EDIT_BOARD, "2");
		this.mockMvc.perform(post("/board/edit").params(params))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));

		List<Board> boards = boardService.findAll();

		boolean exists = false;
		for (Board board : boards) {
			if ("ボード２".equals(board.getName())) {
				assertEquals(Integer.valueOf(2), board.getId());
				// TODO: チームをアサートしたい
				assertEquals("ボード２説明", board.getDescription());
				exists = true;
			}
		}
		assertTrue(exists);
	}

	@Test
	public void test_チーム管理者は管理下のボードのチームを管理外チームに編集できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "1");
		params.add("teamId", "2");
		params.add("name", "ボード１");
		params.add("description", "ボード１の説明");
		//		try {
		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_EDIT_TEAM_NOT_UNDER_CONTROL,
				"2");

		this.mockMvc.perform(post("/board/edit").params(params))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));

		List<Board> boards = boardService.findAll();

		boolean exists = false;
		for (Board board : boards) {
			if ("ボード１".equals(board.getName())) {
				assertEquals(Integer.valueOf(1), board.getId());
				// TODO: チームをアサートしたい
				assertEquals("ボード１説明", board.getDescription());
				exists = true;
			}
		}
		assertTrue(exists);
	}

	@Test
	public void test_チーム管理者は管理下のボードのチームを存在しないチームに編集できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "1");
		params.add("teamId", "123");
		params.add("name", "ボード１");
		params.add("description", "ボード１説明");

		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_EDIT_TEAM_NOT_UNDER_CONTROL,
				"123");

		this.mockMvc.perform(post("/board/edit").params(params))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));

		List<Board> boards = boardService.findAll();

		boolean exists = false;
		for (Board board : boards) {
			if ("ボード１".equals(board.getName())) {
				assertEquals(Integer.valueOf(1), board.getId());
				// TODO: チームをアサートしたい
				assertEquals("ボード１説明", board.getDescription());
				exists = true;
			}
		}
		assertTrue(exists);
	}

	@Test
	public void test_一般ユーザーはボードを編集できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_N");
			}
		};
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("id", "1");
		params.add("teamId", "1");
		params.add("name", "ボード１（更新）");
		params.add("description", "ボード１説明（更新）");
		//		try {
		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_EDIT_BOARD, "1");
		this.mockMvc.perform(post("/board/edit").params(params))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));

		List<Board> boards = boardService.findAll();

		boolean exists = false;
		for (Board board : boards) {
			if ("ボード１".equals(board.getName())) {
				assertEquals(Integer.valueOf(1), board.getId());
				// TODO: チームをアサートしたい
				assertEquals("ボード１説明", board.getDescription());
				exists = true;
			}
		}
		assertTrue(exists);
	}

	//
	//	@Test
	//	public void test_チーム管理者は管理外のチームの編集を実行できない() throws Exception {
	//		new NonStrictExpectations() {
	//			{
	//				ControllerUtil.getLoginUser();
	//				result = getStubUser("user1_1");
	//			}
	//		};
	//
	//		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
	//		params.add("id", "2");
	//		params.add("name", "新しいチーム１（変更）");
	//		params.add("description", "新しいチーム１の説明（変更）");
	//
	//		try {
	//			this.mockMvc.perform(post("/team/edit").params(params))
	//				.andDo(print())
	//				.andExpect(status().is3xxRedirection())
	//				.andExpect(redirectedUrl("/team"));
	//			fail("想定する例外が発生しない。");
	//		} catch (Exception e) {
	//			assertEquals(ApplicationException.class, e.getCause().getClass());
	//			assertEquals(
	//					"Request processing failed; nested exception is java.lang.ApplicationException: 管理対象のチームのみ編集可能です[2]",
	//					e.getMessage());
	//		}
	//	}
	//
	//
	//================================================
	// ボード削除
	//================================================

	@Test
	public void test_全体管理者はボードを削除できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		this.mockMvc.perform(get("/board/delete/1"))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/board"));

	}

	@Test
	public void test_チーム管理者は管理下のボードを削除できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		this.mockMvc.perform(get("/board/delete/1"))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/board"));
	}

	@Test
	public void test_チーム管理者は管理外のボードを削除できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_DELETE_BOARD, "2");
		this.mockMvc.perform(get("/board/delete/2"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	@Test
	public void test_一般ユーザーはボードを削除できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_N");
			}
		};
		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_DELETE_BOARD, "1");
		this.mockMvc.perform(get("/board/delete/1"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	@Test
	public void test_全体管理者はボード画面を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};
		this.mockMvc.perform(get("/board/open/1"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("board"))
				.andExpect(content().string(Matchers.containsString(descs)));
	}

	@Test
	public void test_チーム管理者は参照権限のあるボード画面を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		this.mockMvc.perform(get("/board/open/1"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("board"))
				.andExpect(content().string(Matchers.containsString(descs)));
	}

	@Test
	public void test_チーム管理者は参照権限のないボード画面を表示できない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_OPEN_BOARD_VIEW, "2");
		this.mockMvc.perform(get("/board/open/2"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	@Test
	public void test_一般ユーザーは参照権限のあるボード画面を表示できる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_N");
			}
		};
		this.mockMvc.perform(get("/board/open/1"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("board"))
				.andExpect(content().string(Matchers.containsString(descs)));
	}

	@Test
	public void test_一般ユーザーは参照権限のないボード画面を表示ない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_N");
			}
		};
		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_OPEN_BOARD_VIEW, "2");
		this.mockMvc.perform(get("/board/open/2"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

}