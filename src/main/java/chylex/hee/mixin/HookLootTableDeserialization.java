package chylex.hee.mixin;

import chylex.hee.game.loot.LootTablePatcher;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.util.JSONUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LootTable.Serializer.class)
public abstract class HookLootTableDeserialization {
	@Redirect(method = "deserialize", at = @At(value = "NEW", target = "net/minecraft/loot/LootTable"), require = 1)
	private LootTable redirectConstructor(final LootParameterSet parameterSet, final LootPool[] pools, final ILootFunction[] functions, final JsonElement json) {
		final JsonObject root = JSONUtils.getJsonObject(json, "loot table");
		
		if (root.has("hee:patch")) {
			return LootTablePatcher.patch(JSONUtils.getString(root, "hee:patch"), parameterSet, pools, functions);
		}
		else {
			return new LootTable(parameterSet, pools, functions);
		}
	}
}
