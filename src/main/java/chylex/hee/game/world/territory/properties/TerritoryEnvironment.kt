package chylex.hee.game.world.territory.properties
import chylex.hee.client.render.territory.EnvironmentRenderer
import chylex.hee.game.world.WorldProviderEndCustom.Companion.DEFAULT_CELESTIAL_ANGLE
import chylex.hee.game.world.WorldProviderEndCustom.Companion.DEFAULT_SKY_LIGHT
import chylex.hee.game.world.WorldProviderEndCustom.Companion.DEFAULT_SUN_BRIGHTNESS
import net.minecraft.util.math.Vec3d

abstract class TerritoryEnvironment{
	open val lightBrightnessTable: FloatArray?
		get() = null
	
	open val celestialAngle: Float
		get() = DEFAULT_CELESTIAL_ANGLE
	
	open val sunBrightness: Float
		get() = DEFAULT_SUN_BRIGHTNESS
	
	open val skyLight: Int
		get() = DEFAULT_SKY_LIGHT // TODO use custom chunk for custom skylight levels
	
	abstract val fogColor: Vec3d
	abstract val fogDensity: Float
	
	abstract val voidRadiusMpXZ: Float
	abstract val voidRadiusMpY: Float
	
	open val renderer: EnvironmentRenderer? = null
}
