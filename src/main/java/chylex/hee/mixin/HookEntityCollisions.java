package chylex.hee.mixin;

import chylex.hee.game.entity.item.EntityInfusedTNT;
import net.minecraft.entity.Entity;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.stream.Stream;

@Mixin(Entity.class)
public abstract class HookEntityCollisions {
	@Inject(method = "collideBoundingBoxHeuristically", at = @At("HEAD"), cancellable = true)
	private static void hookInfusedTNTCollisions(final Entity entity, final Vector3d vec, final AxisAlignedBB collisionBox, final World ignoredWorld, final ISelectionContext context, final ReuseableStream<VoxelShape> potentialHits, final CallbackInfoReturnable<Vector3d> ci) {
		if (entity instanceof EntityInfusedTNT) {
			final IWorldReader world = ((EntityInfusedTNT)entity).getWorldReaderForCollisions();
			final boolean zeroX = vec.x == 0.0D;
			final boolean zeroY = vec.y == 0.0D;
			final boolean zeroZ = vec.z == 0.0D;
			
			final Vector3d newMovement = (zeroX && zeroY) || (zeroX && zeroZ) || (zeroY && zeroZ)
				? Entity.getAllowedMovement(vec, collisionBox, world, context, potentialHits)
				: Entity.collideBoundingBox(vec, collisionBox, new ReuseableStream<>(Stream.concat(potentialHits.createStream(), world.getBlockCollisionShapes(entity, collisionBox.expand(vec)))));
			
			ci.setReturnValue(newMovement);
		}
	}
}
