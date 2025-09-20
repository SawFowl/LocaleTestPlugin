package sawfowl.localetest;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import com.google.inject.Inject;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.LocalesList;
import sawfowl.localeapi.api.Logger;
import sawfowl.localeapi.api.config.locale.PluginLocale;
import sawfowl.localeapi.api.event.LocaleServiseEvent;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.api.serializetools.itemstack.SerializedItemStack;

@Plugin("localetest")
public class LocaleTest {
	private Logger logger;
	//private boolean saveHocon = false;
	private boolean saveYaml = false;
	private boolean saveJson = false;
	private boolean saveProperties = false;
	private LocaleService api;
	private static PluginContainer pluginContainer;
	private LocalesList locales;

	public LocaleService getAPI() {
		return api;
	}

	public static PluginContainer getPluginContainer() {
		return pluginContainer;
	}

	@Inject
	public LocaleTest(PluginContainer pluginContainer) {
		LocaleTest.pluginContainer = pluginContainer;
		logger = Logger.createJavaLogger("PluginForTestLocales");
	}

	@Listener
	public void onLocaleServisePostEvent(LocaleServiseEvent.Construct event) {
		api = event.getLocaleService();
		locales = api.createLocales(pluginContainer);
		try {
			api.setItemStackSerializerVariant(pluginContainer, ItemStackSerializerType.JSON);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Listener
	public void onEnable(StartedEngineEvent<Server> event) throws ConfigurateException {
		testWrite();
		// The Sponge configuration works in asynchronous mode. If reading is performed immediately after writing, a delay must be made.
		Sponge.asyncScheduler().submit(Task.builder().delay(Ticks.of(50)).plugin(pluginContainer).execute(() -> {
			testRead();
		}).build());
	}

	private void testWrite() {
		api.setDefaultReference(pluginContainer, LocaleConfig.class);
		if(!api.localesExist(pluginContainer)) {
			// When creating localizations, be sure to create a default localization - Locales.DEFAULT.
			// If the above check is performed, the localization creation will not be performed because it is unnecessary.
			locales.createSimpleTranslation(ConfigTypes.HOCON, Locales.DEFAULT);
			locales.createSimpleTranslation(ConfigTypes.YAML, Locales.EN_CA);
			locales.createSimpleTranslation(ConfigTypes.JSON, Locales.EN_GB);
			locales.createSimpleTranslation(ConfigTypes.HOCON, Locales.RU_RU);
			locales.saveAssetLocales();
		}

		getLocaleUtil(Locales.DEFAULT).save();
		// Test write and save locale - en-CA.
		boolean checkYaml = updateIsSave(saveYaml, getLocaleUtil(Locales.EN_CA).addIfNotExist("&a&len-CA locale. &4&lTest String YAML config", "Test comment", "TestPath"));
		checkYaml = updateIsSave(this.saveYaml, getLocaleUtil(Locales.EN_CA).addIfNotExist(serialize("&a&len-CA locale. &4&lTest JSON string YAML"), "Test comment", "TestComponentPath"));
		checkYaml = updateIsSave(this.saveYaml, getLocaleUtil(Locales.EN_CA).addIfNotExist(String.class, Arrays.asList("&a&len-CA locale. &4&lTest Strings YAML config", "String 2"), "Test comment", "TestListPath"));
		checkYaml = updateIsSave(this.saveYaml,  getLocaleUtil(Locales.EN_CA).addIfNotExist(Component.class, Arrays.asList(serialize("&a&len-CA locale. &4&lTest JSON strings YAML config"), serialize("Component String 2")), "Test comment", "TestListComponentsPath"));
		if(checkYaml) getLocaleUtil(Locales.EN_CA).save();
		
		// Test write and save locale - en-GB.
		boolean checkJson = updateIsSave(saveJson, getLocaleUtil(Locales.EN_GB).addIfNotExist("&a&len-GB locale. &4&lTest String JSON config", null, "TestPath"));
		checkJson = updateIsSave(this.saveJson, getLocaleUtil(Locales.EN_GB).addIfNotExist(serialize("&a&len-GB locale. &4&lTest JSON string JSON"), null, "TestComponentPath"));
		checkJson = updateIsSave(this.saveJson, getLocaleUtil(Locales.EN_GB).addIfNotExist(String.class, Arrays.asList("&a&len-GB locale. &4&lTest Strings JSON config", "String 2"), null, "TestListPath"));
		checkJson = updateIsSave(this.saveJson,  getLocaleUtil(Locales.EN_GB).addIfNotExist(Arrays.asList(serialize("&a&len-GB locale. &4&lTest JSON strings JSON config"), serialize("Component String 2")), null, "TestListComponentsPath"));
		if(checkJson) getLocaleUtil(Locales.EN_GB).save();
		
		// Test write and save locale - ru-RU.
		boolean checkLegacy = updateIsSave(saveProperties, getLocaleUtil(Locales.RU_RU).addIfNotExist("&a&lЛокализация ru-RU. &4&lТест строки конфига PROPERTIES", null, "TestPath", "TestPath2"));
		checkLegacy = updateIsSave(this.saveProperties, getLocaleUtil(Locales.RU_RU).addIfNotExist(serialize("&a&lЛокализация ru-RU. &4&lТест JSON строки конфига PROPERTIES"), null, "TestComponentPath"));
		checkLegacy = updateIsSave(this.saveProperties, getLocaleUtil(Locales.RU_RU).addIfNotExist(String.class, Arrays.asList("&a&lЛокализация ru-RU. &4&lТест строк конфига PROPERTIES", "Строка 2"), null, "TestListPath"));
		checkLegacy = updateIsSave(this.saveProperties,  getLocaleUtil(Locales.RU_RU).addIfNotExist(Component.class, Arrays.asList(serialize("&a&lЛокализация ru-RU. &4&lТест JSON строк конфига PROPERTIES"), serialize("Строка компонент 2")), null, "TestListComponentsPath"));
		if(checkLegacy) getLocaleUtil(Locales.RU_RU).save();
	}

	private void testRead() {
		
		logger.info("Total locales created/saved -> " + locales.size());
		
		try {
			logger.warn("Start test getting ItemStack from config!");
			ItemStack itemStack = getLocaleUtil(Locales.DEFAULT).getRootNode().node("ItemStack").get(ItemStack.class);
			SerializedItemStack stack = new SerializedItemStack(itemStack);
			logger.info(itemStack.get(Keys.CUSTOM_NAME));
			logger.info(itemStack.get(Keys.ITEM_DURABILITY));
			logger.info(itemStack.get(Keys.LORE));
			logger.info(itemStack.get(Keys.APPLIED_ENCHANTMENTS));
			logger.info(stack.getItemStack().type().asComponent());
			logger.info("TestInt -> " + stack.getOrCreateComponent().getObject(pluginContainer, "TestInt", -1));
			logger.info("TestMap -> " + stack.getOrCreateComponent().getObjectsMap(String.class, String.class, pluginContainer, "TestMap", Arrays.asList("Failed!").stream().collect(Collectors.toMap(k -> k, v -> "Error!"))));
			logger.info("TestArray -> " + stack.getOrCreateComponent().getObjectsList(String.class, pluginContainer, "TestArray", Arrays.asList("Failed!")));
			logger.info("TestTag -> " + stack.getOrCreateComponent().getPluginComponent(CustomNBT.class, pluginContainer, "TestTag"));
		} catch (SerializationException e) {
			logger.error(e.getLocalizedMessage());
		}
		//Test writed strings
		logger.warn("Start test strings! TestPath");
		logger.info(getLocaleUtil(Locales.DEFAULT).toReferenceTranslation(LocaleConfig.class).get().getTestPath()); // I deliberately indicated the wrong localization in the code.
		logger.info(getString(Locales.EN_CA, "TestPath"));
		logger.info(getString(Locales.EN_GB, "TestPath"));
		logger.info(getString(Locales.RU_RU, "TestPath", "TestPath2"));
		logger.info(getString(Locales.RU_RU, "TestPath1"));
		logger.info(getString(Locales.RU_RU, "TestNulledPath"));
		
		//test writed components
		logger.warn("Start test components! TestComponentPath");
		logger.info(getComponent(Locales.CA_ES, "TestComponentPath"));
		logger.info(getLocaleUtil(Locales.EN_CA).toReferenceTranslation(LocaleConfig.class).get().getTestComponentPath());
		logger.info(getComponent(Locales.EN_GB, "TestComponentPath"));
		logger.info(getComponent(Locales.RU_RU, "TestComponentPath"));
		
		//Test writed list strings
		logger.warn("Start test list strings! TestListPath");
		logger.info(getListStrings(Locales.CA_ES, "TestListPath").get(0));
		logger.info(getListStrings(Locales.CA_ES, "TestListPath").get(1));
		logger.info(getListStrings(Locales.EN_CA, "TestListPath").get(0));
		logger.info(getListStrings(Locales.EN_CA, "TestListPath").get(1));
		logger.info(getListStrings(Locales.EN_GB, "TestListPath").get(0));
		logger.info(getListStrings(Locales.EN_GB, "TestListPath").get(1));
		logger.info(getListStrings(Locales.RU_RU, "TestListPath").get(0));
		logger.info(getListStrings(Locales.RU_RU, "TestListPath").get(1));
		
		//test writed list components
		logger.warn("Start test list components! TestListComponentsPath");
		logger.info(getListComponents(Locales.CA_ES, "TestListComponentsPath").get(0));
		logger.info(getListComponents(Locales.CA_ES, "TestListComponentsPath").get(1));
		logger.info(getListComponents(Locales.EN_CA, "TestListComponentsPath").get(0));
		logger.info(getListComponents(Locales.EN_CA, "TestListComponentsPath").get(1));
		logger.info(getListComponents(Locales.EN_GB, "TestListComponentsPath").get(0));
		logger.info(getListComponents(Locales.EN_GB, "TestListComponentsPath").get(1));
		logger.info(getListComponents(Locales.RU_RU, "TestListComponentsPath").get(0));
		logger.info(getListComponents(Locales.RU_RU, "TestListComponentsPath").get(1));
	}

	private Component serialize(String string) {
		return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
	}

	private PluginLocale getLocaleUtil(Locale locale) {
		return locales.getLocale(locale);
	}

	private String getString(Locale locale, Object... path) {
		return getLocaleUtil(locale).getString(path);
	}

	private List<String> getListStrings(Locale locale, Object... path) {
		return getLocaleUtil(locale).getList(String.class, path);
	}

	private Component getComponent(Locale locale, Object... path) {
		return getLocaleUtil(locale).getComponent(path);
	}

	private List<Component> getListComponents(Locale locale, Object... path) {
		return getLocaleUtil(locale).getComponents(path);
	}

	private boolean updateIsSave(boolean currentResult, boolean check) {
		if(check) currentResult = check;
		return currentResult;
	}

}
