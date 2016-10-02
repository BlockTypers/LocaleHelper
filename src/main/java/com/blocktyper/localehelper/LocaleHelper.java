package com.blocktyper.localehelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.yaml.snakeyaml.Yaml;

public class LocaleHelper {
	private Locale locale = null;

	private Logger logger;
	private File pluginsFolder;
	private String targetPluginFolderName;

	private static final String ESSENTIALS = "Essentials";

	LocaleHelper() {

	}

	public LocaleHelper(Logger logger, File pluginsFolder) {
		this(logger, pluginsFolder, null);
	}

	public LocaleHelper(Logger logger, File pluginFolder, String targetPluginFolderName) {
		this.logger = logger;
		this.pluginsFolder = pluginFolder;
		this.targetPluginFolderName = targetPluginFolderName;
		if (this.targetPluginFolderName == null || !this.targetPluginFolderName.isEmpty()) {
			this.targetPluginFolderName = ESSENTIALS;
		}
	}

	public Locale getLocale() {
		try {
			if (locale == null)
				initLocaleFromTargetPlugin();
		} catch (Exception e) {
			logWarning("Unexpected error getting locale from " + targetPluginFolderName + " config. Message: "
					+ e.getMessage());
		}
		if (locale == null) {
			logInfo("Using default locale.");
			locale = Locale.getDefault();
		}
		return locale;
	}

	@SuppressWarnings("unchecked")
	String getLocaleFromFileInputStream(InputStream inputStream) {
		Yaml yaml = new Yaml();
		String localeFromPlugin = null;
		Object configObject = yaml.load(inputStream);
		Map<String, Object> configMap = (Map<String, Object>) configObject;
		if (configMap != null) {
			if (configMap.containsKey("locale") && configMap.get("locale") != null) {
				localeFromPlugin = configMap.get("locale").toString();
				logInfo("locale found: " + configMap.get("locale").toString());
			}
		}
		return localeFromPlugin;
	}

	private void initLocaleFromTargetPlugin() {

		try {
			if (pluginsFolder == null || !pluginsFolder.isDirectory()) {
				logInfo("Could not locate plugin directory."
						+ (pluginsFolder == null ? "(null)" : "(Not a directory: " + pluginsFolder.getName() + ")"));
				return;
			}

			File pluginDataFolder = null;
			if (pluginsFolder.listFiles() != null) {
				logInfo("Checking Plugin folder.");
				for (File file : pluginsFolder.listFiles()) {
					if (file == null) {
						continue;
					}
					if (!file.isDirectory()) {
						continue;
					}
					if (file.getName().startsWith(targetPluginFolderName)) {
						pluginDataFolder = file;
						break;
					}
				}
			}

			if (pluginDataFolder == null) {
				logInfo("Could not locate " + targetPluginFolderName + " data folder.");
				return;
			}

			File pluginConfigFile = null;
			if (pluginDataFolder.listFiles() != null) {
				logInfo("Checking " + targetPluginFolderName + " data folder: " + pluginDataFolder.getName() + "("
						+ (pluginDataFolder.isDirectory() ? "dir" : "file") + ")");
				for (File file : pluginDataFolder.listFiles()) {
					if (file == null || file.isDirectory()) {
						continue;
					}
					if (file.isDirectory()) {
						continue;
					}
					if (file.getName().startsWith("config.yml")) {
						logInfo(targetPluginFolderName + " config file found: " + file.getName());
						pluginConfigFile = file;
					}
				}
			}

			if (pluginConfigFile == null) {
				logInfo("Could not locate " + pluginsFolder + " config file.");
				return;
			}

			logInfo("loading " + targetPluginFolderName + " config file");
			String localeFromPlugin = getLocaleFromFileInputStream(new FileInputStream(pluginConfigFile));

			if (localeFromPlugin == null || localeFromPlugin.trim().isEmpty()) {
				logInfo("Locale not set in " + targetPluginFolderName + " config.");
				return;
			}

			logInfo("Using locale from " + targetPluginFolderName + " config: " + localeFromPlugin);
			try {
				if (localeFromPlugin.contains("_")) {
					String language = localeFromPlugin.substring(0, localeFromPlugin.indexOf("_"));
					logInfo("language: " + language);
					String country = null;
					if (localeFromPlugin.length() > language.length()) {
						country = localeFromPlugin.substring(language.length() + 1);
						logInfo("country: " + country);
					}
					if (country == null) {
						locale = Locale.forLanguageTag(language);
					} else {
						locale = new Locale(language, country);
					}
				} else {
					locale = Locale.forLanguageTag(localeFromPlugin);
				}

			} catch (Exception e) {
				logInfo("Error using locale from " + targetPluginFolderName + " config: " + e.getMessage());
			}
		} catch (Exception e) {
			logInfo("Unexpected error while looking up locale from " + targetPluginFolderName + ": " + e.getMessage());
		}
	}

	private void logWarning(String warning) {
		if (logger != null) {
			logger.warning(warning);
		} else {
			System.out.println("[WARNING]: " + warning);
		}
	}

	private void logInfo(String info) {
		if (logger != null) {
			logger.info(info);
		} else {
			System.out.println("[INFO]: " + info);
		}
	}

}
