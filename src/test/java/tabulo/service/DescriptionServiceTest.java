package tabulo.service;

import static org.junit.Assert.*;

import java.util.List;

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

import tabulo.controller.BoardController;
import tabulo.model.Description;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DescriptionServiceTest {

	@Autowired
	private WebApplicationContext webApplicationContext;
	private MockMvc mockMvc;

	@Autowired
	private BoardController boardController;

	//	@Autowired
	//	private TeamService teamService;

	@Autowired
	private DescriptionService service;

	@Before
	public void setUp() {
		System.out.println("★セットアップ");
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		//				List<Map<String, Object>> actualData = this.jdbcTemplate.queryForList("select * from login_user");
		//				System.out.println("★" + actualData);
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
			{"description", "insert into description "
					+ "(id, board_id, x, y, raw, html) values (1, 1, 101.0, 201.0, 'RAW1', 'HTML1')"},
			{"description", "insert into description "
					+ "(id, board_id, x, y, raw, html) values (2, 1, 102.0, 202.0, 'RAW2', 'HTML2')"},
			{"description", "insert into description "
					+ "(id, board_id, x, y, raw, html) values (3, 1, 103.0, 203.0, null, 'HTML3')"},
			{"description", "insert into description "
					+ "(id, board_id, x, y, raw, html) values (4, 2, 103.0, 203.0, null, 'HTML3')"},

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
	public void test_findAllDescriptionData_正常() throws Exception {
		List<Description> datas = service.findAllDescriptionData();
		assertEquals(4, datas.size());
	}

//	@Test
//	public void test_findByBoardId_正常() throws Exception {
//
//		List<Description> datas = service.findByBoardId(1);
//		assertEquals(3, datas.size());
//	}

	// TODO: processMessageのインターフェースを修正した。テストソースを修正すること。
	/*
	@Test
	public void test_create_生成_正常() throws Exception {
		WebSocketMessage message = new WebSocketMessage();
		message.setType("create");
		message.setBoardId(1);
		String data = "{\"x\":123.0,\"y\":234.0, \"raw\":\"RAW\",\"html\":\"HTML\"}";
		message.setData(data);
		WebSocketMessage ret = boardController.processMessage(message);
		String[] exp = new String[] { "1", "123.0", "234.0", "RAW", "HTML" };

		Optional<Description> description = service.findById(Integer.parseInt(ret.getId()));
		Description target = description.get();
		assertDescription(exp, target);
	}

	@Test
	public void test_create_移動_正常() throws Exception {
		WebSocketMessage message = new WebSocketMessage();
		message.setType("move");
		message.setId("1");
		message.setBoardId(1);
		String data = "{\"x\":823.0,\"y\":934.0}";
		message.setData(data);
		WebSocketMessage ret = boardController.processMessage(message);
		String[] exp = new String[] { "1", "823.0", "934.0", "RAW1", "HTML1" };

		Optional<Description> description = service.findById(Integer.parseInt(ret.getId()));
		Description target = description.get();
		assertDescription(exp, target);
	}

	@Test
	public void test_create_update_正常() throws Exception {
		WebSocketMessage message = new WebSocketMessage();
		message.setType("update");
		message.setId("1");
		message.setBoardId(1);
		String data = "{\"raw\":\"NEWRAW\",\"html\":\"NEWHTML\"}";
		message.setData(data);
		WebSocketMessage ret = boardController.processMessage(message);
		String[] exp = new String[] { "1", "101.0", "201.0", "NEWRAW", "NEWHTML" };

		Optional<Description> description = service.findById(Integer.parseInt(ret.getId()));
		Description target = description.get();
		assertDescription(exp, target);
	}

	@Test
	public void test_create_update_rawとhtmlのnull対応() throws Exception {
		WebSocketMessage message = new WebSocketMessage();
		message.setType("update");
		message.setId("1");
		message.setBoardId(1);
		String data = "{}";
		message.setData(data);
		WebSocketMessage ret = boardController.processMessage(message);
		String[] exp = new String[] { "1", "101.0", "201.0", "", null };

		Optional<Description> description = service.findById(Integer.parseInt(ret.getId()));
		Description target = description.get();
		assertDescription(exp, target);
	}

	@Test
	public void test_create_move_存在しない記述() throws Exception {
		WebSocketMessage message = new WebSocketMessage();
		message.setType("move");
		message.setId("123");
		message.setBoardId(1);
		String data = "{\"raw\":\"NEWRAW\",\"html\":\"NEWHTML\"}";
		message.setData(data);
		WebSocketMessage ret = boardController.processMessage(message);
		assertEquals("123", ret.getId());
		assertEquals(4, service.findAllDescriptionData().size());
	}

	@Test
	public void test_create_update_存在しない記述() throws Exception {
		WebSocketMessage message = new WebSocketMessage();
		message.setType("update");
		message.setId("123");
		message.setBoardId(1);
		String data = "{\"raw\":\"NEWRAW\",\"html\":\"NEWHTML\"}";
		message.setData(data);
		WebSocketMessage ret = boardController.processMessage(message);
		assertEquals("123", ret.getId());
		assertEquals(4, service.findAllDescriptionData().size());
	}

	@Test
	public void test_create_remove_正常() throws Exception {
		WebSocketMessage message = new WebSocketMessage();
		message.setType("remove");
		message.setId("1");
		String data = "{}";
		message.setData(data);
		WebSocketMessage ret = boardController.processMessage(message);
		assertEquals("1", ret.getId());
	}

	@Test
	public void test_create_想定外のタイプ() throws Exception {
		WebSocketMessage message = new WebSocketMessage();
		message.setType("NG");
		message.setId("1");
		String data = "{}";
		message.setData(data);
		try {
			boardController.processMessage(message);
			fail("想定する例外が発生しない。");
		} catch (RuntimeException e) {
			assertEquals(RuntimeException.class, e.getClass());
			assertEquals(
					"想定外のタイプが設定されている[NG]",
					e.getMessage());
		}
	}

	private void assertDescription(String[] exp, Description target) {
		assertEquals(Integer.valueOf(exp[0]), target.getBoardId());
		assertEquals(Double.valueOf(exp[1]), target.getX());
		assertEquals(Double.valueOf(exp[2]), target.getY());
		assertEquals(exp[3], target.getRaw());
		assertEquals(exp[4], target.getHtml());
	}
	*/

}