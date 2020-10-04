package chylex.hee.game.block
import chylex.hee.client.MC
import chylex.hee.client.render.block.IBlockLayerCutout
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.system.color.IntColor
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.BlockVine
import chylex.hee.system.migration.EntityLivingBase
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.color.IBlockColor
import net.minecraft.util.math.BlockPos
import net.minecraft.world.FoliageColors
import net.minecraft.world.ILightReader
import net.minecraft.world.IWorldReader
import net.minecraft.world.biome.BiomeColors
import net.minecraft.world.biome.Biomes
import net.minecraft.world.server.ServerWorld
import java.util.Random

class BlockDryVines(builder: BlockBuilder) : BlockVine(builder.p), IBlockLayerCutout{
	
	// Custom behavior
	
	override fun tick(state: BlockState, world: ServerWorld, pos: BlockPos, rand: Random){}
	
	override fun isLadder(state: BlockState, world: IWorldReader, pos: BlockPos, entity: EntityLivingBase): Boolean{
		return !entity.onGround
	}
	
	// Client side
	
	@Sided(Side.CLIENT)
	object Color : IBlockColor{
		private val DEFAULT = dryify(FoliageColors.getDefault())
		
		private fun dryify(color: Int): Int{
			val hsb = IntColor(color).asHSB
			
			return hsb.copy(
				saturation = hsb.saturation * 0.6F,
				brightness = hsb.brightness * 0.8F
			).i
		}
		
		override fun getColor(state: BlockState, uselessWorld: ILightReader?, pos: BlockPos?, tintIndex: Int): Int{
			val world = MC.world
			
			if (world == null || pos == null){
				return DEFAULT
			}
			
			val fromTerritory = world.getBiome(pos).takeIf { it === Biomes.THE_END }?.let { TerritoryInstance.fromPos(pos) }?.let { it.territory.desc.colors.dryVines }
			return fromTerritory ?: dryify(BiomeColors.getFoliageColor(world, pos))
		}
	}
}
