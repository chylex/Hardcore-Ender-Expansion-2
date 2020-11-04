package chylex.hee.mixin;
import chylex.hee.game.entity.living.EntityMobAngryEnderman;
import chylex.hee.game.entity.living.EntityMobEnderman;
import net.minecraft.entity.monster.EndermanEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(EndermanEntity.class)
public abstract class HookEndermanParticles{
	@ModifyConstant(
		method = "livingTick",
		constant = @Constant(intValue = 2, ordinal = 0),
		slice = @Slice(
			from = @At(value = "FIELD", target = "Lnet/minecraft/world/World;isRemote:Z"),
			to = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V")
		)
	)
	private int getLivingTickParticleCount(final int originalValue){
		final EndermanEntity me = (EndermanEntity)(Object)this;
		
		if (me instanceof EntityMobAngryEnderman){
			return 0;
		}
		else if (me instanceof EntityMobEnderman){
			return ((EntityMobEnderman)me).isAggro() ? 0 : 2;
		}
		else{
			return 2;
		}
	}
}
