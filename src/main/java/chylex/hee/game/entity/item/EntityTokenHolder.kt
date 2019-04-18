package chylex.hee.game.entity.item
import chylex.hee.game.item.ItemPortalToken.TokenType
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.init.ModItems
import chylex.hee.network.client.PacketClientLaunchInstantly
import chylex.hee.system.util.addY
import chylex.hee.system.util.getEnum
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.lookPosVec
import chylex.hee.system.util.math.LerpedFloat
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.posVec
import chylex.hee.system.util.setEnum
import chylex.hee.system.util.square
import chylex.hee.system.util.use
import io.netty.buffer.ByteBuf
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.DamageSource
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class EntityTokenHolder(world: World) : Entity(world), IEntityAdditionalSpawnData{
	constructor(world: World, pos: BlockPos, tokenType: TokenType, territoryType: TerritoryType) : this(world){
		setLocationAndAngles(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, 0F, 0F)
		this.tokenType = tokenType
		this.territoryType = territoryType
	}
	
	val renderRotation = LerpedFloat(world.totalWorldTime * 3F)
	
	var tokenType = TokenType.NORMAL
		private set
	
	private var territoryType: TerritoryType? = null
	
	init{
		setSize(0.55F, 0.675F)
		setEntityInvulnerable(true)
		setNoGravity(true)
	}
	
	override fun entityInit(){}
	
	override fun writeSpawnData(buffer: ByteBuf) = buffer.use {
		writeByte(tokenType.ordinal)
	}
	
	override fun readSpawnData(buffer: ByteBuf) = buffer.use {
		tokenType = TokenType.values().getOrElse(readByte().toInt()){ TokenType.NORMAL }
	}
	
	override fun onUpdate(){
		super.onUpdate()
		
		if (world.isRemote){
			renderRotation.update(world.totalWorldTime * 3F)
		}
	}
	
	override fun attackEntityFrom(source: DamageSource, amount: Float): Boolean{
		if (world.isRemote){
			return false
		}
		
		val player = source.immediateSource as? EntityPlayer
		
		if (player != null){
			val droppedToken = territoryType?.let { entityDropItem(ModItems.PORTAL_TOKEN.forTerritory(tokenType, it), (height * 0.5F) - 0.25F) }
			
			if (droppedToken != null){
				val launchVec = player.lookPosVec.subtract(posVec).normalize().scale(0.5).addY(0.1)
				
				droppedToken.setNoPickupDelay()
				droppedToken.motionVec = launchVec
				PacketClientLaunchInstantly(droppedToken, launchVec).sendToTracking(this)
			}
			
			// TODO fx
			setDead()
		}
		
		return false
	}
	
	override fun writeEntityToNBT(nbt: NBTTagCompound) = with(nbt.heeTag){
		setEnum("Type", tokenType)
		setEnum("Territory", territoryType)
	}
	
	override fun readEntityFromNBT(nbt: NBTTagCompound) = with(nbt.heeTag){
		tokenType = getEnum<TokenType>("Type") ?: TokenType.NORMAL
		territoryType = getEnum<TerritoryType>("Territory")
	}
	
	override fun doesEntityNotTriggerPressurePlate() = true
	override fun canTriggerWalking() = false
	override fun canBeCollidedWith() = true
	
	@SideOnly(Side.CLIENT)
	override fun isInRangeToRenderDist(distanceSq: Double): Boolean{
		return distanceSq < square(128.0)
	}
}
