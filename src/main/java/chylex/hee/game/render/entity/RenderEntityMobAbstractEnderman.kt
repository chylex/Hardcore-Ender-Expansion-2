package chylex.hee.game.render.entity
import chylex.hee.game.entity.living.EntityMobAbstractEnderman
import chylex.hee.game.render.util.GL
import chylex.hee.system.util.nextFloat
import net.minecraft.client.renderer.entity.RenderEnderman
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.entity.monster.EntityEnderman
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11.GL_GREATER
import org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA
import org.lwjgl.opengl.GL11.GL_SRC_ALPHA
import java.util.Random

@SideOnly(Side.CLIENT)
open class RenderEntityMobAbstractEnderman(manager: RenderManager) : RenderEnderman(manager){
	private val rand = Random()
	
	override fun doRender(entity: EntityEnderman, x: Double, y: Double, z: Double, rotationYaw: Float, partialTicks: Float){
		super.doRender(entity, x, y, z, rotationYaw, partialTicks)
		
		if (entity is EntityMobAbstractEnderman && entity.isAggressive){
			rand.setSeed(entity.world.totalWorldTime * 2L / 3L)
			
			val prevPrevYaw = entity.prevRotationYawHead
			val prevYaw = entity.rotationYawHead
			
			val prevPrevPitch = entity.prevRotationPitch
			val prevPitch = entity.rotationPitch
			
			repeat(2){
				GL.enableBlend()
				GL.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
				GL.alphaFunc(GL_GREATER, 0.004F)
				GL.depthMask(false)
				GL.color(1F, 1F, 1F, rand.nextFloat(0.05F, 0.3F))
				
				entity.rotationYawHead += rand.nextFloat(-45F, 45F)
				entity.prevRotationYawHead = entity.rotationYawHead
				
				entity.rotationPitch += rand.nextFloat(-30F, 30F)
				entity.prevRotationPitch = entity.rotationPitch
				
				super.doRender(entity, x + rand.nextGaussian() * 0.05, y + rand.nextGaussian() * 0.025, z + rand.nextGaussian() * 0.05, rotationYaw, partialTicks)
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
}
