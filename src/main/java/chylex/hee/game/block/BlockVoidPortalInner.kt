package chylex.hee.game.block

import chylex.hee.game.block.BlockVoidPortalInner.Type.HUB
import chylex.hee.game.block.BlockVoidPortalInner.Type.RETURN_ACTIVE
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.game.block.entity.TileEntityVoidPortalStorage
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.util.Property
import chylex.hee.game.entity.Teleporter
import chylex.hee.game.entity.Teleporter.FxRange.Silent
import chylex.hee.game.entity.util.EntityPortalContact
import chylex.hee.game.mechanics.causatum.EnderCausatum
import chylex.hee.game.territory.TerritoryType
import chylex.hee.game.territory.system.TerritoryInstance
import chylex.hee.game.world.isInEndDimension
import chylex.hee.game.world.server.DimensionTeleporter
import chylex.hee.game.world.server.SpawnInfo
import chylex.hee.game.world.util.Facing4
import chylex.hee.game.world.util.closestTickingTile
import chylex.hee.game.world.util.floodFill
import chylex.hee.game.world.util.getBlock
import chylex.hee.game.world.util.getState
import chylex.hee.game.world.util.max
import chylex.hee.game.world.util.min
import chylex.hee.game.world.util.offsetUntil
import chylex.hee.game.world.util.setAir
import chylex.hee.util.math.Pos
import chylex.hee.util.math.Vec3
import chylex.hee.util.math.center
import chylex.hee.util.math.subtractY
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.state.StateContainer.Builder
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.IStringSerializable
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.World

class BlockVoidPortalInner(builder: BlockBuilder) : BlockAbstractPortal(builder) {
	companion object {
		val TYPE = Property.enum<Type>("type")
		
		private val TELEPORTER = Teleporter(postEvent = false, effectRange = Silent)
		
		fun teleportEntity(entity: Entity, info: SpawnInfo) {
			val targetVec = info.pos.center.subtractY(0.45)
			
			if (entity is LivingEntity) {
				if (entity is PlayerEntity) {
					TerritoryType.fromPos(info.pos)?.let { EnderCausatum.triggerStage(entity, it.stage) }
				}
				
				info.yaw?.let { entity.rotationYaw = it }
				entity.rotationPitch = 0F
				
				TELEPORTER.toLocation(entity, targetVec)
			}
			else {
				entity.setPositionAndUpdate(targetVec.x, targetVec.y, targetVec.z)
				entity.motion = Vec3.ZERO
			}
		}
	}
	
	enum class Type(private val serializableName: String) : IStringSerializable {
		HUB("hub"),
		RETURN_ACTIVE("return_active"),
		RETURN_INACTIVE("return_inactive");
		
		override fun getString(): String {
			return serializableName
		}
	}
	
	interface IVoidPortalController : IPortalController {
		val currentInstanceFactory: ITerritoryInstanceFactory?
	}
	
	interface ITerritoryInstanceFactory {
		val territory: TerritoryType
		fun create(entity: Entity): TerritoryInstance?
	}
	
	// Instance
	
	init {
		defaultState = stateContainer.baseState.with(TYPE, HUB)
	}
	
	override fun fillStateContainer(container: Builder<Block, BlockState>) {
		container.add(TYPE)
	}
	
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity {
		return TileEntityPortalInner.Void()
	}
	
	// Breaking
	
	override fun neighborChanged(state: BlockState, world: World, pos: BlockPos, neighborBlock: Block, neighborPos: BlockPos, isMoving: Boolean) {
		if (neighborBlock is BlockVoidPortalCrafted && neighborPos.getBlock(world) !is BlockVoidPortalCrafted) {
			for (portalPos in pos.floodFill(Facing4) { it.getBlock(world) === this }) {
				portalPos.setAir(world)
			}
		}
		
		@Suppress("DEPRECATION")
		super.neighborChanged(state, world, pos, neighborBlock, neighborPos, isMoving)
	}
	
	// Interaction
	
	private fun findSpawnPortalCenter(entity: Entity, pos: BlockPos): BlockPos? {
		val world = entity.world
		val offsets = Facing4.map { facing -> pos.offsetUntil(facing, 1..MAX_SIZE) { it.getBlock(world) !== this } ?: return null }
		
		val minPos = offsets.reduce(BlockPos::min)
		val maxPos = offsets.reduce(BlockPos::max)
		
		return Pos((minPos.x + maxPos.x) / 2, pos.y, (minPos.z + maxPos.z) / 2)
	}
	
	private fun updateSpawnPortal(entity: Entity, pos: BlockPos) {
		if (entity !is PlayerEntity) {
			return
		}
		
		val centerPos = findSpawnPortalCenter(entity, pos) ?: return
		val instance = TerritoryInstance.fromPos(entity) ?: return
		
		instance.updateSpawnPoint(entity, centerPos)
	}
	
	override fun onEntityInside(world: World, pos: BlockPos, entity: Entity) {
		if (!EntityPortalContact.shouldTeleport(entity)) {
			return
		}
		
		when(pos.getState(world)[TYPE]) {
			HUB -> {
				val info = pos.closestTickingTile<TileEntityVoidPortalStorage>(world, MAX_DISTANCE_FROM_FRAME)?.prepareSpawnPoint(entity)
				
				if (info != null) {
					if (entity.isInEndDimension) {
						DimensionTeleporter.LastHubPortal.updateForEntity(entity, null)
						updateSpawnPortal(entity, pos)
						teleportEntity(entity, info)
					}
					else {
						DimensionTeleporter.LastHubPortal.updateForEntity(entity, pos)
						DimensionTeleporter.changeDimension(entity, World.THE_END, DimensionTeleporter.EndTerritoryPortal(info))
					}
				}
			}
			
			RETURN_ACTIVE -> {
				if (!DimensionTeleporter.LastHubPortal.tryOverrideTeleport(entity)) {
					updateSpawnPortal(entity, pos)
					teleportEntity(entity, TerritoryInstance.THE_HUB_INSTANCE.prepareSpawnPoint(entity as? PlayerEntity, clearanceRadius = 2))
				}
			}
			
			else -> {}
		}
	}
}
