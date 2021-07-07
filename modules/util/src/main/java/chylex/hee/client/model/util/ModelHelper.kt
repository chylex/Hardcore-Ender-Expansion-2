package chylex.hee.client.model.util

import chylex.hee.client.util.MC
import chylex.hee.game.entity.util.lookPosVec
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.Vec3
import chylex.hee.util.math.subtractY
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.Hand.MAIN_HAND
import net.minecraft.util.HandSide
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.vector.Vector3d
import kotlin.math.abs
import kotlin.math.pow

object ModelHelper {
	@Sided(Side.CLIENT)
	fun getItemModel(stack: ItemStack): IBakedModel {
		return MC.itemRenderer.getItemModelWithOverrides(stack, MC.world, null)
	}
	
	@Sided(Side.CLIENT)
	fun getHandPosition(player: PlayerEntity, hand: Hand): Vector3d {
		val yawOffsetMp = (if (player.primaryHand == HandSide.RIGHT) 1 else -1) * (if (hand == MAIN_HAND) 1 else -1)
		
		if (player === MC.player && MC.settings.pointOfView.func_243192_a() /* RENAME isFirstPerson */) {
			val pitch = MathHelper.wrapDegrees(player.rotationPitch)
			val yaw = MathHelper.wrapDegrees(player.rotationYaw)
			val fov = MC.settings.fov.toFloat()
			
			return player
				.lookPosVec
				.subtractY(0.1 + (pitch.coerceIn(-90F, 45F) * 0.0034))
				.add(Vec3.fromYaw(yaw + (yawOffsetMp * fov * 0.6F)).scale(0.25))
				.add(Vec3.fromYaw(yaw + 165F - (fov - 90F) / 2F).scale(abs(pitch.coerceIn(0F, 90F) / 90.0).pow(1.5) * (0.3 - abs(fov - 90.0) / 600F)))
			
			// POLISH kinda weird and inaccurate, maybe use the camera transformations somehow?
		}
		else {
			val handOffset = if (player.isSneaking) 1.15 else 0.75
			
			return player
				.lookPosVec
				.subtractY(handOffset)
				.add(Vec3.fromYaw(player.renderYawOffset + yawOffsetMp * 39F).scale(0.52))
		}
	}
}
