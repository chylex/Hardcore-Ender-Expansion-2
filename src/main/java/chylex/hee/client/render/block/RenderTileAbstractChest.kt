package chylex.hee.client.render.block
import chylex.hee.client.render.util.beginBox
import chylex.hee.client.render.util.rotateY
import chylex.hee.game.block.entity.base.TileEntityBaseChest
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.Atlases
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.model.Material
import net.minecraft.client.renderer.model.ModelRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.util.ResourceLocation
import java.util.function.Function
import kotlin.math.PI
import kotlin.math.pow

@Sided(Side.CLIENT)
abstract class RenderTileAbstractChest<T : TileEntityBaseChest>(dispatcher: TileEntityRendererDispatcher, private val texture: ResourceLocation) : TileEntityRenderer<T>(dispatcher){
	private val modelLid = ModelRenderer(64, 64, 0, 0).apply {
		setRotationPoint(0F, 9F, 1F)
		beginBox.offset(1F, 0F, 0F).size(14, 5, 14).add()
	}
	
	private val modelBottom = ModelRenderer(64, 64, 0, 19).apply {
		beginBox.offset(1F, 0F, 1F).size(14, 10, 14).add()
	}
	
	private val modelLatch = ModelRenderer(64, 64, 0, 0).apply {
		setRotationPoint(0F, 8F, 0F)
		beginBox.offset(7F, -1F, 15F).size(2, 4, 1).add()
	}
	
	override fun render(tile: T, partialTicks: Float, matrix: MatrixStack, buffer: IRenderTypeBuffer, combinedLight: Int, combinedOverlay: Int){
		matrix.push()
		
		val rotation = when(tile.facing){
			EAST  -> 270F
			NORTH -> 180F
			WEST  ->  90F
			else  ->   0F
		}
		
		matrix.translate(0.5, 0.5, 0.5)
		matrix.rotateY(rotation)
		matrix.translate(-0.5, -0.5, -0.5)
		
		val builder = Material(Atlases.CHEST_ATLAS, texture).getBuffer(buffer, Function(RenderType::getEntityCutout))
		
		modelLid.rotateAngleX = -(1F - (1F - tile.lidAngle.get(partialTicks)).pow(3)) * PI.toFloat() * 0.5F
		modelLatch.rotateAngleX = modelLid.rotateAngleX
		modelLid.render(matrix, builder, combinedLight, combinedOverlay)
		modelLatch.render(matrix, builder, combinedLight, combinedOverlay)
		modelBottom.render(matrix, builder, combinedLight, combinedOverlay)
		
		matrix.pop()
	}
}
