package chylex.hee.game.entity.item
import chylex.hee.game.entity.util.EntityData
import chylex.hee.game.item.ItemPortalToken.TokenType
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.init.ModItems
import chylex.hee.network.client.PacketClientLaunchInstantly
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.addY
import chylex.hee.system.util.directionTowards
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
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.util.DamageSource
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData

class EntityTokenHolder(world: World) : Entity(world), IEntityAdditionalSpawnData{
	constructor(world: World, tokenType: TokenType, territoryType: TerritoryType) : this(world){
		this.tokenType = tokenType
		this.territoryType = territoryType
	}
	
	constructor(world: World, pos: BlockPos, tokenType: TokenType, territoryType: TerritoryType) : this(world, tokenType, territoryType){
		setLocationAndAngles(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, 0F, 0F)
	}
	
	private companion object{
		private val DATA_CHARGE = EntityData.register<EntityTokenHolder, Float>(DataSerializers.FLOAT)
	}
	
	val renderRotation = LerpedFloat(world.totalWorldTime * 3F)
	val renderCharge = LerpedFloat(1F)
	
	var tokenType = TokenType.NORMAL
		private set
	
	var territoryType: TerritoryType? = null
		private set
	
	var currentCharge by EntityData(DATA_CHARGE)
	
	init{
		setSize(0.55F, 0.675F)
		setEntityInvulnerable(true)
		setNoGravity(true)
	}
	
	override fun entityInit(){
		dataManager.register(DATA_CHARGE, 1F)
	}
	
	override fun writeSpawnData(buffer: ByteBuf) = buffer.use {
		writeByte(tokenType.ordinal)
		writeShort(territoryType?.ordinal ?: -1)
		writeFloat(currentCharge)
	}
	
	override fun readSpawnData(buffer: ByteBuf) = buffer.use {
		tokenType = TokenType.values().getOrElse(readByte().toInt()){ TokenType.NORMAL }
		territoryType = TerritoryType.values().getOrNull(readShort().toInt())
		renderCharge.updateImmediately(readFloat())
	}
	
	override fun onUpdate(){
		super.onUpdate()
		
		if (world.isRemote){
			renderRotation.update(world.totalWorldTime * 3F)
			renderCharge.update(currentCharge)
		}
		else{
			TerritoryInstance.fromPos(this)?.let { it.territory.desc.tokenHolders.onTick(this, it) }
		}
	}
	
	override fun attackEntityFrom(source: DamageSource, amount: Float): Boolean{
		if (world.isRemote || currentCharge < 1F){
			return false
		}
		
		val player = source.immediateSource as? EntityPlayer
		
		if (player != null){
			if (player.capabilities.isCreativeMode && player.isSneaking){
				setDead()
				return false
			}
			
			val droppedToken = territoryType?.let { entityDropItem(ModItems.PORTAL_TOKEN.forTerritory(tokenType, it), (height * 0.5F) - 0.25F) }
			
			if (droppedToken != null){
				val launchVec = posVec.directionTowards(player.lookPosVec).scale(0.5).addY(0.1)
				
				droppedToken.setNoPickupDelay()
				droppedToken.motionVec = launchVec
				PacketClientLaunchInstantly(droppedToken, launchVec).sendToTracking(this)
			}
			
			// TODO fx
			TerritoryInstance.fromPos(this)?.let { it.territory.desc.tokenHolders.afterUse(this, it) }
		}
		
		return false
	}
	
	override fun writeEntityToNBT(nbt: TagCompound) = with(nbt.heeTag){
		setEnum("Type", tokenType)
		setEnum("Territory", territoryType)
		setFloat("Charge", currentCharge)
	}
	
	override fun readEntityFromNBT(nbt: TagCompound) = with(nbt.heeTag){
		tokenType = getEnum<TokenType>("Type") ?: TokenType.NORMAL
		territoryType = getEnum<TerritoryType>("Territory")
		currentCharge = getFloat("Charge")
	}
	
	override fun doesEntityNotTriggerPressurePlate() = true
	override fun canTriggerWalking() = false
	override fun canBeCollidedWith() = true
	
	@Sided(Side.CLIENT)
	override fun isInRangeToRenderDist(distanceSq: Double): Boolean{
		return distanceSq < square(128.0)
	}
}
