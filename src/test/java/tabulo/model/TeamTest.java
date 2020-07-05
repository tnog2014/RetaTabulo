package tabulo.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TeamTest {

	@Test
	public void test_getter_setter() {
		Board board = new Board();
		List<Board> boards = new ArrayList<Board>();
		boards.add(board);

		Team form = new Team();
		form.setId(123);
		form.setName("name");
		form.setDescription("description");
		form.setBoards(boards);
		assertEquals(Integer.valueOf(123), form.getId());
		assertEquals("name", form.getName());
		assertEquals("description", form.getDescription());
		assertEquals(boards, form.getBoards());
	}

}