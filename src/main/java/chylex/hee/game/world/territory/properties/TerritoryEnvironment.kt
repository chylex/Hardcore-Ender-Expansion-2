package chylex.hee.game.world.territory.properties
import chylex.hee.HEE
import chylex.hee.game.world.WorldProviderEndCustom.Companion.DEFAULT_CELESTIAL_ANGLE
import chylex.hee.game.world.WorldProviderEndCustom.Companion.DEFAULT_SKY_LIGHT
import chylex.hee.game.world.WorldProviderEndCustom.Companion.DEFAULT_SUN_BRIGHTNESS
import net.minecraft.client.Minecraft
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.IRenderHandler
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

abstract class TerritoryEnvironment{
	protected companion object{
		val player
			@SideOnly(Side.CLIENT)
			get() = HEE.proxy.getClientSidePlayer()!!
		
		val world
			@SideOnly(Side.CLIENT)
			get() = player.world!!
		
		val partialTicks
			@SideOnly(Side.CLIENT)
			get() = Minecraft.getMinecraft().renderPartialTicks
	}
	
	open val lightBrightnessTable: FloatArray?
		get() = null
	
	open val celestialAngle: Float
		get() = DEFAULT_CELESTIAL_ANGLE
	
	open val sunBrightness: Float
		get() = DEFAULT_SUN_BRIGHTNESS
	
	open val skyLight: Int
		get() = DEFAULT_SKY_LIGHT // TODO use custom chunk for custom skylight levels
	
	abstract val skyColor: Vec3d
	abstract val skyRenderer: IRenderHandler
	
	abstract val fogColor: Vec3d
	abstract val fogDensity: Float
}
