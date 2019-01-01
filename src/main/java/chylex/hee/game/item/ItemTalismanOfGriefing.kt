package chylex.hee.game.item
import chylex.hee.game.mechanics.TrinketHandler
import chylex.hee.game.mechanics.damage.DamageProperties
import chylex.hee.game.mechanics.damage.DamageType
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ENCHANTMENT_PROTECTION
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.POTION_PROTECTION
import chylex.hee.game.world.util.ExplosionBuilder
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.posVec
import chylex.hee.system.util.square
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.DamageSource
import net.minecraft.util.EnumHand
import net.minecraft.world.EnumDifficulty
import net.minecraft.world.EnumDifficulty.EASY
import net.minecraft.world.EnumDifficulty.HARD
import net.minecraft.world.EnumDifficulty.NORMAL
import net.minecraft.world.EnumDifficulty.PEACEFUL
import net.minecraft.world.Explosion
import net.minecraftforge.common.ISpecialArmor.ArmorProperties
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.LivingHurtEvent
import net.minecraftforge.event.world.ExplosionEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

class ItemTalismanOfGriefing : ItemAbstractTrinket(){
	private companion object{
		private val BLAST_DAMAGE_PROPERTIES = DamageProperties().apply {
			with(Writer()){
				setAllowArmor()
				addType(DamageType.BLAST)
			}
		}.Reader()
		
		private fun getBlastDamageAfterCalculations(explosion: Explosion, player: EntityPlayer): Float{
			val pos = explosion.position
			val radius = explosion.size
			
			val distanceScaled = player.posVec.distanceTo(pos) / (radius * 2F)
			
			val blastPower = (1F - distanceScaled) * player.world.getBlockDensity(pos, player.entityBoundingBox)
			val explosionDamage = 1F + ((square(blastPower) + blastPower) * radius * 7).toInt()
			
			var finalDamage = explosionDamage
			
			finalDamage = ArmorProperties.applyArmor(player, player.inventory.armorInventory, DamageSource.causeExplosionDamage(explosion), finalDamage.toDouble())
			finalDamage = POTION_PROTECTION.modifyDamage(finalDamage, player, BLAST_DAMAGE_PROPERTIES)
			finalDamage = ENCHANTMENT_PROTECTION.modifyDamage(finalDamage, player, BLAST_DAMAGE_PROPERTIES)
			
			return finalDamage
		}
		
		private fun getNormalDifficultyEquivalentDamage(amount: Float, currentDifficulty: EnumDifficulty) = when(currentDifficulty){
			PEACEFUL -> 0F
			EASY     -> max(amount, (amount - 1F) * 2F)
			NORMAL   -> amount
			HARD     -> amount / 1.5F
		}
	}
	
	private val lastRepairMarkTime = ThreadLocal.withInitial { Long.MIN_VALUE }
	private val lastRepairMarkEntities = ThreadLocal.withInitial { HashSet<UUID>(4) }
	
	init{
		maxDamage = 25
		
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	override fun canPlaceIntoTrinketSlot(stack: ItemStack): Boolean{
		return stack.itemDamage < stack.maxDamage
	}
	
	private fun markEntitiesForTalismanRepair(explosion: Explosion, entities: List<Entity>){
		val currentTime = explosion.world.totalWorldTime
		val recentlyExploded = lastRepairMarkEntities.get()
		
		if (lastRepairMarkTime.get() != currentTime){
			lastRepairMarkTime.set(currentTime)
			recentlyExploded.clear()
		}
		
		for(entity in entities){
			if (entity is EntityLivingBase){
				recentlyExploded.add(entity.uniqueID)
			}
		}
	}
	
	@SubscribeEvent
	fun onExplosionDetonate(e: ExplosionEvent.Detonate){
		val explosion = e.explosion
		val entities = e.affectedEntities
		val world = explosion.world
		
		if (world.isRemote || entities.isEmpty()){
			return
		}
		
		val radius = explosion.size
		
		if (radius >= 6F){
			markEntitiesForTalismanRepair(explosion, entities)
		}
		
		val source = explosion.explosivePlacedBy
		
		if (source == null || source is EntityPlayer){ // TODO large fireballs don't set explosion source
			return
		}
		
		val diameter = radius * 2F
		val position = explosion.position
		
		for(entity in entities){
			if (entity is EntityPlayer && !entity.isImmuneToExplosions && entity.posVec.distanceTo(position) <= diameter){
				val trinketHandler = TrinketHandler.get(entity)
				
				if (trinketHandler.isItemActive(this)){
					val finalDamage = getBlastDamageAfterCalculations(explosion, entity)
					val durabilityTaken = (finalDamage / 10F).ceilToInt().coerceAtMost(3)
					
					trinketHandler.transformIfActive(this){
						it.itemDamage = min(it.maxDamage, it.itemDamage + durabilityTaken)
					}
					
					e.affectedBlocks.clear()
					e.affectedEntities.clear()
					
					with(ExplosionBuilder()){
						this.destroyBlocks = false
						this.damageEntities = false
						this.knockbackEntities = true
						
						clone(explosion, source = null)
					}
					
					break
				}
			}
		}
	}
	
	@SubscribeEvent
	fun onLivingHurt(e: LivingHurtEvent){
		if (!e.source.isExplosion){
			return
		}
		
		val entity = e.entityLiving
		val world = entity.world
		
		if (lastRepairMarkTime.get() != world.totalWorldTime || !lastRepairMarkEntities.get().remove(entity.uniqueID)){
			return
		}
		
		val finalAmount = if (e.source.isDifficultyScaled)
			getNormalDifficultyEquivalentDamage(e.amount, world.difficulty)
		else
			e.amount
		
		if (finalAmount < 50F){
			return
		}
		
		for(hand in EnumHand.values()){
			val heldItem = entity.getHeldItem(hand)
			
			if (heldItem.item === this){
				heldItem.itemDamage = 0
				// TODO sound fx
			}
		}
	}
}
