package tabulo;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;

public class JsonConverterTest {

	@Test
	public void test_正常() throws IOException {
		JsonConverter conv = new JsonConverter();
		Map map = conv.convertToMap("{\"key1\":\"value1\"}");
		assertEquals(1, map.size());
		assertEquals("value1", map.get("key1"));
	}

	@Test
	public void test_異常() {
		JsonConverter conv = new JsonConverter();
		try {
			Map map = conv.convertToMap("NG_STRING");
		} catch (Exception e) {
			assertEquals(JsonParseException.class, e.getClass());
			assertEquals("Unrecognized token 'NG_STRING': was expecting 'null', 'true', 'false' or NaN\n" +
					" at [Source: (String)\"NG_STRING\"; line: 1, column: 19]", e.getMessage());
		}
	}

}
