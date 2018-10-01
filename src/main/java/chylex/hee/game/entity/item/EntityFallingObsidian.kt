package chylex.hee.game.entity.item
import chylex.hee.game.entity.item.EntityFallingBlockHeavy.PlacementResult.FAIL
import chylex.hee.game.entity.item.EntityFallingBlockHeavy.PlacementResult.RELOCATION
import chylex.hee.game.entity.item.EntityFallingBlockHeavy.PlacementResult.SUCCESS
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.Damage.Companion.TITLE_FALLING_BLOCK
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ARMOR_PROTECTION
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ENCHANTMENT_PROTECTION
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.client.PacketClientFX.IFXData
import chylex.hee.network.client.PacketClientFX.IFXHandler
import chylex.hee.system.util.Pos
import chylex.hee.system.util.allInCenteredBox
import chylex.hee.system.util.distanceSqTo
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.readPos
import chylex.hee.system.util.selectVulnerableEntities
import chylex.hee.system.util.use
import chylex.hee.system.util.writePos
import io.netty.buffer.ByteBuf
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random
import java.util.UUID
import kotlin.math.ln
import kotlin.math.pow

class EntityFallingObsidian : EntityFallingBlockHeavy{
	companion object{
		private val DAMAGE = Damage(PEACEFUL_EXCLUSION, ARMOR_PROTECTION(false), ENCHANTMENT_PROTECTION)
		
		class FxFallData(private val pos: BlockPos, private val volume: Float) : IFXData{
			override fun write(buffer: ByteBuf) = buffer.use {
				writePos(pos)
				writeFloat(volume)
			}
		}
		
		@JvmStatic
		val FX_FALL = object : IFXHandler{
			override fun handle(buffer: ByteBuf, world: World, rand: Random) = buffer.use {
				val pos = readPos()
				val volume = readFloat()
				
				repeat(2){
					world.playSound(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, ModSounds.BLOCK_OBSIDIAN_LAND, SoundCategory.BLOCKS, volume, rand.nextFloat(0.8F, 1.2F), false)
				}
			}
		}
	}
	
	private var lastFallPos = Pos(this)
	private var entityDamageTime = mutableMapOf<UUID, Long>()
	
	@Suppress("unused")
	constructor(world: World) : super(world)
	
	constructor(world: World, pos: BlockPos, state: IBlockState) : super(world, pos, state)
	
	override fun updateFallState(y: Double, onGround: Boolean, state: IBlockState, pos: BlockPos){
		super.updateFallState(y, onGround, state, pos)
		
		if (!world.isRemote && pos != lastFallPos){
			if (pos.y < lastFallPos.y && fallDistance >= 1F){
				val damageAmount = 5F * (ln(2F * (1.2F + fallDistance)) - 1F).pow(1.8F)
				val worldTime = world.totalWorldTime
				
				for(entity in world.selectVulnerableEntities.inBox<EntityLivingBase>(entityBoundingBox)){
					val uuid = entity.uniqueID
					
					if (entityDamageTime[uuid]?.takeUnless { worldTime - it > 40 } == null){
						entityDamageTime[uuid] = worldTime
						DAMAGE.dealTo(damageAmount, entity, TITLE_FALLING_BLOCK)
					}
				}
			}
			
			lastFallPos = pos
		}
	}
	
	override fun fall(distance: Float, blockDampeningMultiplier: Float){
		super.fall(distance, blockDampeningMultiplier)
		
		if (!world.isRemote){
			val volume = (0.2F + (distance * 0.2F)).coerceAtMost(3F)
			PacketClientFX(FX_FALL, FxFallData(position, volume)).sendToAllAround(this, 48.0)
		}
	}
	
	override fun placeAfterLanding(pos: BlockPos, collidingWith: IBlockState): PlacementResult{
		if (super.placeAfterLanding(pos, collidingWith) == SUCCESS){
			return SUCCESS
		}
		
		val relocationPos = pos.allInCenteredBox(1, 0, 1).filter { canFallThrough(world, it) }.sortedBy { it.distanceSqTo(this) }.firstOrNull()
		
		return if (relocationPos != null){
			if (super.placeAfterLanding(relocationPos, collidingWith) == SUCCESS){
				SUCCESS
			}
			else{
				setPosition(relocationPos.x + 0.5, relocationPos.y + 0.5, relocationPos.z + 0.5)
				RELOCATION
			}
		}
		else{
			FAIL
		}
	}
	
	override fun dropBlockIfPossible(){}
}
