package sawfowl.localetest;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import sawfowl.localeapi.serializetools.CompoundTag;

@ConfigSerializable
public class CustomNBT extends CompoundTag {

	@Setting("TestKey")
	private String test = "Test Value 1";
	@Setting("CustomNBT2")
	private CustomNBT2 customNBT = new CustomNBT2();

	String getString() {
		return test;
	}

	public CustomNBT2 getCustomNBT2() {
		return customNBT;
	}
	
}