package chylex.hee.game.world.territory.properties
import chylex.hee.client.render.territory.EnvironmentRenderer
import chylex.hee.client.render.territory.lightmaps.ILightmap
import chylex.hee.client.render.territory.lightmaps.VanillaEndLightmap
import chylex.hee.game.world.WorldProviderEndCustom.Companion.DEFAULT_CELESTIAL_ANGLE
import chylex.hee.game.world.WorldProviderEndCustom.Companion.DEFAULT_SKY_LIGHT
import chylex.hee.game.world.WorldProviderEndCustom.Companion.DEFAULT_SUN_BRIGHTNESS
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.util.math.Vec3d

abstract class TerritoryEnvironment{
	open val lightBrightnessTable: FloatArray?
		get() = null
	
	open val celestialAngle: Float
		get() = DEFAULT_CELESTIAL_ANGLE
	
	open val sunBrightness: Float
		get() = DEFAULT_SUN_BRIGHTNESS
	
	open val skyLight: Int
		get() = DEFAULT_SKY_LIGHT // TODO use custom chunk for custom skylight levels & check where skylight matters (such as mob spawning)
	
	abstract val fogColor: Vec3d
	abstract val fogDensity: Float
	
	abstract val voidRadiusMpXZ: Float
	abstract val voidRadiusMpY: Float
	open val voidCenterOffset: Vec3d = Vec3d.ZERO
	
	open val renderer: EnvironmentRenderer? = null
	open val lightmap: ILightmap = VanillaEndLightmap
	
	@Sided(Side.CLIENT)
	open fun setupClient(){}
	
	@Sided(Side.CLIENT)
	open fun tickClient(){}
}
