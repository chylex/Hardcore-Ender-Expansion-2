package chylex.hee.game.entity.item

import chylex.hee.game.entity.damage.Damage
import chylex.hee.game.entity.damage.IDamageDealer.Companion.TITLE_FALLING_BLOCK
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.ARMOR_PROTECTION
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.ENCHANTMENT_PROTECTION
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.game.entity.item.EntityFallingBlockHeavy.PlacementResult.FAIL
import chylex.hee.game.entity.item.EntityFallingBlockHeavy.PlacementResult.RELOCATION
import chylex.hee.game.entity.item.EntityFallingBlockHeavy.PlacementResult.SUCCESS
import chylex.hee.game.entity.util.selectVulnerableEntities
import chylex.hee.game.fx.IFxData
import chylex.hee.game.fx.IFxHandler
import chylex.hee.game.fx.util.playClient
import chylex.hee.game.world.util.allInCenteredBox
import chylex.hee.game.world.util.distanceSqTo
import chylex.hee.init.ModEntities
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.random.nextFloat
import chylex.hee.util.buffer.readPos
import chylex.hee.util.buffer.use
import chylex.hee.util.buffer.writePos
import chylex.hee.util.math.Pos
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.network.PacketBuffer
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random
import java.util.UUID
import kotlin.math.ln
import kotlin.math.pow

class EntityFallingObsidian : EntityFallingBlockHeavy {
	@Suppress("unused")
	constructor(type: EntityType<EntityFallingObsidian>, world: World) : super(type, world)
	constructor(world: World, pos: BlockPos, state: BlockState) : super(ModEntities.FALLING_OBSIDIAN, world, pos, state)
	
	companion object {
		private val DAMAGE = Damage(PEACEFUL_EXCLUSION, ARMOR_PROTECTION(false), ENCHANTMENT_PROTECTION)
		
		class FxFallData(private val pos: BlockPos, private val volume: Float) : IFxData {
			override fun write(buffer: PacketBuffer) = buffer.use {
				writePos(pos)
				writeFloat(volume)
			}
		}
		
		val FX_FALL = object : IFxHandler<FxFallData> {
			override fun handle(buffer: PacketBuffer, world: World, rand: Random) = buffer.use {
				val pos = readPos()
				val volume = readFloat()
				
				repeat(2) {
					ModSounds.BLOCK_OBSIDIAN_LAND.playClient(pos, SoundCategory.BLOCKS, volume = volume, pitch = rand.nextFloat(0.8F, 1.2F))
				}
			}
		}
	}
	
	private var lastFallPos = Pos(this)
	private var entityDamageTime = mutableMapOf<UUID, Long>()
	
	override fun updateFallState(y: Double, onGround: Boolean, state: BlockState, pos: BlockPos) {
		super.updateFallState(y, onGround, state, pos)
		
		if (!world.isRemote && pos != lastFallPos) {
			if (pos.y < lastFallPos.y && fallDistance >= 1F) {
				val damageAmount = 5F * (ln(2F * (1.2F + fallDistance)) - 1F).pow(1.8F)
				val worldTime = world.gameTime
				
				for (entity in world.selectVulnerableEntities.inBox<LivingEntity>(boundingBox)) {
					val uuid = entity.uniqueID
					
					if (entityDamageTime[uuid]?.takeUnless { worldTime - it > 40 } == null) {
						entityDamageTime[uuid] = worldTime
						DAMAGE.dealTo(damageAmount, entity, TITLE_FALLING_BLOCK)
					}
				}
			}
			
			lastFallPos = pos
		}
	}
	
	override fun onLivingFall(distance: Float, blockDampeningMultiplier: Float): Boolean {
		super.onLivingFall(distance, blockDampeningMultiplier)
		
		if (!world.isRemote) {
			val volume = (0.2F + (distance * 0.2F)).coerceAtMost(3F)
			PacketClientFX(FX_FALL, FxFallData(position, volume)).sendToAllAround(this, 48.0)
		}
		
		return false
	}
	
	override fun placeAfterLanding(pos: BlockPos, collidingWith: BlockState): PlacementResult {
		if (super.placeAfterLanding(pos, collidingWith) == SUCCESS) {
			return SUCCESS
		}
		
		val relocationPos = pos.allInCenteredBox(1, 0, 1).toList().filter { canFallThrough(world, it) }.minByOrNull { it.distanceSqTo(this) }
		
		return if (relocationPos != null) {
			if (super.placeAfterLanding(relocationPos, collidingWith) == SUCCESS) {
				SUCCESS
			}
			else {
				setPosition(relocationPos.x + 0.5, relocationPos.y + 0.5, relocationPos.z + 0.5)
				RELOCATION
			}
		}
		else {
			FAIL
		}
	}
	
	override fun dropBlockIfPossible() {}
}
