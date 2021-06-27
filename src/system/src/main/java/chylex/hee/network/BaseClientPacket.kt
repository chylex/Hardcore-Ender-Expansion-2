package chylex.hee.network

import chylex.hee.client.MC
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.RegistryKey
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.World
import net.minecraftforge.fml.LogicalSide
import net.minecraftforge.fml.LogicalSide.CLIENT
import net.minecraftforge.fml.LogicalSide.SERVER
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint

abstract class BaseClientPacket : IPacket {
	final override fun handle(side: LogicalSide, player: EntityPlayer) {
		when(side) {
			CLIENT -> MC.instance.execute { handle(player as EntityPlayerSP) }
			SERVER -> throw UnsupportedOperationException("tried handling a client packet on server side: ${this::class.java.simpleName}")
		}
	}
	
	@Sided(Side.CLIENT)
	abstract fun handle(player: EntityPlayerSP)
	
	// External utility functions
	
	@Suppress("NOTHING_TO_INLINE")
	inline fun sendToAll() {
		NetworkManager.sendToAll(this)
	}
	
	@Suppress("NOTHING_TO_INLINE")
	inline fun sendToPlayer(player: EntityPlayer) {
		NetworkManager.sendToPlayer(this, player)
	}
	
	@Suppress("NOTHING_TO_INLINE")
	inline fun sendToDimension(dimension: RegistryKey<World>) {
		NetworkManager.sendToDimension(this, dimension)
	}
	
	@Suppress("NOTHING_TO_INLINE")
	inline fun sendToTracking(entity: Entity) {
		NetworkManager.sendToTracking(this, entity)
	}
	
	fun sendToAllAround(x: Double, y: Double, z: Double, dimension: RegistryKey<World>, range: Double) {
		NetworkManager.sendToAllAround(this, TargetPoint(x, y, z, range, dimension))
	}
	
	fun sendToAllAround(world: World, vec: Vector3d, range: Double) {
		this.sendToAllAround(vec.x, vec.y, vec.z, world.dimensionKey, range)
	}
	
	fun sendToAllAround(world: World, pos: BlockPos, range: Double) {
		this.sendToAllAround(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, world.dimensionKey, range)
	}
	
	fun sendToAllAround(entity: Entity, range: Double) {
		this.sendToAllAround(entity.posX, entity.posY, entity.posZ, entity.world.dimensionKey, range)
	}
	
	fun sendToAllAround(tile: TileEntity, range: Double) {
		this.sendToAllAround(tile.world!!, tile.pos, range)
	}
}
