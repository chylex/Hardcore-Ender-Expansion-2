package chylex.hee.client.render.entity
import chylex.hee.client.render.util.GL
import chylex.hee.game.entity.living.EntityMobVillagerDying
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.totalTime
import net.minecraft.client.model.ModelVillager
import net.minecraft.client.renderer.entity.RenderLiving
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.util.ResourceLocation
import java.util.Random
import kotlin.math.min

@Sided(Side.CLIENT)
class RenderEntityMobVillagerDying(manager: RenderManager) : RenderLiving<EntityMobVillagerDying>(manager, ModelVillager(0F), 0.5F){
	private val rand = Random()
	
	override fun doRender(entity: EntityMobVillagerDying, x: Double, y: Double, z: Double, rotationYaw: Float, partialTicks: Float){
		rand.setSeed(entity.world.totalTime)
		
		val mp = min(1F, entity.deathTime / 50F) * 0.005F
		super.doRender(entity, x + (rand.nextGaussian() * mp), y + (rand.nextGaussian() * mp), z + (rand.nextGaussian() * mp), rotationYaw, partialTicks)
	}
	
	override fun getEntityTexture(entity: EntityMobVillagerDying): ResourceLocation?{
		return entity.profession?.skin
	}
	
	override fun preRenderCallback(entity: EntityMobVillagerDying, partialTicks: Float){
		val scale: Float
		
		if (entity.isChild){
			scale = 0.46875F
			shadowSize = 0.25F
		}
		else{
			scale = 0.9375F
			shadowSize = 0.5F
		}
		
		GL.scale(scale, scale, scale)
	}
	
	override fun setBrightness(entity: EntityMobVillagerDying, partialTicks: Float, combineTextures: Boolean): Boolean{
		val prevDeathTime = entity.deathTime
		entity.deathTime = 0
		
		val result = super.setBrightness(entity, partialTicks, combineTextures)
		
		entity.deathTime = prevDeathTime
		return result
	}
	
	override fun getDeathMaxRotation(entity: EntityMobVillagerDying): Float{
		return 0F
	}
}
