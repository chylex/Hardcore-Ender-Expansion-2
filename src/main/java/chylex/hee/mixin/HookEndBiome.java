package chylex.hee.mixin;
import chylex.hee.client.render.TerritoryRenderer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.TheEndBiome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(TheEndBiome.class)
public abstract class HookEndBiome extends Biome{
	protected HookEndBiome(final Builder builder){
		super(builder);
	}
	
	/**
	 * @author Hardcore Ender Expansion
	 */
	@Override
	@Overwrite
	@OnlyIn(Dist.CLIENT)
	public int getSkyColor(){
		return TerritoryRenderer.getSkyColor();
	}
}
