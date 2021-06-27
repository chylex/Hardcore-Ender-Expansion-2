package chylex.hee.game.block

import chylex.hee.client.MC
import chylex.hee.client.render.block.IBlockLayerCutout
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.system.color.IntColor
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.BlockVine
import chylex.hee.system.migration.EntityLivingBase
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.color.IBlockColor
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.FoliageColors
import net.minecraft.world.IBlockDisplayReader
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorldReader
import net.minecraft.world.biome.BiomeColors
import net.minecraft.world.biome.Biomes
import net.minecraft.world.server.ServerWorld
import java.util.Random

class BlockDryVines(builder: BlockBuilder) : BlockVine(builder.p), IBlockLayerCutout {
	
	// Custom behavior
	
	override fun tick(state: BlockState, world: ServerWorld, pos: BlockPos, rand: Random) {}
	
	override fun isLadder(state: BlockState, world: IWorldReader, pos: BlockPos, entity: EntityLivingBase): Boolean {
		return !entity.isOnGround
	}
	
	override fun getFlammability(state: BlockState, world: IBlockReader, pos: BlockPos, face: Direction): Int {
		return 300
	}
	
	override fun getFireSpreadSpeed(state: BlockState, world: IBlockReader, pos: BlockPos, face: Direction): Int {
		return 100
	}
	
	// Client side
	
	@Sided(Side.CLIENT)
	object Color : IBlockColor {
		private val DEFAULT = dryify(FoliageColors.getDefault())
		
		private fun dryify(color: Int): Int {
			val hsb = IntColor(color).asHSB
			
			return hsb.copy(
				saturation = hsb.saturation * 0.6F,
				brightness = hsb.brightness * 0.8F
			).i
		}
		
		override fun getColor(state: BlockState, uselessWorld: IBlockDisplayReader?, pos: BlockPos?, tintIndex: Int): Int {
			val world = MC.world
			
			if (world == null || pos == null) {
				return DEFAULT
			}
			
			val biomeRegistry = world.func_241828_r().getRegistry(Registry.BIOME_KEY) // RENAME getRegistries
			val fromTerritory = world.getBiome(pos).takeIf { biomeRegistry.getKey(it) == Biomes.THE_END }?.let { TerritoryType.fromPos(pos) }?.let { it.desc.colors.dryVines }
			return fromTerritory ?: dryify(BiomeColors.getFoliageColor(world, pos))
		}
	}
}
