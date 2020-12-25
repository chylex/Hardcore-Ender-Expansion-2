package chylex.hee.client.render.territory.components

import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.Vec3d

class SkyDomeStatic(
	override val texture: ResourceLocation = DEFAULT_TEXTURE,
	override val color1: Vec3d = DEFAULT_COLOR,
	override val color2: Vec3d = DEFAULT_COLOR,
	override val alpha1: Float = DEFAULT_ALPHA,
	override val alpha2: Float = DEFAULT_ALPHA,
) : SkyDomeBase() {
	constructor(texture: ResourceLocation = DEFAULT_TEXTURE, color: Vec3d = DEFAULT_COLOR, alpha: Float = DEFAULT_ALPHA) : this(texture, color, color, alpha, alpha)
	constructor(texture: ResourceLocation = DEFAULT_TEXTURE, color1: Vec3d, color2: Vec3d, alpha: Float = DEFAULT_ALPHA) : this(texture, color1, color2, alpha, alpha)
	constructor(texture: ResourceLocation = DEFAULT_TEXTURE, color: Vec3d = DEFAULT_COLOR, alpha1: Float, alpha2: Float) : this(texture, color, color, alpha1, alpha2)
}
