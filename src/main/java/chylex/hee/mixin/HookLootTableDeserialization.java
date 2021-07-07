package chylex.hee.mixin;

import chylex.hee.game.loot.LootTablePatcher;
import com.google.common.base.MoreObjects;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.lang.reflect.Type;

@Mixin(LootTable.Serializer.class)
@SuppressWarnings("MethodMayBeStatic")
public abstract class HookLootTableDeserialization {
	
	@SuppressWarnings("ZeroLengthArrayAllocation")
	@Inject(method = "deserialize", at = @At("HEAD"), cancellable = true)
	private void patchDeserialization(final JsonElement json, final Type type, final JsonDeserializationContext context, final CallbackInfoReturnable<LootTable> ci) {
		final JsonObject root = JSONUtils.getJsonObject(json, "loot table");
		
		if (root.has("hee:patch")) {
			final LootPool[] pools = JSONUtils.deserializeClass(root, "pools", new LootPool[0], context, LootPool[].class);
			final ILootFunction[] functions = JSONUtils.deserializeClass(root, "functions", new ILootFunction[0], context, ILootFunction[].class);
			final LootParameterSet parameterSet = MoreObjects.firstNonNull(LootParameterSets.getValue(new ResourceLocation(JSONUtils.getString(root, "type"))), LootParameterSets.GENERIC);
			
			ci.setReturnValue(LootTablePatcher.patch(JSONUtils.getString(root, "hee:patch"), parameterSet, pools, functions));
		}
	}
}
