package chylex.hee.client.render.entity
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.GL.DF_ONE_MINUS_SRC_ALPHA
import chylex.hee.client.render.util.GL.SF_SRC_ALPHA
import chylex.hee.game.entity.living.EntityMobAbstractEnderman
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.EntityEnderman
import chylex.hee.system.migration.vanilla.RenderEnderman
import chylex.hee.system.migration.vanilla.RenderManager
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.totalTime
import org.lwjgl.opengl.GL11.GL_GREATER
import java.util.Random

@Sided(Side.CLIENT)
open class RenderEntityMobAbstractEnderman(manager: RenderManager) : RenderEnderman(manager){
	private val rand = Random()
	
	override fun doRender(entity: EntityEnderman, x: Double, y: Double, z: Double, rotationYaw: Float, partialTicks: Float){
		if (entity !is EntityMobAbstractEnderman){
			return
		}
		
		if (entity.isShaking){
			rand.setSeed(entity.world.totalTime)
			super.doRender(entity, x + (rand.nextGaussian() * 0.01), y + (rand.nextGaussian() * 0.01), z + (rand.nextGaussian() * 0.01), rotationYaw, partialTicks)
		}
		else{
			super.doRender(entity, x, y, z, rotationYaw, partialTicks)
		}
		
		val cloneCount = getCloneCount(entity)
		
		if (cloneCount > 0){
			rand.setSeed(entity.world.totalTime * 2L / 3L)
			
			val prevPrevYaw = entity.prevRotationYawHead
			val prevYaw = entity.rotationYawHead
			
			val prevPrevPitch = entity.prevRotationPitch
			val prevPitch = entity.rotationPitch
			
			repeat(cloneCount){
				GL.enableBlend()
				GL.blendFunc(SF_SRC_ALPHA, DF_ONE_MINUS_SRC_ALPHA)
				GL.alphaFunc(GL_GREATER, 0.004F)
				GL.depthMask(false)
				GL.color(1F, 1F, 1F, rand.nextFloat(0.05F, 0.3F))
				
				if (rand.nextInt(3) == 0){
					entity.rotationYawHead += rand.nextFloat(-45F, 45F)
					entity.prevRotationYawHead = entity.rotationYawHead
					
					entity.rotationPitch += rand.nextFloat(-30F, 30F)
					entity.prevRotationPitch = entity.rotationPitch
				}
				
				super.doRender(entity, x + rand.nextGaussian() * 0.04, y + rand.nextGaussian() * 0.025, z + rand.nextGaussian() * 0.04, rotationYaw, partialTicks)
			}
			
			entity.prevRotationYawHead = prevPrevYaw
			entity.rotationYawHead = prevYaw
			
			entity.prevRotationPitch = prevPrevPitch
			entity.rotationPitch = prevPitch
			
			GL.depthMask(true)
			GL.alphaFunc(GL_GREATER, 0.1F)
			GL.disableBlend()
		}
	}
	
	protected open fun getCloneCount(entity: EntityMobAbstractEnderman): Int{
		return if (entity.hurtTime == 0 && entity.isAggro) 2 else 0
	}
}
