package chylex.hee.network
import chylex.hee.client.util.MC
import chylex.hee.init.ModNetwork
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.Side.CLIENT
import net.minecraftforge.fml.relauncher.Side.SERVER
import net.minecraftforge.fml.relauncher.SideOnly

abstract class BaseClientPacket : IPacket{
	final override fun handle(side: Side, player: EntityPlayer){
		when(side){
			CLIENT -> MC.instance.addScheduledTask { handle(player as EntityPlayerSP) }
			SERVER -> throw UnsupportedOperationException("tried handling a client packet on server side: ${this::class.java.simpleName}")
		}
	}
	
	@SideOnly(CLIENT)
	abstract fun handle(player: EntityPlayerSP)
	
	// External utility functions
	
	inline fun sendToAll(){
		ModNetwork.sendToAll(this)
	}
	
	inline fun sendToPlayer(player: EntityPlayer){
		ModNetwork.sendToPlayer(this, player)
	}
	
	inline fun sendToDimension(dimension: Int){
		ModNetwork.sendToDimension(this, dimension)
	}
	
	inline fun sendToTracking(entity: Entity){
		ModNetwork.sendToTracking(this, entity)
	}
	
	inline fun sendToAllAround(x: Double, y: Double, z: Double, dimension: Int, range: Double){
		ModNetwork.sendToAllAround(this, TargetPoint(dimension, x, y, z, range))
	}
	
	fun sendToAllAround(world: World, vec: Vec3d, range: Double){
		this.sendToAllAround(vec.x, vec.y, vec.z, world.provider.dimension, range)
	}
	
	fun sendToAllAround(world: World, pos: BlockPos, range: Double){
		this.sendToAllAround(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, world.provider.dimension, range)
	}
	
	fun sendToAllAround(entity: Entity, range: Double){
		this.sendToAllAround(entity.posX, entity.posY, entity.posZ, entity.dimension, range)
	}
	
	fun sendToAllAround(tile: TileEntity, range: Double){
		this.sendToAllAround(tile.world, tile.pos, range)
	}
}
