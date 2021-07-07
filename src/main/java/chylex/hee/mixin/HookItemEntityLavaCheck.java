package chylex.hee.mixin;
import chylex.hee.game.entity.item.EntityItemIgneousRock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(ItemEntity.class)
public abstract class HookItemEntityLavaCheck {
	@Redirect(
		method = "tick",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isTagged(Lnet/minecraft/tags/ITag;)Z"),
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"),
			to = @At(value = "FIELD", target = "Lnet/minecraft/util/SoundEvents;ENTITY_GENERIC_BURN:Lnet/minecraft/util/SoundEvent;")
		)
	)
	public boolean cancelLavaCheck(final FluidState state, final ITag<Fluid> tag) {
		final ItemEntity item = (ItemEntity)(Object)this;
		
		if (item instanceof EntityItemIgneousRock && tag == FluidTags.LAVA) {
			return false;
		}
		
		return state.isTagged(tag);
	}
}
