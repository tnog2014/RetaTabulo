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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("file:./config/tabulo.properties")
@ConfigurationProperties(
		prefix = "tabulo")
public class PropertyAccessor {

	private int connectionCheckInterval;

	private int sendBeaconInterval;

	private String allowedOrigins;

	private String locales;

	private boolean debug = false;

	private String logOutputLocale;

	/** この時間以上接続確認が取れないユーザーを退室とみなす */
	private int thresholdPeriodInSeconds;

	public int getConnectionCheckInterval() {
		return connectionCheckInterval;
	}

	public int getSendBeaconInterval() {
		return sendBeaconInterval;
	}

	public void setConnectionCheckInterval(int connectionCheckInterval) {
		this.connectionCheckInterval = connectionCheckInterval;
	}

	public void setSendBeaconInterval(int sendBeaconInterval) {
		this.sendBeaconInterval = sendBeaconInterval;
	}

	public String getAllowedOrigins() {
		return allowedOrigins;
	}

	public void setAllowedOrigins(String allowedOrigins) {
		this.allowedOrigins = allowedOrigins;
	}

	public int getThresholdPeriodInSeconds() {
		return thresholdPeriodInSeconds;
	}

	public void setThresholdPeriodInSeconds(int thresholdPeriodInSeconds) {
		this.thresholdPeriodInSeconds = thresholdPeriodInSeconds;
	}

	public String getLocales() {
		return locales;
	}

	public void setLocales(String locales) {
		this.locales = locales;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public String getLogOutputLocale() {
		return logOutputLocale;
	}

	public void setLogOutputLocale(String logOutputLocale) {
		this.logOutputLocale = logOutputLocale;
	}

}
