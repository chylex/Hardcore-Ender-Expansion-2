package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.init.ModLoot
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getState
import chylex.hee.system.util.setAir
import chylex.hee.system.util.size
import net.minecraft.block.BlockBush
import net.minecraft.block.BlockReed
import net.minecraft.block.BlockSapling
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.NonNullList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Explosion
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.EnumPlantType.Crop
import net.minecraftforge.common.EnumPlantType.Plains
import net.minecraftforge.common.IPlantable
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.Random

class BlockHumus(builder: BlockBuilder) : BlockSimple(builder){
	init{
		tickRandomly = true
		
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
	
	override fun randomTick(world: World, pos: BlockPos, state: IBlockState, rand: Random){
		super.randomTick(world, pos, state, rand)
		
		if (rand.nextInt(5) <= 1){
			val plantPos = pos.up()
			val plant = plantPos.getBlock(world)
			
			if (plant is IPlantable && canSustainPlant(state, world, pos, UP, plant)){
				plant.randomTick(world, plantPos, plantPos.getState(world), rand)
			}
		}
	}
	
	override fun canSustainPlant(state: IBlockState, world: IBlockAccess, pos: BlockPos, direction: EnumFacing, plant: IPlantable): Boolean{
		val type = plant.getPlantType(world, pos)
		
		return (
			type == Crop ||
			type == Plains ||
			plant is BlockSapling ||
			plant is BlockReed ||
			(plant is BlockBush && super.canSustainPlant(state, world, pos, direction, plant)) // UPDATE: check if BlockBush still returns before plantType switch
		)
	}
	
	override fun isFertile(world: World, pos: BlockPos): Boolean{
		return true
	}
	
	override fun onFallenUpon(world: World, pos: BlockPos, entity: Entity, fallDistance: Float){
		super.onFallenUpon(world, pos, entity, fallDistance)
		
		if (entity.canTrample(world, this, pos, fallDistance)){
			val plantPos = pos.up()
			val plant = plantPos.getBlock(world)
			
			if (plant is IPlantable && plant.getPlantType(world, plantPos) == Crop){
				plant.dropBlockAsItem(world, plantPos, plantPos.getState(world), 0)
				plantPos.setAir(world)
			}
		}
	}
	
	override fun canDropFromExplosion(explosion: Explosion): Boolean{
		return false
	}
	
	override fun onExplosionDestroy(world: World, pos: BlockPos, explosion: Explosion){
		if (!world.isRemote){
			val drops = NonNullList.create<ItemStack>()
			ModLoot.HUMUS_EXPLODED.generateDrops(drops, world, 0)
			
			for(drop in drops){
				spawnAsEntity(world, pos, drop)
			}
		}
	}
}
