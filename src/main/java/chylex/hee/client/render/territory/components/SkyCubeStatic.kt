package chylex.hee.client.render.territory.components
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.Vec3d

class SkyCubeStatic(
	override val texture: ResourceLocation,
	override val color: Vec3d,
	override val alpha: Float = DEFAULT_ALPHA,
	override val distance: Double = DEFAULT_DISTANCE
) : SkyCubeBase()
