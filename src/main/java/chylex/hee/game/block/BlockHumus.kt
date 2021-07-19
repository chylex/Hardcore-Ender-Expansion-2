package chylex.hee.game.block

import chylex.hee.game.Resource.location
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockStateModel
import chylex.hee.game.block.properties.BlockStatePreset
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.game.world.util.breakBlock
import chylex.hee.game.world.util.getBlock
import chylex.hee.game.world.util.getState
import chylex.hee.init.ModBlocks
import chylex.hee.util.math.center
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.BushBlock
import net.minecraft.block.SaplingBlock
import net.minecraft.block.SugarCaneBlock
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.loot.LootContext
import net.minecraft.loot.LootParameters
import net.minecraft.util.Direction
import net.minecraft.util.Direction.UP
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Explosion
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.common.IPlantable
import net.minecraftforge.common.PlantType
import java.util.Random

class BlockHumus(builder: BlockBuilder, mergeBottom: Block) : BlockSimpleMergingBottom(builder, mergeBottom) {
	override val model
		get() = BlockStateModel(
			BlockStatePreset.None,
			BlockModel.Multi(
				BlockModel.Cube,
				BlockModel.Suffixed("_merge", BlockModel.CubeBottomTop(
					side   = this.location("_merge"),
					bottom = ModBlocks.ENDERSOL.location("_top"),
					top    = this.location,
				))
			),
			ItemModel.AsBlock
		)
	
	override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, rand: Random) {
		@Suppress("DEPRECATION")
		super.randomTick(state, world, pos, rand)
		
		if (rand.nextInt(5) <= 1) {
			val plantPos = pos.up()
			val plant = plantPos.getBlock(world)
			
			if (plant is IPlantable && canSustainPlant(state, world, pos, UP, plant)) {
				@Suppress("DEPRECATION")
				plant.randomTick(plantPos.getState(world), world, plantPos, rand)
			}
		}
	}
	
	override fun canSustainPlant(state: BlockState, world: IBlockReader, pos: BlockPos, direction: Direction, plant: IPlantable): Boolean {
		val type = plant.getPlantType(world, pos)
		
		return (
			type == PlantType.CROP ||
			type == PlantType.PLAINS ||
			plant is SaplingBlock ||
			plant is SugarCaneBlock ||
			(plant is BushBlock && super.canSustainPlant(state, world, pos, direction, plant)) // UPDATE 1.16 (check if BlockBush still returns before plantType switch in super method)
		)
	}
	
	override fun isFertile(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean {
		return true
	}
	
	override fun onFallenUpon(world: World, pos: BlockPos, entity: Entity, fallDistance: Float) {
		super.onFallenUpon(world, pos, entity, fallDistance)
		
		if (ForgeHooks.onFarmlandTrample(world, pos, pos.getState(world), fallDistance, entity)) {
			val plantPos = pos.up()
			val plant = plantPos.getBlock(world)
			
			if (plant is IPlantable && plant.getPlantType(world, plantPos) == PlantType.CROP) {
				plantPos.breakBlock(world, true)
			}
		}
	}
	
	override fun canDropFromExplosion(explosion: Explosion): Boolean {
		return false
	}
	
	override fun onBlockExploded(state: BlockState, world: World, pos: BlockPos, explosion: Explosion) {
		super.onBlockExploded(state, world, pos, explosion)
		
		if (world is ServerWorld) {
			LootContext.Builder(world)
				.withRandom(world.rand)
				.withParameter(LootParameters.ORIGIN, pos.center)
				.withParameter(LootParameters.EXPLOSION_RADIUS, explosion.size)
				.withParameter(LootParameters.TOOL, ItemStack.EMPTY)
				.withNullableParameter(LootParameters.BLOCK_ENTITY, null)
				.let(state::getDrops)
				.forEach { spawnAsEntity(world, pos, it) }
		}
	}
}
