package chylex.hee.game.particle.spawner
import chylex.hee.game.particle.util.IShape
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random

interface IParticleSpawner{
	fun spawn(shape: IShape, rand: Random)
	
	companion object{ // TODO make properties protected and JvmStatic in kotlin 1.3
		val mc: Minecraft
			@SideOnly(Side.CLIENT)
			get() = Minecraft.getMinecraft()
	}
}
