package fr.vmarchaud.mineweb.common.configuration;

import lombok.Data;

@Data
public class PluginConfiguration extends Configuration {

	public String		logLevel = "FINE";
	public String		secretkey;
	public String		licenseId;
	public String		licenseKey;
	public String		domain;
}
