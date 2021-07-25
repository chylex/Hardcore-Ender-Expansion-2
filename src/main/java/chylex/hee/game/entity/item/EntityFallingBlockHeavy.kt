package chylex.hee.game.entity.item

import chylex.hee.game.entity.IHeeEntityType
import chylex.hee.game.entity.item.EntityFallingBlockHeavy.PlacementResult.FAIL
import chylex.hee.game.entity.item.EntityFallingBlockHeavy.PlacementResult.RELOCATION
import chylex.hee.game.entity.item.EntityFallingBlockHeavy.PlacementResult.SUCCESS
import chylex.hee.game.entity.properties.EntitySize
import chylex.hee.game.entity.properties.EntityTrackerInfo
import chylex.hee.game.entity.util.motionY
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.world.util.breakBlock
import chylex.hee.game.world.util.getBlock
import chylex.hee.game.world.util.getHardness
import chylex.hee.game.world.util.getState
import chylex.hee.game.world.util.getTile
import chylex.hee.game.world.util.removeBlock
import chylex.hee.game.world.util.setState
import chylex.hee.init.ModEntities
import chylex.hee.util.buffer.use
import chylex.hee.util.math.Pos
import chylex.hee.util.math.Vec3
import chylex.hee.util.math.subtractY
import chylex.hee.util.nbt.TagCompound
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.FallingBlock
import net.minecraft.block.material.Material
import net.minecraft.entity.EntityType
import net.minecraft.entity.MoverType.SELF
import net.minecraft.entity.item.FallingBlockEntity
import net.minecraft.network.IPacket
import net.minecraft.network.PacketBuffer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.GameRules.DO_ENTITY_DROPS
import net.minecraft.world.World
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData
import net.minecraftforge.fml.network.NetworkHooks

open class EntityFallingBlockHeavy(type: EntityType<out EntityFallingBlockHeavy>, world: World) : FallingBlockEntity(type, world), IEntityAdditionalSpawnData {
	constructor(world: World, pos: BlockPos, state: BlockState) : this(ModEntities.FALLING_BLOCK_HEAVY, world, pos, state)
	
	@Suppress("LeakingThis")
	constructor(type: EntityType<out EntityFallingBlockHeavy>, world: World, pos: BlockPos, state: BlockState) : this(type, world) {
		val x = pos.x.toDouble()
		val y = pos.y.toDouble()
		val z = pos.z.toDouble()
		
		fallTile = state
		origin = pos
		
		preventEntitySpawning = true
		
		setPosition(x, y + ((1F - height) / 2F), z)
		prevPosX = x
		prevPosY = y
		prevPosZ = z
		motion = Vec3.ZERO
	}
	
	open class BaseType<T : EntityFallingBlockHeavy> : IHeeEntityType<T> {
		final override val size
			get() = EntitySize(0.98F)
		
		final override val tracker
			get() = EntityTrackerInfo.Defaults.FALLING_BLOCK
	}
	
	companion object {
		val TYPE = BaseType<EntityFallingBlockHeavy>()
		
		private val ignoredTileKeys = setOf("x", "y", "z")
		
		fun canFallThrough(world: World, pos: BlockPos): Boolean {
			val state = pos.getState(world)
			return FallingBlock.canFallThrough(state) || state.material.isReplaceable || (state.getBlockHardness(world, pos) == 0F && state.getCollisionShape(world, pos).isEmpty)
		}
	}
	
	protected enum class PlacementResult {
		SUCCESS, RELOCATION, FAIL
	}
	
	override fun createSpawnPacket(): IPacket<*> {
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	override fun writeSpawnData(buffer: PacketBuffer) = buffer.use {
		writeInt(fallTile?.let(Block::getStateId) ?: 0)
	}
	
	override fun readSpawnData(buffer: PacketBuffer) = buffer.use {
		fallTile = Block.getStateById(readInt())
	}
	
	override fun tick() {
		val state = this.fallTile
		
		if (state == null || state.material === Material.AIR) {
			if (!world.isRemote) {
				remove()
			}
			
			return
		}
		
		prevPosX = posX
		prevPosY = posY
		prevPosZ = posZ
		
		if (fallTime++ == 0) {
			val pos = Pos(this)
			
			if (pos.getBlock(world) === state.block) {
				pos.removeBlock(world)
			}
			else if (!world.isRemote) {
				remove()
				return
			}
		}
		
		if (!hasNoGravity()) {
			motionY -= 0.04
		}
		
		move(SELF, motion)
		
		if (!world.isRemote) {
			val pos = Pos(this)
			
			if (onGround) {
				val posSlightlyBelow = Pos(posVec.subtractY(0.01))
				
				if (canFallThrough(world, posSlightlyBelow) && posY != prevPosY) { // if posY hasn't changed, it's stuck on a solid replaceable block
					onGround = false
					return
				}
				
				motion = motion.mul(0.7, -0.5, 0.7)
				
				val collidingWith = pos.getState(world)
				
				if (collidingWith.block !== Blocks.MOVING_PISTON) {
					val landedOnPos = if (canFallThrough(world, pos)) pos else pos.up() // allow landing on non-full solid blocks, even if it's kinda ugly
					val placementResult = placeAfterLanding(landedOnPos, collidingWith)
					
					if (placementResult == FAIL) {
						dropBlockIfPossible()
					}
					
					if (placementResult != RELOCATION) {
						remove()
					}
				}
			}
			else if ((fallTime > 100 && (pos.y < 1 || pos.y > 256)) || fallTime > 600) {
				dropBlockIfPossible()
				remove()
			}
		}
		
		motion = motion.scale(0.98)
	}
	
	protected open fun placeAfterLanding(pos: BlockPos, collidingWith: BlockState): PlacementResult {
		if (!canFallThrough(world, pos.down()) && canFallThrough(world, pos)) {
			if (pos.getHardness(world) == 0F) {
				pos.breakBlock(world, true)
			}
			
			val state = fallTile
			
			if (state.isValidPosition(world, pos) && pos.setState(world, state)) {
				val block = state.block
				
				if (block is FallingBlock) {
					block.onEndFalling(world, pos, state, collidingWith, this)
				}
				
				if (tileEntityData != null && block.hasTileEntity(state)) {
					pos.getTile<TileEntity>(world)?.let {
						val nbt = it.write(TagCompound())
						
						for (key in tileEntityData.keySet()) {
							if (key !in ignoredTileKeys) {
								nbt.put(key, tileEntityData.getCompound(key).copy())
							}
						}
						
						it.read(state, nbt)
						it.markDirty()
					}
				}
				
				return SUCCESS
			}
		}
		
		return FAIL
	}
	
	protected open fun dropBlockIfPossible() {
		val state = fallTile
		
		if (!world.isRemote && shouldDropItem && world.gameRules.getBoolean(DO_ENTITY_DROPS) && state != null) {
			entityDropItem(state.block)
		}
	}
}
