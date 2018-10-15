package chylex.hee.game.block
import chylex.hee.game.block.BlockSimple.Builder.Companion.INDESTRUCTIBLE_HARDNESS
import chylex.hee.game.block.BlockSimple.Builder.Companion.INDESTRUCTIBLE_RESISTANCE
import chylex.hee.system.util.setAir
import net.minecraft.block.BlockEndPortal
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

class BlockEndPortalOverride : BlockEndPortal(Material.PORTAL){
	init{
		setHardness(INDESTRUCTIBLE_HARDNESS)
		setResistance(INDESTRUCTIBLE_RESISTANCE)
		
		tickRandomly = true
	}
	
	override fun updateTick(world: World, pos: BlockPos, state: IBlockState, rand: Random){
		pos.setAir(world)
	}
	
	override fun onEntityCollision(world: World, pos: BlockPos, state: IBlockState, entity: Entity){
		pos.setAir(world)
	}
}
