package chylex.hee.client.render.gl

import com.mojang.blaze3d.platform.GlStateManager.DestFactor
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.renderer.RenderState
import net.minecraft.client.renderer.RenderState.AlphaState
import net.minecraft.client.renderer.RenderState.CullState
import net.minecraft.client.renderer.RenderState.DepthTestState
import net.minecraft.client.renderer.RenderState.DiffuseLightingState
import net.minecraft.client.renderer.RenderState.FogState
import net.minecraft.client.renderer.RenderState.LayerState
import net.minecraft.client.renderer.RenderState.LightmapState
import net.minecraft.client.renderer.RenderState.LineState
import net.minecraft.client.renderer.RenderState.OverlayState
import net.minecraft.client.renderer.RenderState.ShadeModelState
import net.minecraft.client.renderer.RenderState.TextureState
import net.minecraft.client.renderer.RenderState.TexturingState
import net.minecraft.client.renderer.RenderState.TransparencyState
import net.minecraft.client.renderer.RenderState.WriteMaskState
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderType.State
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.util.ResourceLocation
import java.util.OptionalDouble

class RenderStateBuilder {
	private val state = State.getBuilder()
	
	fun tex(location: ResourceLocation, blur: Boolean = false, mipmap: Boolean = false) {
		state.texture(TextureState(location, blur, mipmap))
	}
	
	fun texturing(texturing: TexturingState) {
		state.texturing(texturing)
	}
	
	fun layering(layering: LayerState) {
		state.layer(layering)
	}
	
	fun blend(blend: TransparencyState) {
		state.transparency(blend)
	}
	
	fun blend(src: SourceFactor, dst: DestFactor) {
		state.transparency(TransparencyState("hee:blend_${src.name}_${dst.name}",
			{ RenderSystem.enableBlend(); RenderSystem.blendFunc(src, dst) },
			{ RenderSystem.disableBlend() }
		))
	}
	
	fun blend(srgb: SourceFactor, drgb: DestFactor, sa: SourceFactor, da: DestFactor) {
		state.transparency(TransparencyState("hee:blend_${srgb.name}_${drgb.name}_${sa.name}_${da.name}",
			{ RenderSystem.enableBlend(); RenderSystem.blendFuncSeparate(srgb, drgb, sa, da) },
			{ RenderSystem.disableBlend() }
		))
	}
	
	fun lighting(lighting: DiffuseLightingState) {
		state.diffuseLighting(lighting)
	}
	
	fun shade(shade: ShadeModelState) {
		state.shadeModel(shade)
	}
	
	fun alpha(alpha: AlphaState) {
		state.alpha(alpha)
	}
	
	fun alpha(greaterThan: Float) {
		state.alpha(AlphaState(greaterThan))
	}
	
	fun fog(fog: FogState) {
		state.fog(fog)
	}
	
	fun cull(cull: CullState) {
		state.cull(cull)
	}
	
	fun depth(depth: DepthTestState) {
		state.depthTest(depth)
	}
	
	fun lightmap(lightmap: LightmapState) {
		state.lightmap(lightmap)
	}
	
	fun overlay(overlay: OverlayState) {
		state.overlay(overlay)
	}
	
	fun mask(mask: WriteMaskState) {
		state.writeMask(mask)
	}
	
	fun line(thickness: Double) {
		state.line(LineState(OptionalDouble.of(thickness)))
	}
	
	fun build(affectsOutline: Boolean = false): State {
		return state.build(affectsOutline)
	}
	
	fun buildType(name: String, vertexFormat: VertexFormat, drawMode: Int, bufferSize: Int, useDelegate: Boolean = false, needsSorting: Boolean = true, affectsOutline: Boolean = false): RenderType {
		@Suppress("INACCESSIBLE_TYPE")
		return RenderType.makeType(name, vertexFormat, drawMode, bufferSize, useDelegate, needsSorting, build(affectsOutline))
	}
	
	/*
	 * TextureState texture = RenderState.NO_TEXTURE;
	 * TransparencyState transparency = RenderState.NO_TRANSPARENCY;
	 * DiffuseLightingState diffuseLighting = RenderState.DIFFUSE_LIGHTING_DISABLED;
	 * ShadeModelState shadeModel = RenderState.SHADE_DISABLED;
	 * AlphaState alpha = RenderState.ZERO_ALPHA;
	 * DepthTestState depthTest = RenderState.DEPTH_LEQUAL;
	 * CullState cull = RenderState.CULL_ENABLED;
	 * LightmapState lightmap = RenderState.LIGHTMAP_DISABLED;
	 * OverlayState overlay = RenderState.OVERLAY_DISABLED;
	 * FogState fog = RenderState.FOG;
	 * LayerState layer = RenderState.NO_LAYERING;
	 * TargetState target = RenderState.MAIN_TARGET;
	 * TexturingState texturing = RenderState.DEFAULT_TEXTURING;
	 * WriteMaskState writeMask = RenderState.COLOR_DEPTH_WRITE;
	 * LineState line = RenderState.DEFAULT_LINE;
	 */
	
	@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
	companion object : RenderState(null, null, null) {
		val LAYERING_NONE:       LayerState get() = NO_LAYERING
		val LAYERING_PROJECTION: LayerState get() = PROJECTION_LAYERING
		
		val BLEND_NONE: TransparencyState get() = NO_TRANSPARENCY
		
		val LIGHTING_ENABLED:  DiffuseLightingState get() = DIFFUSE_LIGHTING_ENABLED
		val LIGHTING_DISABLED: DiffuseLightingState get() = DIFFUSE_LIGHTING_DISABLED
		
		val SHADE_ENABLED:  ShadeModelState get() = RenderState.SHADE_ENABLED
		val SHADE_DISABLED: ShadeModelState get() = RenderState.SHADE_DISABLED
		
		val ALPHA_NONE:   AlphaState get() = ZERO_ALPHA
		val ALPHA_CUTOUT: AlphaState get() = DEFAULT_ALPHA
		
		val FOG_DISABLED: FogState get() = NO_FOG
		val FOG_ENABLED:  FogState get() = FOG
		val FOG_BLACK:    FogState get() = BLACK_FOG
		
		val CULL_DISABLED: CullState get() = RenderState.CULL_DISABLED
		val CULL_ENABLED:  CullState get() = RenderState.CULL_ENABLED
		
		val DEPTH_ALWAYS: DepthTestState get() = RenderState.DEPTH_ALWAYS
		
		val LIGHTMAP_DISABLED: LightmapState get() = RenderState.LIGHTMAP_DISABLED
		val LIGHTMAP_ENABLED:  LightmapState get() = RenderState.LIGHTMAP_ENABLED
		
		val OVERLAY_DISABLED: OverlayState get() = RenderState.OVERLAY_DISABLED
		val OVERLAY_ENABLED:  OverlayState get() = RenderState.OVERLAY_ENABLED
		
		val MASK_COLOR:       WriteMaskState get() = COLOR_WRITE
		val MASK_DEPTH:       WriteMaskState get() = DEPTH_WRITE
		val MASK_COLOR_DEPTH: WriteMaskState get() = COLOR_DEPTH_WRITE
	}
}
