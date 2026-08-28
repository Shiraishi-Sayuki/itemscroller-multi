package com.sayuki.itemscroller.neoforge;

import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import fi.dy.masa.itemscroller.ItemScroller;
import fi.dy.masa.itemscroller.gui.GuiConfigs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// NeoForge用エントリーポイント - クライアント限定で共通初期化と設定画面登録をやる
// ItemScrollerはネットワーク通信が無いのでペイロード登録は不要
@Mod(value = "itemscroller", dist = Dist.CLIENT)
public class ItemScrollerNeoForge {
    public ItemScrollerNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        // 共通初期化 - Fabric側と同じ流れでmasaのコードを呼ぶ
        ItemScroller.onInitialize();

        // 設定画面登録 - Mod一覧のボタンから開けるようにする、Supplierは明示キャストしないとオーバーロードで曖昧になる
        Registry.CONFIG_SCREEN.registerConfigScreenFactory(new ModInfo("itemscroller", "Item Scroller", GuiConfigs::new));
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (java.util.function.Supplier<IConfigScreenFactory>) () -> (IConfigScreenFactory) (minecraft, parent) -> {
                    GuiConfigs gui = new GuiConfigs();
                    gui.setParent(parent);
                    return gui;
                });
    }
}
