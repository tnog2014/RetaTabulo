package tabulo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public class CheckI18NMessages {

	private static final String HTML_DIR = "src/main/resources/templates";

	private static final String MESSAGE_DIR = "src/main/resources/messages";

	public static void main(String[] args) throws IOException {
		CheckI18NMessages main = new CheckI18NMessages();
		main.check();
	}

	private static final Pattern pat = Pattern.compile("#\\{([a-zA-Z0-9_\\.\\-]*)\\}");

	public void check() throws IOException {
		List<String> list = extractMessageKeys();

		System.out.println("===== 利用しているキー =====");
		for (String item : list) {
			System.out.println(item);
		}
		System.out.println("===== チェック開始 =====");
		File[] messages = new File(MESSAGE_DIR).listFiles();
		if (messages != null) {
			for (File message : messages) {
				if (!message.getName().endsWith("properties")) {
					continue;
				}
				Map<String, String> map = getPropertyMap(message);
				for (String item : list) {
					if (!map.keySet().contains(item)) {
						System.out.println("★定義されていません:" + message.getName() + ":" + item);
					}
				}
			}
		}
		System.out.println("===== チェック終了 =====");
	}

	private List<String> extractMessageKeys() throws IOException {
		File[] htmls = new File(HTML_DIR).listFiles();
		Set<String> set = new HashSet<String>();
		if (htmls != null) {
			for (File html : htmls) {
				String content = FileUtils.readFileToString(html, "UTF8");
				Matcher mat = pat.matcher(content);
				while (mat.find()) {
					String group = mat.group(1);
					set.add(group);
				}
			}
		}
		List<String> list = new ArrayList<String>(set);
		Collections.sort(list);
		return list;
	}

	public Map<String, String> getPropertyMap(File message) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		List<String> lines = FileUtils.readLines(message, "UTF8");
		for (String line : lines) {
			if (line.trim().length() == 0 && line.startsWith("#")) {
				continue;
			}
			if (line.trim().length() > 0) {
				String[] items = split(line);
				String key = items[0];
				String value = items[1];
				map.put(key, value);
			}
		}
		return map;
	}

	public String[] split(String keyValue) {
		int index = keyValue.indexOf("=");
		String key = keyValue.substring(0, index);
		String value = keyValue.substring(index + 1, keyValue.length());
		return new String[]{key, value};
	}
}
