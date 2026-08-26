package com.sayuki.itemscroller.forge;

import fi.dy.masa.itemscroller.Reference;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import fi.dy.masa.itemscroller.ItemScroller;
import fi.dy.masa.itemscroller.gui.GuiConfigs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkConstants;

// Forgeエントリポイント - mods.tomlから呼ばれる、クライアント限定で共通初期化と設定画面を登録する
@Mod(Reference.MOD_ID)
public class ItemScrollerForge {
    public ItemScrollerForge() {
        // クライアント限定 - サーバー側では何もしない、表示互換だけ確保する
        if (FMLLoader.getDist().isClient()) {
            ModContainer modContainer = ModLoadingContext.get().getActiveContainer();

            // サーバー無視マーク - このMODがサーバー側に無くても接続拒否にならないようにする
            modContainer.registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                    () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (remote, isLocal) -> true));

            // 共通初期化 - Fabric側と同じ流れでmasaのコードを呼ぶ
            ItemScroller.onInitialize();

            // 設定画面スイッチャー用 - Fabric側と同じく正式名で登録する
            Registry.CONFIG_SCREEN.registerConfigScreenFactory(
                    new ModInfo(Reference.MOD_ID, Reference.MOD_NAME, GuiConfigs::new)
            );

            // 設定画面登録 - ModMenuの代わりにForgeの拡張ポイントで出す
            modContainer.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> {
                        GuiConfigs gui = new GuiConfigs();
                        gui.setParent(parent);
                        return gui;
                    }));
        }
    }
}
