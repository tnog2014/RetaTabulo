package tabulo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import tabulo.constant.ErrorCode;
import tabulo.model.Description;
import tabulo.repository.DescriptionRepository;

@Controller
public class URIEncodeConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(URIEncodeConverter.class);

	@Autowired
	DescriptionRepository descRepository;

	/**
	 * 旧型データ保持方法のレコードを新型にコンバートする.
	 *
	 * 初期のデータ保持方法ではdescriptionに特殊記号が入る場合に正しく表示できないことが分かったため、
	 * データ保持方法を変更した。
	 * raw
	 * ・初期：JSON.stringifyした文字列を保持
	 * ・今回：JSON.stringifyした文字列をencodeURIComponentした文字列を保持
	 * html
	 * ・初期：いくつかの文字をエスケープした状態で保持
	 * ・今回：rawと同様。
	 *
	 * @throws ApplicationException
	 */
	public void convert() throws ApplicationException {
		LOGGER.info("convert start");
		List<Description> list = descRepository.findAll();
		for (Description desc : list) {
			String raw = desc.getRaw();
			String html = desc.getHtml();
			boolean flagUpdate = false;
			try {
				if (!raw.startsWith("%7B")) {
					flagUpdate = true;
					String converted = encodeURIComponent(raw);
					LOGGER.info(String.format(
							"Conversion target (raw) [%d]:[%s]->[%s]",
							desc.getId(), raw, converted));
					desc.setRaw(converted);
				}
				if (!html.startsWith("%22")) {
					flagUpdate = true;
					html = html.replaceAll("\"", "\\\\\""); // 文字列のJSON.stringifyで行われるエスケープ
					String converted = encodeURIComponent("\"" + html + "\"");
					LOGGER.info(String.format(
							"Conversion target (html) [%d]:[%s]->[%s]",
							desc.getId(), html, converted));
					desc.setHtml(converted);
				}

				// 作業用
				//	String html2 = desc.getHtml();
				//	String innerHtml = html2.replaceAll("^%22", "")
				//			.replaceAll("%22$", "");
				//	if (innerHtml.contains("%22")) {
				//		System.out.println("★bef:" + innerHtml);
				//		innerHtml = innerHtml.replaceAll("%22", "%5C%22");
				//		innerHtml = innerHtml.replaceAll("%5C%5C%22", "%5C%22");
				//		System.out.println("★aft:" + innerHtml);
				//		desc.setHtml("%22" + innerHtml + "%22");
				//		flagUpdate = true;
				//	}

				if (flagUpdate) {
					LOGGER.info(String.format("Update desc of %d", desc.getId()));
					descRepository.save(desc);
				}
			} catch (Exception e) {
				throw new ApplicationException(e,
						ErrorCode.ERR_FAILURE_IN_CONVERSION, desc.getId());
			}
		}
		LOGGER.info("convert end");
	}

	public String encodeURIComponent(String url) throws UnsupportedEncodingException {

		return URLEncoder.encode(url, "UTF-8")
				.replaceAll("\\+", "%20")
				.replaceAll("\\%21", "!")
				.replaceAll("\\%27", "'")
				.replaceAll("\\%28", "(")
				.replaceAll("\\%29", ")")
				.replaceAll("\\%7E", "~");
	}

}
