package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.migration.vanilla.BlockBush
import chylex.hee.system.migration.vanilla.BlockReed
import chylex.hee.system.migration.vanilla.BlockSapling
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.migration.vanilla.Items
import chylex.hee.system.util.breakBlock
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getState
import chylex.hee.system.util.setAir
import chylex.hee.system.util.size
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Explosion
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootParameters
import net.minecraftforge.common.IPlantable
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.PlantType
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent
import java.util.Random

class BlockHumus(builder: BlockBuilder) : BlockSimple(builder){
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
	
	override fun randomTick(state: BlockState, world: World, pos: BlockPos, rand: Random){
		super.randomTick(state, world, pos, rand)
		
		if (rand.nextInt(5) <= 1){
			val plantPos = pos.up()
			val plant = plantPos.getBlock(world)
			
			if (plant is IPlantable && canSustainPlant(state, world, pos, UP, plant)){
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
			(plant is BlockBush && super.canSustainPlant(state, world, pos, direction, plant)) // UPDATE: check if BlockBush still returns before plantType switch
		)
	}
	
	override fun isFertile(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean{
		return true
	}
	
	override fun onFallenUpon(world: World, pos: BlockPos, entity: Entity, fallDistance: Float){
		super.onFallenUpon(world, pos, entity, fallDistance)
		
		if (entity.canTrample(pos.getState(world), pos, fallDistance)){
			val plantPos = pos.up()
			val plant = plantPos.getBlock(world)
			
			if (plant is IPlantable && plant.getPlantType(world, plantPos) == PlantType.Crop){
				plantPos.breakBlock(world, true) // UPDATE test
				plantPos.setAir(world)
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
				.let { spawnDrops(state, it) }
		}
	}
}
