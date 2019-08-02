package chylex.hee.game.entity.living
import chylex.hee.game.entity.IImmuneToCorruptedEnergy
import chylex.hee.game.entity.IMobBypassPeacefulDespawn
import chylex.hee.game.mechanics.instability.Instability
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModLoot
import chylex.hee.system.util.Pos
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.square
import net.minecraft.entity.EntityLivingBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.DamageSource
import net.minecraft.util.ResourceLocation
import net.minecraft.world.EnumDifficulty.PEACEFUL
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class EntityMobEndermiteInstability(world: World) : EntityMobEndermite(world), IImmuneToCorruptedEnergy, IMobBypassPeacefulDespawn{
	private var spawnCorruptedEnergy = false
	
	override fun onLivingUpdate(){
		super.onLivingUpdate()
		
		if (world.difficulty == PEACEFUL && attackTarget != null){
			super.setAttackTarget(null)
		}
	}
	
	override fun setAttackTarget(newTarget: EntityLivingBase?){
		if (world.difficulty != PEACEFUL){
			super.setAttackTarget(newTarget)
		}
	}
	
	override fun onDeath(cause: DamageSource){
		super.onDeath(cause)
		spawnCorruptedEnergy = true
	}
	
	override fun setDead(){
		if (!world.isRemote && spawnCorruptedEnergy && !isDead){
			val pos = Pos(this)
			
			Instability.get(world).triggerRelief(20u, pos)
			ModBlocks.CORRUPTED_ENERGY.spawnCorruptedEnergy(world, pos, 2)
		}
		
		super.setDead()
	}
	
	override fun getLootTable(): ResourceLocation{
		return ModLoot.ENDERMITE_INSTABILITY
	}
	
	// Serialization
	
	override fun writeEntityToNBT(nbt: NBTTagCompound) = with(nbt.heeTag){
		super.writeEntityToNBT(nbt)
		
		setBoolean("Explode", spawnCorruptedEnergy)
	}
	
	override fun readEntityFromNBT(nbt: NBTTagCompound) = with(nbt.heeTag){
		super.readEntityFromNBT(nbt)
		
		spawnCorruptedEnergy = getBoolean("Explode")
	}
	
	// Client side
	
	@SideOnly(Side.CLIENT)
	override fun isInRangeToRenderDist(distanceSq: Double): Boolean{
		return distanceSq < square(96.0)
	}
}
