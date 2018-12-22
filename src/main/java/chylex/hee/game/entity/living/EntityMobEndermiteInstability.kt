package chylex.hee.game.entity.living
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModLoot
import chylex.hee.system.util.Pos
import chylex.hee.system.util.heeTag
import net.minecraft.entity.EntityLivingBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.DamageSource
import net.minecraft.util.ResourceLocation
import net.minecraft.world.EnumDifficulty.PEACEFUL
import net.minecraft.world.World

class EntityMobEndermiteInstability(world: World) : EntityMobEndermite(world){
	private var spawnCorruptedEnergy = false
	
	// TODO allow them to remain on peaceful difficulty
	
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
			ModBlocks.CORRUPTED_ENERGY.spawnCorruptedEnergy(world, Pos(this), 2)
		}
		
		super.setDead()
	}
	
	override fun getLootTable(): ResourceLocation{
		return ModLoot.ENDERMITE_INSTABILITY
	}
	
	override fun writeEntityToNBT(nbt: NBTTagCompound) = with(nbt.heeTag){
		super.writeEntityToNBT(nbt)
		
		setBoolean("Explode", spawnCorruptedEnergy)
	}
	
	override fun readEntityFromNBT(nbt: NBTTagCompound) = with(nbt.heeTag){
		super.readEntityFromNBT(nbt)
		
		spawnCorruptedEnergy = getBoolean("Explode")
	}
}
