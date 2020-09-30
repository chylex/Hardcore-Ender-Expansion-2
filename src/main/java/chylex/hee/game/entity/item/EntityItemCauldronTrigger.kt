package chylex.hee.game.entity.item
import chylex.hee.game.block.BlockAbstractCauldron
import chylex.hee.game.block.with
import chylex.hee.game.entity.selectEntities
import chylex.hee.game.inventory.size
import chylex.hee.game.particle.ParticleBubbleCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.Constant
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.world.Pos
import chylex.hee.game.world.getState
import chylex.hee.game.world.playClient
import chylex.hee.game.world.setBlock
import chylex.hee.game.world.setState
import chylex.hee.game.world.totalTime
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModEntities
import chylex.hee.init.ModItems
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.fx.FxBlockData
import chylex.hee.network.fx.FxBlockHandler
import chylex.hee.system.migration.EntityItem
import chylex.hee.system.migration.Facing.UP
import net.minecraft.block.Blocks
import net.minecraft.block.CauldronBlock.LEVEL
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.Random
import kotlin.math.min

class EntityItemCauldronTrigger : EntityItemBase{
	@Suppress("unused")
	constructor(type: EntityType<EntityItemCauldronTrigger>, world: World) : super(type, world)
	constructor(world: World, stack: ItemStack, replacee: Entity) : super(ModEntities.ITEM_CAULDRON_TRIGGER, world, stack, replacee)
	
	companion object{
		private val RECIPE_DRAGONS_BREATH = arrayOf(
			ModItems.END_POWDER to 1,
			ModItems.ANCIENT_DUST to 1,
			ModItems.DRAGON_SCALE to 1
		)
		
		private val RECIPE_PURITY_EXTRACT = arrayOf(
			ModItems.END_POWDER to 3,
			ModItems.VOID_ESSENCE to 1
		)
		
		private val PARTICLE_RECIPE_FINISH = ParticleSpawnerCustom(
			type = ParticleBubbleCustom,
			pos = Constant(0.35F, UP) + InBox(0.3F),
			mot = Constant(0.45F, UP) + InBox(0F, 0.15F, 0F)
		)
		
		val FX_RECIPE_FINISH = object : FxBlockHandler(){
			override fun handle(pos: BlockPos, world: World, rand: Random){
				PARTICLE_RECIPE_FINISH.spawn(Point(pos, 22), rand)
				ModSounds.BLOCK_CAULDRON_BREW.playClient(pos, SoundCategory.BLOCKS, volume = 0.5F, pitch = 1.2F)
				ModSounds.BLOCK_CAULDRON_BREW.playClient(pos, SoundCategory.BLOCKS, volume = 0.8F, pitch = 0.8F)
			}
		}
	}
	
	override fun tick(){
		super.tick()
		
		if (!world.isRemote && world.totalTime % 5L == 0L){
			val pos = Pos(this)
			val state = pos.getState(world)
			val block = state.block
			
			if (block === Blocks.CAULDRON){
				if (state[LEVEL] > 0 && tryUseIngredients(RECIPE_DRAGONS_BREATH)){
					pos.setState(world, ModBlocks.CAULDRON_DRAGONS_BREATH.with(LEVEL, state[LEVEL]))
				}
			}
			else if (block === ModBlocks.CAULDRON_PURIFIED_ENDER_GOO){
				if (state[LEVEL] == BlockAbstractCauldron.MAX_LEVEL && tryUseIngredients(RECIPE_PURITY_EXTRACT)){
					pos.setBlock(world, Blocks.CAULDRON)
					
					EntityItem(world, pos.x + 0.5, pos.y + 0.4, pos.z + 0.5, ItemStack(ModItems.PURITY_EXTRACT)).apply {
						motion = Vec3d.ZERO
						setDefaultPickupDelay()
						world.addEntity(this)
					}
				}
			}
		}
	}
	
	private fun tryUseIngredients(list: Array<Pair<Item, Int>>): Boolean{
		val pos = Pos(this)
		val itemEntities = world.selectEntities.inBox<EntityItem>(AxisAlignedBB(pos))
		
		if (itemEntities.size < list.size){
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
	
	private fun validateIngredients(itemEntities: List<EntityItem>, remainingIngredients: Map<Item, Int>): Boolean{
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
