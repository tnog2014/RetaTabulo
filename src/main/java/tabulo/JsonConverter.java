/**
 * Copyright(c) 2020 tnog2014. All rights reserved.
 *
 * This file is part of RetaTabulo.
 *
 * RetaTabulo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RetaTabulo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RetaTabulo.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package tabulo;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonConverter<T> {

	private ObjectMapper objectMapper;

	/**
	 * コンストラクタ.
	 */
	public JsonConverter() {
		this.objectMapper = new ObjectMapper();
	}

	/**
	 * JSON文字列からMapオブジェクトに変換します.
	 *
	 * @param jsonStr JSON文字列
	 * @return Mapオブジェクト
	 * @throws IOException 変換に失敗した場合
	 */
	public Map<String, Object> convertToMap(String jsonStr) throws IOException {
		return this.objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {
		});
	}

	public String convertToJson(Map<String, Object> map) throws IOException {
		return this.objectMapper.writeValueAsString(map);
	}

}
