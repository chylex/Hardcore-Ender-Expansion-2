package chylex.hee.game.entity.item
import chylex.hee.game.entity.util.EntityData
import chylex.hee.game.fx.FxEntityData
import chylex.hee.game.fx.FxEntityHandler
import chylex.hee.game.item.ItemPortalToken.TokenType
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.InSphere
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.init.ModEntities
import chylex.hee.init.ModItems
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.client.PacketClientLaunchInstantly
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.addY
import chylex.hee.system.util.directionTowards
import chylex.hee.system.util.getEnum
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.lookPosVec
import chylex.hee.system.util.math.LerpedFloat
import chylex.hee.system.util.posVec
import chylex.hee.system.util.putEnum
import chylex.hee.system.util.square
import chylex.hee.system.util.totalTime
import chylex.hee.system.util.use
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.network.IPacket
import net.minecraft.network.PacketBuffer
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.util.DamageSource
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData
import net.minecraftforge.fml.network.NetworkHooks
import java.util.Random

class EntityTokenHolder(type: EntityType<EntityTokenHolder>, world: World) : Entity(type, world), IEntityAdditionalSpawnData{
	constructor(world: World, tokenType: TokenType, territoryType: TerritoryType) : this(ModEntities.TOKEN_HOLDER, world){
		this.tokenType = tokenType
		this.territoryType = territoryType
	}
	
	constructor(world: World, pos: BlockPos, tokenType: TokenType, territoryType: TerritoryType) : this(world, tokenType, territoryType){
		setLocationAndAngles(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, 0F, 0F)
	}
	
	companion object{
		private val DATA_CHARGE = EntityData.register<EntityTokenHolder, Float>(DataSerializers.FLOAT)
		
		private const val TOKEN_TYPE_TAG = "Type"
		private const val TERRITORY_TYPE_TAG = "Territory"
		private const val CHARGE_TAG = "Charge"
		
		private val PARTICLE_BREAK = ParticleSpawnerCustom(
			type = ParticleSmokeCustom,
			data = ParticleSmokeCustom.Data(lifespan = 7..8, scale = 1.7F),
			pos = InSphere(0.65F)
		)
		
		val FX_BREAK = object : FxEntityHandler(){
			override fun handle(entity: Entity, rand: Random){
				PARTICLE_BREAK.spawn(Point(entity, 0.5F, 75), rand)
				// TODO sound
			}
		}
	}
	
	private val nextRotation
		get() = ((world.totalTime * 3L) % 360L).toFloat()
	
	val renderRotation = LerpedFloat(nextRotation)
	val renderCharge = LerpedFloat(1F)
	
	var tokenType = TokenType.NORMAL
		private set
	
	var territoryType: TerritoryType? = null
		private set
	
	var currentCharge by EntityData(DATA_CHARGE)
	
	init{
		isInvulnerable = true
		setNoGravity(true)
	}
	
	override fun createSpawnPacket(): IPacket<*>{
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	override fun registerData(){
		dataManager.register(DATA_CHARGE, 1F)
	}
	
	override fun writeSpawnData(buffer: PacketBuffer) = buffer.use {
		writeByte(tokenType.ordinal)
		writeShort(territoryType?.ordinal ?: -1)
		writeFloat(currentCharge)
	}
	
	override fun readSpawnData(buffer: PacketBuffer) = buffer.use {
		tokenType = TokenType.values().getOrElse(readByte().toInt()){ TokenType.NORMAL }
		territoryType = TerritoryType.values().getOrNull(readShort().toInt())
		renderCharge.updateImmediately(readFloat())
	}
	
	override fun tick(){
		super.tick()
		
		if (world.isRemote){
			val prevRotation = renderRotation.currentValue
			val nextRotation = nextRotation
			
			if (nextRotation < prevRotation){
				renderRotation.updateImmediately(prevRotation - 360F)
			}
			
			renderRotation.update(nextRotation)
			renderCharge.update(currentCharge)
		}
		else{
			TerritoryInstance.fromPos(this)?.let { it.territory.desc.tokenHolders.onTick(this, it) }
		}
	}
	
	fun forceDropToken(motion: Vec3d){
		val droppedToken = territoryType?.let { entityDropItem(ModItems.PORTAL_TOKEN.forTerritory(tokenType, it), (height * 0.5F) - 0.25F) }
		
		if (droppedToken != null){
			droppedToken.setNoPickupDelay()
			droppedToken.motion = motion
			PacketClientLaunchInstantly(droppedToken, motion).sendToTracking(this)
		}
		
		PacketClientFX(FX_BREAK, FxEntityData(this)).sendToAllAround(this, 24.0)
	}
	
	fun forceDropTokenTowards(player: EntityPlayer){
		forceDropToken(posVec.directionTowards(player.lookPosVec).scale(0.5).addY(0.1))
	}
	
	override fun attackEntityFrom(source: DamageSource, amount: Float): Boolean{
		if (world.isRemote){
			return false
		}
		
		val player = source.immediateSource as? EntityPlayer ?: return false
		
		if (player.abilities.isCreativeMode && player.isSneaking){
			remove()
		}
		else if (currentCharge >= 1F){
			forceDropTokenTowards(player)
			TerritoryInstance.fromPos(this)?.let { it.territory.desc.tokenHolders.afterUse(this, it) }
		}
		
		return false
	}
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		putEnum(TOKEN_TYPE_TAG, tokenType)
		putEnum(TERRITORY_TYPE_TAG, territoryType)
		putFloat(CHARGE_TAG, currentCharge)
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		tokenType = getEnum<TokenType>(TOKEN_TYPE_TAG) ?: TokenType.NORMAL
		territoryType = getEnum<TerritoryType>(TERRITORY_TYPE_TAG)
		currentCharge = getFloat(CHARGE_TAG)
	}
	
	override fun doesEntityNotTriggerPressurePlate() = true
	override fun canTriggerWalking() = false
	override fun canBeCollidedWith() = true
	
	@Sided(Side.CLIENT)
	override fun isInRangeToRenderDist(distanceSq: Double): Boolean{
		return distanceSq < square(128.0)
	}
}
