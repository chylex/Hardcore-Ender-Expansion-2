package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.BlockVine
import chylex.hee.system.migration.vanilla.EntityLivingBase
import chylex.hee.system.util.color.IntColor
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.color.IBlockColor
import net.minecraft.util.math.BlockPos
import net.minecraft.world.FoliageColors
import net.minecraft.world.IEnviromentBlockReader
import net.minecraft.world.IWorldReader
import net.minecraft.world.World
import net.minecraft.world.biome.BiomeColors
import net.minecraft.world.biome.Biomes
import java.util.Random

class BlockDryVines(builder: BlockBuilder) : BlockVine(builder.p){
	
	// Custom behavior
	
	override fun tick(state: BlockState, world: World, pos: BlockPos, rand: Random){}
	
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
		
		override fun getColor(state: BlockState, world: IEnviromentBlockReader?, pos: BlockPos?, tintIndex: Int): Int{
			if (world == null || pos == null){
				return DEFAULT
			}
			
			val fromTerritory = world.getBiome(pos).takeIf { it === Biomes.THE_END }?.let { TerritoryInstance.fromPos(pos) }?.let { it.territory.desc.colors.dryVines }
			return fromTerritory ?: dryify(BiomeColors.getFoliageColor(world, pos))
		}
	}
}
