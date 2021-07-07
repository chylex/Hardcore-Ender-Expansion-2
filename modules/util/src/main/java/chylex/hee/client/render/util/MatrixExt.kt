package chylex.hee.client.render.util

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.util.math.vector.Vector3f

fun MatrixStack.translateX(amount: Double) {
	this.translate(amount, 0.0, 0.0)
}

fun MatrixStack.translateY(amount: Double) {
	this.translate(0.0, amount, 0.0)
}

fun MatrixStack.translateZ(amount: Double) {
	this.translate(0.0, 0.0, amount)
}

fun MatrixStack.rotateX(degrees: Float) {
	this.rotate(Vector3f.XP.rotationDegrees(degrees))
}

fun MatrixStack.rotateY(degrees: Float) {
	this.rotate(Vector3f.YP.rotationDegrees(degrees))
}

fun MatrixStack.rotateZ(degrees: Float) {
	this.rotate(Vector3f.ZP.rotationDegrees(degrees))
}

fun MatrixStack.scaleX(amount: Float) {
	this.scale(amount, 1F, 1F)
}

fun MatrixStack.scaleY(amount: Float) {
	this.scale(1F, amount, 1F)
}

fun MatrixStack.scaleZ(amount: Float) {
	this.scale(1F, 1F, amount)
}

fun MatrixStack.scale(amount: Float) {
	this.scale(amount, amount, amount)
}
