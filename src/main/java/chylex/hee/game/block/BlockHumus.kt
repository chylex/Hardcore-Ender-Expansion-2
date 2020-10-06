package chylex.hee.game.block
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.inventory.size
import chylex.hee.game.world.breakBlock
import chylex.hee.game.world.getBlock
import chylex.hee.game.world.getState
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.migration.BlockBush
import chylex.hee.system.migration.BlockReed
import chylex.hee.system.migration.BlockSapling
import chylex.hee.system.migration.Facing.UP
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Explosion
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootParameters
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.common.IPlantable
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.PlantType
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent
import java.util.Random

class BlockHumus(builder: BlockBuilder, mergeBottom: Block) : BlockSimpleMergingBottom(builder, mergeBottom){
	init{
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	@SubscribeEvent
	fun onHarvestDrops(e: HarvestDropsEvent){
		if (e.state.block === Blocks.POTATOES && e.pos.down().getBlock(e.world) === this){
			e.drops.replaceAll {
				if (it.item === Items.POISONOUS_POTATO)
					ItemStack(Items.POTATO, it.size).apply { deserializeNBT(it.serializeNBT()) }
				else
					it
			}
		}
	}
	
	override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, rand: Random){
		@Suppress("DEPRECATION")
		super.randomTick(state, world, pos, rand)
		
		if (rand.nextInt(5) <= 1){
			val plantPos = pos.up()
			val plant = plantPos.getBlock(world)
			
			if (plant is IPlantable && canSustainPlant(state, world, pos, UP, plant)){
				@Suppress("DEPRECATION")
				plant.randomTick(plantPos.getState(world), world, plantPos, rand)
			}
		}
	}
	
	override fun canSustainPlant(state: BlockState, world: IBlockReader, pos: BlockPos, direction: Direction, plant: IPlantable): Boolean{
		val type = plant.getPlantType(world, pos)
		
		return (
			type == PlantType.Crop ||
			type == PlantType.Plains ||
			plant is BlockSapling ||
			plant is BlockReed ||
			(plant is BlockBush && super.canSustainPlant(state, world, pos, direction, plant)) // UPDATE 1.15 (check if BlockBush still returns before plantType switch in super method)
		)
	}
	
	override fun isFertile(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean{
		return true
	}
	
	override fun onFallenUpon(world: World, pos: BlockPos, entity: Entity, fallDistance: Float){
		super.onFallenUpon(world, pos, entity, fallDistance)
		
		if (ForgeHooks.onFarmlandTrample(world, pos, pos.getState(world), fallDistance, entity)){
			val plantPos = pos.up()
			val plant = plantPos.getBlock(world)
			
			if (plant is IPlantable && plant.getPlantType(world, plantPos) == PlantType.Crop){
				plantPos.breakBlock(world, true)
			}
		}
	}
	
	override fun canDropFromExplosion(explosion: Explosion): Boolean{
		return false
	}
	
	override fun onBlockExploded(state: BlockState, world: World, pos: BlockPos, explosion: Explosion){
		super.onBlockExploded(state, world, pos, explosion)
		
		if (world is ServerWorld){
			LootContext.Builder(world)
				.withRandom(world.rand)
				.withParameter(LootParameters.POSITION, pos)
				.withParameter(LootParameters.EXPLOSION_RADIUS, explosion.size)
				.withParameter(LootParameters.TOOL, ItemStack.EMPTY)
				.withNullableParameter(LootParameters.BLOCK_ENTITY, null)
				.let(state::getDrops)
				.forEach { spawnAsEntity(world, pos, it) }
		}
	}
}
