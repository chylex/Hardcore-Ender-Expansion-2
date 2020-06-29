package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.system.migration.vanilla.BlockEndPortal
import chylex.hee.system.util.setAir
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import java.util.Random

class BlockEndPortalOverride(builder: BlockBuilder) : BlockEndPortal(builder.p){
	override fun tick(state: BlockState, world: ServerWorld, pos: BlockPos, rand: Random){
		pos.setAir(world)
	}
	
	override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity){
		pos.setAir(world)
	}
}
