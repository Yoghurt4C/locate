package com.github.draylar.locate;

import com.github.draylar.locate.api.LocateRegistry;
import com.github.draylar.locate.command.BiomeCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.gen.feature.Feature;

public class Locate implements ModInitializer
{
	@Override
	public void onInitialize()
	{
		System.out.println("hi");
		if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
			LocateRegistry.registerLocatableFeature(Feature.SHIPWRECK);
		}

		CommandRegistry.INSTANCE.register(false, BiomeCommand::register);
	}
}
