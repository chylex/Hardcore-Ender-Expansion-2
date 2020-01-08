package chylex.hee.game.entity.item
import chylex.hee.client.util.MC
import chylex.hee.game.block.BlockEnderGooPurified
import chylex.hee.game.fx.IFxData
import chylex.hee.game.fx.IFxHandler
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModEntities
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.migration.vanilla.BlockFlowingFluid
import chylex.hee.system.util.Pos
import chylex.hee.system.util.allInCenteredSphereMutable
import chylex.hee.system.util.center
import chylex.hee.system.util.directionTowards
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getState
import chylex.hee.system.util.playClient
import chylex.hee.system.util.posVec
import chylex.hee.system.util.readPos
import chylex.hee.system.util.setState
import chylex.hee.system.util.size
import chylex.hee.system.util.use
import chylex.hee.system.util.with
import chylex.hee.system.util.writePos
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketBuffer
import net.minecraft.util.DamageSource
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random
import kotlin.math.min

class EntityItemRevitalizationSubstance : EntityItemBase{
	@Suppress("unused")
	constructor(type: EntityType<EntityItemRevitalizationSubstance>, world: World) : super(type, world)
	constructor(world: World, stack: ItemStack, replacee: Entity) : super(ModEntities.ITEM_REVITALIZATION_SUBSTANCE, world, stack, replacee)
	
	companion object{
		private const val MAX_RADIUS = 8.5F
		
		class FxRevitalizeGooData(private val center: BlockPos, private val radius: Float) : IFxData{
			override fun write(buffer: PacketBuffer) = buffer.use {
				writePos(center)
				writeFloat(radius)
			}
		}
		
		val FX_REVITALIZE_GOO = object : IFxHandler<FxRevitalizeGooData>{
			override fun handle(buffer: PacketBuffer, world: World, rand: Random) = buffer.use {
				val center = readPos()
				val radius = readFloat()
				
				forEachGoo(world, center, radius){
					pos, _ -> BlockEnderGooPurified.FX_PLACE.let {
						it.handle(pos, world, rand)
						it.handle(pos.up(), world, rand)
					}
				}
				
				val centerVec = center.center
				val playerVec = MC.player?.posVec ?: return
				
				val soundOffset = min(playerVec.distanceTo(centerVec), (radius * 0.5) - 0.5)
				val soundPos = centerVec.add(centerVec.directionTowards(playerVec).scale(soundOffset))
				
				ModSounds.ENTITY_REVITALIZATION_SUBSTANCE_HEAL.playClient(soundPos, SoundCategory.BLOCKS, volume = 1F + (radius / MAX_RADIUS), pitch = 0.8F)
				ModSounds.ENTITY_REVITALIZATION_SUBSTANCE_HEAL.playClient(soundPos, SoundCategory.BLOCKS, volume = 2F * (radius / MAX_RADIUS), pitch = 1.2F)
			}
		}
		
		private inline fun forEachGoo(world: World, center: BlockPos, radius: Float, callback: (BlockPos, BlockState) -> Unit){
			for(pos in center.allInCenteredSphereMutable(radius.toDouble())){
				val state = pos.getState(world)
				
				if (state.block === ModBlocks.ENDER_GOO){
					callback(pos, state)
				}
			}
		}
	}
	
	private var isBeingDamaged = false
	
	override fun attackEntityFrom(source: DamageSource, amount: Float): Boolean{
		isBeingDamaged = true
		val result = super.attackEntityFrom(source, amount)
		isBeingDamaged = false
		
		return result
	}
	
	override fun remove(){
		if (!world.isRemote && (age >= lifespan || isBeingDamaged)){
			val center = Pos(this)
			
			if (center.getBlock(world) === ModBlocks.ENDER_GOO){
				val radius = min(MAX_RADIUS, 0.5F * (item.size + 1))
				
				PacketClientFX(FX_REVITALIZE_GOO, FxRevitalizeGooData(center, radius)).sendToAllAround(this, 24.0)
				
				forEachGoo(world, center, radius){
					pos, state -> pos.setState(world, ModBlocks.PURIFIED_ENDER_GOO.with(BlockFlowingFluid.LEVEL, state[BlockFlowingFluid.LEVEL]))
				}
			}
		}
		
		super.remove()
	}
}
