package com.litestatbars;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class LiteStatBarsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(LiteRegenMeterPlugin.class);
		RuneLite.main(args);
	}
}