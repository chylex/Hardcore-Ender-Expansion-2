package chylex.hee.client.render.territory.components

import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.vector.Vector3d

class SunStatic(
	override val texture: ResourceLocation,
	override val color: Vector3d = DEFAULT_COLOR,
	override val alpha: Float = DEFAULT_ALPHA,
	override val size: Float,
	override val distance: Float = DEFAULT_DISTANCE,
) : SunBase()
