package nhb.mario.test.external.configuration;

import java.util.Collection;
import java.util.Properties;

import com.mario.entity.impl.BaseLifeCycle;
import com.mario.external.configuration.ExternalConfiguration;
import com.nhb.common.async.Callback;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObjectRO;

public class ExternalConfigurationHandler extends BaseLifeCycle {

	@Override
	public void init(PuObjectRO initParams) {
		String textConfiguration = initParams.getString("textConfig", null);
		if (textConfiguration != null) {
			ExternalConfiguration extConfig = getApi().getExternalConfiguration(textConfiguration);
			getLogger().debug("Text config: " + extConfig.get());
			extConfig.addUpdateListener(new Callback<Collection<String>>() {

				@Override
				public void apply(Collection<String> result) {
					getLogger().debug("text config change: {}", result);
				}
			});
		}

		String propertiesConfiguration = initParams.getString("propertiesConfig", null);
		if (propertiesConfiguration != null) {
			ExternalConfiguration extConfig = getApi().getExternalConfiguration(propertiesConfiguration);
			getLogger().debug("Properties config: " + extConfig.get());
			extConfig.addUpdateListener(new Callback<Properties>() {

				@Override
				public void apply(Properties result) {
					getLogger().debug("Properties config change: {}", result);
				}
			});
		}

		String puElementXmlConfiguration = initParams.getString("puElementXmlFileConfig", null);
		if (puElementXmlConfiguration != null) {
			ExternalConfiguration extConfig = getApi().getExternalConfiguration(puElementXmlConfiguration);
			getLogger().debug("PuElement config: " + extConfig.get());
			extConfig.addUpdateListener(new Callback<PuElement>() {

				@Override
				public void apply(PuElement result) {
					getLogger().debug("PuElement config change: {}", result);
				}
			});
		}

		String yamlConfiguration = initParams.getString("yamlConfig", null);
		if (yamlConfiguration != null) {
			ExternalConfiguration extConfig = getApi().getExternalConfiguration(yamlConfiguration);
			getLogger().debug("Yaml config: " + extConfig.get());
			extConfig.addUpdateListener(new Callback<CustomConfig>() {

				@Override
				public void apply(CustomConfig result) {
					getLogger().debug("Yaml config change: {}", result);
				}
			});
		}

		String customParserConfiguration = initParams.getString("customParserConfig", null);
		if (yamlConfiguration != null) {
			ExternalConfiguration extConfig = getApi().getExternalConfiguration(customParserConfiguration);
			getLogger().debug("Custom parser config: " + extConfig.get());
			extConfig.addUpdateListener(new Callback<Object>() {

				@Override
				public void apply(Object result) {
					getLogger().debug("Custom parser config change: {}", result);
				}
			});
		}
	}
}
