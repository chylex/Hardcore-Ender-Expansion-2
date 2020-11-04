package chylex.hee.mixin;
import net.minecraft.entity.monster.EndermiteEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(EndermiteEntity.class)
public abstract class HookEndermiteParticles{
	@ModifyConstant(
		method = "livingTick",
		constant = @Constant(intValue = 2, ordinal = 0),
		slice = @Slice(
			from = @At(value = "FIELD", target = "Lnet/minecraft/world/World;isRemote:Z"),
			to = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V")
		)
	)
	private int getLivingTickParticleCount(final int originalValue){
		return 0;
	}
}
