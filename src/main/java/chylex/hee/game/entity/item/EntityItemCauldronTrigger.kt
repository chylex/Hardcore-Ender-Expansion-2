package chylex.hee.game.entity.item
import chylex.hee.game.block.BlockAbstractCauldron
import chylex.hee.game.fx.FxBlockData
import chylex.hee.game.fx.FxBlockHandler
import chylex.hee.game.particle.ParticleBubbleCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.Constant
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.util.Pos
import chylex.hee.system.util.cloneFrom
import chylex.hee.system.util.get
import chylex.hee.system.util.getState
import chylex.hee.system.util.playClient
import chylex.hee.system.util.selectEntities
import chylex.hee.system.util.size
import net.minecraft.block.BlockCauldron.LEVEL
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Blocks
import net.minecraft.init.SoundEvents
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random
import kotlin.math.min

class EntityItemCauldronTrigger : EntityItem{
	companion object{
		private val PARTICLE_RECIPE_FINISH = ParticleSpawnerCustom(
			type = ParticleBubbleCustom,
			pos = Constant(0.35F, UP) + InBox(0.3F),
			mot = Constant(0.45F, UP) + InBox(0F, 0.15F, 0F)
		)
		
		val FX_RECIPE_FINISH = object : FxBlockHandler(){
			override fun handle(pos: BlockPos, world: World, rand: Random){
				PARTICLE_RECIPE_FINISH.spawn(Point(pos, 22), rand)
				SoundEvents.BLOCK_BREWING_STAND_BREW.playClient(pos, SoundCategory.BLOCKS, volume = 0.5F, pitch = 1.2F)
				SoundEvents.BLOCK_BREWING_STAND_BREW.playClient(pos, SoundCategory.BLOCKS, volume = 0.8F, pitch = 0.8F)
			}
		}
	}
	
	@Suppress("unused")
	constructor(world: World) : super(world)
	
	constructor(world: World, stack: ItemStack, replacee: Entity) : super(world, replacee.posX, replacee.posY, replacee.posZ, stack){
		this.cloneFrom(replacee)
	}
	
	override fun onUpdate(){
		super.onUpdate()
		
		if (!world.isRemote && world.totalWorldTime % 5L == 0L){
			val pos = Pos(this)
			val state = pos.getState(world)
			val block = state.block
			
			// TODO
		}
	}
	
	private fun tryUseIngredients(list: Array<Pair<Item, Int>>): Boolean{
		val pos = Pos(this)
		val itemEntities = world.selectEntities.inBox<EntityItem>(AxisAlignedBB(pos))
		
		if (itemEntities.count() < list.size){
			return false
		}
		
		val remainingIngredients = mutableMapOf(*list)
		
		if (!validateIngredients(itemEntities, remainingIngredients)){
			return false
		}
		
		for(itemEntity in itemEntities){
			val stack = itemEntity.item
			val item = stack.item
			
			val remainingCount = remainingIngredients[item] ?: continue
			val removedCount = min(remainingCount, stack.size)
			
			remainingIngredients[item] = remainingCount - removedCount
			itemEntity.item = stack.also { it.size -= removedCount }
		}
		
		PacketClientFX(FX_RECIPE_FINISH, FxBlockData(pos)).sendToAllAround(world, pos, 24.0)
		return true
	}
	
	private fun validateIngredients(itemEntities: Sequence<EntityItem>, remainingIngredients: Map<Item, Int>): Boolean{
		val testIngredients = HashMap(remainingIngredients)
		
		for(itemEntity in itemEntities){
			if (itemEntity.ticksExisted < 15){
				continue
			}
			
			val stack = itemEntity.item
			val item = stack.item
			
			testIngredients[item] = (testIngredients[item] ?: continue) - stack.size
		}
		
		return testIngredients.none { it.value > 0 }
	}
}
