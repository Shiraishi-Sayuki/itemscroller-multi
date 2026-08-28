package com.sayuki.itemscroller.fabric;

import net.fabricmc.api.ModInitializer;
import fi.dy.masa.itemscroller.ItemScroller;

// Fabric用エントリーポイント - fabric.mod.jsonから呼ばれる、共通初期化に丸投げ
// MOD自体はクライアント限定(environment: client)なのでModInitializerで問題ない
public class ItemScrollerFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ItemScroller.onInitialize();
    }
}
