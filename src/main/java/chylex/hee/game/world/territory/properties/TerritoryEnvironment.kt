package chylex.hee.game.world.territory.properties
import chylex.hee.client.render.territory.AbstractEnvironmentRenderer
import chylex.hee.client.render.territory.components.SkyCubeStatic
import chylex.hee.client.render.territory.lightmaps.ILightmap
import chylex.hee.client.render.territory.lightmaps.VanillaEndLightmap
import chylex.hee.game.world.WorldProviderEndCustom.Companion.DEFAULT_CELESTIAL_ANGLE
import chylex.hee.game.world.WorldProviderEndCustom.Companion.DEFAULT_SKY_LIGHT
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.facades.Resource
import net.minecraft.util.math.Vec3d

abstract class TerritoryEnvironment{
	protected companion object{
		val VANILLA = SkyCubeStatic(
			texture = Resource.Vanilla("textures/environment/end_sky.png"),
			color = (40.0 / 255.0).let { Vec3d(it, it, it) },
			distance = 100F
		)
	}
	
	open val lightBrightnessTable: FloatArray?
		get() = null
	
	open val celestialAngle
		get() = DEFAULT_CELESTIAL_ANGLE
	
	open val skyLight
		get() = DEFAULT_SKY_LIGHT // TODO use in a custom light manager & check where skylight matters (such as mob spawning)
	
	abstract val fogColor: Vec3d
	abstract val fogDensity: Float
	open val fogRenderDistanceModifier = 0F
	
	abstract val voidRadiusMpXZ: Float
	abstract val voidRadiusMpY: Float
	open val voidCenterOffset: Vec3d = Vec3d.ZERO
	
	open val renderer: AbstractEnvironmentRenderer? = null
	open val lightmap: ILightmap = VanillaEndLightmap
	
	@Sided(Side.CLIENT)
	open fun setupClient(player: EntityPlayer){}
	
	@Sided(Side.CLIENT)
	open fun tickClient(player: EntityPlayer){}
}
