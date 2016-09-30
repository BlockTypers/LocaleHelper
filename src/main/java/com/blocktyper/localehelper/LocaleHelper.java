package com.blocktyper.localehelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.yaml.snakeyaml.Yaml;

public class LocaleHelper {
	private Locale locale = null;
	private ResourceBundle bundle = null;
	private boolean bundleLoadFailed = false;

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

	

	public String getLocalizedMessage(String key) {

		String value = key;
		try {
			if (bundle == null) {
				initLocaleFromTargetPlugin();

				if (locale == null) {
					logInfo("Using default locale.");
					locale = Locale.getDefault();
				}

				try {
					bundle = ResourceBundle.getBundle("resources/Messages", locale);
				} catch (Exception e) {
					logWarning("Messages bundle did not load successfully from default location.");
				}
				if (bundle == null) {
					logInfo("Checking for Messages bundle in secondary location.");
					try {
						bundle = ResourceBundle.getBundle("Messages", locale);
					} catch (Exception e) {
						logWarning("Messages bundle did not load successfully from secondary location.");
					}

					if (bundle == null) {
						logWarning(
								"Messages will appear as dot separated key names.  Please remove this plugin from your plugin folder if this behaviour is not desired.");
						bundleLoadFailed = true;
						return key;
					} else {
						logInfo("Messages bundle loaded successfully from secondary location.");
					}
				} else {
					logInfo("Messages bundle loaded successfully from default location.");
				}
			}

			if (bundleLoadFailed) {
				return key;
			}

			value = bundle.getString(key);

			value = key != null ? (value != null && !value.trim().isEmpty() ? value : key) : "null key";
		} catch (Exception e) {
			logWarning("Unexpected error getting localized string for key(" + key + "). Message: " + e.getMessage());
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	String getLocaleFromFileInputStream(InputStream inputStream) {
		Yaml yaml = new Yaml();
		String localeFromEssentials = null;
		Object configObject = yaml.load(inputStream);
		Map<String, Object> configMap = (Map<String, Object>) configObject;
		if (configMap != null) {
			if (configMap.containsKey("locale") && configMap.get("locale") != null) {
				localeFromEssentials = configMap.get("locale").toString();
				logInfo("locale found: " + configMap.get("locale").toString());
			}
		}
		return localeFromEssentials;
	}

	private void initLocaleFromTargetPlugin() {

		try {
			if (pluginsFolder == null || !pluginsFolder.isDirectory()) {
				logInfo("Could not locate plugin directory."
						+ (pluginsFolder == null ? "(null)" : "(Not a directory: " + pluginsFolder.getName() + ")"));
				return;
			}

			File essentialsDataFolder = null;
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
						essentialsDataFolder = file;
						break;
					}
				}
			}

			if (essentialsDataFolder == null) {
				logInfo("Could not locate " + targetPluginFolderName + " data folder.");
				return;
			}

			File essentialsConfigFile = null;
			if (essentialsDataFolder.listFiles() != null) {
				logInfo("Checking " + targetPluginFolderName + " data folder: " + essentialsDataFolder.getName() + "("
						+ (essentialsDataFolder.isDirectory() ? "dir" : "file") + ")");
				for (File file : essentialsDataFolder.listFiles()) {
					if (file == null || file.isDirectory()) {
						continue;
					}
					if (file.isDirectory()) {
						continue;
					}
					if (file.getName().startsWith("config.yml")) {
						logInfo(targetPluginFolderName + " config file found: " + file.getName());
						essentialsConfigFile = file;
					}
				}
			}

			if (essentialsConfigFile == null) {
				logInfo("Could not locate Essesntials config file.");
				return;
			}

			logInfo("loading " + targetPluginFolderName + " config file");
			String localeFromEssentials = getLocaleFromFileInputStream(new FileInputStream(essentialsConfigFile));

			if (localeFromEssentials == null || localeFromEssentials.trim().isEmpty()) {
				logInfo("Locale not set in " + targetPluginFolderName + " config.");
				return;
			}

			logInfo("Using locale from " + targetPluginFolderName + " config: " + localeFromEssentials);
			try {
				if (localeFromEssentials.contains("_")) {
					String language = localeFromEssentials.substring(0, localeFromEssentials.indexOf("_"));
					logInfo("language: " + language);
					String country = null;
					if (localeFromEssentials.length() > language.length()) {
						country = localeFromEssentials.substring(language.length() + 1);
						logInfo("country: " + country);
					}
					if (country == null) {
						locale = Locale.forLanguageTag(language);
					} else {
						locale = new Locale(language, country);
					}
				} else {
					locale = Locale.forLanguageTag(localeFromEssentials);
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
