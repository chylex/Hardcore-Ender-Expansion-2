package chylex.hee.game.block
import chylex.hee.HEE
import chylex.hee.game.block.BlockVoidPortalInner.Type.HUB
import chylex.hee.game.block.BlockVoidPortalInner.Type.RETURN_ACTIVE
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.game.block.entity.TileEntityVoidPortalStorage
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.Property
import chylex.hee.game.entity.Teleporter
import chylex.hee.game.entity.Teleporter.FxRange.Silent
import chylex.hee.game.mechanics.causatum.EnderCausatum
import chylex.hee.game.mechanics.portal.DimensionTeleporter
import chylex.hee.game.mechanics.portal.EntityPortalContact
import chylex.hee.game.mechanics.portal.SpawnInfo
import chylex.hee.game.world.Pos
import chylex.hee.game.world.center
import chylex.hee.game.world.closestTickingTile
import chylex.hee.game.world.floodFill
import chylex.hee.game.world.getBlock
import chylex.hee.game.world.getState
import chylex.hee.game.world.max
import chylex.hee.game.world.min
import chylex.hee.game.world.offsetUntil
import chylex.hee.game.world.setAir
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.system.facades.Facing4
import chylex.hee.system.math.subtractY
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.migration.EntityPlayer
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.state.StateContainer.Builder
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.IStringSerializable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionType

class BlockVoidPortalInner(builder: BlockBuilder) : BlockAbstractPortal(builder){
	companion object{
		val TYPE = Property.enum<Type>("type")
		
		private val TELEPORTER = Teleporter(postEvent = false, effectRange = Silent)
		
		fun teleportEntity(entity: Entity, info: SpawnInfo){
			val targetVec = info.pos.center.subtractY(0.45)
			
			if (entity is EntityLivingBase){
				if (entity is EntityPlayer){
					TerritoryInstance.fromPos(info.pos)?.let { EnderCausatum.triggerStage(entity, it.territory.stage) }
				}
				
				info.yaw?.let { entity.rotationYaw = it }
				entity.rotationPitch = 0F
				
				TELEPORTER.toLocation(entity, targetVec)
			}
			else{
				entity.setPositionAndUpdate(targetVec.x, targetVec.y, targetVec.z)
				entity.motion = Vec3d.ZERO
			}
		}
	}
	
	enum class Type(private val serializableName: String) : IStringSerializable{
		HUB("hub"),
		RETURN_ACTIVE("return_active"),
		RETURN_INACTIVE("return_inactive");
		
		override fun getName(): String{
			return serializableName
		}
	}
	
	interface IVoidPortalController : IPortalController{
		val currentInstanceFactory: ITerritoryInstanceFactory?
	}
	
	interface ITerritoryInstanceFactory{
		val territory: TerritoryType
		fun create(entity: Entity): TerritoryInstance?
	}
	
	// Instance
	
	init{
		defaultState = stateContainer.baseState.with(TYPE, HUB)
	}
	
	override fun fillStateContainer(container: Builder<Block, BlockState>){
		container.add(TYPE)
	}
	
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity{
		return TileEntityPortalInner.Void()
	}
	
	// Breaking
	
	override fun neighborChanged(state: BlockState, world: World, pos: BlockPos, neighborBlock: Block, neighborPos: BlockPos, isMoving: Boolean){
		if (neighborBlock is BlockVoidPortalCrafted && neighborPos.getBlock(world) !is BlockVoidPortalCrafted){
			for(portalPos in pos.floodFill(Facing4){ it.getBlock(world) === this }){
				portalPos.setAir(world)
			}
		}
		
		@Suppress("DEPRECATION")
		super.neighborChanged(state, world, pos, neighborBlock, neighborPos, isMoving)
	}
	
	// Interaction
	
	private fun findSpawnPortalCenter(entity: Entity, pos: BlockPos): BlockPos?{
		val world = entity.world
		val offsets = Facing4.map { facing -> pos.offsetUntil(facing, 1..MAX_SIZE){ it.getBlock(world) !== this } ?: return null }
		
		val minPos = offsets.reduce(BlockPos::min)
		val maxPos = offsets.reduce(BlockPos::max)
		
		return Pos((minPos.x + maxPos.x) / 2, pos.y, (minPos.z + maxPos.z) / 2)
	}
	
	private fun updateSpawnPortal(entity: Entity, pos: BlockPos){
		if (entity !is EntityPlayer){
			return
		}
		
		val centerPos = findSpawnPortalCenter(entity, pos) ?: return
		val instance = TerritoryInstance.fromPos(entity) ?: return
		
		instance.updateSpawnPoint(entity, centerPos)
	}
	
	override fun onEntityInside(world: World, pos: BlockPos, entity: Entity){
		if (!EntityPortalContact.shouldTeleport(entity)){
			return
		}
		
		when(pos.getState(world)[TYPE]){
			HUB -> {
				val info = pos.closestTickingTile<TileEntityVoidPortalStorage>(world, MAX_DISTANCE_FROM_FRAME)?.prepareSpawnPoint(entity)
				
				if (info != null){
					if (entity.dimension === HEE.dim){
						DimensionTeleporter.LastHubPortal.updateForEntity(entity, null)
						updateSpawnPortal(entity, pos)
						teleportEntity(entity, info)
					}
					else{
						DimensionTeleporter.LastHubPortal.updateForEntity(entity, pos)
						DimensionTeleporter.changeDimension(entity, DimensionType.THE_END, DimensionTeleporter.EndTerritoryPortal(info))
					}
				}
			}
			
			RETURN_ACTIVE -> {
				if (!DimensionTeleporter.LastHubPortal.tryOverrideTeleport(entity)){
					updateSpawnPortal(entity, pos)
					teleportEntity(entity, TerritoryInstance.THE_HUB_INSTANCE.prepareSpawnPoint(entity as? EntityPlayer, clearanceRadius = 2))
				}
			}
			
			else -> {}
		}
	}
}
