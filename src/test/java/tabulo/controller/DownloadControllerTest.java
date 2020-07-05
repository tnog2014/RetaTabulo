package tabulo.controller;

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
import org.springframework.web.context.WebApplicationContext;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import tabulo.ControllerUtil;
import tabulo.CustomUser;
import tabulo.MessageUtil;
import tabulo.constant.ErrorCode;
import tabulo.model.LoginUser;
import tabulo.service.BoardService;
import tabulo.service.TeamService;
import tabulo.service.UserService;

// TODO: ユーザーIDを直接指定している個所をgetUserIdOfに変更する。
@RunWith(SpringRunner.class)
@SpringBootTest
public class DownloadControllerTest {

	@Autowired
	private WebApplicationContext webApplicationContext;
	private MockMvc mockMvc;

	@Autowired
	private UserService userService;

	@Autowired
	private TeamService teamService;

	@Autowired
	private BoardService boardService;

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
			{"board", "insert into board "
					+ "(id, team_id, name, description) values (1, 1, 'ボード１', 'ボード１説明')"},
			{"board", "insert into board "
					+ "(id, team_id, name, description) values (2, 2, 'ボード２', 'ボード２説明')"},
			{"description", "insert into description "
					+ "(id, board_id, x, y, width, height, raw, html) values (1, 1, 101.0, 201.0, 301.0, 401.0, 'RAW1', 'HTML1')"},
			{"description", "insert into description "
					+ "(id, board_id, x, y, width, height, raw, html) values (2, 1, 102.0, 202.0, 302.0, 402.0, 'RAW2', 'HTML2')"},

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
	// CSVダウンロード
	//================================================
	String expectedContent = "1	1			101.0	201.0	301.0	401.0	RAW1	HTML1\r\n" +
			"1	2			102.0	202.0	302.0	402.0	RAW2	HTML2\r\n";

	@Test
	public void test_全体管理者はボードのCSVをダウンロードできる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};

		this.mockMvc.perform(get("/download/csv/1"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().string(expectedContent));
	}

	@Test
	public void test_チーム管理者は管理下のボードのCSVをダウンロードできる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};

		this.mockMvc.perform(get("/download/csv/1"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().string(expectedContent));
	}

	@Test
	public void test_チーム管理者は管理外のボードのCSVをダウンロードできない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_DOWNLOAD_CSV, "2");
		this.mockMvc.perform(get("/download/csv/2"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

	//================================================
	// HTMLダウンロード
	//================================================
	String expectedContentHTML = "<script>\r\n"
			+ "	/*<![CDATA[*/\r\n"
			+ "	var descs = [\r\n"
			+ "{id:'1', create_name: null, update_name: null, x: 101.0, y: 201.0, width: 301.0, height: 401.0, html:'HTML1'},\r\n"
			+ "{id:'2', create_name: null, update_name: null, x: 102.0, y: 202.0, width: 302.0, height: 402.0, html:'HTML2'}]\r\n"
			+ ";\r\n"
			+ "	/*]]>*/\r\n"
			+ "</script>";

	@Test
	public void test_全体管理者はボードのHTMLをダウンロードできる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("admin");
			}
		};

		this.mockMvc.perform(get("/download/html/1"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString(expectedContentHTML)));
	}

	@Test
	public void test_チーム管理者は管理下のボードのHTMLをダウンロードできる() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};

		this.mockMvc.perform(get("/download/html/1"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString(expectedContentHTML)));
	}

	@Test
	public void test_チーム管理者は管理外のボードのhtmlをダウンロードできない() throws Exception {
		new NonStrictExpectations() {
			{
				ControllerUtil.getLoginUser();
				result = getStubUser("user1_1");
			}
		};
		String message = messageUtil.decode(ErrorCode.ERR_CANNOT_DOWNLOAD_HTML, "2");
		this.mockMvc.perform(get("/download/html/2"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(Matchers.containsString(message)));
	}

}