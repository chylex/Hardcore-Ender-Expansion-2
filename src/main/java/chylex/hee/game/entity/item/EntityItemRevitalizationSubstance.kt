package chylex.hee.game.entity.item
import chylex.hee.client.util.MC
import chylex.hee.game.block.BlockEnderGooPurified
import chylex.hee.game.fx.IFxData
import chylex.hee.game.fx.IFxHandler
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.util.Pos
import chylex.hee.system.util.allInCenteredBoxMutable
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.center
import chylex.hee.system.util.cloneFrom
import chylex.hee.system.util.directionTowards
import chylex.hee.system.util.distanceSqTo
import chylex.hee.system.util.get
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getState
import chylex.hee.system.util.playClient
import chylex.hee.system.util.posVec
import chylex.hee.system.util.readPos
import chylex.hee.system.util.setState
import chylex.hee.system.util.size
import chylex.hee.system.util.square
import chylex.hee.system.util.use
import chylex.hee.system.util.with
import chylex.hee.system.util.writePos
import io.netty.buffer.ByteBuf
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.ItemStack
import net.minecraft.util.DamageSource
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fluids.BlockFluidBase
import java.util.Random
import kotlin.math.min

class EntityItemRevitalizationSubstance : EntityItem{
	companion object{
		private const val MAX_RADIUS = 8.5F
		
		class FxRevitalizeGooData(private val center: BlockPos, private val radius: Float) : IFxData{
			override fun write(buffer: ByteBuf) = buffer.use {
				writePos(center)
				writeFloat(radius)
			}
		}
		
		@JvmStatic
		val FX_REVITALIZE_GOO = object : IFxHandler<FxRevitalizeGooData>{
			override fun handle(buffer: ByteBuf, world: World, rand: Random) = buffer.use {
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
		
		private inline fun forEachGoo(world: World, center: BlockPos, radius: Float, callback: (BlockPos, IBlockState) -> Unit){
			val radiusSq = square(radius)
			val offset = radiusSq.ceilToInt()
			
			for(pos in center.allInCenteredBoxMutable(offset, offset, offset)){
				if (pos.distanceSqTo(center) <= radiusSq){
					val state = pos.getState(world)
					
					if (state.block === ModBlocks.ENDER_GOO){
						callback(pos, state)
					}
				}
			}
		}
	}
	
	@Suppress("unused")
	constructor(world: World) : super(world)
	
	constructor(world: World, stack: ItemStack, replacee: Entity) : super(world, replacee.posX, replacee.posY, replacee.posZ, stack){
		this.cloneFrom(replacee)
	}
	
	private var isBeingDamaged = false
	
	override fun attackEntityFrom(source: DamageSource, amount: Float): Boolean{
		isBeingDamaged = true
		val result = super.attackEntityFrom(source, amount)
		isBeingDamaged = false
		
		return result
	}
	
	override fun setDead(){
		if (!world.isRemote && (age >= lifespan || isBeingDamaged)){
			val center = Pos(this)
			
			if (center.getBlock(world) === ModBlocks.ENDER_GOO){
				val radius = min(MAX_RADIUS, 0.5F * (item.size + 1))
				
				PacketClientFX(FX_REVITALIZE_GOO, FxRevitalizeGooData(center, radius)).sendToAllAround(this, 24.0)
				
				forEachGoo(world, center, radius){
					pos, state -> pos.setState(world, ModBlocks.PURIFIED_ENDER_GOO.with(BlockFluidBase.LEVEL, state[BlockFluidBase.LEVEL]))
				}
			}
		}
		
		super.setDead()
	}
}
