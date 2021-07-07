package chylex.hee.game.territory.behavior

import chylex.hee.game.block.BlockGraveDirt
import chylex.hee.game.block.BlockVoidPortalInner
import chylex.hee.game.block.BlockVoidPortalInner.Type.RETURN_ACTIVE
import chylex.hee.game.block.BlockVoidPortalInner.Type.RETURN_INACTIVE
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.game.block.util.with
import chylex.hee.game.entity.living.EntityMobUndread
import chylex.hee.game.entity.living.behavior.UndreadDustEffects
import chylex.hee.game.entity.util.OP_MUL_INCR_GROUPED
import chylex.hee.game.entity.util.getAttributeInstance
import chylex.hee.game.entity.util.lookPosVec
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.entity.util.selectVulnerableEntities
import chylex.hee.game.fx.FxVecData
import chylex.hee.game.fx.util.playPlayer
import chylex.hee.game.mechanics.dust.DustType
import chylex.hee.game.potion.util.makeInstance
import chylex.hee.game.territory.storage.ForgottenTombsEndData
import chylex.hee.game.territory.system.ITerritoryBehavior
import chylex.hee.game.territory.system.TerritoryInstance
import chylex.hee.game.world.generation.feature.tombdungeon.piece.TombDungeonRoom_Tomb.MobSpawnerTrigger.Companion.FX_SPAWN_UNDREAD
import chylex.hee.game.world.util.allInBoxMutable
import chylex.hee.game.world.util.breakBlock
import chylex.hee.game.world.util.getBlock
import chylex.hee.game.world.util.isAir
import chylex.hee.game.world.util.offsetUntil
import chylex.hee.game.world.util.setState
import chylex.hee.init.ModBlocks
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextItemOrNull
import chylex.hee.system.random.nextRounded
import chylex.hee.util.math.BoundingBox
import chylex.hee.util.math.Pos
import chylex.hee.util.math.ceilToInt
import chylex.hee.util.math.directionTowards
import chylex.hee.util.math.floorToInt
import com.google.common.collect.Sets
import net.minecraft.entity.SpawnReason.STRUCTURE
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.entity.ai.attributes.Attributes.FOLLOW_RANGE
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.potion.Effects
import net.minecraft.util.Direction.UP
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.world.Difficulty.PEACEFUL
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import java.util.UUID
import kotlin.math.sqrt

class ForgottenTombsEndSequenceBehavior(private val instance: TerritoryInstance, private val endData: ForgottenTombsEndData) : ITerritoryBehavior {
	private companion object {
		private val undreadFollowRangeModifier = AttributeModifier(UUID.fromString("8F3A50CE-DAE7-4F3E-91E8-41505CBFB7A9"), "Undread End Follow Range", 3.0, OP_MUL_INCR_GROUPED)
	}
	
	override fun tick(world: ServerWorld) {
		val ticks = endData.endSequenceTicks?.takeIf { it != Int.MAX_VALUE } ?: return
		val aabb = endData.roomAABB ?: return
		
		if (world.difficulty == PEACEFUL) {
			if (activatePortal(world, aabb)) {
				endData.endSequenceTicks = Int.MAX_VALUE
				endData.isPortalActivated = true
				return
			}
		}
		
		endData.endSequenceTicks = ticks + 1
		
		if (ticks < 50) {
			return
		}
		
		if (endData.undreadsToActivate.let { it != null && it <= 0 } && activatePortal(world, aabb)) {
			endData.undreadsToActivate = null
			endData.isPortalActivated = true
			return
		}
		
		val rand = world.rand
		val mod = 3 * sqrt((ticks - 20) * 0.12F).floorToInt().coerceIn(2, 10)
		
		if (ticks % mod == 0 && rand.nextInt(5) != 0) {
			val targets = world.selectVulnerableEntities.inBox<PlayerEntity>(aabb)
			for (attempt in 0 until rand.nextRounded(1.19F)) {
				val (bb, amount) = endData.nextUndreadSpawn() ?: break
				spawnUndreads(world, targets, bb, amount)
				
				if (attempt == 0) {
					for (target in targets) {
						val playerVec = target.lookPosVec
						val spawnVec = bb.centerVec
						
						val dist = playerVec.distanceTo(spawnVec)
						val scale = if (dist < 9.0) dist else 9.0 + sqrt(dist - 8.0)
						val soundVec = playerVec.add(playerVec.directionTowards(spawnVec).scale(scale))
						
						ModBlocks.GRAVE_DIRT_PLAIN.soundType.let {
							it.breakSound.playPlayer(target, soundVec, SoundCategory.HOSTILE, it.volume * 0.9F, it.pitch + rand.nextFloat(-0.25F, 0.2F))
						}
					}
				}
			}
		}
	}
	
	private fun spawnUndreads(world: ServerWorld, targets: List<PlayerEntity>, bb: BoundingBox, amount: Int) {
		for (pos in bb.min.allInBoxMutable(bb.max)) {
			if (pos.getBlock(world) is BlockGraveDirt) {
				pos.breakBlock(world, drops = false)
			}
		}
		
		if (amount == 0) {
			return
		}
		
		val rand = world.rand
		val dustList = mutableListOf<DustType>()
		
		repeat(rand.nextRounded(1.27F)) {
			val dust = when (rand.nextInt(0, 25 - (world.difficulty.id * 2))) {
				in 0..3   -> DustType.END_POWDER
				in 4..7   -> DustType.REDSTONE
				in 8..10  -> DustType.SUGAR
				in 11..13 -> DustType.GUNPOWDER
				in 14..15 -> DustType.ANCIENT_DUST
				else      -> null
			}
			
			if (dust != null) {
				dustList.add(dust)
			}
		}
		
		val dusts = UndreadDustEffects(Sets.newEnumSet(dustList, DustType::class.java))
		
		repeat(amount) {
			val undread = EntityMobUndread(world).apply {
				isForgottenTombsEnd = true
				attackTarget = rand.nextItemOrNull(targets)
				getAttributeInstance(FOLLOW_RANGE).applyPersistentModifier(undreadFollowRangeModifier)
				addPotionEffect(Effects.SLOW_FALLING.makeInstance(Int.MAX_VALUE, 0, showParticles = false))
				enablePersistence()
			}
			
			for (attempt in 1..50) {
				val pos = Pos(
					rand.nextInt(bb.min.x, bb.max.x),
					rand.nextInt(bb.min.y, bb.max.y),
					rand.nextInt(bb.min.z, bb.max.z),
				).offsetUntil(UP, 0..10) {
					it.isAir(world) && it.up().isAir(world)
				} ?: continue
				
				undread.setLocationAndAngles(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, rand.nextFloat(0F, 360F), 0F)
				
				if (world.checkNoEntityCollision(undread)) {
					undread.onInitialSpawn(world, world.getDifficultyForLocation(pos), STRUCTURE, dusts, null)
					PacketClientFX(FX_SPAWN_UNDREAD, FxVecData(undread.posVec)).sendToAllAround(undread, 32.0)
					world.addEntity(undread)
					break
				}
			}
		}
	}
	
	private fun activatePortal(world: World, aabb: AxisAlignedBB): Boolean {
		val chunkX1 = aabb.minX.floorToInt() shr 4
		val chunkZ1 = aabb.minZ.floorToInt() shr 4
		val chunkX2 = aabb.maxX.ceilToInt() shr 4
		val chunkZ2 = aabb.maxZ.ceilToInt() shr 4
		var foundPortal = false
		
		for (chunkX in chunkX1..chunkX2) for (chunkZ in chunkZ1..chunkZ2) {
			val chunk = world.getChunk(chunkX, chunkZ)
			
			for (tile in chunk.tileEntityMap.values) {
				if (tile is TileEntityPortalInner.Void && tile.blockState[BlockVoidPortalInner.TYPE] == RETURN_INACTIVE) {
					tile.pos.setState(world, ModBlocks.VOID_PORTAL_INNER.with(BlockVoidPortalInner.TYPE, RETURN_ACTIVE))
					// TODO fx
					foundPortal = true
				}
			}
		}
		
		return foundPortal
	}
}
